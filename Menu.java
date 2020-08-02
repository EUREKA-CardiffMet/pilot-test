package com.emotionrobotics.apps.sanbotmotion;


import android.app.Fragment;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.RadioGroup;

import java.util.HashMap;
import java.util.Map;

public class Menu extends AppCompatActivity {
    private Fragment currentFragment;
    private RadioGroup radioGroup1;
    private Map<Integer, Fragment> rbFragMapping;
    final DrinkFragment drinkFragment = DrinkFragment.newInstance(null,null);
    final FoodFragment foodFragment = FoodFragment.newInstance(null,null);
    final TeaFragment teaFragment = TeaFragment.newInstance(null,null);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        radioGroup1 = (RadioGroup) findViewById(R.id.radiogroup1);
        initFragment();
        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                showTab(checkedId);
            }
        });
    }

    private void initFragment() {


        rbFragMapping = new HashMap<>();
        rbFragMapping.put(R.id.drinkOrderBTN,drinkFragment);
        rbFragMapping.put(R.id.foodOrderBTN,foodFragment);
        rbFragMapping.put(R.id.teaOrderBTN,teaFragment);

        showTab(R.id.foodOrderBTN);


    }
    private void showTab(int radioId) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment targettoshow = rbFragMapping.get(radioId);
        if(targettoshow.isAdded()){
            transaction.show(targettoshow);
        }else{
            transaction.add(R.id.fragment_menu,targettoshow);
        }
        if(currentFragment!=null){
            transaction.hide(currentFragment);
        }
        currentFragment = targettoshow;
        transaction.commit();
    }
}
