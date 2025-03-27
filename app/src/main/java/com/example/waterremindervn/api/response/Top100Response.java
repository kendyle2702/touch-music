package com.example.waterremindervn.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Top100Response {
    private boolean success;
    private int statusCode;
    private String message;
    private List<PlaylistItem> data;

    public boolean isSuccess() {
        return success;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public List<PlaylistItem> getData() {
        return data;
    }

    public static class PlaylistItem {
        private String title;
        private List<Item> items;

        public String getTitle() {
            return title;
        }

        public List<Item> getItems() {
            return items;
        }
    }

    public static class Item {
        @SerializedName("encodeId")
        private String id;
        private String title;
        private String thumbnail;
        
        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getThumbnail() {
            return thumbnail;
        }
    }
} 