package com.example.howlstagram;


import android.net.Uri;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class WordItemData {
    static FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
    static String s[]=new String[1];
    static Uri uri;


    public Uri word;
    public String meaning;

    // 화면에 표시될 문자열 초기화
    public WordItemData(Uri word, String meaning) {
        this.word = word;
        this.meaning = meaning;
    }

    // 입력받은 숫자의 리스트생성
    public static ArrayList<WordItemData> createContactsList(int numContacts) {
        ArrayList<WordItemData> contacts = new ArrayList<WordItemData>();


        for (int i = 1; i <= numContacts; i++) {
            contacts.add(new WordItemData(uri, "wohahahaha"+i));
        }

        return contacts;
    }
}


