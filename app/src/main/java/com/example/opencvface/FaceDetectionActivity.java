package com.example.opencvface;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;


public class FaceDetectionActivity extends CameraActivity implements CvCameraViewListener2 {
    private static final String TAG = "CSHsu::FaceDetectionActivity";
    private static final String MSG_FACE_DETECTED = "FACE_DETECTED";
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    private static final int RESIZED_IMAGE_WIDTH = 160;
    private static final int RESIZED_IMAGE_HEIGHT = 160;
    private String faceLabel;
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mRgba;
    private Mat mGray;
    private Mat mRgbaTranspose;
    private Mat mGrayTranspose;
    private CascadeClassifier mCascadeDetector;
    private int mAbsoluteFaceSize = 0;
    private Mat matFaceDetectedResized;
    private Handler mHandler;
    private ImageView previewImageView;
    private int mCameraId = CameraBridgeViewBase.CAMERA_ID_BACK;
    private ToggleButton toggleBtnSwitchCameraLen;
    private Button btnFaceRecognitionRegister;
    private Button btnFaceRecognitionVerify;

    public FaceDetectionActivity() {
        super();
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /*
     * Implementation of the onManagerConnected(int status) method for the BaseLoaderCallback class.
     */
    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully");
                try {
                    InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
                    File mCascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                    File mCascadeFile = new File(mCascadeDir, "cascade.xml");
                    FileOutputStream fos = new FileOutputStream(mCascadeFile);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                    is.close();
                    fos.close();
                    mCascadeDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                    if (mCascadeDetector.empty()) {
                        Log.e(TAG, "Failed to load cascade classifier");
                        mCascadeDetector = null;
                    } else {
                        Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
                    }
                    boolean deleted = mCascadeDir.delete();
                    if (!deleted) {
                        Log.e(TAG, "Fail to delete the cascade classifier from " + mCascadeDir.getAbsolutePath());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                }
                mOpenCvCameraView.enableView();
            } else {
                super.onManagerConnected(status);
            }
        }
    };


    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_face_detection);

        // ----- get the faceLabel -----//
        Intent intent = getIntent();
        FaceDetectionActivity.this.faceLabel = intent.getStringExtra("faceLabel");
        if (FaceDetectionActivity.this.faceLabel == null) {
            Toast.makeText(getApplicationContext(), "Fail to get the faceLabel", Toast.LENGTH_SHORT).show();
        }
        // ---------------------------- //

        previewImageView = findViewById(R.id.previewImageView);
        toggleBtnSwitchCameraLen = findViewById(R.id.toggleBtnSwitchCameraLen);
        btnFaceRecognitionRegister = findViewById(R.id.btnFaceRecognitionRegister);
        btnFaceRecognitionVerify = findViewById(R.id.btnFaceRecognitionVerify);

        mOpenCvCameraView = findViewById(R.id.javaCameraView);
        mOpenCvCameraView.setCameraIndex(mCameraId);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        // Handling the detected face image
        // 將偵測到的人臉區域顯示在畫面左下角
        previewImageView.setVisibility(ImageView.VISIBLE);
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.obj == MSG_FACE_DETECTED) {
                    // make the face image
                    Bitmap bitmap = Bitmap.createBitmap(matFaceDetectedResized.width(), matFaceDetectedResized.height(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(matFaceDetectedResized, bitmap);
                    // show the face image on the ImageView
                    previewImageView.setImageBitmap(bitmap);
                }
            }
        };

        // Switching between front and back camera lens
        toggleBtnSwitchCameraLen.setOnClickListener(v -> {
            if (toggleBtnSwitchCameraLen.isChecked()) {  // 當按鈕第一次被點擊時候響應的事件
                mCameraId = CameraBridgeViewBase.CAMERA_ID_FRONT;
            } else {  // 當按鈕再次被點擊時候響應的事件
                mCameraId = CameraBridgeViewBase.CAMERA_ID_BACK;
            }
            mOpenCvCameraView.disableView();
            mOpenCvCameraView.setCameraIndex(mCameraId);
            mOpenCvCameraView.enableView();
        });

        // face descriptor register (upload image)
        btnFaceRecognitionRegister.setOnClickListener(v -> {
            Log.d(TAG, "btnFaceRecognitionRegister onClicked");
            if (matFaceDetectedResized == null) {
                Toast.makeText(FaceDetectionActivity.this.getApplicationContext(), "尚未偵測到臉部", Toast.LENGTH_SHORT).show();
                return;
            }
            mOpenCvCameraView.disableView();
            btnFaceRecognitionRegister.setEnabled(false);
            btnFaceRecognitionVerify.setEnabled(false);
            toggleBtnSwitchCameraLen.setEnabled(false);

            // connection info. 人臉影像登錄 (register-by-tensor)
            String action = ConnectionParams.FR_ACTION_REGISTER;
            // on post execute
            FaceTask frTask = new FaceTask(FaceDetectionActivity.this.faceLabel, ConnectionParams.FR_IP, ConnectionParams.FR_PORT, action, response -> {
                Log.d(TAG, "onUploadComplete: " + response);
                Intent myIntent = new Intent(FaceDetectionActivity.this, FaceTaskResultActivity.class);
                myIntent.putExtra("response", response);
                startActivity(myIntent);
            });
            // do in background
            frTask.execute(matFaceDetectedResized);
        });

        // face verification (upload image)
        btnFaceRecognitionVerify.setOnClickListener(v -> {
            Log.d(TAG, "btnFaceRecognitionVerify onClicked");
            if (matFaceDetectedResized == null) {
                Toast.makeText(FaceDetectionActivity.this.getApplicationContext(), "尚未偵測到臉部", Toast.LENGTH_SHORT).show();
                return;
            }
            mOpenCvCameraView.disableView();
            btnFaceRecognitionRegister.setEnabled(false);
            btnFaceRecognitionVerify.setEnabled(false);
            toggleBtnSwitchCameraLen.setEnabled(false);

            // connection info. 人臉驗證(影像上傳)
            String action = ConnectionParams.FR_ACTION_VERIFY;
            // on post execute
            FaceTask frTask = new FaceTask(FaceDetectionActivity.this.faceLabel, ConnectionParams.FR_IP, ConnectionParams.FR_PORT, action, response -> {
                Log.d(TAG, "onUploadComplete: " + response);
                Intent myIntent = new Intent(FaceDetectionActivity.this, FaceTaskResultActivity.class);
                myIntent.putExtra("response", response);
                startActivity(myIntent);
            });
            // do in background
            frTask.execute(matFaceDetectedResized);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        mOpenCvCameraView.enableView();
        btnFaceRecognitionRegister.setEnabled(true);
        btnFaceRecognitionVerify.setEnabled(true);
        toggleBtnSwitchCameraLen.setEnabled(true);
    }

    @Override
    /*
     * Very Important!!!!
     * OpenCV 4.x.x需要有這一個方法的實作，否則會無法使用相機擷取影像
     * OpenCV 3.x.x不需要
     */
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        // for landscape orientation
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
        // for portrait orientation
        mRgbaTranspose = mRgba.t();
        mGrayTranspose = mGray.t();
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        mRgbaTranspose.release();
        mGrayTranspose.release();
    }

    public Mat rotateMat(Mat matImage) {
        Mat rotated = matImage.t();
        Core.flip(rotated, rotated, 1);
        return rotated;
    }

    /**
     * 1. 為了防止前後鏡頭在手機畫面直向與橫向間切換時失控，
     *    必須在app的AndroidManifest.xml的FaceDetectionActivity宣告中設定
     *    android:configChanges="keyboardHidden|orientation|screenSize"
     * 2. 為了讓手機畫面能夠在直向與橫向間自由切換
     *    必須在app的ndroidManifest.xml的FaceDetectionActivity宣告中設定
     *    android:screenOrientation="fullSensor"
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        MatOfRect faces = new MatOfRect();
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            mRgba = inputFrame.rgba();
            mGray = inputFrame.gray();
            int height = mGray.rows();
            if (Math.round(height * 0.2) > 0) {
                mAbsoluteFaceSize = (int) Math.round(height * 0.2);
            }

            // CascadeClassifier.detectMultiScale(...)
            // image為輸入的灰階影像
            // objects為得到被檢測物體的矩形框向量組
            // scaleFactor: scaleFactor為每一個圖像尺度中的尺度參數，預設值為1.1。scale_factor參數可以決定兩個不同大小的窗口掃描之間有多大的跳躍，這個參數設置的大，則意味著計算會變快，但如果窗口錯過了某個大小的人臉，則可能丟失物體。
            // minNeighbors: 目標至少被檢測幾次以上才認為人臉存在，預設值為3
            // flag: Objdetect.CASCADE_SCALE_IMAGE (=2)
            // cvSize(): 指示尋找人臉的最小區域。設置這個參數過大，會以丟失小物體為代價減少計算量。此參數對於效能的影響甚鉅，建議不要設定為太小的值。
            // 如果要做單一張人臉辨識，可以使用這個分類器：Objdetect.CASCADE_FIND_BIGGEST_OBJECT
            mCascadeDetector.detectMultiScale(
                    mGray,
                    faces,
                    1.03,
                    12,
                    Objdetect.CASCADE_SCALE_IMAGE,
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize),
                    new Size());
            mGray.release();
            // 每當只偵測到單一張人臉時，就將此人臉紀錄並顯示於預覽區
            if (faces.toArray().length == 1) {
                Rect rect = faces.toArray()[0];
                Mat matFaceDetected = mRgba.submat(rect);  // original size
                // resize the detected area
                matFaceDetectedResized = new Mat(RESIZED_IMAGE_HEIGHT, RESIZED_IMAGE_WIDTH, matFaceDetected.type());
                Imgproc.resize(matFaceDetected, matFaceDetectedResized, matFaceDetectedResized.size(), 0, 0, Imgproc.INTER_AREA);  // resizing
                // make the mirror image when using the front camera
                if (mCameraId == CameraBridgeViewBase.CAMERA_ID_FRONT) {
                    Core.flip(matFaceDetectedResized, matFaceDetectedResized, 1);
                }
                // show the detected face on the ImageView
                Message msg = new Message();
                msg.obj = MSG_FACE_DETECTED;
                mHandler.sendMessage(msg);
            }
            // draw the rectangle areas of the detected faces
            for (Rect rect : faces.toArray()) {
                // rect.tl()返回rect的左上頂點(top-left)座標
                // rect.br()返回rect的右下頂點(bottom-right)座標
                Imgproc.rectangle(mRgba, rect.tl(), rect.br(), FACE_RECT_COLOR, 2);
            }
        } else {
            mRgba = inputFrame.rgba();
            mGray = rotateMat(inputFrame.gray());
            if (mAbsoluteFaceSize == 0) {
                int height = mGray.cols();
                if (Math.round(height * 0.2) > 0) {
                    mAbsoluteFaceSize = (int) Math.round(height * 0.2);
                }
            }
            Mat newMat = rotateMat(mRgba);
            if (mCameraId == CameraBridgeViewBase.CAMERA_ID_FRONT) {
                Core.flip(newMat, newMat, -1);
                Core.flip(mGray, mGray, -1);
            }
            if (mCascadeDetector != null) {
                mCascadeDetector.detectMultiScale(
                        mGray,
                        faces,
                        1.03,
                        12,
                        Objdetect.CASCADE_SCALE_IMAGE,
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize),
                        new Size());
            }
            mGray.release();
            Rect[] facesArray = faces.toArray();
            // 每當只偵測到單一張人臉時，就將此人臉紀錄並顯示於預覽區
            if (facesArray.length == 1) {
                Rect rect = facesArray[0];
                Mat matFaceDetected = newMat.submat(rect);  // original size
                // resize the detected area
                matFaceDetectedResized = new Mat(RESIZED_IMAGE_HEIGHT, RESIZED_IMAGE_WIDTH, matFaceDetected.type());
                Imgproc.resize(matFaceDetected, matFaceDetectedResized, matFaceDetectedResized.size(), 0, 0, Imgproc.INTER_AREA);  // resizing
                // make the mirror image when using the front camera
                if (mCameraId == CameraBridgeViewBase.CAMERA_ID_FRONT) {
                    Core.flip(matFaceDetectedResized, matFaceDetectedResized, 1);
                }
                // show the detected face on the ImageView
                Message msg = new Message();
                msg.obj = MSG_FACE_DETECTED;
                mHandler.sendMessage(msg);
            }
            // draw the rectangle areas of the detected faces
            for (Rect rect : facesArray) {
                Imgproc.rectangle(newMat, rect.tl(), rect.br(), FACE_RECT_COLOR, 2);
            }
            Imgproc.resize(newMat, mRgba, new Size(mRgba.width(), mRgba.height()));
            newMat.release();
        }
        if (mCameraId == CameraBridgeViewBase.CAMERA_ID_FRONT) {
            Core.flip(mRgba, mRgba, 1);
            Core.flip(mGray, mGray, 1);
        }
        return mRgba;
    }

}
