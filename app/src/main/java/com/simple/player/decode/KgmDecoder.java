package com.simple.player.decode;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.simple.player.model.Song;
import com.simple.player.util.AppConfigure;
import com.simple.player.util.FileUtil;
import com.simple.player.util.ProgressHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tukaani.xz.XZInputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import kotlin.Unit;

public class KgmDecoder {

    private static final String TAG = "KgmHeader";

    public static final short[] vprHeader = {
            0x05, 0x28, 0xBC, 0x96, 0xE9, 0xE4, 0x5A, 0x43,
            0x91, 0xAA, 0xBD, 0xD0, 0x7A, 0xF5, 0x36, 0x31
    };

    public static final short[] kgmHeader = {
            0x7C, 0xD5, 0x32, 0xEB, 0x86, 0x02, 0x7F, 0x4B,
            0xA8, 0xAF, 0xA6, 0x8E, 0x0F, 0xFF, 0x99, 0x14
    };

    public static final short[] maskDiffVpr = {
            0x25, 0xDF, 0xE8, 0xA6, 0x75, 0x1E, 0x75, 0x0E,
            0x2F, 0x80, 0xF3, 0x2D, 0xB8, 0xB6, 0xE3, 0x11,
            0x00
    };

    public static final short[] maskV2PreDef = {
            0xB8, 0xD5, 0x3D, 0xB2, 0xE9, 0xAF, 0x78, 0x8C, 0x83, 0x33, 0x71, 0x51, 0x76, 0xA0, 0xCD, 0x37,
            0x2F, 0x3E, 0x35, 0x8D, 0xA9, 0xBE, 0x98, 0xB7, 0xE7, 0x8C, 0x22, 0xCE, 0x5A, 0x61, 0xDF, 0x68,
            0x69, 0x89, 0xFE, 0xA5, 0xB6, 0xDE, 0xA9, 0x77, 0xFC, 0xC8, 0xBD, 0xBD, 0xE5, 0x6D, 0x3E, 0x5A,
            0x36, 0xEF, 0x69, 0x4E, 0xBE, 0xE1, 0xE9, 0x66, 0x1C, 0xF3, 0xD9, 0x02, 0xB6, 0xF2, 0x12, 0x9B,
            0x44, 0xD0, 0x6F, 0xB9, 0x35, 0x89, 0xB6, 0x46, 0x6D, 0x73, 0x82, 0x06, 0x69, 0xC1, 0xED, 0xD7,
            0x85, 0xC2, 0x30, 0xDF, 0xA2, 0x62, 0xBE, 0x79, 0x2D, 0x62, 0x62, 0x3D, 0x0D, 0x7E, 0xBE, 0x48,
            0x89, 0x23, 0x02, 0xA0, 0xE4, 0xD5, 0x75, 0x51, 0x32, 0x02, 0x53, 0xFD, 0x16, 0x3A, 0x21, 0x3B,
            0x16, 0x0F, 0xC3, 0xB2, 0xBB, 0xB3, 0xE2, 0xBA, 0x3A, 0x3D, 0x13, 0xEC, 0xF6, 0x01, 0x45, 0x84,
            0xA5, 0x70, 0x0F, 0x93, 0x49, 0x0C, 0x64, 0xCD, 0x31, 0xD5, 0xCC, 0x4C, 0x07, 0x01, 0x9E, 0x00,
            0x1A, 0x23, 0x90, 0xBF, 0x88, 0x1E, 0x3B, 0xAB, 0xA6, 0x3E, 0xC4, 0x73, 0x47, 0x10, 0x7E, 0x3B,
            0x5E, 0xBC, 0xE3, 0x00, 0x84, 0xFF, 0x09, 0xD4, 0xE0, 0x89, 0x0F, 0x5B, 0x58, 0x70, 0x4F, 0xFB,
            0x65, 0xD8, 0x5C, 0x53, 0x1B, 0xD3, 0xC8, 0xC6, 0xBF, 0xEF, 0x98, 0xB0, 0x50, 0x4F, 0x0F, 0xEA,
            0xE5, 0x83, 0x58, 0x8C, 0x28, 0x2C, 0x84, 0x67, 0xCD, 0xD0, 0x9E, 0x47, 0xDB, 0x27, 0x50, 0xCA,
            0xF4, 0x63, 0x63, 0xE8, 0x97, 0x7F, 0x1B, 0x4B, 0x0C, 0xC2, 0xC1, 0x21, 0x4C, 0xCC, 0x58, 0xF5,
            0x94, 0x52, 0xA3, 0xF3, 0xD3, 0xE0, 0x68, 0xF4, 0x00, 0x23, 0xF3, 0x5E, 0x0A, 0x7B, 0x93, 0xDD,
            0xAB, 0x12, 0xB2, 0x13, 0xE8, 0x84, 0xD7, 0xA7, 0x9F, 0x0F, 0x32, 0x4C, 0x55, 0x1D, 0x04, 0x36,
            0x52, 0xDC, 0x03, 0xF3, 0xF9, 0x4E, 0x42, 0xE9, 0x3D, 0x61, 0xEF, 0x7C, 0xB6, 0xB3, 0x93, 0x50,
    };

    public static byte[] mask;

//    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Nullable
    public static byte[] decode(@NotNull InputStream inputStream) {
        boolean isKgm = false;
        boolean isVpr = false;
        byte[] originData = readBytes(inputStream);
//        Log.e(TAG, "decode: originData length = " + originData.length);
        if (isKgmFile(originData)) {
            isKgm = true;
        }
        if (isVprFile(originData)) {
            isVpr = true;
        }
        if (!isKgm && !isVpr) {
            Log.e(TAG, "unsupport" + isKgm + " " + isVpr);
            return null;
        }
        byte[] key = Arrays.copyOfRange(originData, 0x1c, 0x2c + 1);
        key[key.length - 1] = 0;
//        _ = d.file[0x2c:0x3c] //todo: key2
        long headerSize = calcHeaderSize(originData);
        byte[] encryptedData = Arrays.copyOfRange(originData, (int)headerSize, originData.length);
        int dataLength = encryptedData.length;
        initMask();
        if (mask == null) {
            Log.e(TAG, "mask read failed");
            return null;
        }
        if (mask.length < encryptedData.length) {
            //Log.e(TAG, "decode: The file is too large and the processed audio is incomplete");
            //dataLength = mask.length;
        }
        byte[] audio = new byte[dataLength];
        for (int i = 0; i < dataLength; i++) {
            short med8 = (short) (encryptedData[i] ^ key[i % 17] ^ maskV2PreDef[i % (16 * 17)] ^ mask[i >> 4]);
            audio[i] = (byte) (med8 ^ (med8 & 0xf) << 4);
        }
        if (isVpr) {
            for (int i = 0; i < dataLength; i++) {
                audio[i] ^= maskDiffVpr[i % 17];
            }
        }
        return audio;
    }

    public static void initMask() {
        // for test
        InputStream inputStream = null;
        if (FileUtil.mMaskFile.exists()) {
            mask = FileUtil.INSTANCE.readBytes(FileUtil.mMaskFile);
            return;
        } else {
            inputStream = FileUtil.INSTANCE.getAssetInputStream("mask/kgm.v2.mask");
        }
        if (inputStream == null) {
            return;
        }
        XZInputStream xzInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024 * 63);
        try {
            xzInputStream = new XZInputStream(inputStream);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = xzInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "initMask: " + e.getCause());
        } finally {
            FileUtil.INSTANCE.closeStream(xzInputStream, inputStream);
            FileUtil.INSTANCE.closeStream(byteArrayOutputStream);
        }
        byte[] maskData = byteArrayOutputStream.toByteArray();
        long maskSize = (AppConfigure.Player.INSTANCE.getMaxSongSize() >> 4);
        if (maskData.length > maskSize) {
            mask = Arrays.copyOfRange(maskData, 0, (int) maskSize);
            Log.e(TAG, "initMask:" + mask.length );
            ProgressHandler.INSTANCE.handle(null, null, () -> {
                FileUtil.INSTANCE.writeBytes(FileUtil.mMaskFile, mask);
                return Unit.INSTANCE;
            });
        }
    }

    // Test
    public static long calcHeaderSize(byte[] originData) {
        byte[] sizeByte = Arrays.copyOfRange(originData, 0x10, 0x14);
//        long size = 0;
//        size = size | sizeByte[3];
//        size = (size << 8) | sizeByte[2];
//        size = (size << 8) | sizeByte[1];
//        size = (size << 8) | sizeByte[0];
//        return size;
        return calcHeaderSize(sizeByte, 0);
    }

    public static long calcHeaderSize(byte[] sizeBytes, int offset) {
//        byte[] sizeByte = Arrays.copyOfRange(sizeBytes, 0x10, 0x14);
        long size = 0;
        size = size | sizeBytes[3 + offset];
        size = (size << 8) | sizeBytes[2 + offset];
        size = (size << 8) | sizeBytes[1 + offset];
        size = (size << 8) | sizeBytes[offset];
        return size;
    }

    public static boolean isVprFile(byte[] originData) {
        for (int i = 0; i < vprHeader.length; i++) {
            if (Byte.toUnsignedInt(originData[i]) != Short.toUnsignedInt(vprHeader[i])) {
                return false;
            }
        }
        return true;
    }

    public static boolean isKgmFile(byte[] originData) {
        for (int i = 0; i < kgmHeader.length; i++) {
            if (Byte.toUnsignedInt(originData[i]) != Short.toUnsignedInt(kgmHeader[i])) {
                return false;
            }
        }
        return true;
    }

    private static byte[] readBytes(InputStream inputStream) {
        byte[] data = new byte[0];
        try (
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        ) {
            byte[] buff = new byte[1024];
            int len;
            while ((len = inputStream.read(buff)) != -1) {
                outputStream.write(buff, 0, len);
            }
            data = outputStream.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "readBytes: ", e);
                }
            }
        }
        return data;
    }

    public static int readKgm(int decryptIndex, int data, byte[] key) {
        int med8 = (data ^ key[decryptIndex % 17] ^ KgmDecoder.maskV2PreDef[decryptIndex % (16 * 17)] ^ KgmDecoder.mask[decryptIndex >> 4]);
        return (med8 ^ (med8 & 0xf) << 4);
    }

    public static int readKgm1(int decryptIndex, int data, byte[] key) {
        int med8 = (data ^ key[fitRange(decryptIndex, 17)] ^ KgmDecoder.maskV2PreDef[fitRange(decryptIndex, 272)] ^ KgmDecoder.mask[decryptIndex >> 4]);
        return (med8 ^ (med8 & 0xf) << 4);
    }

    public static int readVpr(int decryptIndex, int data, byte[] key) {
        int med8 = (data ^ key[decryptIndex % 17] ^ KgmDecoder.maskV2PreDef[decryptIndex % (16 * 17)] ^ KgmDecoder.mask[decryptIndex >> 4]);
        data = (med8 ^ (med8 & 0xf) << 4);
        data ^= KgmDecoder.maskDiffVpr[decryptIndex % 17];
        return data;
    }

    public static int fitRange(int target, int max) {
        return target - max * (target / max);
    }

}
