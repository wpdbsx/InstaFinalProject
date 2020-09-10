package com.example.howlstagram.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class ChatModel {


   public Map<String,Boolean> users = new HashMap<>(); //채티방 유저들
   public Map<String,Comment> comments = new HashMap<>();//채티방의 대화내용

    public static class Comment{
        public String uid;
        public String message;
        public Object timestamp;
        public Map<String,Object> readUsers =new HashMap<>();

    }

}
