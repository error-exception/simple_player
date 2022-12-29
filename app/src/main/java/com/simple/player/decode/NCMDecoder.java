package com.simple.player.decode;

import com.simple.player.util.AESUtils;
import com.simple.player.util.Base64Utils;
import com.simple.player.util.FileUtil;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class NCMDecoder {

    public static short[] magicHeader = {
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

    private static int offsetKey = 0;
    private static int dataLength = 0;
    private static int offsetMeta = 0;
    private static int offsetCover = 0;
    private static int offsetAudio = 0;
    private static byte[] key;
    private static byte[] box;
    private static String metaType;
    private static byte[] metaRaw;
    private static byte[] audio;
    private static byte[] cover;

    public static byte[] decode(InputStream inputStream) {
        byte[] originData = FileUtil.INSTANCE.readBytes(inputStream);
        if (!isNCM(originData)) {
            System.out.println("not a ncm file");
            return null;
        }
        dataLength = originData.length;
        readKeyData(originData);
        buildKeyBox();
        readMetaData(originData);
        parseMeta(originData);
        readCoverData(originData);
        readAudioData(originData);
        return audio;
    }

    public static boolean isNCM(byte[] originData) {
        for (int i = 0; i < magicHeader.length; i++) {
            if (Short.toUnsignedInt(magicHeader[i]) != Byte.toUnsignedInt(originData[i])) {
                return false;
            }
        }
        offsetKey = 8 + 2;
        return true;
    }

    public static void readKeyData(byte[] originData) {
        if (offsetKey == 0 || offsetKey + 4 > dataLength) {
            System.out.println("invalid cover file offset");
        }
        byte[] bKeyLen = Arrays.copyOfRange(originData, offsetKey, offsetKey + 4);
        System.out.println(Arrays.toString(bKeyLen));
        int iKeyLen = (int) calcSize(bKeyLen);
        System.out.println(Integer.toUnsignedLong(iKeyLen));
        System.out.println("iKeyLen: " + iKeyLen);
        offsetMeta = offsetKey + 4 + iKeyLen;
        byte[] bKeyRaw = new byte[iKeyLen];
        for (int i = 0; i < iKeyLen; i++) {
            bKeyRaw[i] = (byte) (originData[i + 4 + offsetKey] ^ 0x64);
        }
        byte[] decryptedKey = AESUtils.INSTANCE.decrypt(bKeyRaw, keyCore);
        key = Arrays.copyOfRange(decryptedKey, 17, decryptedKey.length);
    }

    private static void buildKeyBox() {
        short[] box = new short[256];
        for (int i = 0; i < 256; i++) {
            box[i] = (short) i;
        }
        int keyLen = key.length;
        short j = 0;
        for (int i = 0; i < 256; i++) {
            j = (short) ((box[i] + j + key[i % keyLen]) & 0xff);
            short temp = box[i];
            box[i] = box[j];
            box[j] = temp;
        }
        NCMDecoder.box = new byte[256];
        for (int i = 0; i < 256; i++) {
            j = (short) ((i + 1) & 0xff);
            short si = box[j];
            short sj = box[(j + si) & 0xff];
            NCMDecoder.box[i] = (byte) box[(si + sj) & 0xff];
        }
    }

    private static void readMetaData(byte[] originData) {
        if (offsetMeta == 0 || offsetMeta + 4 > dataLength) {
            System.out.println("invalid meta file offset");
            return;
        }
        byte[] bMetaLen = Arrays.copyOfRange(originData, offsetMeta, offsetMeta + 4);
        int iMetaLen = (int) calcSize(bMetaLen);
        offsetCover = offsetMeta + 4 + iMetaLen;
        if (iMetaLen == 0) {
            System.out.println("no any meta file found");
            return;
        }
        byte[] bKeyRaw = new byte[iMetaLen - 22];
        for (int i = 0; i < iMetaLen - 22; i++) {
            bKeyRaw[i] = (byte) (originData[offsetMeta + 4 + 22 + i] ^ 0x63);
        }
        String cipherText = Base64Utils.INSTANCE.decodeToString(bKeyRaw);
        if (cipherText == null) {
            System.out.println("decode ncm meta failed");
            return;
        }
        byte[] metaRaw = AESUtils.INSTANCE.decrypt(cipherText.getBytes(), (byte[]) keyMeta);// TODO
        int sepIndex = indexOf(metaRaw, (byte) ':');
        if (sepIndex == -1) {
            System.out.println("invalid ncm meta file");
            return;
        }
        byte[] type = Arrays.copyOfRange(metaRaw, 0, sepIndex);
        metaType = new String(type, 0, type.length, StandardCharsets.UTF_8);
        NCMDecoder.metaRaw = Arrays.copyOfRange(metaRaw, sepIndex + 1, metaRaw.length);
    }

    public static void parseMeta(byte[] originData) {
        if ("music".equals(metaType)) {

        } else if ("dj".equals(metaType)) {

        } else {
            System.out.println("unknown ncm meta type: " + metaType);
        }
    }

    public static void readCoverData(byte[] originData) {
        if (offsetCover == 0 || offsetCover + 13 > dataLength) {
            System.out.println("invalid cover file offset");
            return;
        }
        int coverLenStart = offsetCover + 5 + 4;
        byte[] bCoverLen = Arrays.copyOfRange(originData, coverLenStart, coverLenStart + 4);
        int iCoverLen = (int) calcSize(bCoverLen);
        offsetAudio = coverLenStart + 4 + iCoverLen;
        if (iCoverLen == 0) {
            System.out.println("no any cover file found");
            return;
        }
        cover = Arrays.copyOfRange(originData, coverLenStart + 4, 4 + coverLenStart + iCoverLen);
    }

    public static void readAudioData(byte[] originData) {
        if (offsetAudio == 0 || offsetAudio > dataLength) {
            System.out.println("invalid audio offset");
            return;
        }
        byte[] audioRaw = Arrays.copyOfRange(originData, offsetAudio, originData.length);
        int audioLen = audioRaw.length;
        audio = new byte[audioLen];
        for (int i = 0; i < audioLen; i++) {
            audio[i] = (byte) (box[i & 0xff] ^ audioRaw[i]);
        }
    }

    private static int indexOf(byte[] bytes, byte target) {
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == target) {
                return i;
            }
        }
        return -1;
    }

    public static long calcSize(byte[] sizeByte) {
        long size = 0;
        size = size | Byte.toUnsignedInt(sizeByte[3]);
        size = (size << 8) | Byte.toUnsignedInt(sizeByte[2]);
        size = (size << 8) | Byte.toUnsignedInt(sizeByte[1]);
        size = (size << 8) | Byte.toUnsignedInt(sizeByte[0]);
        return size;
    }

}
