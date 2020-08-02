package com.emotionrobotics.apps.sanbotmotion;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import com.qihancloud.opensdk.base.BindBaseActivity;
import com.qihancloud.opensdk.function.beans.FaceRecognizeBean;
import com.qihancloud.opensdk.function.unit.MediaManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.VideoView;

import com.qihancloud.opensdk.beans.FuncConstant;
import com.qihancloud.opensdk.function.beans.StreamOption;
import com.qihancloud.opensdk.function.unit.MediaManager;
import com.qihancloud.opensdk.function.unit.interfaces.media.FaceRecognizeListener;
import com.qihancloud.opensdk.function.unit.interfaces.media.MediaListener;
import com.qihancloud.opensdk.function.unit.interfaces.media.MediaStreamListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class Vedio_Activity extends BindBaseActivity implements SurfaceHolder.Callback {
    private SurfaceView surfaceView;
    private ImageView faceIV;
    MediaCodec videoDecoder;
    ByteBuffer[] videoInputBuffers;
    final static String videoMimeType = "video/avc";
    final String TAG = getClass().getName();
    AudioTrack audioTrack;
    Surface surface;
    MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
    long decodeTimeout = 16000;
    int i=0;
    MediaManager mediaManager;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_vedio_);
        surfaceView = findViewById(R.id.sfv_video);
        faceIV = findViewById(R.id.faceIv);
        mediaManager = (MediaManager) getUnitManager(FuncConstant.MEDIA_MANAGER);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL,AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_SHOW_UI);
        int sampleRate = 8000;
        int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minBufferSize+100, AudioTrack.MODE_STREAM);
        audioTrack.play();
        mediaManager.setMediaListener(new MediaStreamListener() {

            @Override
            public void getVideoStream(byte[] bytes) {
                Log.i("info", "获取到了视频流");
                drawVideoSample(ByteBuffer.wrap(bytes));
            }

            @Override
            public void getAudioStream(byte[] data) {
                Log.i("info", "获取到了音频流" + i++);
                audioTrack.write(data,0,data.length);
            }

        });

        mediaManager.setMediaListener(new FaceRecognizeListener() {
            @Override
            public void recognizeResult(List<FaceRecognizeBean> faceRecognizeBean) {
//                    if(bitmap != null){
                        Log.i(TAG, "recognizeResult获取到人脸============: ");

                         Bitmap bitmap = mediaManager.getVideoImage();
//                        Bitmap  bmp= BitmapFactory.decodeResource(Vedio_Activity.this.getResources(),R.drawable.group2x);
//                        faceIV.setImageBitmap( mediaManager.getVideoImage());


//                        File file = getDiskCacheDir(Vedio_Activity.this,"picture");

//                }else{
//                        Log.i(TAG, "recognizeResulterrorr======================: ");
//                    }
            }
        });
        surfaceView.getHolder().addCallback(this);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surface = holder.getSurface();
        startDecoding(640, 360);
        StreamOption streamOption = new StreamOption();
        streamOption.setChannel(StreamOption.SUB_STREAM);
        String result = mediaManager.openStream(streamOption).getResult();
        Log.e("result", result);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mediaManager.closeStream();
        stopDecoding();
        audioTrack.stop();
        audioTrack.release();
        Log.e("result", "关闭surface==========");
    }
    private boolean startDecoding(int width, int height) {
        try {
            if (videoInputBuffers != null) {
                Log.i(TAG, "startDecoding: videoInputBuffers already created!=======");
                return false;

            } else if (videoDecoder != null) {
                Log.i(TAG, "startDecoding: videoDecoder already created!=========");
                return false;

            }
            // format
            MediaFormat format = MediaFormat.createVideoFormat(
                    videoMimeType, width, height);
            Log.i(TAG, "" + format);

            videoDecoder = MediaCodec.createDecoderByType(videoMimeType);
            videoDecoder.configure(format, surface, null, 0);
            videoDecoder.start();

            videoInputBuffers = videoDecoder.getInputBuffers();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            Log.e("CODEC", "onCreateCodec===========");
        }
        return true;
    }

    public void stopDecoding() {
        if (videoDecoder != null) {
            videoDecoder.stop();
            videoDecoder.release();
            videoDecoder = null;
            Log.i(TAG, "stopDecoding============");
        }
        videoInputBuffers = null;
    }

    public void drawVideoSample(ByteBuffer sampleData) {

        try {
            // put sample data
            int inIndex = videoDecoder.dequeueInputBuffer(decodeTimeout);
            if (inIndex >= 0) {
                ByteBuffer buffer = videoInputBuffers[inIndex];
                int sampleSize = sampleData.limit();
                buffer.clear();
                buffer.put(sampleData);
                buffer.flip();
                // Log.i("DecodeActivity", "" + buffer.toString());
                videoDecoder.queueInputBuffer(inIndex, 0, sampleSize, 0, 0);
            }
            // output, 1 microseconds = 100,0000 / 1 second
            int ret = videoDecoder.dequeueOutputBuffer(videoBufferInfo, decodeTimeout);
            if (ret < 0) {
                onDecodingError(ret);
                return;
            }
            videoDecoder.releaseOutputBuffer(ret, true);
        } catch (Exception e) {
            Log.e(TAG, "发生错误============", e);
        }

    }

    private void onDecodingError(int index) {
        switch (index) {
            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                Log.e(TAG, "onDecodingError: The output buffers have changed============");
                // The output buffers have changed, the client must refer to the
                // new
                // set of output buffers returned by getOutputBuffers() from
                // this
                // point on.
                // outputBuffers = decoder.getOutputBuffers();
                break;

            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                Log.d(TAG, "New format: ==============" + videoDecoder.getOutputFormat());
                // The output format has changed, subsequent data will follow
                // the
                // new format. getOutputFormat() returns the new format.
                break;

            case MediaCodec.INFO_TRY_AGAIN_LATER:
                Log.d(TAG, "dequeueOutputBuffer timed out!===========");
                // If a non-negative timeout had been specified in the call to
                // dequeueOutputBuffer(MediaCodec.BufferInfo, long), indicates
                // that
                // the call timed out.
                break;

            default:
                break;
        }
    }
    private static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
            Log.i("info", "getDiskCacheDir=====: "+cachePath);
        } else {
            cachePath = context.getCacheDir().getPath();
            Log.i("info", "getDiskCacheDir=====: "+cachePath);

        }
        return new File(cachePath + File.separator + uniqueName);
    }
    protected void onMainServiceConnected() {

    }

}
