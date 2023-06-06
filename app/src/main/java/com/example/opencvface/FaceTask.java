package com.example.opencvface;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class FaceTask {
    private static final String TAG = "CSHsu::FaceTask";
    private final Callback mCallback;
    private final String faceLabel;
    private final String urlStr;

    public FaceTask(String faceLabel, String ip, int port, String action, Callback callback) {
        this.faceLabel = faceLabel;
        this.mCallback = callback;
        this.urlStr = getURLString(ip, port, action);
    }

    private String getURLString(String ip, int port, String action) {
        return "http://" + ip + ":" + port + "/" + action;
    }

    public void execute(Mat mat) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            // doInBackground
            Log.d(TAG, "doInBackground");
            String result = uploadImage(mat);

            // onPostExecute
            handler.post(() -> {
                // UI Thread work here
                Log.d(TAG, "onPostExecute");
                mCallback.onUploadComplete(result);
            });
        });
    }

    private String uploadImage(Mat mat) {
        StringBuilder response = new StringBuilder();
        URL url;
        HttpURLConnection urlConnection = null;

        if (mat == null) {
            Log.d(TAG, "尚未偵測到臉部");
            return "尚未偵測到臉部";
        }

        try {
            Log.d(TAG, urlStr);
            url = new URL(this.urlStr);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setConnectTimeout(8000);
            urlConnection.setReadTimeout(8000);
            urlConnection.setDoInput(true);
            //if app需要輸出資料至server，則DoOutput為true
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");

            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));

            // --- convert mat to bitmap and bitmap to jpeg --- //
            Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mat, bitmap);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            String jpegStr = new String(Base64.encode(byteArray, Base64.DEFAULT));  // Encoding data byte array to Base64.
            // ------------------------------------------------ //

            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
            String format = "JPEG";

            String post_data = "faceLabel=" + URLEncoder.encode(this.faceLabel, "UTF-8") + "&" +
                    "width=" + URLEncoder.encode(Integer.toString(width), "UTF-8") + "&" +
                    "height=" + URLEncoder.encode(Integer.toString(height), "UTF-8") + "&" +
                    "format=" + URLEncoder.encode(format, "UTF-8") + "&" +
                    "image=" + URLEncoder.encode(jpegStr, "UTF-8");
            writer.write(post_data);
            writer.flush();
            writer.close();
            out.close();

            int responseCode = urlConnection.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
            } else {
                response = new StringBuilder("connection/server error: " + responseCode);
            }
            return response.toString();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return "connection/server error";
    }

    public interface Callback {
        void onUploadComplete(String response);
    }

}