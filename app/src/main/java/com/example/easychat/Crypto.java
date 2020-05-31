package com.example.easychat;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {
    private static final String pass = "skillbox";
    private static SecretKeySpec keySpec;

    static {
        MessageDigest shaDigest = null;
        try {
            shaDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] bytes = pass.getBytes();
        shaDigest.update(bytes, 0, bytes.length);
        byte[] key = shaDigest.digest();
        keySpec = new SecretKeySpec(key, "AES");
    }

    public static String encrypt(String rawText) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(rawText.getBytes());
        return Base64.encodeToString(encrypted, Base64.DEFAULT);
    }

    public static String decrypt(String cipheredText) throws Exception {
        byte[] ciphered = Base64.decode(cipheredText, Base64.DEFAULT);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] rawText = cipher.doFinal(ciphered);
        return new String(rawText, "UTF-8");
    }
}
