package com.example.waterremindervn.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.waterremindervn.model.Song;

import java.util.List;

@Dao
public interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Song song);

    @Update
    void update(Song song);

    @Delete
    void delete(Song song);

    @Query("DELETE FROM songs WHERE id = :songId")
    void deleteSongById(String songId);

    @Query("SELECT * FROM songs")
    LiveData<List<Song>> getAllSongs();

    @Query("SELECT * FROM songs WHERE id = :songId")
    Song getSongById(String songId);

    @Query("SELECT EXISTS(SELECT 1 FROM songs WHERE id = :songId LIMIT 1)")
    boolean songExists(String songId);
} 