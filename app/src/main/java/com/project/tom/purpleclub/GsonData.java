package com.project.tom.purpleclub;

/**
 * Created by Tom on 2016/1/28.
 */
public class GsonData {
    private String access_token;

    public static final String DRIBBBLE_GET_CODE_PARAM = "https://dribbble.com/oauth/authorize?client_id=f6a62b7f35784ebc46ca965c7b7375de8a3172f4887c8ee86e10427e748c27ee&scope=public+write+comment+upload";

    public static final String DRIBBBLE_GET_ACCESS_TOKEN = "https://dribbble.com/oauth/token?client_id=f6a62b7f35784ebc46ca965c7b7375de8a3172f4887c8ee86e10427e748c27ee&client_secret=7260ba76972c21b693c6960d976f991454930ef19c69eb9e1ed944dee82a1feb&";

    public static final String DRIBBBLE_GET_JSON_WITH_ACCESS_TOKEN = "https://api.dribbble.com/v1/user";

    public static final String ACCESS_TOKEN = "access_token=";

    public static final String BUCKETS_ID = "/buckets/:id";


    public String getAccessToken() {
        return access_token;
    }

    public void setAccessToken(String accessToken) {
        this.access_token = accessToken;
    }
}
