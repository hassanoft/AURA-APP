package com.aura.app.models;

import com.google.gson.annotations.SerializedName;

/** Publication AURA — table posts Supabase */
public class Post {

    @SerializedName("id")             private String  id;
    @SerializedName("user_id")        private String  userId;
    @SerializedName("content")        private String  content;
    @SerializedName("media_url")      private String  mediaUrl;
    @SerializedName("media_type")     private String  mediaType;   // "image" | "video" | ""
    @SerializedName("likes_count")    private int     likesCount;
    @SerializedName("comments_count") private int     commentsCount;
    @SerializedName("shares_count")   private int     sharesCount;
    @SerializedName("created_at")     private String  createdAt;

    // Champs joints depuis la table users
    private String  authorUsername;
    private String  authorAvatar;
    private String  authorFirstName;
    private boolean authorVerified;

    // État UI local (non persisté en base)
    private boolean likedByMe;
    private boolean savedByMe;

    public Post() {}

    // ─── Getters ──────────────────────────────────────────────────────────────

    public String  getId()             { return id != null ? id : ""; }
    public String  getUserId()         { return userId != null ? userId : ""; }
    public String  getContent()        { return content != null ? content : ""; }
    public String  getMediaUrl()       { return mediaUrl != null ? mediaUrl : ""; }
    public String  getMediaType()      { return mediaType != null ? mediaType : ""; }
    public int     getLikesCount()     { return likesCount; }
    public int     getCommentsCount()  { return commentsCount; }
    public int     getSharesCount()    { return sharesCount; }
    public String  getCreatedAt()      { return createdAt != null ? createdAt : ""; }
    public String  getAuthorUsername() { return authorUsername != null ? authorUsername : ""; }
    public String  getAuthorAvatar()   { return authorAvatar != null ? authorAvatar : ""; }
    public String  getAuthorFirstName(){ return authorFirstName != null ? authorFirstName : ""; }
    public boolean isAuthorVerified()  { return authorVerified; }
    public boolean isLikedByMe()       { return likedByMe; }
    public boolean isSavedByMe()       { return savedByMe; }
    public boolean hasMedia()          { return mediaUrl != null && !mediaUrl.isEmpty(); }
    public boolean isImage()           { return "image".equals(mediaType); }
    public boolean isVideo()           { return "video".equals(mediaType); }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setId(String v)             { id = v; }
    public void setUserId(String v)         { userId = v; }
    public void setContent(String v)        { content = v; }
    public void setMediaUrl(String v)       { mediaUrl = v; }
    public void setMediaType(String v)      { mediaType = v; }
    public void setLikesCount(int v)        { likesCount = v; }
    public void setCommentsCount(int v)     { commentsCount = v; }
    public void setSharesCount(int v)       { sharesCount = v; }
    public void setCreatedAt(String v)      { createdAt = v; }
    public void setAuthorUsername(String v) { authorUsername = v; }
    public void setAuthorAvatar(String v)   { authorAvatar = v; }
    public void setAuthorFirstName(String v){ authorFirstName = v; }
    public void setAuthorVerified(boolean v){ authorVerified = v; }
    public void setLikedByMe(boolean v)     { likedByMe = v; }
    public void setSavedByMe(boolean v)     { savedByMe = v; }
}
