package com.example.androidseries;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

//import com.chaquo.python.PyObject;
//import com.chaquo.python.Python;
//import com.chaquo.python.android.AndroidPlatform;

import org.jetbrains.annotations.NotNull;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.features2d.*;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;

import static org.opencv.core.CvType.CV_32F;



public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    CameraBridgeViewBase    cameraBridgeViewBase ;
    BaseLoaderCallback      baseLoaderCallback;  // allows as to get the frames from the camera

    Button btn;
    ImageView iv;
    BitmapDrawable drawable;
    Bitmap bitmap;
    String imageString = "";
    Interpreter interperter;


    /**
     * PyObject pyo = py.getModule("model_input");
     *             PyObject obj = pyo.callAttr("main", imageString);
     *
     *             float[][] outputs = new float[1][1];
     *             interperter.run(s, outputs);
     *             System.out.println(obj.toString());
     * @param savedInstanceState
     */
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
            interperter = new Interpreter(loadModelFile(), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // this is the important one...
    // getting frames from camera before showing it.
    // here need to implement the logic's. processing media
    // Mat is the metrics of the frame. 20/30 fps
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        Mat frame = inputFrame.rgba();

        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGB2YUV);
        Imgproc.GaussianBlur(frame, frame, new Size(3, 3), 0, 0);

        Mat f = new Mat();
        Imgproc.resize(frame,f,new Size(200, 66));
     //   f = Dnn.blobFromImage(f, 0.00392, new Size(200, 66) , new Scalar(0,0 ,0), false,false);
        f.convertTo(f,CV_32F);
        StringBuilder sb = new StringBuilder();
        String s = new String();
        System.out.println("hei "+ f.height()+", wit" + f.width() + "ch " + f.channels());
        System.out.println("col "+ f.cols()+", row" + f.rows() + "ch " + f.channels());

        float[][][][] inputs = new float[1][200][66][3];
        float fs[] = new float[3];
        for( int r=0 ; r<f.rows() ; r++ ) {
            //sb.append(""+r+") ");
            for( int c=0 ; c<f.cols() ; c++ ) {
                f.get(r, c, fs);
                //sb.append( "{");
                inputs[0][c][r][0]=fs[0]/255;
                inputs[0][c][r][1]=fs[1]/255;
                inputs[0][c][r][2]=fs[2]/255;
                //sb.append( String.valueOf(fs[0]));
                //sb.append( ' ' );
                //sb.append( String.valueOf(fs[1]));
                //sb.append( ' ' );
                //sb.append( String.valueOf(fs[2]));
                //sb.append( "}");
                //sb.append( ' ' );
            }
            //sb.append( '\n' );
        }
        //System.out.println(sb);




        float[][] outputs = new float[1][1];
        //float[][][][] inputs = new float[1][200][66][3]; //desired form to model
//
////
        interperter.run(inputs ,outputs);
        System.out.println("output: " + outputs[0][0]);

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