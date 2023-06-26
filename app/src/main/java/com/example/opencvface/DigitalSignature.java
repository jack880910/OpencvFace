package com.example.opencvface;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.util.concurrent.BlockingDeque;


public class DigitalSignature {
    public static final String TAG = "ECDSA_action";

    /**
     * sign() can generate the digital signature using a specific signing algorithm.
     * @param data: the message to be signed
     * @param privateKey: the private key used to sign the message
     * @param algorithm: the signing algorithm ("SHA256withRSA", "SHA256withECDSA", ...)
     * @return the digital signature
     * @throws Exception
     */
    public static byte[] sign(byte[] data, PrivateKey privateKey, String algorithm) throws Exception {
        Signature signature = Signature.getInstance(algorithm);
        signature.initSign(privateKey);
        signature.update(data);
        byte[] digitalSignature = new byte[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            digitalSignature = Base64.getEncoder().encode(signature.sign());
        }
        return digitalSignature;
    }


    /**
     * verify() can verify the digital signature.
     * @param data: the message to be verified
     * @param publicKey: the public key
     * @param digitalSignature: the digital signature
     * @param algorithm: the signing algorithm ("SHA256withRSA", "SHA256withECDSA", ...)
     * @return a boolean value (true or false) representing the verification result
     * @throws Exception
     */
    public static boolean verify(byte[] data, PublicKey publicKey, byte[] digitalSignature, String algorithm) throws Exception {
            Signature signature = Signature.getInstance(algorithm);
            signature.initVerify(publicKey);
            signature.update(data);
            boolean result = signature.verify(digitalSignature);
            Log.d(TAG, "verify_alg: " + algorithm);
            Log.d(TAG, "verify_data: " + data);
            Log.d(TAG, "verify_publicKey: " + publicKey);
            Log.d(TAG, "verify簽章: " + digitalSignature);
            Log.d(TAG, "verify驗證結果: " + result);
            return result;
    }

    public static void genKeypair(String key) throws Exception {
        KeyPair keyPair = CryptoUtil.generateECKeyPair();

        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        String privateKeyStr = null;
        String publicKeyStr = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            privateKeyStr = new String(Base64.getEncoder().encode(privateKey.getEncoded()), "UTF-8");
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            publicKeyStr = new String(Base64.getEncoder().encode(publicKey.getEncoded()), "UTF-8");
        }
        //store user's private_key
        User.private_key = privateKey;
        //put public_key into blockchain
        createPubkey(publicKeyStr);
        Log.d(TAG, "publicKeyStr: " + publicKeyStr);

    }

    public static void createPubkey(String publicKeyStr) {
        CreatePubkeyTask task = new CreatePubkeyTask(new CreatePubkeyTask.CreatePubkeyCallback() {
            @Override
            public void onCreatePubkeyComplete(String result) {
                // 处理结果，result为true或false
                Log.d(TAG, "onCreatePubkeyComplete: " + result);
            }
        });
        task.execute(publicKeyStr);
    }



    public static void verifySignature(String data, String signature) {
        VerifySignatureTask task = new VerifySignatureTask(new VerifySignatureTask.VerifySignatureCallback() {
            @Override
            public void onVerifyComplete(boolean result) {
                // 处理验证结果
                if(result == true){
                    User.isVerify = "通過";
                    Log.d(TAG, "數位簽章驗證結果:" + User.isVerify);
                }else{
                    User.isVerify = "不通過";
                    Log.d(TAG, "數位簽章驗證結果:" + User.isVerify);
                }
            }
        });
        task.execute(data, signature);
    }


}
