package com.example.howlstagram.fragment2;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.*;
import com.example.howlstagram.R;
import com.example.howlstagram.WordItemData;

import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;

public class burgerfood extends Fragment {
    private RecyclerView recyclerView;
    private burgerFoodAdapter adapter;
    private ArrayList<WordItemData> list = new ArrayList<>();


    GestureDetector gestureDetector = new GestureDetector(getApplicationContext(),new GestureDetector.SimpleOnGestureListener() {

        //누르고 뗄 때 한번만 인식하도록 하기위해서
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return true;
        }
    });




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.burger_recycler_view, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.burger_recyclerview);

        list = WordItemData.createContactsList(5);
        recyclerView.setHasFixedSize(true);
        adapter = new burgerFoodAdapter(getActivity(), list);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(
                new RecyclerView.OnItemTouchListener() {
                    @Override
                    public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

                        View childView = recyclerView.findChildViewUnder(motionEvent.getX(),motionEvent.getY());

                        //터치한 곳의 View가 RecyclerView 안의 아이템이고 그 아이템의 View가 null이 아니라
                        //정확한 Item의 View를 가져왔고, gestureDetector에서 한번만 누르면 true를 넘기게 구현했으니
                        //한번만 눌려서 그 값이 true가 넘어왔다면
                        if(childView != null && gestureDetector.onTouchEvent(motionEvent)){

                            //현재 터치된 곳의 position을 가져오고
                            int currentPosition = recyclerView.getChildAdapterPosition(childView);

                            //해당 위치의 Data를 가져옴

                            if (currentPosition==0){
                                Uri uri = Uri.parse("https://store.naver.com/restaurants/detail?id=1074762079");
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri); startActivity(intent);
                            }
                            if (currentPosition==1){
                                Uri uri = Uri.parse("https://store.naver.com/restaurants/detail?id=16556704");
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri); startActivity(intent);
                            }
                            if (currentPosition==2){
                                Uri uri = Uri.parse("https://store.naver.com/restaurants/detail?entry=plt&id=20311872&query=%EB%A7%A5%EB%8F%84%EB%82%A0%EB%93%9C%20%EA%B2%BD%EC%84%B1%EB%8C%80%EC%A0%90");
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri); startActivity(intent);
                            }
                            if (currentPosition==3){
                                Uri uri = Uri.parse("https://store.naver.com/restaurants/detail?id=38325282&tab=fsasReview#_tab");
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri); startActivity(intent);
                            }
                            if (currentPosition==4){
                                Uri uri = Uri.parse("https://store.naver.com/restaurants/detail?id=1571591259");
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri); startActivity(intent);
                            }
                            return true;
                        }

                        return false;
                    }

                    @Override
                    public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

                    }

                    @Override
                    public void onRequestDisallowInterceptTouchEvent(boolean b) {

                    }
                }

        );






        Log.e("Frag", "MainFragment");
        return rootView;
    }




}