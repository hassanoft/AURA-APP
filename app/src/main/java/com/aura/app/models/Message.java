package com.aura.app.models;

import com.google.gson.annotations.SerializedName;

/** Message privé entre deux utilisateurs */
public class Message {
    @SerializedName("id")              private String  id;
    @SerializedName("conversation_id") private String  conversationId;
    @SerializedName("sender_id")       private String  senderId;
    @SerializedName("content")         private String  content;
    @SerializedName("media_url")       private String  mediaUrl;
    @SerializedName("is_read")         private boolean isRead;
    @SerializedName("created_at")      private String  createdAt;
    private String senderUsername;
    private String senderAvatar;

    public Message() {}

    public String  getId()             { return id != null ? id : ""; }
    public String  getConversationId() { return conversationId != null ? conversationId : ""; }
    public String  getSenderId()       { return senderId != null ? senderId : ""; }
    public String  getContent()        { return content != null ? content : ""; }
    public String  getMediaUrl()       { return mediaUrl != null ? mediaUrl : ""; }
    public boolean isRead()            { return isRead; }
    public String  getCreatedAt()      { return createdAt != null ? createdAt : ""; }
    public String  getSenderUsername() { return senderUsername != null ? senderUsername : ""; }
    public String  getSenderAvatar()   { return senderAvatar != null ? senderAvatar : ""; }

    public void setId(String v)             { id = v; }
    public void setConversationId(String v) { conversationId = v; }
    public void setSenderId(String v)       { senderId = v; }
    public void setContent(String v)        { content = v; }
    public void setMediaUrl(String v)       { mediaUrl = v; }
    public void setRead(boolean v)          { isRead = v; }
    public void setCreatedAt(String v)      { createdAt = v; }
    public void setSenderUsername(String v) { senderUsername = v; }
    public void setSenderAvatar(String v)   { senderAvatar = v; }
}
