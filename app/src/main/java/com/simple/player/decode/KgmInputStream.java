package com.simple.player.decode;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class KgmInputStream extends InputStream {

    private static final String TAG = "KgmInputStream";
    private InputStream inputStream;
    private int decryptIndex = 0;
    private final boolean isKgm;
    private final boolean isVpr;
    private final boolean isPrepareToRead;
    private final byte[] key;

    public KgmInputStream(@NotNull InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new NullPointerException("inputStream is null");
        }
        this.inputStream = inputStream;
        int maxLength = 1024;
        byte[] buffer = new byte[maxLength];
        int magicHeaderLength = inputStream.read(buffer, 0, KgmDecoder.kgmHeader.length);
        if (magicHeaderLength < KgmDecoder.kgmHeader.length) {
            inputStream.close();
            throw new IOException("input file too small");
        }
        isKgm = KgmDecoder.isKgmFile(buffer);
        isVpr = KgmDecoder.isVprFile(buffer);
        if (!isKgm && !isVpr) {
            inputStream.close();
            throw new IOException("unsupported file");
        }
        // read header size
        int length = inputStream.read(buffer, 0, 4);
        if (length < 4) {
            throw new IOException("the file is incomplete");
        }
        long headerLength = KgmDecoder.calcHeaderSize(buffer, 0);
        long remainsToReadLength = headerLength - length - magicHeaderLength;
        if (remainsToReadLength > buffer.length) {
            buffer = new byte[(int) remainsToReadLength];
        }
        length = inputStream.read(buffer, 0, (int) remainsToReadLength);
        if (length < remainsToReadLength) {
            throw new IOException("the file is incomplete");
        }
        key = Arrays.copyOfRange(buffer, 8, 8 + (0x2c + 1 - 0x1c));
        key[key.length - 1] = 0;
        if (KgmDecoder.mask == null) {
            KgmDecoder.initMask();
        }
//        Log.e(TAG, "KgmInputStream: " + (KgmDecoder.mask == null));
        if (KgmDecoder.mask == null) {
            throw new IOException("mask read failed");
        }
        isPrepareToRead = true;
    }

    @Override
    public int read() throws IOException {
        int data = inputStream.read();
        if (data == -1) {
            return -1;
        }
        byte b = 0;
        if (isKgm) {
            b = (byte) KgmDecoder.readKgm(decryptIndex++, data, key);
        } else {
            b = (byte) KgmDecoder.readVpr(decryptIndex++, data, key);
        }
        data = Byte.toUnsignedInt(b);
        return data;
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (inputStream == null) {
            return;
        }
        inputStream.close();
        inputStream = null;
    }

}