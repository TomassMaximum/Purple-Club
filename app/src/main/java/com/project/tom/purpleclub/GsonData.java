package com.project.tom.purpleclub;

/**
 * Created by Tom on 2016/1/28.
 */
public class GsonData {

    private String access_token;

    public static final String DRIBBBLE_GET_CODE_PARAM = "https://dribbble.com/oauth/authorize?client_id=f6a62b7f35784ebc46ca965c7b7375de8a3172f4887c8ee86e10427e748c27ee&scope=public+write+comment+upload";

    public static final String DRIBBBLE_GET_ACCESS_TOKEN = "https://dribbble.com/oauth/token?client_id=f6a62b7f35784ebc46ca965c7b7375de8a3172f4887c8ee86e10427e748c27ee&client_secret=7260ba76972c21b693c6960d976f991454930ef19c69eb9e1ed944dee82a1feb&";

    public static final String DRIBBBLE_GET_JSON_WITH_ACCESS_TOKEN = "https://api.dribbble.com/v1/user";

    public static final String DRIBBBLE_GET_SHOTS = "https://api.dribbble.com/v1/shots?";

    public static final String ACCESS_TOKEN = "access_token=";

    public static final String SORT_RECENT = "&sort=recent";

    public static final String SORT_COMMENTS = "&sort=comments";

    public static final String SORT_VIEWS = "&sort=views";

    private String id;
    private String name;
    private String username;
    private String html_url;
    private String avatar_url;
    private String bio;
    private String location;
    private String links;
    private String web;
    private String twitter;
    private String buckets_count;
    private String comments_received_count;
    private String followers_count;
    private String followings_count;
    private String likes_count;
    private String likes_received_count;
    private String projects_count;
    private String rebounds_received_count;
    private String shots_count;
    private String teams_count;
    private String can_upload_shot;
    private String type;
    private String pro;

    public String getAccessToken() {
        return access_token;
    }

    public void setAccessToken(String access_token) {
        this.access_token = access_token;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHtml_url() {
        return html_url;
    }

    public void setHtml_url(String html_url) {
        this.html_url = html_url;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLinks() {
        return links;
    }

    public void setLinks(String links) {
        this.links = links;
    }

    public String getWeb() {
        return web;
    }

    public void setWeb(String web) {
        this.web = web;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getBuckets_count() {
        return buckets_count;
    }

    public void setBuckets_count(String buckets_count) {
        this.buckets_count = buckets_count;
    }

    public String getComments_received_count() {
        return comments_received_count;
    }

    public void setComments_received_count(String comments_received_count) {
        this.comments_received_count = comments_received_count;
    }

    public String getFollowers_count() {
        return followers_count;
    }

    public void setFollowers_count(String followers_count) {
        this.followers_count = followers_count;
    }

    public String getFollowings_count() {
        return followings_count;
    }

    public void setFollowings_count(String followings_count) {
        this.followings_count = followings_count;
    }

    public String getLikes_count() {
        return likes_count;
    }

    public void setLikes_count(String likes_count) {
        this.likes_count = likes_count;
    }

    public String getLikes_received_count() {
        return likes_received_count;
    }

    public void setLikes_received_count(String likes_received_count) {
        this.likes_received_count = likes_received_count;
    }

    public String getProjects_count() {
        return projects_count;
    }

    public void setProjects_count(String projects_count) {
        this.projects_count = projects_count;
    }

    public String getRebounds_received_count() {
        return rebounds_received_count;
    }

    public void setRebounds_received_count(String rebounds_received_count) {
        this.rebounds_received_count = rebounds_received_count;
    }

    public String getShots_count() {
        return shots_count;
    }

    public void setShots_count(String shots_count) {
        this.shots_count = shots_count;
    }

    public String getTeams_count() {
        return teams_count;
    }

    public void setTeams_count(String teams_count) {
        this.teams_count = teams_count;
    }

    public String getCan_upload_shot() {
        return can_upload_shot;
    }

    public void setCan_upload_shot(String can_upload_shot) {
        this.can_upload_shot = can_upload_shot;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPro() {
        return pro;
    }

    public void setPro(String pro) {
        this.pro = pro;
    }

}
