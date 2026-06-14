package com.aura.app.models;

import com.google.gson.annotations.SerializedName;

/** Notification utilisateur (like, commentaire, abonnement, mention) */
public class Notification {
    @SerializedName("id")         private String  id;
    @SerializedName("user_id")    private String  userId;
    @SerializedName("sender_id")  private String  senderId;
    @SerializedName("type")       private String  type;      // like|comment|follow|mention
    @SerializedName("post_id")    private String  postId;
    @SerializedName("is_read")    private boolean isRead;
    @SerializedName("created_at") private String  createdAt;
    private String senderUsername;
    private String senderAvatar;

    public Notification() {}

    public String  getId()             { return id != null ? id : ""; }
    public String  getUserId()         { return userId != null ? userId : ""; }
    public String  getSenderId()       { return senderId != null ? senderId : ""; }
    public String  getType()           { return type != null ? type : ""; }
    public String  getPostId()         { return postId != null ? postId : ""; }
    public boolean isRead()            { return isRead; }
    public String  getCreatedAt()      { return createdAt != null ? createdAt : ""; }
    public String  getSenderUsername() { return senderUsername != null ? senderUsername : ""; }
    public String  getSenderAvatar()   { return senderAvatar != null ? senderAvatar : ""; }

    public void setId(String v)             { id = v; }
    public void setUserId(String v)         { userId = v; }
    public void setSenderId(String v)       { senderId = v; }
    public void setType(String v)           { type = v; }
    public void setPostId(String v)         { postId = v; }
    public void setRead(boolean v)          { isRead = v; }
    public void setCreatedAt(String v)      { createdAt = v; }
    public void setSenderUsername(String v) { senderUsername = v; }
    public void setSenderAvatar(String v)   { senderAvatar = v; }

    /** Message humain lisible de la notification */
    public String getDisplayText() {
        String who = "@" + senderUsername;
        switch (type != null ? type : "") {
            case "like":    return who + " a aimé votre publication";
            case "comment": return who + " a commenté votre publication";
            case "follow":  return who + " a commencé à vous suivre";
            case "mention": return who + " vous a mentionné";
            default:        return who + " a interagi avec vous";
        }
    }
}
