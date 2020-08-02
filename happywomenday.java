package com.emotionrobotics.apps.sanbotmotion;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.qihancloud.opensdk.base.BindBaseActivity;
import com.qihancloud.opensdk.beans.FuncConstant;
import com.qihancloud.opensdk.function.beans.LED;
import com.qihancloud.opensdk.function.beans.handmotion.AbsoluteAngleHandMotion;
import com.qihancloud.opensdk.function.beans.headmotion.AbsoluteAngleHeadMotion;
import com.qihancloud.opensdk.function.unit.HandMotionManager;
import com.qihancloud.opensdk.function.unit.HardWareManager;
import com.qihancloud.opensdk.function.unit.HeadMotionManager;
import com.qihancloud.opensdk.function.unit.SpeechManager;


public class happywomenday extends BindBaseActivity {
    private HandMotionManager handMotionManager;
    private boolean WhiteLight = false;
    @Override
    protected void onMainServiceConnected() {
        faceForward();
        handsDown();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_happywomenday);
        final Button BTNspeak = (Button)findViewById(R.id.buttonwomen);
        BTNspeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }
        });
    }
    private void faceForward() {
        HeadMotionManager Head = (HeadMotionManager) getUnitManager(FuncConstant.HEADMOTION_MANAGER);
        Head.doAbsoluteAngleMotion(new AbsoluteAngleHeadMotion(AbsoluteAngleHeadMotion.ACTION_VERTICAL, 25));
        Head.doAbsoluteAngleMotion(new AbsoluteAngleHeadMotion(AbsoluteAngleHeadMotion.ACTION_HORIZONTAL, 90));
        setLEDValue(LED.PART_LEFT_HEAD, LED.MODE_WHITE);
        setLEDValue(LED.PART_RIGHT_HEAD, LED.MODE_WHITE);
    }

    //Move hands to straight down
    private void handsDown() {
        handMotionManager.doAbsoluteAngleMotion(new AbsoluteAngleHandMotion(AbsoluteAngleHandMotion.PART_LEFT, 6, 170));
        handMotionManager.doAbsoluteAngleMotion(new AbsoluteAngleHandMotion(AbsoluteAngleHandMotion.PART_RIGHT, 5, 170));
    }
    public void setLEDValue(byte part, byte mode) {
        final HardWareManager HwM = (HardWareManager) getUnitManager(FuncConstant.HARDWARE_MANAGER);
        LED led = new LED(part, mode, (byte) 255, (byte) 0);
        HwM.setLED(led);
    }
    public void speak() {
        final SpeechManager speechManager = (SpeechManager) getUnitManager(FuncConstant.SPEECH_MANAGER);
        Log.i("info", "speak: =================== ");
        speechManager.startSpeak("I want to sing a song today; I want to ring the bell today, I want to dedicate it all to your today,The entire warm light of the sun is yours today,Happy woman’s day to all woman’s!");
    }

}
