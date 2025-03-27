package com.example.waterremindervn.api;

import com.example.waterremindervn.api.response.DetailPlaylistResponse;
import com.example.waterremindervn.api.response.SearchResponse;
import com.example.waterremindervn.api.response.SongInfoResponse;
import com.example.waterremindervn.api.response.SongResponse;
import com.example.waterremindervn.api.response.Top100Response;
import com.example.waterremindervn.api.response.ZingChartResponse;
import com.example.waterremindervn.api.response.ZingMediaResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiService {
    // Get song stream URL from ZingMP3
    @GET("xhr/media/get-source")
    Call<ZingMediaResponse> getSongSource(@Query("type") String type, @Query("key") String key);

    // Get chart data for top 100 from ZingMP3
    @GET("xhr/chart-realtime")
    Call<ZingChartResponse> getChartRealtime(
            @Query("songId") int songId,
            @Query("videoId") int videoId,
            @Query("albumId") int albumId,
            @Query("chart") String chart,
            @Query("time") int time);

    // API endpoints on localhost:5000
    @GET("/api/v1/song")
    Call<SongResponse> getSong(@Query("id") String id);

    @GET("/api/v1/detailplaylist")
    Call<DetailPlaylistResponse> getDetailPlaylist(@Query("id") String id);

    @GET("/api/v1/infosong")
    Call<SongInfoResponse> getSongInfo(@Query("id") String id);
    
    // Search endpoint
    @GET("/api/v1/search")
    Call<SearchResponse> searchLegacy(@Query("keyword") String keyword);
    
    // Search endpoint
    @GET("/api/v1/search")
    Call<SearchResponse> search(@Query("keyword") String keyword);
    
    // Top100 endpoint
    @GET("/api/v1/top100")
    Call<Top100Response> getTop100();
} 