package com.simple.player.decode;


import com.simple.player.util.AESUtils;
import com.simple.player.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class NCMDecoderTest {

    public static byte[] magicHeader = {
            0x43, 0x54, 0x45, 0x4E, 0x46, 0x44, 0x41, 0x4D
    };
    public static byte[] keyCore = {
            0x68, 0x7a, 0x48, 0x52, 0x41, 0x6d, 0x73, 0x6f,
            0x35, 0x6b, 0x49, 0x6e, 0x62, 0x61, 0x78, 0x57
    };
    public static byte[] keyMeta = {
            0x23, 0x31, 0x34, 0x6C, 0x6A, 0x6B, 0x5F, 0x21,
            0x5C, 0x5D, 0x26, 0x30, 0x55, 0x3C, 0x27, 0x28
    };

    public static byte[] originData;
    public static int length;
    public static int offsetMeta;

    public static byte[] decode(InputStream inputStream) throws IOException {
        originData = FileUtil.INSTANCE.readBytes(inputStream);

        byte[] key = readKey();
        int[] box = buildKeyBox(key);
        readMetaData();
        readCover();
        return readAudioData(box);
    }

    public static boolean isNCMFile(byte[] data) {
        for (int i = 0; i < magicHeader.length; i++) {
            if (magicHeader[i] != data[i]) {
                return false;
            }
        }
        return true;
    }

    public static byte[] readKey() {
        int offset = magicHeader.length + 2; // skip gap bytes
//        byte[] sizeByte = Arrays.copyOfRange(originData, offset, offset + 4);
        int keyLength = (int) calcSize(originData, offset);
//        System.out.println(keyLength);
        offsetMeta = offset + 4 + keyLength; // locate meta data part

        offset += 4; // skip length bytes
        byte[] keyBytes = new byte[keyLength];
        for (int i = 0; i < keyLength; i++) {
            keyBytes[i] = (byte) (originData[i + offset] ^ 0x64);
        }
        byte[] decryptedKey = AESUtils.INSTANCE.decrypt(keyBytes, keyCore);
//        System.out.println(new String(decryptedKey, 0, decryptedKey.length, StandardCharsets.UTF_8));
        return Arrays.copyOfRange(decryptedKey, 17, decryptedKey.length); // skip string "neteasecloudmusic" length 17
    }

    /*
     * RC4 S盒
     * */
    public static int[] buildKeyBox(byte[] key) {
        int[] finalBox = new int[256];
        int[] box = new int[256];
        for (int i = 0; i < box.length; i++) {
            box[i] = i;
        }
        int j = 0;
        for (int i = 0; i < 256; i++) {
            j = (j + box[i] + key[i % key.length]) & 0xff;
            int tmp = box[i];
            box[i] = box[j];
            box[j] = tmp;
        }
        for (int i = 0; i < 256; i++) {
            j = (i + 1) & 0xff;
            int si = box[j];
            int sj = box[(j + si) & 0xff];
            finalBox[i] = box[(si + sj) & 0xff];
        }
        return finalBox;
    }

    public static int offsetCover;
    public static void readMetaData() {
        byte[] sizeBytes = Arrays.copyOfRange(originData, offsetMeta, offsetMeta + 4);
        int metaLength = (int) calcSize(originData, offsetMeta);
        offsetCover = offsetMeta + 4 + metaLength; // locate cover part
        // TODO: decrypt meta data
    }

    public static int offsetAudio;
    public static void readCover() {
        int coverLenStart = offsetCover + 5 + 4; // skip gap and crc
        byte[] sizeBytes = Arrays.copyOfRange(originData, coverLenStart, coverLenStart + 4);
        int coverLength = (int) calcSize(sizeBytes, coverLenStart);
        System.out.println(coverLength);
        offsetAudio = coverLenStart + 4 + coverLength; // skip length bytes and cover data

    }

    public static byte[] readAudioData(int[] box) {
        byte[] decryptedAudio;
        byte[] encryptedAudio = Arrays.copyOfRange(originData, offsetAudio, originData.length);
        decryptedAudio = new byte[encryptedAudio.length];
        for (int i = 0; i < encryptedAudio.length; i++) {
            decryptedAudio[i] = decryptByte(i, box, encryptedAudio[i]);
        }
        return decryptedAudio;
    }

    public static byte decryptByte(int index, int[] box, byte data) {
        return (byte) (box[index & 0xff] ^ data);
    }

    public static long calcSize(byte[] sizeByte, int offset) {
        long size = 0;
        int a1 = Byte.toUnsignedInt(sizeByte[offset]);
        int a2 = Byte.toUnsignedInt(sizeByte[1 + offset]);
        int a3 = Byte.toUnsignedInt(sizeByte[2 + offset]);
        int a4 = Byte.toUnsignedInt(sizeByte[3 + offset]);

        size = size | a4;
        size = (size << 8) | a3;
        size = (size << 8) | a2;
        size = (size << 8) | a1;

        return size;
    }

}
