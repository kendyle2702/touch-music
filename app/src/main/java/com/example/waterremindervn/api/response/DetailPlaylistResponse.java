package com.example.waterremindervn.api.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DetailPlaylistResponse {
    private boolean success;
    private int statusCode;
    private String message;
    private PlaylistData data;

    public boolean isSuccess() {
        return success;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public PlaylistData getData() {
        return data;
    }

    public static class PlaylistData {
        @SerializedName("encodeId")
        private String id;
        
        private String title;
        
        private String thumbnail;
        
        @SerializedName("thumbnailM")
        private String thumbnailM;
        
        private String description;
        
        private SongSection song;

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getThumbnail() {
            return thumbnail;
        }

        public String getThumbnailM() {
            return thumbnailM;
        }

        public String getDescription() {
            return description;
        }

        public SongSection getSong() {
            return song;
        }
    }

    public static class SongSection {
        private List<SongItem> items;

        public List<SongItem> getItems() {
            return items;
        }
    }

    public static class SongItem {
        @SerializedName("encodeId")
        private String id;
        
        private String title;
        
        @SerializedName("artistsNames")
        private String artistsNames;
        
        private String thumbnail;
        
        private int duration;

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getArtistsNames() {
            return artistsNames;
        }

        public String getThumbnail() {
            return thumbnail;
        }

        public int getDuration() {
            return duration;
        }
    }
} 