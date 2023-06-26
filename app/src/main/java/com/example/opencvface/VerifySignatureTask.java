package com.example.opencvface;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class VerifySignatureTask extends AsyncTask<String, Void, Boolean> {
    private static final String TAG = "VerifySignatureTask";
    private VerifySignatureCallback callback;

    public VerifySignatureTask(VerifySignatureCallback callback) {
        this.callback = callback;
    }

    protected Boolean doInBackground(String... params) {
        String data = params[0];
        String signature = params[1];
        String ip = ConnectionParams.FR_IP;
        int port = ConnectionParams.FR_PORT;
        String action = ConnectionParams.BLOCK_ACTION_VERIFY;
        String urlStr = "http://" + ip + ":" + port + "/" + action;

        try {
            URL url = new URL(urlStr);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setConnectTimeout(8000);
            urlConnection.setReadTimeout(8000);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");

            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));

            String post_data = "keyowner=" + URLEncoder.encode(User.vaccination_org, "UTF-8") + "&" +
                    "signature=" + URLEncoder.encode(signature, "UTF-8") + "&" +
                    "result=" + URLEncoder.encode(data, "UTF-8");
            writer.write(post_data);
            writer.flush();
            writer.close();
            out.close();

            int responseCode = urlConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // 解析响应结果
                boolean result = Boolean.parseBoolean(response.toString());
                return result;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    protected void onPostExecute(Boolean result) {
        // 在UI线程中处理验证结果
        Log.d(TAG, "Verification result: " + result);
        // 这里可以根据需要执行相应的操作
        if (callback != null) {
            callback.onVerifyComplete(result);
        }
    }

    public interface VerifySignatureCallback {
        void onVerifyComplete(boolean result);
    }
}

