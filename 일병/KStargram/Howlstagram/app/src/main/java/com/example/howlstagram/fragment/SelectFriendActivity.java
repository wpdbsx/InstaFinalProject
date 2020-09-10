package com.example.howlstagram.fragment;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.howlstagram.R;
import com.example.howlstagram.chat.MessageActivity;
import com.example.howlstagram.model.ChatModel;
import com.example.howlstagram.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SelectFriendActivity extends AppCompatActivity {
   ChatModel chatModel = new ChatModel();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friend);
         RecyclerView recyclerView = findViewById(R.id.selectFriendActivity_recyclerview);
         recyclerView.setAdapter(new SelectFriendRecyclerViewAdapter());
         recyclerView.setLayoutManager(new LinearLayoutManager(this));
         Button button =  findViewById(R.id.selectFriendActivty_button);
         button.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 String myUid= FirebaseAuth.getInstance().getCurrentUser().getUid();
                 chatModel.users.put(myUid,true);
                 FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel);
             }
         });
    }
    class SelectFriendRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<UserModel> userModels;

        public SelectFriendRecyclerViewAdapter() {

            userModels = new ArrayList<>();

            final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    userModels.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {


                        UserModel userModel = snapshot.getValue(UserModel.class);

                        if (userModel.youruid.equals(myUid)) {
                            continue;
                        }

                        userModels.add(userModel);
                    }
                    notifyDataSetChanged();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_select, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            //   Log.v("gd",userModels.get(position).ProfileImageUrl);
//

            Glide.with
                    (holder.itemView.getContext())
                    .load(userModels.get(position).ProfileImageUrl)
                    .apply(new RequestOptions().circleCrop())
                    .into(((CustomViewHolder) holder).imageView);



            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();


            ((CustomViewHolder) holder).textView.setText(userModels.get(position).userName);

            holder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent(view.getContext(), MessageActivity.class);
                intent.putExtra("destinationUid",userModels.get(position).youruid);
                ActivityOptions activityOptions = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(),R.anim.fromright,R.anim.toleft);
                    startActivity(intent,activityOptions.toBundle());
                }

            });
            if(userModels.get(position).comment !=null) {
                ((CustomViewHolder) holder).textView_comment.setText(userModels.get(position).comment);
            }
            ((CustomViewHolder)holder).checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                //체크상태
                if(isChecked){
                    chatModel.users.put(userModels.get(position).youruid,true);
                    //체크취소상태
                 }else{
                    chatModel.users.remove(userModels.get(position));
                 }
            });
        }


        @Override
        public int getItemCount() {
            return userModels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {

            public ImageView imageView;
            public TextView textView;
            public TextView textView_comment;
            public CheckBox checkBox;

            public CustomViewHolder(View view) {
                super(view);
                imageView = (ImageView) view.findViewById(R.id.frienditem_imageview);
                textView = (TextView) view.findViewById(R.id.frienditem_textview);
                textView_comment = view.findViewById(R.id.frienditem_textview_comment);
                checkBox = view.findViewById(R.id.frienditem_checkbox);
            }
        }
    }

}
