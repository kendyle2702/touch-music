package com.example.waterremindervn.api;

import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:5000/"; // Local API server
    private static final String ZING_MP3_URL = "https://mp3.zing.vn/"; // ZingMP3 base URL for chart data

    private static Retrofit retrofit = null;
    private static Retrofit zingRetrofit = null;

    // For local API server (localhost:5000)
    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    // Thêm cấu hình mới cho tối ưu hiệu suất và ổn định
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .connectionPool(new ConnectionPool(5, 30, TimeUnit.SECONDS))
                    .addInterceptor(chain -> {
                        Request.Builder requestBuilder = chain.request().newBuilder();
                        // Thêm header để không nén dữ liệu, giúp giảm thiểu vấn đề phát âm thanh
                        requestBuilder.header("Accept-Encoding", "identity");
                        requestBuilder.header("Connection", "keep-alive");
                        requestBuilder.header("Cache-Control", "max-age=0");
                        return chain.proceed(requestBuilder.build());
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
    
    // For ZingMP3 API (chart-realtime)
    public static Retrofit getZingClient() {
        if (zingRetrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    // Thêm cấu hình mới cho tối ưu hiệu suất và ổn định
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .connectionPool(new ConnectionPool(5, 30, TimeUnit.SECONDS))
                    .addInterceptor(chain -> {
                        Request.Builder requestBuilder = chain.request().newBuilder();
                        // Thêm header để không nén dữ liệu, giúp giảm thiểu vấn đề phát âm thanh
                        requestBuilder.header("Accept-Encoding", "identity");
                        requestBuilder.header("Connection", "keep-alive");
                        requestBuilder.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
                        return chain.proceed(requestBuilder.build());
                    })
                    .build();

            zingRetrofit = new Retrofit.Builder()
                    .baseUrl(ZING_MP3_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return zingRetrofit;
    }

    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }
    
    public static ApiService getZingApiService() {
        return getZingClient().create(ApiService.class);
    }
} 