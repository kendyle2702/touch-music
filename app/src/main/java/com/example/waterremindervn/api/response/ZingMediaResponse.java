package com.example.waterremindervn.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class ZingMediaResponse {
    private String err;
    private String msg;
    private Data data;

    public String getErr() {
        return err;
    }

    public String getMsg() {
        return msg;
    }

    public Data getData() {
        return data;
    }

    public boolean isSuccess() {
        return err == null || err.isEmpty() || "0".equals(err);
    }

    public static class Data {
        private String name;
        private String title;
        private String performer;
        @SerializedName("artists_names")
        private String artistsNames;
        private Map<String, String> source;
        private Album album;

        public String getName() {
            return name;
        }

        public String getTitle() {
            return title;
        }

        public String getPerformer() {
            return performer;
        }

        public String getArtistsNames() {
            return artistsNames;
        }

        public Map<String, String> getSource() {
            return source;
        }

        public Album getAlbum() {
            return album;
        }

        // Get the stream URL based on quality (128, 320, etc.)
        public String getStreamUrl(String quality) {
            if (source != null && source.containsKey(quality)) {
                return source.get(quality);
            }
            // Default to 128 quality if the requested quality is not available
            if (source != null && source.containsKey("128")) {
                return source.get("128");
            }
            return null;
        }
    }

    public static class Album {
        private String name;
        private String thumbnail;

        public String getName() {
            return name;
        }

        public String getThumbnail() {
            return thumbnail;
        }
    }
} 