package com.example.waterremindervn.fragment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.waterremindervn.R;
import com.example.waterremindervn.adapter.SongAdapter;
import com.example.waterremindervn.model.Song;
import com.example.waterremindervn.repository.MusicRepository;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MyListFragment extends Fragment implements SongAdapter.OnSongClickListener {
    
    private RecyclerView recyclerView;
    private TextView tvEmptyList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SongAdapter adapter;
    private MusicRepository repository;
    private OnSongActionListener listener;
    
    public interface OnSongActionListener {
        void onSongSelected(Song song, List<Song> playlist, int position);
        void onFavoriteToggled(Song song);
    }
    
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnSongActionListener) {
            listener = (OnSongActionListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnSongActionListener");
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_list, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        recyclerView = view.findViewById(R.id.recyclerView);
        tvEmptyList = view.findViewById(R.id.tvEmptyList);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        
        repository = new MusicRepository(requireContext());
        adapter = new SongAdapter(this, false);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        
        // Thiết lập swipe-to-delete
        setupSwipeToDelete();
        
        // Thiết lập pull-to-refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            repository.refreshFavoriteSongs();
        });
        
        observeFavoriteSongs();
    }
    
    private void setupSwipeToDelete() {
        // Tạo ItemTouchHelper để xử lý vuốt để xóa
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            private final ColorDrawable background = new ColorDrawable(Color.RED);
            
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // Không quan tâm đến việc di chuyển item
            }
            
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Song songToRemove = adapter.getCurrentList().get(position);
                
                // Đánh dấu bài hát không còn là yêu thích
                songToRemove.setFavorite(false);
                
                // Thông báo cho MainActivity để cập nhật repository
                if (listener != null) {
                    listener.onFavoriteToggled(songToRemove);
                }
                
                // Hiển thị Snackbar với tùy chọn hoàn tác
                View rootView = requireView();
                Snackbar.make(rootView, "Đã xóa khỏi danh sách yêu thích", Snackbar.LENGTH_LONG)
                        .setAction("Hoàn tác", v -> {
                            // Đánh dấu lại là yêu thích và thông báo cho MainActivity
                            songToRemove.setFavorite(true);
                            if (listener != null) {
                                listener.onFavoriteToggled(songToRemove);
                            }
                        })
                        .show();
            }
            
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                
                // Vẽ nền màu đỏ khi vuốt
                if (dX > 0) { // Vuốt phải
                    background.setBounds(itemView.getLeft(), itemView.getTop(), (int) dX, itemView.getBottom());
                } else { // Vuốt trái
                    background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                }
                background.draw(c);
                
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(recyclerView);
    }
    
    private void observeFavoriteSongs() {
        repository.getFavoriteSongs().observe(getViewLifecycleOwner(), songs -> {
            adapter.submitList(songs);
            
            if (songs.isEmpty()) {
                tvEmptyList.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                tvEmptyList.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
            
            // Kết thúc trạng thái refreshing nếu đang active
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
    
    @Override
    public void onSongClick(Song song, int position) {
        if (listener != null) {
            List<Song> favoriteSongs = adapter.getCurrentList();
            listener.onSongSelected(song, favoriteSongs, position);
        }
    }
    
    @Override
    public void onFavoriteClick(Song song, int position) {
        if (listener != null) {
            // Trong trang My List, khi bấm nút trái tim sẽ luôn là hủy thích
            song.setFavorite(false);
            listener.onFavoriteToggled(song);
            
            // Hiển thị Snackbar với tùy chọn hoàn tác
            View rootView = requireView();
            Snackbar.make(rootView, "Đã xóa khỏi danh sách yêu thích", Snackbar.LENGTH_LONG)
                    .setAction("Hoàn tác", v -> {
                        // Đánh dấu lại là yêu thích và thông báo cho MainActivity
                        song.setFavorite(true);
                        if (listener != null) {
                            listener.onFavoriteToggled(song);
                        }
                    })
                    .show();
        }
    }
} 