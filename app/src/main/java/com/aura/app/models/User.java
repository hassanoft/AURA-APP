package com.aura.app.models;

import com.google.gson.annotations.SerializedName;

/** Utilisateur AURA — table users Supabase */
public class User {

    @SerializedName("id")            private String  id;
    @SerializedName("firstname")     private String  firstName;
    @SerializedName("lastname")      private String  lastName;
    @SerializedName("email")         private String  email;
    @SerializedName("username")      private String  username;
    @SerializedName("birthdate")     private String  birthDate;
    @SerializedName("avatar_url")    private String  avatarUrl;
    @SerializedName("bio")           private String  bio;
    @SerializedName("followers")     private int     followers;
    @SerializedName("following")     private int     following;
    @SerializedName("posts")         private int     postsCount;
    @SerializedName("verified")      private boolean verified;
    @SerializedName("created_at")    private String  createdAt;

    public User() {}

    public User(String id, String firstName, String lastName, String email,
                String username, String birthDate, String avatarUrl, String bio,
                int followers, int following, int postsCount, boolean verified) {
        this.id         = id;
        this.firstName  = firstName;
        this.lastName   = lastName;
        this.email      = email;
        this.username   = username;
        this.birthDate  = birthDate;
        this.avatarUrl  = avatarUrl;
        this.bio        = bio;
        this.followers  = followers;
        this.following  = following;
        this.postsCount = postsCount;
        this.verified   = verified;
    }

    public String  getId()          { return id != null ? id : ""; }
    public String  getFirstName()   { return firstName != null ? firstName : ""; }
    public String  getLastName()    { return lastName != null ? lastName : ""; }
    public String  getEmail()       { return email != null ? email : ""; }
    public String  getUsername()    { return username != null ? username : ""; }
    public String  getBirthDate()   { return birthDate != null ? birthDate : ""; }
    public String  getAvatarUrl()   { return avatarUrl != null ? avatarUrl : ""; }
    public String  getBio()         { return bio != null ? bio : ""; }
    public int     getFollowers()   { return followers; }
    public int     getFollowing()   { return following; }
    public int     getPostsCount()  { return postsCount; }
    public boolean isVerified()     { return verified; }
    public String  getCreatedAt()   { return createdAt != null ? createdAt : ""; }
    public String  getFullName()    { return firstName + " " + lastName; }
    public String  getAtUsername()  { return "@" + username; }

    public void setId(String v)          { id = v; }
    public void setFirstName(String v)   { firstName = v; }
    public void setLastName(String v)    { lastName = v; }
    public void setEmail(String v)       { email = v; }
    public void setUsername(String v)    { username = v; }
    public void setBirthDate(String v)   { birthDate = v; }
    public void setAvatarUrl(String v)   { avatarUrl = v; }
    public void setBio(String v)         { bio = v; }
    public void setFollowers(int v)      { followers = v; }
    public void setFollowing(int v)      { following = v; }
    public void setPostsCount(int v)     { postsCount = v; }
    public void setVerified(boolean v)   { verified = v; }
    public void setCreatedAt(String v)   { createdAt = v; }
}
