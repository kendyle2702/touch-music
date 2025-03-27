package com.example.waterremindervn.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ZingChartResponse {
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
        private List<SongItem> song;

        public List<SongItem> getSong() {
            return song;
        }
    }

    public static class SongItem {
        private String id;
        private String name;
        private String title;
        private String code;
        private String thumbnail;
        private String performer;
        @SerializedName("artists_names")
        private String artistsNames;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getTitle() {
            return title;
        }

        public String getCode() {
            return code;
        }

        public String getThumbnail() {
            return thumbnail;
        }

        public String getPerformer() {
            return performer;
        }

        public String getArtistsNames() {
            return artistsNames;
        }
    }
} 