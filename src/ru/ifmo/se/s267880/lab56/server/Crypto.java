package ru.ifmo.se.s267880.lab56.server;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class Crypto {
    private static SecureRandom sr;
    private static SecretKeyFactory keyFactory;
    private static final char[] PASSWORD_PEPPER = ("6zsT5*aHL6jXnH&r").toCharArray();
    private static final int N_ITERATION = 1001;
    private static final int KEY_LENGTH = 64 * 8;
    static {
        try {
            sr = SecureRandom.getInstance("SHA1PRNG");
            keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    public static byte[] generateSalt() {
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }

    public static String hashPassword(char[] pass) throws InvalidKeySpecException {
        return hashPassword(pass, generateSalt());
    }

    public static String hashPassword(char[] password, byte[] salt) throws InvalidKeySpecException {
        char[] pepperedPassword = new char[password.length + PASSWORD_PEPPER.length];
        System.arraycopy(password, 0, pepperedPassword, 0, password.length);
        System.arraycopy(PASSWORD_PEPPER, 0, pepperedPassword, password.length, PASSWORD_PEPPER.length);
        PBEKeySpec keySpec = new PBEKeySpec(pepperedPassword, salt, N_ITERATION, KEY_LENGTH);
        byte[] key = keyFactory.generateSecret(keySpec).getEncoded();
        Base64.Encoder b64e = Base64.getEncoder();
        return String.format("%s:%s", b64e.encodeToString(salt), b64e.encodeToString(key));
    }

    public static boolean validatePassword(char[] password, String hashedPassword) throws InvalidKeySpecException {
        String[] hashedParts = hashedPassword.split(":");
        String newHashedPassword = hashPassword(password, Base64.getDecoder().decode(hashedParts[0]));
        return newHashedPassword.equals(hashedPassword);
    }
}
