package com.github.brickwall2900.utils;

import com.github.brickwall2900.Main;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
public class ThisIsAnInsaneEncryptAlgorithm {
    private static final SecureRandom RANDOM;

    private static final boolean DEBUG = true;
    private static final byte[] SALT;

    static {
        SecureRandom random;
        try {
            random = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            random = new SecureRandom();
        }
        RANDOM = random;
        if (!DEBUG) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                try (FileInputStream fis = new FileInputStream(new File(Main.class.getProtectionDomain().getCodeSource().getLocation()
                        .toURI()).getPath())) {
                    byte[] b = fis.readAllBytes();
                    SALT = digest.digest(b);
                    eraseData(b);
                }
            } catch (NoSuchAlgorithmException | IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        } else {
//            RANDOM.nextBytes(SALT = new byte[32]);
            SALT = new byte[] {
                    (byte) -102, (byte) 117, (byte) 31, (byte) 35, (byte) 84, (byte) 92, (byte) 100, (byte) 38, (byte) -97, (byte) 62, (byte) -113, (byte) -122, (byte) 103, (byte) 120, (byte) 83, (byte) 46
            };
        }
    }

    public static void eraseData(byte[] bytes) {
        for (int i = 0; i < bytes.length / 2; i++) {
            RANDOM.nextBytes(bytes);
        }
        Arrays.fill(bytes, (byte) 0);
    }

    public static void eraseData(char[] bytes) {
        for (int i = 0; i < bytes.length / 2; i++) {
            for (int j = 0; j < bytes.length; j++) {
                bytes[j] = (char) RANDOM.nextInt();
            }
        }
        Arrays.fill(bytes, (char) 0);
    }

    private static void generateBIv(byte[] bytes) {
        RANDOM.nextBytes(bytes);
    }

    private static Cipher encrypt(char[] key, byte[] initVector) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(key, SALT, 65536, 256);
            SecretKeySpec skeySpec = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            return cipher;
        } catch (Exception ex) {
            if (DEBUG) ex.printStackTrace();
        }

        return null;
    }

    private static Cipher decrypt(char[] key, byte[] initVector) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(key, SALT, 65536, 256);
            SecretKeySpec skeySpec = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            return cipher;
        } catch (Exception ex) {
            if (DEBUG) ex.printStackTrace();
        }

        return null;
    }

    public static UUID generateUUIDFromString(char[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] inBytes = new byte[input.length * 2];
            for (int i = 0; i < input.length; i += 2) {
                ByteBuffer buffer = ByteBuffer.allocate(2);
                buffer.putChar(input[i]);
                byte[] byteArray = buffer.array();
                inBytes[i] = byteArray[0];
                inBytes[i + 1] = byteArray[1];
                buffer.clear();
                eraseData(byteArray);
            }
            byte[] hashBytes = digest.digest(inBytes);
            eraseData(inBytes);
            return UUID.nameUUIDFromBytes(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The first method you call for encrypting a byte of array.
     * @param bytes This will be the input of the unencrypted data. <b>[INPUT MAY BE MODIFIED!]</b>
     * @param iterations how many steps the encryption algorithm will run.
     * @param uid A unique identifier that serves as the one of the keys.
     * @param uuid Another unique identifier that serves as the one of the keys.
     */
    private static byte[] encryptStage1(byte[] bytes, byte iterations, long uid, UUID uuid) throws Exception {
        Random random = new Random();
        long seed = -0xF0AA0FF93433EL;
        long twoBytes = (uid >> 32) & 0xFF;
        long mod1 = ((long) bytes.length << 6);
        seed = twoBytes * ((long) bytes.length * bytes.length) + mod1 - seed;
        for (int i = 0; i < bytes.length; i++) {
                seed = twoBytes * i * mod1 + seed;
                random.setSeed(seed);
            bytes[i] = (byte)(bytes[i] + random.nextInt(0, 99));
        }
        bytes = Base64.getEncoder().encode(bytes);
        for (byte b = 0; b < iterations; b++) {
            bytes = encryptStage2(bytes, uid, uuid);
        }
        return bytes;
    }

    private static byte[] encryptStage2(byte[] bytes, long uid, UUID uuid) throws Exception {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(new DataOutputStream(bos))) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            oos.writeObject(digest.digest(bytes));
            oos.writeObject(bytes);
            return encryptStage3(bos.toByteArray(), uid, uuid);
        }
    }

    public static char[] generatePassword(long uid, UUID uuid) {
        long l1 = uid;
        long l2 = uuid.getLeastSignificantBits();
        long l3 = uuid.getMostSignificantBits();
        if (l1 == 0 || l2 == 0 || l3 == 0) throw new AssertionError("UID and UUID null!");
        if (l1 == l2 || l1 == l3 || l2 == l3) throw new AssertionError("Not unique enough!");
        Random random = new Random();
        random.setSeed(l1 * l2 * l3 * Arrays.hashCode(SALT));
        int i1 = (random.nextInt(8) * 8);
        long l4 = (l2 % (((l1 >> i1) & 0xFF) + random.nextInt()));
        int l5 = (int) (l4 ^ Integer.MIN_VALUE);
        short pl = (short) random.nextInt(l5);
        while (pl <= 0) pl = (short) random.nextInt(l5);
        char[] pwd = new char[pl];
        for (int i = 0; i < pl; i++) {
            long c1 = l1 / (((l2 >> 16) & 0xFF) + 1);
            long c2 = c1 * l3 + l2;
            long c3 = c1 << i - c2;
            long c4 = pl - c2 * (l3 << 8) + c1;
            long c5 = l1 + l2 + l3 - c2 * c3 * c1;
            long c6 = c4 - c5;
            int c7 = (random.nextInt(3) * 16);
            char c8 = (char) (c6 >> (c7 & 0xFF));
            pwd[i] = c8;
            l1 += l2 - c3;
            l2 -= c1 + c2 * i;
            l3 -= c5 * c4 * c6 * i * i;
        }
        return pwd;
    }

    private static byte[] encryptStage3(byte[] bytes, long uid, UUID uuid) throws Exception {
        byte[] iv = new byte[16];
        generateBIv(iv);
        char[] password = generatePassword(uid, uuid);
        byte[] out;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            try (CipherOutputStream outputStream = new CipherOutputStream(bos, encrypt(password, iv))) {
                bos.write(iv);
                outputStream.write(bytes);
//            return bos.toByteArray(); // i made the same fucking mistake like 2 months ago ahhhhhhhh
            }
            out = bos.toByteArray();
        }
        return out;
    }

    /**
     * The first method you call for decrypting a byte of array.
     * @param bytes This will be the input of the encrypted data. <b>[INPUT MAY BE MODIFIED!]</b>
     * @param iterations how many steps the encryption algorithm will run.
     * @param uid A unique identifier that serves as the one of the keys.
     * @param uuid Another unique identifier that serves as the one of the keys.
     */
    private static byte[] decryptStage1(byte[] bytes, byte iterations, long uid, UUID uuid) throws Exception {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            byte[] iv = new byte[16];
            if (bis.read(iv) != 16) throw new IOException("File malformed?");
            char[] password = generatePassword(uid, uuid);
            try (CipherInputStream inputStream = new CipherInputStream(bis, decrypt(password, iv))) {
                return decryptStage2(inputStream.readAllBytes(), iterations, uid, uuid);
            }
        }
    }

    private static byte[] decryptStage2(byte[] bytes, byte iterations, long uid, UUID uuid) throws Exception {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(new DataInputStream(bis))) {
            byte[] digestedBytes = (byte[]) ois.readObject();
            byte[] enc = (byte[]) ois.readObject();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] newDigestedBytes = digest.digest(enc);
            if (!Arrays.equals(digestedBytes, newDigestedBytes)) throw new IOException("File corrupted!");
            return decryptStage3(enc, iterations, uid, uuid);
        }
    }

    private static byte[] decryptStage3(byte[] bytes, byte iterations, long uid, UUID uuid) throws Exception {
        for (byte b = 0; b < iterations - 1; b++) {
            bytes = decryptStage1(bytes, (byte) -1, uid, uuid);
        }
        if (iterations > 0) {
            bytes = Base64.getDecoder().decode(bytes);
            Random random = new Random();
            long seed = -0xF0AA0FF93433EL;
            long twoBytes = (uid >> 32) & 0xFF;
            long mod1 = ((long) bytes.length << 6);
            seed = twoBytes * ((long) bytes.length * bytes.length) + mod1 - seed;
            for (int i = 0; i < bytes.length; i++) {
                seed = twoBytes * i * mod1 + seed;
                random.setSeed(seed);
                bytes[i] = (byte) (bytes[i] - random.nextInt(0, 99));
            }
        }
        return bytes;
    }

    // PUBLIC APIS

    public static byte[] encrypt(Key key, byte[] data) {
        try {
            return encryptStage1(data, key.iterations, key.uid1, key.uid2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] decrypt(Key key, byte[] data) {
        try {
            return decryptStage1(data, key.iterations, key.uid1, key.uid2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public record Key(byte iterations, long uid1, UUID uid2) {}
}
