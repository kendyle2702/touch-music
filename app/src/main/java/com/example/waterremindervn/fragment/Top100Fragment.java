package com.example.waterremindervn.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.waterremindervn.R;
import com.example.waterremindervn.adapter.SongAdapter;
import com.example.waterremindervn.model.Song;
import com.example.waterremindervn.repository.MusicRepository;

import java.util.List;

public class Top100Fragment extends Fragment implements SongAdapter.OnSongClickListener {
    private static final String TAG = "Top100Fragment";
    
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvError;
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
        return inflater.inflate(R.layout.fragment_top100, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        tvError = view.findViewById(R.id.tvError);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        
        repository = new MusicRepository(requireContext());
        adapter = new SongAdapter(this, true); // true for Top100 to show ranks
        
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        
        // Set swipe refresh colors
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorAccent,
                R.color.colorPrimary,
                R.color.colorPrimaryDark
        );
        
        swipeRefreshLayout.setOnRefreshListener(this::loadTop100Songs);
        
        observeTop100Songs();
        loadTop100Songs();
    }
    
    private void observeTop100Songs() {
        repository.getTop100Songs().observe(getViewLifecycleOwner(), songs -> {
            Log.d(TAG, "Received " + (songs != null ? songs.size() : 0) + " songs");
            adapter.submitList(songs);
            
            if (songs != null && !songs.isEmpty()) {
                showContent();
            } else {
                // If songs list is empty and not currently refreshing, show error
                if (!swipeRefreshLayout.isRefreshing()) {
                    tvError.setText("No songs found. Try again.");
                    showError();
                }
            }
            
            swipeRefreshLayout.setRefreshing(false);
        });
    }
    
    private void loadTop100Songs() {
        if (!swipeRefreshLayout.isRefreshing()) {
            showLoading();
        }
        repository.fetchTop100();
    }
    
    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);
        
        // Don't hide the RecyclerView if it already has content
        if (adapter.getItemCount() == 0) {
            recyclerView.setVisibility(View.GONE);
        }
    }
    
    private void showContent() {
        progressBar.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }
    
    private void showError() {
        progressBar.setVisibility(View.GONE);
        tvError.setVisibility(View.VISIBLE);
        
        // Don't hide the RecyclerView if it already has content
        if (adapter.getItemCount() == 0) {
            recyclerView.setVisibility(View.GONE);
        }
    }
    
    @Override
    public void onSongClick(Song song, int position) {
        if (listener != null) {
            List<Song> top100Songs = adapter.getCurrentList();
            listener.onSongSelected(song, top100Songs, position);
        }
    }
    
    @Override
    public void onFavoriteClick(Song song, int position) {
        if (listener != null) {
            // Update the song favorite status in the repository
            song.setFavorite(!song.isFavorite());
            
            // Notify the activity to update database
            listener.onFavoriteToggled(song);
            
            // Refresh the adapter to update the UI
            adapter.notifyItemChanged(position);
        }
    }
    
    /**
     * Cập nhật trạng thái yêu thích của một bài hát và cập nhật UI
     * @param updatedSong Bài hát có trạng thái yêu thích đã được cập nhật
     */
    public void updateSongFavoriteStatus(Song updatedSong) {
        if (adapter == null) return;
        
        List<Song> currentSongs = adapter.getCurrentList();
        for (int i = 0; i < currentSongs.size(); i++) {
            Song song = currentSongs.get(i);
            if (song.getId().equals(updatedSong.getId())) {
                // Cập nhật trạng thái yêu thích
                song.setFavorite(updatedSong.isFavorite());
                // Cập nhật UI
                adapter.notifyItemChanged(i);
            }
        }
    }
} 