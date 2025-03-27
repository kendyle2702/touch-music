package com.example.waterremindervn.api.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SongInfoResponse {
    private String err;
    private String msg;
    private SongInfoData data;
    private long timestamp;

    public String getErr() {
        return err;
    }

    public String getMsg() {
        return msg;
    }

    public SongInfoData getData() {
        return data;
    }
    
    public long getTimestamp() {
        return timestamp;
    }

    public boolean isSuccess() {
        return err == null || err.isEmpty() || "0".equals(err);
    }

    public static class SongInfoData {
        @SerializedName("encodeId")
        private String id;
        
        private String title;
        private String alias;
        private boolean isOffical;
        private String username;
        
        @SerializedName("artistsNames")
        private String artistsNames;
        
        private List<Artist> artists;
        private boolean isWorldWide;
        
        @SerializedName("thumbnailM")
        private String thumbnailM;
        
        private String link;
        private String thumbnail;
        private int duration;
        private boolean zingChoice;
        private boolean isPrivate;
        private boolean preRelease;
        private long releaseDate;
        private List<String> genreIds;
        private Album album;
        private boolean isRBT;
        private int like;
        private int listen;
        private boolean liked;
        private int comment;

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

        public List<Artist> getArtists() {
            return artists;
        }
        
        public Album getAlbum() {
            return album;
        }
        
        public int getListen() {
            return listen;
        }
        
        public int getLike() {
            return like;
        }
    }
    
    public static class Artist {
        private String id;
        private String name;
        private String link;
        private String thumbnail;
        private String alias;
        
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
    }
    
    public static class Album {
        @SerializedName("encodeId")
        private String id;
        private String title;
        private String thumbnail;
        private boolean isoffical;
        private String link;
        private String artistsNames;
        
        public String getId() {
            return id;
        }
        
        public String getTitle() {
            return title;
        }
        
        public String getThumbnail() {
            return thumbnail;
        }
        
        public String getArtistsNames() {
            return artistsNames;
        }
    }
} 