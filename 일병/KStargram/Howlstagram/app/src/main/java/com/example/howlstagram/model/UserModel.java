package com.example.howlstagram.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UserModel {

    public String userName;
    public String ProfileImageUrl;
    public String youruid;
    public String comment;
    public String pushToken;
    public UserModel() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public UserModel(String username, String url) {
        this.userName = username;
        this.ProfileImageUrl = url;
    }
    public UserModel(String username, String url,String youruid)  {
        this.userName = username;
        this.ProfileImageUrl = url;
        this.youruid = youruid;
    }
    public UserModel(String username, String url,String youruid,String comment)  {
        this.userName = username;
        this.ProfileImageUrl = url;
        this.youruid = youruid;
        this.comment= comment;
    }

}
