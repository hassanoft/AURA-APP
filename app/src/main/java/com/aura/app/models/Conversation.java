package com.aura.app.models;

import com.google.gson.annotations.SerializedName;

/** Conversation (fil de messages) entre deux utilisateurs */
public class Conversation {
    @SerializedName("id")           private String  id;
    @SerializedName("user1_id")     private String  user1Id;
    @SerializedName("user2_id")     private String  user2Id;
    @SerializedName("last_message") private String  lastMessage;
    @SerializedName("updated_at")   private String  updatedAt;
    @SerializedName("unread_count") private int     unreadCount;

    // Champs de l'interlocuteur (jointure)
    private String otherUsername;
    private String otherAvatar;
    private boolean otherOnline;

    public Conversation() {}

    public String  getId()            { return id != null ? id : ""; }
    public String  getUser1Id()       { return user1Id != null ? user1Id : ""; }
    public String  getUser2Id()       { return user2Id != null ? user2Id : ""; }
    public String  getLastMessage()   { return lastMessage != null ? lastMessage : ""; }
    public String  getUpdatedAt()     { return updatedAt != null ? updatedAt : ""; }
    public int     getUnreadCount()   { return unreadCount; }
    public String  getOtherUsername() { return otherUsername != null ? otherUsername : ""; }
    public String  getOtherAvatar()   { return otherAvatar != null ? otherAvatar : ""; }
    public boolean isOtherOnline()    { return otherOnline; }

    public void setId(String v)             { id = v; }
    public void setUser1Id(String v)        { user1Id = v; }
    public void setUser2Id(String v)        { user2Id = v; }
    public void setLastMessage(String v)    { lastMessage = v; }
    public void setUpdatedAt(String v)      { updatedAt = v; }
    public void setUnreadCount(int v)       { unreadCount = v; }
    public void setOtherUsername(String v)  { otherUsername = v; }
    public void setOtherAvatar(String v)    { otherAvatar = v; }
    public void setOtherOnline(boolean v)   { otherOnline = v; }
}
