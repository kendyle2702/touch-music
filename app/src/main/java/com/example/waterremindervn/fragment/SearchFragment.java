package com.example.waterremindervn.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waterremindervn.R;
import com.example.waterremindervn.adapter.SongAdapter;
import com.example.waterremindervn.model.Song;
import com.example.waterremindervn.repository.MusicRepository;

import java.util.List;

public class SearchFragment extends Fragment implements SongAdapter.OnSongClickListener {
    
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvNoResults;
    private EditText editSearch;
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
        return inflater.inflate(R.layout.fragment_search, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        tvNoResults = view.findViewById(R.id.tvNoResults);
        editSearch = view.findViewById(R.id.editSearch);
        
        repository = new MusicRepository(requireContext());
        adapter = new SongAdapter(this, false);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        
        setupSearchListener();
        observeSearchResults();
        
        editSearch.setOnClickListener(v -> {
            editSearch.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editSearch, InputMethodManager.SHOW_IMPLICIT);
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        editSearch.clearFocus();
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
    }
    
    private void setupSearchListener() {
        editSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editSearch, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearch();
                return true;
            }
            return false;
        });
    }
    
    private void performSearch() {
        String query = editSearch.getText().toString().trim();
        if (!query.isEmpty()) {
            InputMethodManager imm = (InputMethodManager) requireContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
            
            showLoading();
            repository.search(query);
        }
    }
    
    private void observeSearchResults() {
        repository.getSearchResults().observe(getViewLifecycleOwner(), songs -> {
            adapter.submitList(songs);
            
            if (songs != null) {
                if (songs.isEmpty()) {
                    showEmpty();
                } else {
                    showContent();
                }
            }
        });
    }
    
    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoResults.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }
    
    private void showContent() {
        progressBar.setVisibility(View.GONE);
        tvNoResults.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }
    
    private void showEmpty() {
        progressBar.setVisibility(View.GONE);
        tvNoResults.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }
    
    @Override
    public void onSongClick(Song song, int position) {
        if (listener != null) {
            List<Song> searchResults = adapter.getCurrentList();
            listener.onSongSelected(song, searchResults, position);
        }
    }
    
    @Override
    public void onFavoriteClick(Song song, int position) {
        if (listener != null) {
            song.setFavorite(!song.isFavorite());
            listener.onFavoriteToggled(song);
            adapter.notifyItemChanged(position);
        }
    }
    
    public void updateSongFavoriteStatus(Song updatedSong) {
        if (adapter == null) return;
        
        List<Song> currentSongs = adapter.getCurrentList();
        for (int i = 0; i < currentSongs.size(); i++) {
            Song song = currentSongs.get(i);
            if (song.getId().equals(updatedSong.getId())) {
                song.setFavorite(updatedSong.isFavorite());
                adapter.notifyItemChanged(i);
            }
        }
    }
} 