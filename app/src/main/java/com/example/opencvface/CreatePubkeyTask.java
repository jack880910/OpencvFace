package com.example.opencvface;

import android.os.AsyncTask;

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

public class CreatePubkeyTask extends AsyncTask<String, Void, String> {
    private static final String TAG = "CreatePubkeyTask";
    private CreatePubkeyCallback callback;

    public CreatePubkeyTask(CreatePubkeyCallback callback) {
        this.callback = callback;
    }

    protected String doInBackground(String... params) {
        String publicKey = params[0];
        String result = null;
        String ip = ConnectionParams.FR_IP;
        int port = ConnectionParams.FR_PORT;
        String action = ConnectionParams.BLOCK_ACTION_CREATEKEY;
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

            String post_data = "key=" + URLEncoder.encode(User.key, "UTF-8") + "&" +
                    "publickey=" + URLEncoder.encode(publicKey, "UTF-8");
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
                result = response.toString();
                return result;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    protected void onPostExecute(String result) {
        // 在UI线程中处理结果
        if (callback != null) {
            callback.onCreatePubkeyComplete(result);
        }
    }

    public interface CreatePubkeyCallback {
        void onCreatePubkeyComplete(String result);
    }
}
