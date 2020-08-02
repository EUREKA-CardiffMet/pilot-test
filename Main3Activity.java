package com.emotionrobotics.apps.sanbotmotion;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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

import java.time.Instant;


public class Main3Activity extends BindBaseActivity {
    //define Manager objects - this covers hands and leds - for head movement see faceForward()
    private HandMotionManager handMotionManager;
    private boolean WhiteLight = false;

    @Override
    protected void onMainServiceConnected() {
        //On connect to Sanbot controller service face forward and put hands straight down
        faceForward();
        handsDown();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        //hide action bar and disable screen saver
        //MediaManager mediaManager = (MediaManager) getUnitManager(FuncConstant.MEDIA_MANAGER);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //set Hand Motion proxy
        handMotionManager = (HandMotionManager) getUnitManager(FuncConstant.HANDMOTION_MANAGER);
        //get all buttons
        final Button BTNRed = (Button) findViewById(R.id.BTNRed);
        final Button BTNGreen = (Button) findViewById(R.id.BTNGrn);
        final Button BTNBlue = (Button) findViewById(R.id.BTNBlue);
        final Button BTNTorch = (Button) findViewById(R.id.BTNTorch);
        final Button BTNArmsUp = (Button) findViewById(R.id.BTNArmsUp);
        final Button BTNArmsDown = (Button) findViewById(R.id.BTNArmsDown);
        final Button Speech = (Button) findViewById(R.id.speech);
        final Button FaceRec = (Button) findViewById(R.id.faceRecognizedBtn);

        //set onClick listeners
        BTNRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLEDValue(LED.PART_ALL, LED.MODE_RED);
            }
        });

        BTNGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLEDValue(LED.PART_ALL, LED.MODE_GREEN);
            }
        });

        BTNBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLEDValue(LED.PART_ALL, LED.MODE_BLUE);
            }
        });

        BTNTorch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main3Activity.this,Menu.class);
                startActivity(intent);
            }
        });

        BTNArmsUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handsUp();
            }
        });
        BTNArmsDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handsDown();
            }
        });
        Speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }
        });

        FaceRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent intent = new Intent(Main3Activity.this,Camera_Activity.class);
                    startActivity(intent);
            }
        });
    }


    //Position head to forward - set Head LEDS to white
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

    //Move hands to straight down
    private void handsUp() {
        handMotionManager.doAbsoluteAngleMotion(new AbsoluteAngleHandMotion(AbsoluteAngleHandMotion.PART_LEFT, 6, 5));
        handMotionManager.doAbsoluteAngleMotion(new AbsoluteAngleHandMotion(AbsoluteAngleHandMotion.PART_RIGHT, 5, 5));
    }

    //Set LED colours and modes
    public void setLEDValue(byte part, byte mode) {
        final HardWareManager HwM = (HardWareManager) getUnitManager(FuncConstant.HARDWARE_MANAGER);
        LED led = new LED(part, mode, (byte) 255, (byte) 0);
        HwM.setLED(led);
    }

    //Set head torch on/off
    public void setTorch() {
        final HardWareManager HwM = (HardWareManager) getUnitManager(FuncConstant.HARDWARE_MANAGER);
        if (WhiteLight) {
            HwM.switchWhiteLight(false);
            WhiteLight = false;
        } else {
            HwM.switchWhiteLight(true);
            WhiteLight = true;
        }
    }

    public void speak() {
        final SpeechManager speechManager = (SpeechManager) getUnitManager(FuncConstant.SPEECH_MANAGER);
        Log.i("info", "speak: =================== ");
        speechManager.startSpeak("I want to sing a song today; I want to ring the bell today, I want to dedicate it all to your today, The entire warm light of the sun is yours today, Happy woman’s day to all woman’s!");
    }

}



