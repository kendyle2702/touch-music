package com.example.waterremindervn.api.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SearchResponse {
    private String err;
    private String msg;
    private SearchData data;
    private long timestamp;

    public String getErr() {
        return err;
    }

    public String getMsg() {
        return msg;
    }

    public SearchData getData() {
        return data;
    }
    
    public long getTimestamp() {
        return timestamp;
    }

    public boolean isSuccess() {
        return err == null || err.isEmpty() || "0".equals(err);
    }

    public static class SearchData {
        private List<SongItem> songs;
        private List<ArtistItem> artists;
        private SongItem top;

        public List<SongItem> getSongs() {
            return songs;
        }
        
        public List<ArtistItem> getArtists() {
            return artists;
        }
        
        public SongItem getTop() {
            return top;
        }
    }

    public static class SongItem {
        @SerializedName("encodeId")
        private String id;
        
        private String title;
        private String alias;
        
        @SerializedName("artistsNames")
        private String artistsNames;
        
        private List<ArtistItem> artists;
        private boolean isWorldWide;
        
        @SerializedName("thumbnailM")
        private String thumbnailM;
        
        private String link;
        private String thumbnail;
        private int duration;
        private boolean zingChoice;
        private boolean isPrivate;
        private boolean hasLyric;
        private String objectType;
        
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
        
        public String getThumbnailM() {
            return thumbnailM;
        }

        public int getDuration() {
            return duration;
        }
        
        public String getLink() {
            return link;
        }
        
        public List<ArtistItem> getArtists() {
            return artists;
        }
    }
    
    public static class ArtistItem {
        private String id;
        private String name;
        private String link;
        private String alias;
        private String thumbnail;
        
        @SerializedName("thumbnailM")
        private String thumbnailM;
        
        private int totalFollow;
        private boolean isOA;
        
        public String getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        public String getLink() {
            return link;
        }
        
        public String getThumbnail() {
            return thumbnail;
        }
        
        public String getThumbnailM() {
            return thumbnailM;
        }
        
        public int getTotalFollow() {
            return totalFollow;
        }
    }
} 