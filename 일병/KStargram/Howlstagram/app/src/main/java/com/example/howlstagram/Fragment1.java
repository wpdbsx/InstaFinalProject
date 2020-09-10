package com.example.howlstagram;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.howlstagram.fragment2.*;

import java.util.Random;

public class Fragment1 extends Fragment {
    ImageView imageView;
    ImageView imageView1;
    ImageView imageView2;
    ImageView imageView3;
    ImageView imageView4;
    ImageView imageView5;
    ImageView imageView6;
    ImageView imageView7;
    Button imageView8;
    Button imageView9;
    TextView textView;
    int a;
    // MainActivity activity;
    String text[] = {"이겼닭, 오늘 식사는 치킨이닭",
            "식후 커피하잔 정도는 괜찮잖아?",
            "시간이 없는데 햄버거 먹자",
            "이 기분은 피자다",
            "부운시이익",
            "차이나에서 고백하면 차이나",
            "일식 이식 삼식",
            "마 밥 아니것나 한식 가즈아",
            "@ 굶어라 @"
    };

    ViewGroup rootView;
    // MainActivity activity;
    @Override
    public void onAttach(Context context) {        // 액티비티에 프래그먼트가 올라감.
        super.onAttach(context);
        // activity = (MainActivity) getActivity(); // 메인 액티비티 참조
    }

    @Override
    public void onDetach() {                    // 액티비티에서 프래그먼트가 내려감.
        super.onDetach();
        //   activity = null;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_1, container,false);

//        Button button = (Button) rootView.findViewById(R.id.button);
        textView = (TextView)rootView.findViewById(R.id.weather);

        imageView=(ImageView)rootView.findViewById(R.id.button11);
        imageView1=(ImageView)rootView.findViewById(R.id.button12) ;
        imageView2=(ImageView)rootView.findViewById(R.id.button13) ;
        imageView3=(ImageView)rootView.findViewById(R.id.button14) ;
        imageView4=(ImageView)rootView.findViewById(R.id.button15) ;
        imageView5=(ImageView)rootView.findViewById(R.id.button16) ;
        imageView6=(ImageView)rootView.findViewById(R.id.button17) ;
        imageView7=(ImageView)rootView.findViewById(R.id.button18) ;
        imageView8=(Button) rootView.findViewById(R.id.button19) ;
        imageView9=(Button) rootView.findViewById(R.id.button20) ;

        imageView.setOnClickListener(     myListener      );
        imageView1.setOnClickListener(     myListener      );
        imageView2.setOnClickListener(     myListener      );
        imageView3.setOnClickListener(     myListener      );
        imageView4.setOnClickListener(     myListener      );
        imageView5.setOnClickListener(     myListener      );
        imageView6.setOnClickListener(     myListener      );
        imageView7.setOnClickListener(     myListener      );
        imageView8.setOnClickListener(     myListener      );
        imageView9.setOnClickListener(     myListener      );

        return rootView;
    }


    View.OnClickListener myListener = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            imageView.setSelected(false);


            switch (v.getId())
            {
                case R.id.button11 :
                    imageView.setSelected(true);
                    movePage1List1();
                    break;
                case R.id.button12 :
                    imageView.setSelected(true);
                    movePage1List2();
                    break;
                case R.id.button13 :
                    imageView.setSelected(true);
                    movePage1List3();
                    break;

                case R.id.button14 :
                    imageView.setSelected(true);
                    movePage1List4();
                    break;
                case R.id.button15 :
                    imageView.setSelected(true);
                    movePage1List5();
                    break;
                case R.id.button16 :
                    imageView.setSelected(true);
                    movePage1List6();
                    break;
                case R.id.button17 :
                    imageView.setSelected(true);
                    movePage1List7();
                    break;
                case R.id.button18 :
                    imageView.setSelected(true);
                    movePage1List8();
                    break;
                case R.id.button19 :
                    Random random = new Random();
                    a =random.nextInt(9);
                    textView.setText(text[a]);
                    break;
                case R.id.button20 :
                    Uri uri = Uri.parse("https://cms2.ks.ac.kr/nuri/sub.do?mCode=MN0045");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri); startActivity(intent);
                    break;
            }

        }
    };

    public void movePage1List1()
    {

        koreafood page1_list1 = new koreafood();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().replace(R.id.main_content, page1_list1);
        //메인화면의 프래그먼트 , 보여줄프래그먼트
        fragmentTransaction.commit();
    }

    public void movePage1List2()
    {

        Chinafood page1_list1 = new Chinafood();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().replace(R.id.main_content, page1_list1);
        //메인화면의 프래그먼트 , 보여줄프래그먼트
        fragmentTransaction.commit();
    }

    public void movePage1List3()
    {

        Japanfood page1_list1 = new Japanfood();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().replace(R.id.main_content, page1_list1);
        //메인화면의 프래그먼트 , 보여줄프래그먼트
        fragmentTransaction.commit();
    }
    public void movePage1List4()
    {

        burgerfood page1_list1 = new burgerfood();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().replace(R.id.main_content, page1_list1);
        //메인화면의 프래그먼트 , 보여줄프래그먼트
        fragmentTransaction.commit();
    }
    public void movePage1List5()
    {

        chickenfood page1_list1 = new chickenfood();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().replace(R.id.main_content, page1_list1);
        //메인화면의 프래그먼트 , 보여줄프래그먼트
        fragmentTransaction.commit();
    }
    public void movePage1List6()
    {

        pizzafood page1_list1 = new pizzafood();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().replace(R.id.main_content, page1_list1);
        //메인화면의 프래그먼트 , 보여줄프래그먼트
        fragmentTransaction.commit();
    }
    public void movePage1List7()
    {

        cafe page1_list1 = new cafe();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().replace(R.id.main_content, page1_list1);
        //메인화면의 프래그먼트 , 보여줄프래그먼트
        fragmentTransaction.commit();
    }

    public void movePage1List8()
    {

        bunsikfood page1_list1 = new bunsikfood();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().replace(R.id.main_content, page1_list1);
        //메인화면의 프래그먼트 , 보여줄프래그먼트
        fragmentTransaction.commit();
    }



//    public void movePage1List3()
//    {
//        //ChinezFoodFragment page1_list1 = new ChinezFoodFragment();
//        ChinezFoodFragment page1_list1 = new ChinezFoodFragment();
//        FragmentManager fragmentManager = getFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().replace(R.id.main_content, page1_list1);
//        //메인화면의 프래그먼트 , 보여줄프래그먼트
//        fragmentTransaction.commit();
//    }





}