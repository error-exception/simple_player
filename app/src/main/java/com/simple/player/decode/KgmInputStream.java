//package com.simple.player.decode;
//
//import android.util.Log;
//
//import org.jetbrains.annotations.NotNull;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Arrays;
//
//
//this class cannot work correctly!
//
//public class KgmInputStream extends InputStream {
//
//    private static final String TAG = "KgmInputStream";
//    private InputStream inputStream;
//    private int decryptIndex = 0;
//    private final boolean isKgm;
//    private final boolean isVpr;
//    private final boolean isPrepareToRead;
//    private final byte[] key;
//
//    public KgmInputStream(@NotNull InputStream inputStream) throws IOException {
//        this.inputStream = inputStream;
//        int maxLength = 1024;
//        byte[] buffer = new byte[maxLength];
//        int magicHeaderLength = inputStream.read(buffer, 0, KgmDecoder.kgmHeader.length);
//        if (magicHeaderLength < KgmDecoder.kgmHeader.length) {
//            inputStream.close();
//            throw new IOException("input file too small");
//        }
//        isKgm = KgmDecoder.isKgmFile(buffer);
//        isVpr = KgmDecoder.isVprFile(buffer);
//        if (!isKgm && !isVpr) {
//            inputStream.close();
//            throw new IOException("unsupported file");
//        }
//        // read header size
//        int length = inputStream.read(buffer, 0, 4);
//        if (length < 4) {
//            throw new IOException("the file is incomplete");
//        }
//        long headerLength = calcHeaderSize(buffer, 0);
//        long remainsToReadLength = headerLength - length - magicHeaderLength;
//        if (remainsToReadLength > buffer.length) {
//            buffer = new byte[(int) remainsToReadLength];
//        }
//        length = inputStream.read(buffer, 0, (int) remainsToReadLength);
//        if (length < remainsToReadLength) {
//            throw new IOException("the file is incomplete");
//        }
//        key = Arrays.copyOfRange(buffer, 8, 8 + (0x2c + 1 - 0x1c));
//        key[key.length - 1] = 0;
//        if (KgmDecoder.mask == null) {
//            KgmDecoder.initMask();
//        }
////        Log.e(TAG, "KgmInputStream: " + (KgmDecoder.mask == null));
//        if (KgmDecoder.mask == null) {
//            throw new IOException("mask read failed");
//        }
//        isPrepareToRead = true;
//    }
//
//    @Override
//    public int read() throws IOException {
//        if (inputStream == null) {
//            throw new IOException("constructor parameter can not be null");
//        }
//        return isKgm ? readKgm() : readVpr();
//    }
//
//    private int readKgm() throws IOException {
//        if (!isPrepareToRead) {
//            return -1;
//        }
//        int data = inputStream.read();
//        if (data == -1) {
//            return -1;
//        }
//        int med8 = (data ^ key[decryptIndex % 17] ^ KgmDecoder.maskV2PreDef[decryptIndex % (16 * 17)] ^ KgmDecoder.mask[decryptIndex >> 4]);
//        int decrypted = (med8 ^ (med8 & 0xf) << 4);
//        decryptIndex++;
//        return decrypted;
//    }
//
//    public static int readKgm(int decryptIndex, int data, byte[] key) {
//        int med8 = (data ^ key[decryptIndex % 17] ^ KgmDecoder.maskV2PreDef[decryptIndex % (16 * 17)] ^ KgmDecoder.mask[decryptIndex >> 4]);
//        return (med8 ^ (med8 & 0xf) << 4);
//    }
//
//    public static int readVpr(int decryptIndex, int data, byte[] key) {
//        int med8 = (data ^ key[decryptIndex % 17] ^ KgmDecoder.maskV2PreDef[decryptIndex % (16 * 17)] ^ KgmDecoder.mask[decryptIndex >> 4]);
//        data = (med8 ^ (med8 & 0xf) << 4);
//        data ^= KgmDecoder.maskDiffVpr[decryptIndex % 17];
//        return data;
//    }
//
//    private int readVpr() throws IOException {
//        if (!isPrepareToRead) {
//            return -1;
//        }
//        short data = (short) inputStream.read();
//        short med8 = (short) (data ^ key[decryptIndex % 17] ^ KgmDecoder.maskV2PreDef[decryptIndex % (16 * 17)] ^ KgmDecoder.mask[decryptIndex >> 4]);
//        data = (short) (med8 ^ (med8 & 0xf) << 4);
//        data ^= KgmDecoder.maskDiffVpr[decryptIndex % 17];
//        decryptIndex++;
//        return data;
//    }
//
//    public static long calcHeaderSize(byte[] sizeBytes, int offset) {
////        byte[] sizeByte = Arrays.copyOfRange(sizeBytes, 0x10, 0x14);
//        long size = 0;
//        size = size | sizeBytes[3 + offset];
//        size = (size << 8) | sizeBytes[2 + offset];
//        size = (size << 8) | sizeBytes[1 + offset];
//        size = (size << 8) | sizeBytes[offset];
//        return size;
//    }
//
//    @Override
//    public int available() throws IOException {
//        return inputStream.available();
//    }
//
//    @Override
//    public void close() throws IOException {
//        if (inputStream == null) {
//            return;
//        }
//        super.close();
//        inputStream.close();
//        inputStream = null;
//    }
//}