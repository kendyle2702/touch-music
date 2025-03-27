package com.example.waterremindervn.api.response;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class SongResponse {
    private String err;
    private String msg;
    private SongData data;
    private long timestamp;

    public String getErr() {
        return err;
    }

    public String getMsg() {
        return msg;
    }

    public SongData getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isSuccess() {
        return err == null || err.isEmpty() || "0".equals(err);
    }

    public static class SongData {
        @SerializedName("128")
        private String quality128;
        
        @SerializedName("320")
        private String quality320;

        public String getQuality128() {
            return quality128;
        }

        public String getQuality320() {
            return quality320;
        }

        public String getUrl() {
            // Ưu tiên chất lượng 128 như yêu cầu
            return quality128 != null ? quality128 : quality320;
        }
    }
} 