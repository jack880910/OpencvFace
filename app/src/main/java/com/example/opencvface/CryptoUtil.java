package com.example.opencvface;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.security.KeyFactory;
import java.security.PublicKey;


public class CryptoUtil {

    public static PublicKey getECPublicKeyFromPEM(String publicKeyPEM) throws Exception {
        String publicKey = publicKeyPEM
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\n", "")
                .trim();
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        byte[] encodedKey = new byte[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            encodedKey = Base64.getDecoder().decode(publicKey.getBytes());
        }
        return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
    }

    /**
     * Generates a pair of RSA private and public keys.
     * @return a pair of RSA private and public keys
     * @throws InvalidAlgorithmParameterException
     * @throws Exception
     */
    public static KeyPair generateRSAKeyPair(int keySize) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(keySize, RSAKeyGenParameterSpec.F4);
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(spec);
        keyPairGen.initialize(keySize, new SecureRandom());
        KeyPair keyPair = keyPairGen.generateKeyPair();
        return keyPair;
    }


    /**
     * generateRSAKeyPair generates a pair of EC (Elliptic Curve) private and public keys.
     * @return a pair of EC private and public keys
     * @throws Exception
     */
    public static KeyPair generateECKeyPair() throws Exception {
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("EC");
        keyPairGen.initialize(ecSpec, new SecureRandom());
        KeyPair keyPair = keyPairGen.generateKeyPair();
        return keyPair;
    }


    /**
     * convertPrivateKeyToPEM converts the private key to the PEM (Privacy Enhanced Mail) format.
     * @param key: the secret key
     * @return the PEM encoded secret key
     */
    public static String convertPrivateKeyToPEM(String key) {
        // header
        StringBuffer buff = new StringBuffer("-----BEGIN PRIVATE KEY-----\n");
        // body
        int n = key.length();
        int num_lines = (int)Math.ceil((double)n/64.0d);
        for(int i = 0; i < num_lines; i++) {
            int start = i*64;
            int end = (i+1)*64;
            if(end > n) end = n;
            buff.append(key.substring(start, end) + "\n");
        }
        // footer
        buff.append("-----END PRIVATE KEY-----");

        return buff.toString();
    }


    /**
     * convertPublicKeyToPEM converts the public key to the PEM (Privacy Enhanced Mail) format.
     * @param key: the public key
     * @return the PEM encoded secret key
     */
    public static String convertPublicKeyToPEM(String key) {
        // header
        StringBuffer buff = new StringBuffer("-----BEGIN PUBLIC KEY-----\n");
        // body
        int n = key.length();
        int num_lines = (int)Math.ceil((double)n/64.0d);
        for(int i = 0; i < num_lines; i++) {
            int start = i*64;
            int end = (i+1)*64;
            if(end > n) end = n;
            buff.append(key.substring(start, end) + "\n");
        }
        // footer
        buff.append("-----END PUBLIC KEY-----");
        return buff.toString();
    }


}

