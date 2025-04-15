package com.example.waterremindervn;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.media3.common.util.UnstableApi;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.waterremindervn.adapter.ViewPagerAdapter;
import com.example.waterremindervn.fragment.MyListFragment;
import com.example.waterremindervn.fragment.SearchFragment;
import com.example.waterremindervn.fragment.Top100Fragment;
import com.example.waterremindervn.model.Song;
import com.example.waterremindervn.repository.MusicRepository;
import com.example.waterremindervn.service.MusicPlayerService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@UnstableApi
public class MainActivity extends AppCompatActivity implements 
        MyListFragment.OnSongActionListener,
        Top100Fragment.OnSongActionListener,
        SearchFragment.OnSongActionListener,
        MusicPlayerService.PlayerCallback {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ViewPagerAdapter viewPagerAdapter;
    
    // Player controls
    private ImageView imgAlbumArt;
    private TextView tvSongTitle;
    private TextView tvArtist;
    private SeekBar seekBarProgress;
    private MaterialButton btnPlayPause;
    private MaterialButton btnNext;
    
    // Service connection
    private MusicPlayerService musicService;
    private boolean serviceBound = false;
    private MusicRepository repository;
    
    // Update seekbar
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable updateSeekBarTask = new Runnable() {
        @Override
        public void run() {
            if (musicService != null && musicService.isPlaying() && !musicService.isSeeking()) {
                long currentPosition = musicService.getCurrentPosition();
                long duration = musicService.getDuration();
                updateSeekBar(currentPosition, duration);
            }
            handler.postDelayed(this, 1000);
        }
    };
    
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.MusicBinder binder = (MusicPlayerService.MusicBinder) service;
            musicService = binder.getService();
            serviceBound = true;
            
            // Register for updates
            musicService.registerCallback(MainActivity.this);
            
            // Start seekbar update
            handler.post(updateSeekBarTask);
            
            // Update UI if there's a song already playing
            Song currentSong = repository.getCurrentSong().getValue();
            if (currentSong != null) {
                updatePlayerUI(currentSong);
                updatePlayPauseButton(musicService.isPlaying());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            musicService = null;
        }
    };

    private Animation rotateAnimation;
    
    // LiveData để thông báo khi có thay đổi trạng thái yêu thích
    private MutableLiveData<Song> favoriteChangeEvent = new MutableLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Load rotation animation
        rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_album);
        
        // Xóa cache của Glide khi khởi động app một cách mạnh mẽ hơn
        new Thread(() -> {
            try {
                // Xóa cả disk cache và memory cache
                Glide.get(getApplicationContext()).clearDiskCache();
                
                // Chạy trên main thread để xóa memory cache
                runOnUiThread(() -> {
                    try {
                        Glide.get(getApplicationContext()).clearMemory();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        
        // Initialize repository
        repository = new MusicRepository(this);
        
        // Find views
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        imgAlbumArt = findViewById(R.id.imgAlbumArt);
        tvSongTitle = findViewById(R.id.tvSongTitle);
        tvArtist = findViewById(R.id.tvArtist);
        seekBarProgress = findViewById(R.id.seekBarProgress);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);
        
        // Setup ViewPager with TabLayout
        setupViewPager();
        setupTabLayout();
        
        // Setup player controls
        setupPlayerControls();
        
        // Start the music service
        startAndBindMusicService();
    }
    
    private void setupViewPager() {
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);
        
        // Xử lý chuyển tab để đảm bảo bàn phím đóng khi chuyển tab
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Ẩn bàn phím khi chuyển tab
                View currentFocus = getCurrentFocus();
                if (currentFocus != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                    currentFocus.clearFocus();
                }
            }
        });
    }
    
    private void setupTabLayout() {
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case ViewPagerAdapter.POSITION_MY_LIST:
                    tab.setText("My List");
                    break;
                case ViewPagerAdapter.POSITION_TOP100:
                    tab.setText("Top 100");
                    break;
                case ViewPagerAdapter.POSITION_SEARCH:
                    tab.setText("Search");
                    break;
            }
        }).attach();
    }
    
    private void setupPlayerControls() {
        // Play/Pause button
        btnPlayPause.setOnClickListener(v -> {
            if (serviceBound && musicService != null) {
                if (musicService.isPlaying()) {
                    musicService.pause();
                } else {
                    musicService.play();
                }
            }
        });
        
        // Next button
        btnNext.setOnClickListener(v -> {
            if (serviceBound && musicService != null) {
                musicService.playNext();
            }
        });
        
        // SeekBar change listener
        seekBarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Not needed, we'll handle this in onStopTrackingTouch
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (musicService != null) {
                    musicService.setSeeking(true);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (musicService != null) {
                    long duration = musicService.getDuration();
                    long newPosition = (duration * seekBar.getProgress()) / 100;
                    musicService.seekTo(newPosition);
                    musicService.setSeeking(false);
                }
            }
        });
    }
    
    private void startAndBindMusicService() {
        Intent intent = new Intent(this, MusicPlayerService.class);
        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    
    private void updatePlayerUI(Song song) {
        tvSongTitle.setText(song.getName());
        tvArtist.setText(song.getSinger());
        
        // Lấy URL ảnh và xử lý nó
        String imageUrl = song.getImage();
        // Log URL ảnh để debug
        android.util.Log.d("MainActivity", "Loading album art: " + imageUrl + " for song: " + song.getName());
        
        // Đảm bảo ImageView không có tint
        imgAlbumArt.setColorFilter(null);
        
        // Cải thiện cách tải ảnh bằng Glide
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_music_note)
                .error(R.drawable.ic_music_note)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .timeout(30000) // Tăng timeout lên 30 giây
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, 
                                              Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, 
                                              boolean isFirstResource) {
                        android.util.Log.e("MainActivity", "Lỗi tải ảnh album: " + imageUrl + 
                                        ", lỗi: " + (e != null ? e.getMessage() : "null"));
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, 
                                                com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, 
                                                com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        android.util.Log.d("MainActivity", "Tải ảnh album thành công: " + imageUrl);
                        return false;
                    }
                })
                .into(imgAlbumArt);
    }
    
    private void updatePlayPauseButton(boolean isPlaying) {
        btnPlayPause.setIconResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
        
        // Xoay ảnh đĩa khi đang phát, dừng xoay khi tạm dừng
        if (isPlaying) {
            imgAlbumArt.startAnimation(rotateAnimation);
        } else {
            imgAlbumArt.clearAnimation();
        }
    }
    
    private void updateSeekBar(long currentPosition, long duration) {
        if (duration > 0) {
            int progress = (int) ((currentPosition * 100) / duration);
            seekBarProgress.setProgress(progress);
        } else {
            seekBarProgress.setProgress(0);
        }
    }
    
    private String formatDuration(long durationMs) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
    
    // MyListFragment.OnSongActionListener implementation
    @Override
    public void onSongSelected(Song song, List<Song> playlist, int position) {
        if (serviceBound && musicService != null) {
            musicService.setPlaylist(playlist, position);
        }
    }

    @Override
    public void onFavoriteToggled(Song song) {
        if (song.isFavorite()) {
            // Kiểm tra trước khi thêm vào danh sách yêu thích
            boolean isNewFavorite = repository.addToFavoriteWithCheck(song);
            if (isNewFavorite) {
                showToast("Added to favorites");
            } else {
                showToast("Song already in favorites");
            }
        } else {
            // Xóa khỏi danh sách yêu thích
            repository.removeFromFavorites(song);
            showToast("Removed from favorites");
        }
        
        // Thông báo cho tất cả các tab về sự thay đổi trạng thái yêu thích
        favoriteChangeEvent.setValue(song);
        
        // Cập nhật UI của các tab thông qua ViewPagerAdapter
        ViewPagerAdapter pagerAdapter = (ViewPagerAdapter) viewPager.getAdapter();
        if (pagerAdapter != null) {
            // Cập nhật tab Top 100 nếu đang hiển thị
            Fragment topFragment = pagerAdapter.getFragment(ViewPagerAdapter.POSITION_TOP100);
            if (topFragment instanceof Top100Fragment) {
                ((Top100Fragment) topFragment).updateSongFavoriteStatus(song);
            }
            
            // Cập nhật tab Search nếu đang hiển thị
            Fragment searchFragment = pagerAdapter.getFragment(ViewPagerAdapter.POSITION_SEARCH);
            if (searchFragment instanceof SearchFragment) {
                ((SearchFragment) searchFragment).updateSongFavoriteStatus(song);
            }
        }
    }
    
    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }
    
    // MusicPlayerService.PlayerCallback implementation
    @Override
    public void onPlaybackStateChanged(boolean isPlaying, long currentPosition, long duration) {
        updatePlayPauseButton(isPlaying);
        if (!musicService.isSeeking()) {
            updateSeekBar(currentPosition, duration);
        }
    }

    @Override
    public void onSongChanged(Song song) {
        updatePlayerUI(song);
    }
    
    @Override
    protected void onDestroy() {
        handler.removeCallbacks(updateSeekBarTask);
        
        if (serviceBound) {
            musicService.unregisterCallback(this);
            unbindService(serviceConnection);
            serviceBound = false;
        }
        
        super.onDestroy();
    }
}