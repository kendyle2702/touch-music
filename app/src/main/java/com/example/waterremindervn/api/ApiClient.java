package com.example.waterremindervn.api;

import okhttp3.OkHttpClient;
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