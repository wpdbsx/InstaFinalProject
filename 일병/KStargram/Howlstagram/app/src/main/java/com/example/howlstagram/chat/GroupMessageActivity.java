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

public class GroupMessageActivity extends AppCompatActivity {

    Map<String, UserModel> users = new HashMap<>();
    String destinationRoom;
    String uid;
    EditText editText;

    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private RecyclerView recyclerView;

    List<ChatModel.Comment> comments = new ArrayList<>();

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");

    int peopleCount = 0;
    String url = "https://fcm.googleapis.com/fcm/send";

    String serverKey =
            "AAAAFPpVLVs:APA91bGzeEWT3uIQVwNlZ5zaQIE_ANgOtOcKc7dnBLvKCFlyg2D-lyq25LcIgwmLEpvcITpb06JGqroqVZ4KSD5gUvUnxikOsKrgiIgHjCLxSWJ_JZW_X2TzSJ7qeOpaJm0RKv8XSPDF";

    NotificationModel notificationModel = new NotificationModel();
     String userName1 = "";
    private UserModel userModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_message);

        destinationRoom = getIntent().getStringExtra("destinationRoom");
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        editText = (EditText)findViewById(R.id.groupMessageActivity_editText);

        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // 여러개 배열로 넘어오는데 for문으로 하나씩 뽑아오는데 이 키값을 해쉬맵에 저장
                // 그 후 value 값을 usermodel로 캐스팅해서 List의 Usermodel에 저장
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    users.put(item.getKey(), item.getValue(UserModel.class));
                }

                init();
                recyclerView = (RecyclerView)findViewById(R.id.groupMessageActivity_recyclerview);
                recyclerView.setAdapter(new GroupMessageRecyclerViewAdapter());
                recyclerView.setLayoutManager(new LinearLayoutManager(GroupMessageActivity.this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    void init(){
        Button button = (Button)findViewById(R.id.groupMessageActivity_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatModel.Comment comment = new ChatModel.Comment();
                comment.uid = uid;
                comment.message = editText.getText().toString();
                comment.timestamp = ServerValue.TIMESTAMP;
                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destinationRoom).child("comments").push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destinationRoom).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Map<String, Boolean> map = (Map<String, Boolean>) dataSnapshot.getValue();

                                // 한 줄씩 키값을 받아오게 된다.
                                for(String item : map.keySet()){
                                    if(item.equals(uid)){
                                        continue;
                                    }
                                    sendGcm(users.get(item).pushToken);
                                }
                                editText.setText("");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
        });
    }
    void sendGcm(String pushToken){

        Gson gson = new Gson();



        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override

                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot item : dataSnapshot.getChildren()) {
                            if (item.getKey() .equals("userName") ) {
                                String comment = item.getValue().toString();
                                userName1 = comment;

                            }
                        }






                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        notificationModel.notification.title = userName1;
        notificationModel.data.title = userName1;

        Log.v("안녕23415",notificationModel.data.title );
        notificationModel.to = pushToken;
        notificationModel.notification.text = editText.getText().toString();
        notificationModel.data.text = editText.getText().toString();

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
                System.out.println(response.body().toString());
            }
        });
    }

    class GroupMessageRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        public GroupMessageRecyclerViewAdapter() {
            getMessageList();
        }

        void getMessageList(){
            databaseReference = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destinationRoom).child("comments");

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
                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destinationRoom).child("comments").updateChildren(readUserMap).addOnCompleteListener(new OnCompleteListener<Void>() {
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

            return new GroupMessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            GroupMessageViewHolder messageViewHolder = ((GroupMessageViewHolder)holder);

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
                        .load(users.get(comments.get(position).uid).ProfileImageUrl)
                        .apply(new RequestOptions().circleCrop())
                        .into(messageViewHolder.imageView_profiles);
                messageViewHolder.textview_name.setText(users.get(comments.get(position).uid).userName);
                messageViewHolder.linearLayout_destination.setVisibility(View.VISIBLE);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.leftbubble);
                messageViewHolder.textView_message.setText(comments.get(position).message);

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

                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destinationRoom).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
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

        private class GroupMessageViewHolder extends RecyclerView.ViewHolder {

            public TextView textView_message;
            public TextView textview_name;
            public ImageView imageView_profiles;
            public LinearLayout linearLayout_destination;
            public LinearLayout linearLayout_main;
            public TextView textView_timestamp;
            public TextView textView_readCounter_left;
            public TextView textView_readCounter_right;

            public GroupMessageViewHolder(View view) {
                super(view);

                textView_message = (TextView) view.findViewById(R.id.messageItem_textView_message);
                textview_name = (TextView) view.findViewById(R.id.messageItem_textview_name);
                imageView_profiles = (ImageView) view.findViewById(R.id.messageItem_imageview_profile);
                linearLayout_destination = (LinearLayout) view.findViewById(R.id.messageItem_linearlayout_destination);
                linearLayout_main = view.findViewById(R.id.messageItem_linearlayout_main);
                textView_timestamp = view.findViewById(R.id.messageItem_textview_timestamp);
                textView_readCounter_left = view.findViewById(R.id.messageItem_textview_readCounter_left);
                textView_readCounter_right = view.findViewById(R.id.messageItem_textview_readCounter_right);

            }
        }
    }

}
