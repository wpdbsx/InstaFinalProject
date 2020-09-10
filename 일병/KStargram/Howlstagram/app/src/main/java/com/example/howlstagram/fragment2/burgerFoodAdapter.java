package com.example.howlstagram.fragment2;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.howlstagram.R;
import com.example.howlstagram.WordItemData;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class burgerFoodAdapter extends RecyclerView.Adapter<burgerFoodAdapter.Holder> {

    FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
    Bitmap bitmap;
    String[] s =new String[5];
    String[] s1 =new String[5];
    Uri uri=null;
    String test="";
    String food[]={"burger_king","burger_lotte","burger_mcd","burger_subway","burger_yam"};
    View view;
    Holder holder;



    int  i=0;
    private Context context;
    private List<WordItemData> list = new ArrayList<>();


    public burgerFoodAdapter(Context context, List<WordItemData> list) {
        this.context = context;
        this.list = list;

    }



    // ViewHolder 생성
    // row layout을 화면에 뿌려주고 holder에 연결
    @Override
    public  Holder onCreateViewHolder(ViewGroup parent, int viewType) {

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_voca_row, parent, false);

        holder = new Holder(view);

        return holder;
    }

    /*
     * Todo 만들어진 ViewHolder에 data 삽입 ListView의 getView와 동일
     *
     * */
    @Override
    public void onBindViewHolder(final Holder holder, final int position) {
        this.holder= holder;

        // 각 위치에 문자열 세팅
        int itemposition = position;

        firebaseFirestore.document("burger_f/"+food[i]).get().addOnSuccessListener(

                new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        s[i] = documentSnapshot.getData().get("image").toString();
                        s1[i] = documentSnapshot.getData().get("name").toString();
                        holder.meaningText.setText(s1[i]);
                        Log.v("test[]",s1[i]);
                        try {

                            Glide.with(context)
                                    .load(s[i])
                                    .into(holder.wordText);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );




        if(i<4){i++;}
        Log.e("StudyApp", "onBindViewHolder" + itemposition);
    }

    // 몇개의 데이터를 리스트로 뿌려줘야하는지 반드시 정의해줘야한다
    @Override
    public int getItemCount() {
        return list.size(); // RecyclerView의 size return
    }

    // ViewHolder는 하나의 View를 보존하는 역할을 한다
    public class Holder extends RecyclerView.ViewHolder{
        public ImageView wordText;
        public TextView meaningText;

        public Holder(View view){
            super(view);
            wordText = (ImageView) view.findViewById(R.id.wordText);
            meaningText = (TextView) view.findViewById(R.id.meaningText);

        }
    }
}