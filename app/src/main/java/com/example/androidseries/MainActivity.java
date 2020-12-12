package com.example.androidseries;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.features2d.*;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import static org.opencv.core.CvType.CV_32F;


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    CameraBridgeViewBase    cameraBridgeViewBase ;
    BaseLoaderCallback      baseLoaderCallback;  // allows as to get the frames from the camera
    int counter = 0;




    Interpreter interpreter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);
                switch (status){
                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };


        try {
            interpreter = new Interpreter(loadModelFile(), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
//            interpreter = new Interpreter(new File("model.tflite"));

    }

    // this is the important one...
    // getting frames from camera before showing it.
    // here need to implement the logic's. processing media
    // Mat is the metrics of the frame. 20/30 fps
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {


        Mat frame = inputFrame.rgba();
        Imgproc.cvtColor(frame,frame, Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(frame,frame, Imgproc.COLOR_RGB2YUV);

        Mat imageBlob = Dnn.blobFromImage(frame, 0.00392, new Size(66,200), new Scalar(0, 0, 0), false, false);


        float result =0 ;
        interpreter.run(imageBlob, result);
        System.out.println("result prediction "+ result);

        return frame;
    }



    private MappedByteBuffer loadModelFile() throws IOException{
        AssetFileDescriptor assetFileDescriptor = this.getAssets().openFd("model.tflite");
        FileInputStream fileInputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startoffset = assetFileDescriptor.getStartOffset();
        long length = assetFileDescriptor.getLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startoffset, length);
    }












    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(), "there's a problem!", Toast.LENGTH_LONG).show();
        }else{
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cameraBridgeViewBase!=null){
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraBridgeViewBase!=null){
            cameraBridgeViewBase.disableView();
        }
    }
}