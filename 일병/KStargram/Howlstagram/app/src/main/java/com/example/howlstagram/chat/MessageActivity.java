package com.example.howlstagram.chat;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.howlstagram.R;
import com.example.howlstagram.model.ChatModel;
import com.example.howlstagram.model.NotificationModel;
import com.example.howlstagram.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MessageActivity extends AppCompatActivity {

    private String destinationUid;
    private Button button;
    private EditText editText;

    private String uid;
    private String chatRoomUid;

    private RecyclerView recyclerView;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");

    private UserModel userModel;

    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    int peopleCount = 0;

    String url = "https://fcm.googleapis.com/fcm/send";
    String serverKey =
            "AAAAFPpVLVs:APA91bGzeEWT3uIQVwNlZ5zaQIE_ANgOtOcKc7dnBLvKCFlyg2D-lyq25LcIgwmLEpvcITpb06JGqroqVZ4KSD5gUvUnxikOsKrgiIgHjCLxSWJ_JZW_X2TzSJ7qeOpaJm0RKv8XSPDF";
    NotificationModel notificationModel = new NotificationModel();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();                 // 채팅을 요구하는 아이디 즉 단말기에 로그인된 UID
        destinationUid = getIntent().getStringExtra("destinationUid");       // 채팅을 당하는 아이디
        button = (Button)findViewById(R.id.messageActivity_button);
        editText = (EditText)findViewById(R.id.messageActivity_editText);

        recyclerView = (RecyclerView)findViewById(R.id.messageActivity_recyclerview);
        button.setOnClickListener(view -> {
            ChatModel chatModel = new ChatModel();
            chatModel.users.put(uid, true);
            chatModel.users.put(destinationUid, true);

            if(chatRoomUid == null){
                button.setEnabled(false);
                FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel).addOnSuccessListener(aVoid -> checkChatRoom());
                // push 일종의 프라이머 / 채팅방 이름 생성
            }else{
                ChatModel.Comment comment = new ChatModel.Comment();
                comment.uid = uid;
                comment.message = editText.getText().toString();
                comment.timestamp = ServerValue.TIMESTAMP;
                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        Log.v("안녕","안녕");
                        sendGcm();
                        editText.setText("");

                        Log.v("안녕","안녕4");
                    }
                });
            }
        });
        checkChatRoom();

    }

    void sendGcm(){


        Log.v("안녕","안녕2");
        Gson gson = new Gson();
        final String[] userName1 = {""};
        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override

                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.v("하이","하이1");
                        for (DataSnapshot item : dataSnapshot.getChildren()) {
                            if (item.getKey() .equals("userName") ) {
                                String comment = item.getValue().toString();
                                 userName1[0] = comment;
                                 Log.v("하이ㅎㅇ", comment);

                                Log.v("하이","하이2");

                            }
                        }


                        notificationModel.to = userModel.pushToken;
                        notificationModel.notification.title = userName1[0];
                        notificationModel.data.title = userName1[0];




                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        notificationModel.notification.text = editText.getText().toString();
        notificationModel.data.text = editText.getText().toString();
////
        Log.v("안녕234",notificationModel.data.text);
        Log.v("안녕234", userName1[0]);
//        RequestBody requestBody = RequestBody.create(JSON, gson.toJson(notificationModel));

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"), gson.toJson(notificationModel));

        Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                .addHeader("Authorization", "key="+serverKey)
                .url(url)
                .post(requestBody)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                Log.v("안녕2",userName1[0]);
                System.out.println(response.body().toString());
            }
        });



        Log.v("안녕","안녕3");
    }


    void checkChatRoom(){
        FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    ChatModel chatModel = item.getValue(ChatModel.class);
                    if(chatModel.users.containsKey(destinationUid) && chatModel.users.size() == 2){
                        chatRoomUid = item.getKey();
                        button.setEnabled(true);
                        recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
                        recyclerView.setAdapter(new RecyclerViewAdapter());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<ChatModel.Comment> comments;

        public RecyclerViewAdapter() {
            comments = new ArrayList<>();

            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userModel = dataSnapshot.getValue(UserModel.class);
                    getMessageList();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        void getMessageList(){
            databaseReference = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments");

            // 메세지가 없을 경우 valueEventListener 값이 null이 되기 때문에 백 키를 누를 경우 오류가 생길 수 있다.
            valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    comments.clear();                                           // 데이터가 쌓이는 것을 방지
                    Map<String, Object> readUserMap = new HashMap<>();

                    for(DataSnapshot item : dataSnapshot.getChildren()){
                        String key = item.getKey();
                        ChatModel.Comment comment_origin = item.getValue(ChatModel.Comment.class);
                        ChatModel.Comment comment_modify = item.getValue(ChatModel.Comment.class);
                        comment_modify.readUsers.put(uid, true);

                        readUserMap.put(key, comment_modify);
                        comments.add(comment_origin);
                    }

                    if(comments.size() == 0){return;}

                    if(!comments.get(comments.size()-1).readUsers.containsKey(uid)) {
                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").updateChildren(readUserMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                // 메세지가 갱신
                                notifyDataSetChanged();

                                // 맨 마지막 포시션으로 이동
                                recyclerView.scrollToPosition(comments.size() - 1);
                            }
                        });
                    }
                    else{
                        // 메세지가 갱신
                        notifyDataSetChanged();

                        // 맨 마지막 포시션으로 이동
                        recyclerView.scrollToPosition(comments.size() - 1);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);

            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            MessageViewHolder messageViewHolder = ((MessageViewHolder)holder);

            // 내가 보낸 메세지
            if(comments.get(position).uid.equals(uid)){
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.rightbubble);
                messageViewHolder.linearLayout_destination.setVisibility(View.INVISIBLE);
                messageViewHolder.linearLayout_main.setGravity(Gravity.RIGHT);
                setReadCounter(position, messageViewHolder.textView_readCounter_left);

            }

            // 상대방이 보낸 메세지
            else{
                Glide.with(holder.itemView.getContext())
                        .load(userModel.ProfileImageUrl)
                        .apply(new RequestOptions().circleCrop())
                        .into(messageViewHolder.imageView_profile);
                messageViewHolder.textView_name.setText(userModel.userName);
                messageViewHolder.linearLayout_destination.setVisibility(View.VISIBLE);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.leftbubble);
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.textView_message.setTextSize(26);
                messageViewHolder.linearLayout_main.setGravity(Gravity.LEFT);
                setReadCounter(position, messageViewHolder.textView_readCounter_right);
            }

            long unixTime = (long) comments.get(position).timestamp;
            Date date = new Date(unixTime);

            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            String time = simpleDateFormat.format(date);
            messageViewHolder.textView_timestamp.setText(time);

        }

        void setReadCounter(final int position, final TextView textView){

            if(peopleCount == 0) {

                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String, Boolean> users = (Map<String, Boolean>) dataSnapshot.getValue();
                        peopleCount = users.size();

                        // 코멘트를 읽지 않은 사람의 카운트를 가져옴
                        int count = peopleCount - comments.get(position).readUsers.size();

                        if (count > 0) {
                            textView.setVisibility(View.VISIBLE);
                            textView.setText(String.valueOf(count));
                        } else {
                            textView.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            else{
                int count = peopleCount - comments.get(position).readUsers.size();

                if (count > 0) {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(String.valueOf(count));
                } else {
                    textView.setVisibility(View.INVISIBLE);
                }
            }


        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class MessageViewHolder extends RecyclerView.ViewHolder {
            public TextView textView_message;
            public TextView textView_name;
            public ImageView imageView_profile;
            public LinearLayout linearLayout_destination;
            public LinearLayout linearLayout_main;
            public TextView textView_timestamp;
            public TextView textView_readCounter_left;
            public TextView textView_readCounter_right;

            public MessageViewHolder(View view) {
                super(view);
                textView_message =(TextView) view.findViewById(R.id.messageItem_textView_message);
                textView_name = (TextView) view.findViewById(R.id.messageItem_textview_name);
                imageView_profile = (ImageView)view.findViewById(R.id.messageItem_imageview_profile);
                linearLayout_destination = (LinearLayout)view.findViewById(R.id.messageItem_linearlayout_destination);
                linearLayout_main = (LinearLayout)view.findViewById(R.id.messageItem_linearlayout_main);
                textView_timestamp = (TextView)view.findViewById(R.id.messageItem_textview_timestamp);
                textView_readCounter_left = (TextView)view.findViewById(R.id.messageItem_textview_readCounter_left);
                textView_readCounter_right = (TextView)view.findViewById(R.id.messageItem_textview_readCounter_right);


            }
        }
    }
    @Override
    public void onBackPressed(){


        if(valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
        }

        // 백 키 누를 때 watching 하는 것이 꺼지게 된다.
        finish();
        overridePendingTransition(R.anim.fromleft, R.anim.toright);
    }
}