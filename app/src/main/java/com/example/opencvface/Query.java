package com.example.opencvface;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Query extends AsyncTask<String, Void, JSONObject> {

    @Override
    protected JSONObject doInBackground(String... keys) {
        try {
            String key = keys[0];
            String ip = ConnectionParams.FR_IP;
            int port = ConnectionParams.FR_PORT;
            String action = ConnectionParams.BLOCK_ACTION_QUERY;
            String urlStr = "http://" + ip + ":" + port + "/" + action;


            Log.d("URL字串inBackgroung", urlStr);
            URL url = new URL(urlStr);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
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

            String post_data = "key=" + URLEncoder.encode(key, "UTF-8");
            writer.write(post_data);
            writer.flush();
            writer.close();
            out.close();


            int responseCode = urlConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 读取响应数据
                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                inputStream.close();

                // 将响应数据转换为JSON对象
                String jsonString = stringBuilder.toString();
                Log.d("JSON物件inBackground", jsonString);
                return new JSONObject(jsonString);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        // 在这里处理JSON对象，例如解析数据或更新UI
        if (jsonObject != null) {
            try {
                // 获取JSON对象中的值
                User.name = jsonObject.getString("name");
                User.birthday = jsonObject.getString("birthday");
                User.vaccine_name = jsonObject.getString("vaccine_name");
                User.vaccine_batchNumber = jsonObject.getString("vaccine_batchNumber");
                User.vaccination_date = jsonObject.getString("vaccination_date");
                User.vaccination_org = jsonObject.getString("vaccination_org");
                User.signature_org = jsonObject.getString("signature_org");
                User.json_data = jsonObject.toString();
                Log.d("Jsonobj", "JsonObj" + jsonObject);

                //處理json
                jsonObject.remove("signature_org");
                String jsonString = jsonObject.toString();
                Log.d("Jsonobj", "Jsonstring" + jsonString);
                //驗證數位簽章
                DigitalSignature.verifySignature(jsonString, User.signature_org);

                //user簽章
                String content_sign = User.key + "&" +
                        User.name + "&" +
                        User.birthday + "&" +
                        User.vaccine_name + "&" +
                        User.vaccine_batchNumber + "&" +
                        User.vaccination_date + "&" +
                        User.vaccination_org + "&" +
                        User.signature_org;
                byte [] byte_data = content_sign.getBytes("UTF-8");
                byte[] signatureBuf = DigitalSignature.sign(byte_data, User.private_key, "SHA256withECDSA");
                User.signature_user = new String(signatureBuf, "UTF-8");
                Log.d("User_signature:", "" + User.signature_user);
                Log.d("content_sign", "" + content_sign);



            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return ;
    }
}
