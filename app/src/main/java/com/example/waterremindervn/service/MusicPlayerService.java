package com.example.waterremindervn.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.waterremindervn.MainActivity;
import com.example.waterremindervn.R;
import com.example.waterremindervn.model.Song;
import com.example.waterremindervn.repository.MusicRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicPlayerService extends Service {
    private static final String TAG = "MusicPlayerService";
    private static final String CHANNEL_ID = "MusicPlayerChannel";
    private static final int NOTIFICATION_ID = 1;

    private final IBinder binder = new MusicBinder();
    private ExoPlayer player;
    private MusicRepository repository;
    private List<Song> playlist = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isRandom = false;
    private boolean isRepeat = false;
    private boolean isSeeking = false;
    private Notification notification;
    private Bitmap currentArt;

    private final List<PlayerCallback> callbacks = new ArrayList<>();

    public class MusicBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player = new ExoPlayer.Builder(this).build();
        repository = new MusicRepository(this);
        
        createNotificationChannel();
        
        // Listen for playback state changes
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    if (isRepeat) {
                        // Repeat current song
                        player.seekTo(0);
                        player.play();
                    } else if (isRandom) {
                        // Play random song
                        playRandomSong();
                    } else {
                        // Play next song
                        playNext();
                    }
                }
                updateCallbacks();
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                updateNotification();
                updateCallbacks();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createEmptyNotification());
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            player.release();
            player = null;
        }
        super.onDestroy();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Player",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Music Player Controls");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createEmptyNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Music Player")
                .setContentText("Playing music...")
                .setSmallIcon(R.drawable.ic_music_note)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void updateNotification() {
        if (getCurrentSong() == null) return;

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Song currentSong = getCurrentSong();
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(currentSong.getName())
                .setContentText(currentSong.getSinger())
                .setSmallIcon(R.drawable.ic_music_note)
                .setContentIntent(pendingIntent);
        
        if (currentArt != null) {
            builder.setLargeIcon(currentArt);
        }
        
        notification = builder.build();
        
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void loadArtwork(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return;
        
        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                    currentArt = resource;
                    updateNotification();
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    currentArt = null;
                }
            });
    }

    // Player control methods
    public void setPlaylist(List<Song> songs, int startIndex) {
        if (songs == null || songs.isEmpty()) return;
        
        playlist.clear();
        playlist.addAll(songs);
        currentIndex = Math.min(startIndex, playlist.size() - 1);
        
        playSong(currentIndex);
    }
    
    public void playSong(int index) {
        if (index < 0 || index >= playlist.size()) return;
        
        currentIndex = index;
        Song song = playlist.get(currentIndex);
        
        player.stop();
        player.clearMediaItems();
        
        // Update the repository with current song
        repository.setCurrentSong(song);
        
        // Load artwork for notification
        loadArtwork(song.getImage());
        
        // Get streaming URL
        repository.getSongStreamUrl(song.getId(), new MusicRepository.SongUrlCallback() {
            @Override
            public void onSuccess(String url) {
                // Update song with streaming URL
                song.setPath(url);
                
                // Prepare and play
                MediaItem mediaItem = MediaItem.fromUri(url);
                player.setMediaItem(mediaItem);
                player.prepare();
                player.play();
                
                updateCallbacks();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error getting song URL: " + errorMessage);
            }
        });
    }
    
    public void play() {
        player.play();
    }
    
    public void pause() {
        player.pause();
    }
    
    public void playNext() {
        if (playlist.isEmpty()) return;
        
        int nextIndex = (currentIndex + 1) % playlist.size();
        playSong(nextIndex);
    }
    
    public void playPrevious() {
        if (playlist.isEmpty()) return;
        
        int prevIndex = (currentIndex - 1 + playlist.size()) % playlist.size();
        playSong(prevIndex);
    }
    
    public void playRandomSong() {
        if (playlist.size() <= 1) return;
        
        int randomIndex;
        do {
            randomIndex = (int) (Math.random() * playlist.size());
        } while (randomIndex == currentIndex);
        
        playSong(randomIndex);
    }
    
    public void seekTo(long positionMs) {
        player.seekTo(positionMs);
    }
    
    public long getCurrentPosition() {
        return player.getCurrentPosition();
    }
    
    public long getDuration() {
        return player.getDuration();
    }
    
    public boolean isPlaying() {
        return player.isPlaying();
    }
    
    public void setRepeat(boolean repeat) {
        isRepeat = repeat;
    }
    
    public boolean isRepeat() {
        return isRepeat;
    }
    
    public void setRandom(boolean random) {
        isRandom = random;
    }
    
    public boolean isRandom() {
        return isRandom;
    }
    
    public Song getCurrentSong() {
        if (playlist.isEmpty() || currentIndex < 0 || currentIndex >= playlist.size()) {
            return null;
        }
        return playlist.get(currentIndex);
    }
    
    public List<Song> getPlaylist() {
        return Collections.unmodifiableList(playlist);
    }
    
    public int getCurrentIndex() {
        return currentIndex;
    }
    
    public void setSeeking(boolean seeking) {
        isSeeking = seeking;
    }
    
    public boolean isSeeking() {
        return isSeeking;
    }
    
    // Callback methods
    public interface PlayerCallback {
        void onPlaybackStateChanged(boolean isPlaying, long currentPosition, long duration);
        void onSongChanged(Song song);
    }
    
    public void registerCallback(PlayerCallback callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }
    
    public void unregisterCallback(PlayerCallback callback) {
        callbacks.remove(callback);
    }
    
    private void updateCallbacks() {
        Song currentSong = getCurrentSong();
        boolean isPlaying = player.isPlaying();
        long currentPosition = player.getCurrentPosition();
        long duration = player.getDuration();
        
        for (PlayerCallback callback : callbacks) {
            callback.onPlaybackStateChanged(isPlaying, currentPosition, duration);
            if (currentSong != null) {
                callback.onSongChanged(currentSong);
            }
        }
    }
} 