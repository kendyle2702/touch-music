package com.example.waterremindervn.glide;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

@GlideModule
public class MyAppGlideModule extends AppGlideModule {
    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        // Tăng bộ nhớ cache
        builder.setMemoryCache(new LruResourceCache(30 * 1024 * 1024)); // 30MB memory cache
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, 100 * 1024 * 1024)); // 100MB disk cache
        
        // Tạo request options mặc định
        builder.setDefaultRequestOptions(
            new RequestOptions()
                .timeout(30000) // 30 giây timeout
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
        );
        
        // Thêm log cho debug
        builder.setLogLevel(Log.VERBOSE);
    }
} 