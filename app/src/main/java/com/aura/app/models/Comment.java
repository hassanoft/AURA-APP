package com.aura.app.models;

import com.google.gson.annotations.SerializedName;

/** Commentaire sur un post */
public class Comment {
    @SerializedName("id")         private String id;
    @SerializedName("post_id")    private String postId;
    @SerializedName("user_id")    private String userId;
    @SerializedName("content")    private String content;
    @SerializedName("created_at") private String createdAt;
    private String authorUsername;
    private String authorAvatar;

    public Comment() {}

    public String getId()             { return id != null ? id : ""; }
    public String getPostId()         { return postId != null ? postId : ""; }
    public String getUserId()         { return userId != null ? userId : ""; }
    public String getContent()        { return content != null ? content : ""; }
    public String getCreatedAt()      { return createdAt != null ? createdAt : ""; }
    public String getAuthorUsername() { return authorUsername != null ? authorUsername : ""; }
    public String getAuthorAvatar()   { return authorAvatar != null ? authorAvatar : ""; }

    public void setId(String v)             { id = v; }
    public void setPostId(String v)         { postId = v; }
    public void setUserId(String v)         { userId = v; }
    public void setContent(String v)        { content = v; }
    public void setCreatedAt(String v)      { createdAt = v; }
    public void setAuthorUsername(String v) { authorUsername = v; }
    public void setAuthorAvatar(String v)   { authorAvatar = v; }
}
