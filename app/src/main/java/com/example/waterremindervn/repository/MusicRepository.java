package com.example.waterremindervn.repository;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.waterremindervn.api.ApiClient;
import com.example.waterremindervn.api.ApiService;
import com.example.waterremindervn.api.response.DetailPlaylistResponse;
import com.example.waterremindervn.api.response.SearchResponse;
import com.example.waterremindervn.api.response.SongInfoResponse;
import com.example.waterremindervn.api.response.SongResponse;
import com.example.waterremindervn.api.response.Top100Response;
import com.example.waterremindervn.api.response.ZingChartResponse;
import com.example.waterremindervn.api.response.ZingMediaResponse;
import com.example.waterremindervn.database.AppDatabase;
import com.example.waterremindervn.database.SongDao;
import com.example.waterremindervn.model.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MusicRepository {
    private static final String TAG = "MusicRepository";
    
    private final ApiService apiService;
    private final ApiService zingApiService;
    private final SongDao songDao;
    private final MutableLiveData<List<Song>> searchResults = new MutableLiveData<>();
    private final MutableLiveData<List<Song>> top100Songs = new MutableLiveData<>();
    private final MutableLiveData<Song> currentSong = new MutableLiveData<>();

    public MusicRepository(Context context) {
        apiService = ApiClient.getApiService(); // LocalHost:5000 API
        zingApiService = ApiClient.getZingApiService(); // ZingMP3 API
        songDao = AppDatabase.getInstance(context).songDao();
    }

    // Database operations
    public LiveData<List<Song>> getFavoriteSongs() {
        return songDao.getAllSongs();
    }

    public void addSongToFavorites(Song song) {
        AsyncTask.execute(() -> songDao.insert(song));
    }

    public void removeSongFromFavorites(String songId) {
        AsyncTask.execute(() -> songDao.deleteSongById(songId));
    }

    public boolean isSongFavorite(String songId) {
        try {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Boolean> future = executor.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    return songDao.songExists(songId);
                }
            });
            
            boolean result = future.get();
            executor.shutdown();
            return result;
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "Error checking if song is favorite: " + e.getMessage());
            return false;
        }
    }

    // API operations
    public LiveData<List<Song>> getSearchResults() {
        return searchResults;
    }

    public LiveData<List<Song>> getTop100Songs() {
        return top100Songs;
    }

    public LiveData<Song> getCurrentSong() {
        return currentSong;
    }

    public void setCurrentSong(Song song) {
        currentSong.postValue(song);
    }

    public void search(String keyword) {
        apiService.search(keyword).enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Song> songs = new ArrayList<>();
                    SearchResponse.SearchData data = response.body().getData();
                    
                    // Add top result if available
                    if (data.getTop() != null) {
                        SearchResponse.SongItem topItem = data.getTop();
                        songs.add(new Song(
                                topItem.getId(),
                                topItem.getTitle(),
                                topItem.getArtistsNames(),
                                "", // We'll fetch the URL when the song is played
                                processImageUrl(topItem.getThumbnail() != null ? topItem.getThumbnail() : topItem.getThumbnailM())
                        ));
                    }
                    
                    // Add song results
                    if (data.getSongs() != null) {
                        for (SearchResponse.SongItem item : data.getSongs()) {
                            Song song = new Song(
                                    item.getId(),
                                    item.getTitle(),
                                    item.getArtistsNames(),
                                    "", // We'll fetch the URL when the song is played
                                    processImageUrl(item.getThumbnail() != null ? item.getThumbnail() : item.getThumbnailM())
                            );
                            song.setFavorite(isSongFavorite(item.getId()));
                            songs.add(song);
                        }
                    }
                    
                    searchResults.postValue(songs);
                } else {
                    Log.e(TAG, "Error searching songs: " + 
                          (response.body() != null ? response.body().getErr() : response.message()));
                    searchResults.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                Log.e(TAG, "Error searching songs: " + t.getMessage());
                searchResults.postValue(new ArrayList<>());
            }
        });
    }

    public void fetchTop100() {
        // Call the ZingMP3 chart API
        zingApiService.getChartRealtime(0, 0, 0, "song", -1).enqueue(new Callback<ZingChartResponse>() {
            @Override
            public void onResponse(Call<ZingChartResponse> call, Response<ZingChartResponse> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<Song> songs = new ArrayList<>();
                        ZingChartResponse.Data data = response.body().getData();
                        
                        if (data != null && data.getSong() != null) {
                            List<ZingChartResponse.SongItem> songItems = data.getSong();
                            // Limit to top 100 songs (or fewer if the API returns less)
                            int limit = Math.min(songItems.size(), 100);
                            
                            for (int i = 0; i < limit; i++) {
                                ZingChartResponse.SongItem item = songItems.get(i);
                                Song song = new Song(
                                        item.getId(),
                                        item.getTitle() != null ? item.getTitle() : item.getName(),
                                        item.getArtistsNames() != null ? item.getArtistsNames() : item.getPerformer(),
                                        "",  // We'll fetch the URL when the song is played
                                        processImageUrl(item.getThumbnail())
                                );
                                
                                // Save the song code for later use when getting the stream URL
                                song.setFavorite(isSongFavorite(item.getId()));
                                song.setPath(item.getCode());  // Temporarily store the code in the path field
                                
                                songs.add(song);
                            }
                        }
                        top100Songs.postValue(songs);
                    } else {
                        String errorMsg = response.body() != null ? 
                            response.body().getErr() : response.message();
                        Log.e(TAG, "Error fetching Top 100: " + errorMsg);
                        top100Songs.postValue(new ArrayList<>());
                        
                        // Fall back to legacy API as last resort
                        fallbackFetchTop100();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception when processing Top 100 response: " + e.getMessage());
                    e.printStackTrace();
                    top100Songs.postValue(new ArrayList<>());
                    
                    // Fall back to legacy API as last resort
                    fallbackFetchTop100();
                }
            }

            @Override
            public void onFailure(Call<ZingChartResponse> call, Throwable t) {
                Log.e(TAG, "Error fetching Top 100: " + t.getMessage());
                top100Songs.postValue(new ArrayList<>());
                
                // Fall back to legacy API as last resort
                fallbackFetchTop100();
            }
        });
    }
    
    // Fallback to legacy Top 100 API
    private void fallbackFetchTop100() {
        apiService.getTop100().enqueue(new Callback<Top100Response>() {
            @Override
            public void onResponse(Call<Top100Response> call, Response<Top100Response> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<Song> songs = new ArrayList<>();
                        List<Top100Response.PlaylistItem> playlists = response.body().getData();
                        
                        if (playlists != null && !playlists.isEmpty()) {
                            Top100Response.PlaylistItem firstPlaylist = playlists.get(0);
                            List<Top100Response.Item> items = firstPlaylist.getItems();
                            
                            if (items != null) {
                                for (Top100Response.Item item : items) {
                                    Song song = new Song(
                                            item.getId(),
                                            item.getTitle(),
                                            "", // No artist info in this response
                                            "", // We'll fetch the URL when the song is played
                                            processImageUrl(item.getThumbnail())
                                    );
                                    song.setFavorite(isSongFavorite(item.getId()));
                                    songs.add(song);
                                }
                            }
                        }
                        top100Songs.postValue(songs);
                    } else {
                        Log.e(TAG, "Error fetching Top 100 (legacy): " + response.message());
                        top100Songs.postValue(new ArrayList<>());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception when processing Top 100 response (legacy): " + e.getMessage());
                    e.printStackTrace();
                    top100Songs.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<Top100Response> call, Throwable t) {
                Log.e(TAG, "Error fetching Top 100 (legacy): " + t.getMessage());
                top100Songs.postValue(new ArrayList<>());
            }
        });
    }

    public void getSongStreamUrl(String songId, SongUrlCallback callback) {
        // First try to use the ZingMP3 media source API if we have a song code
        Song currentSong = getCurrentSong().getValue();
        if (currentSong != null && currentSong.getPath() != null && !currentSong.getPath().isEmpty() 
                && currentSong.getId().equals(songId)) {
            
            // Use the code stored in the path field to get the stream URL
            String songCode = currentSong.getPath();
            
            zingApiService.getSongSource("audio", songCode).enqueue(new Callback<ZingMediaResponse>() {
                @Override
                public void onResponse(Call<ZingMediaResponse> call, Response<ZingMediaResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        ZingMediaResponse.Data data = response.body().getData();
                        if (data != null) {
                            String streamUrl = data.getStreamUrl("128");  // Try to get 128kbps quality
                            if (streamUrl != null && !streamUrl.isEmpty()) {
                                callback.onSuccess(streamUrl);
                                return;
                            }
                        }
                        callback.onError("No stream URL found from ZingMP3");
                    } else {
                        // Fall back to the new song API
                        getSongUrlFromApi(songId, callback);
                    }
                }

                @Override
                public void onFailure(Call<ZingMediaResponse> call, Throwable t) {
                    Log.e(TAG, "Error getting song stream URL from ZingMP3: " + t.getMessage());
                    // Fall back to the new song API
                    getSongUrlFromApi(songId, callback);
                }
            });
        } else {
            // Fall back to the new song API if we don't have a song code
            getSongUrlFromApi(songId, callback);
        }
    }
    
    // Get song URL from the API
    private void getSongUrlFromApi(String songId, SongUrlCallback callback) {
        apiService.getSong(songId).enqueue(new Callback<SongResponse>() {
            @Override
            public void onResponse(Call<SongResponse> call, Response<SongResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    SongResponse.SongData data = response.body().getData();
                    if (data != null && data.getUrl() != null) {
                        callback.onSuccess(data.getUrl());
                    } else {
                        callback.onError("No stream URL found");
                    }
                } else {
                    callback.onError("Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SongResponse> call, Throwable t) {
                Log.e(TAG, "Error getting song URL: " + t.getMessage());
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    // Hàm để thêm bài hát vào danh sách yêu thích với kiểm tra trùng lặp
    public boolean addToFavoriteWithCheck(Song song) {
        if (isSongFavorite(song.getId())) {
            // Bài hát đã tồn tại trong danh sách yêu thích
            return false;
        } else {
            // Bài hát chưa có trong danh sách yêu thích, thêm vào
            song.setFavorite(true);
            addSongToFavorites(song);
            return true;
        }
    }
    
    // Hàm để xóa bài hát khỏi danh sách yêu thích
    public void removeFromFavorites(Song song) {
        if (song != null) {
            song.setFavorite(false);
            removeSongFromFavorites(song.getId());
        }
    }

    public interface SongUrlCallback {
        void onSuccess(String url);
        void onError(String errorMessage);
    }

    // Xử lý URL ảnh thumbnail
    private String processImageUrl(String url) {
        if (url == null || url.isEmpty()) {
            // Sử dụng URL mặc định nếu không có
            return "https://photo-resize-zmp3.zmdcdn.me/w240_r1x1_jpeg/cover/0/9/2/4/0924037d6c3d0e7f914e6a2ed15aa0c1.jpg";
        }
        
        // Kiểm tra xem URL có phải là URL đầy đủ hay không
        if (!url.startsWith("http")) {
            url = "https://photo-resize-zmp3.zmdcdn.me" + url;
        }
        
        // Thay đổi kích thước ảnh (từ w94 lên w240 để chất lượng tốt hơn)
        url = url.replace("w94_r1x1", "w240_r1x1");
        
        // Loại bỏ các tham số query có thể gây vấn đề
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }
        
        // Log URL để debug
        Log.d(TAG, "Processed image URL: " + url);
        return url;
    }
} 