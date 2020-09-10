package com.example.howlstagram.fragment;


import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.howlstagram.R;
import com.example.howlstagram.chat.MessageActivity;
import com.example.howlstagram.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class PeopleFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_people, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.peoplefragment_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new PeopleFragmentRecyclerViewAdapter());
        FloatingActionButton floatingActionButton = view.findViewById(R.id.peoplefragment_floatingButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                startActivity(new Intent(view.getContext(),SelectFriendActivity.class));

            }
        });
        return view;

    }

    class PeopleFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<UserModel> userModels;

        public PeopleFragmentRecyclerViewAdapter() {

            userModels = new ArrayList<>();

            final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    userModels.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {


                        UserModel userModel = snapshot.getValue(UserModel.class);
                        Log.v("모델",userModel.youruid);
                        Log.v("모델2",myUid);
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
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
            if(userModels.get(position).comment !=null)
            ((CustomViewHolder)holder).textView_comment.setText(userModels.get(position).comment);
        }


        @Override
        public int getItemCount() {
            return userModels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {

            public ImageView imageView;
            public TextView textView;
            public TextView textView_comment;

            public CustomViewHolder(View view) {
                super(view);
                imageView = (ImageView) view.findViewById(R.id.frienditem_imageview);
                textView = (TextView) view.findViewById(R.id.frienditem_textview);
                textView_comment = view.findViewById(R.id.frienditem_textview_comment);
            }
        }
    }


}

/*

}

package com.example.howlstagram.fragment;


//import android.app.Fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.howlstagram.R;
import com.example.howlstagram.model.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class PeopleFragment extends Fragment
{

    @Nullable
    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.v("화면출력","screnn");
        View view = inflater.inflate(R.layout.fragment_people,container,false);
        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.peoplefragment_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new PeopleFragmentRecyclerViewAdapter());
        Log.v("화면출력","screnn");
        //super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    class PeopleFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        List<UserModel> userModels;

        public PeopleFragmentRecyclerViewAdapter(){
            Log.v("class","class input");
            userModels = new ArrayList<>();
            FirebaseDatabase.getInstance().getReference().child("users1").addValueEventListener(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            userModels.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                userModels.add(snapshot.getValue(UserModel.class));
                            }
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    }

            );
            Log.v("class","class output");
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            Log.v("item","friendin");
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_friend, viewGroup,false);
            Log.v("item","friend");

            return new CustomViewHolder(view);
        }


//        @NonNull
//        @Override
//        public RecyclerView.ViewHolder onCreateViewHolder(@Nullable ViewGroup parent, int viewType) {
//            assert parent != null;
//            Log.v("item","friendin");
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent,false);
//                Log.v("item","friend");
//            return new CustomViewHolder(view);
//        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//            Glide.with(holder.itemView.getContext())
//                    .load(userModels.get(position).imageURl)
//                    .apply(new RequestOptions().circleCrop())
//                    .into(((CustomViewHolder)holder).imageView);
//            ((CustomViewHolder)holder).textView.setText(userModels.get(position).userName);
        }

        @Override
        public int getItemCount() {
            return userModels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public TextView textView;
            public ImageView imageView;

            public CustomViewHolder(View view) {
                super(view);
                Log.v("2개입력in","input");
                imageView=(ImageView) view.findViewById(R.id.frienditem_imageview);
                textView = (TextView)view.findViewById(R.id.frienditem_textview);
                Log.v("2개입력out","input");
            }
        }

//        private class CustomViewHolder extends RecyclerView.ViewHolder {
//
//           public TextView textView;
//           public ImageView imageView;
//
//            public CustomViewHolder(View view) {
//                super(view);
//                imageView=(ImageView) view.findViewById(R.id.frienditem_imageview);
//                 textView = (TextView)view.findViewById(R.id.frienditem_textview);
//            }
//        }
    }
}
*/
