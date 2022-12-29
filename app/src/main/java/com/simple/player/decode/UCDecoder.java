package com.simple.player.decode;

import com.simple.player.util.FileUtil;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

public class UCDecoder {

    @NotNull
    public static byte[] decode(InputStream inputStream) {
        byte[] originData = FileUtil.INSTANCE.readBytes(inputStream);
        for (int i = 0; i < originData.length; i++) {
            byte data = originData[i];
            data ^= 0xA3;
            originData[i] = data;
        }
        return originData;
    }

    public static int decryptByte(byte b) {
        return b ^ 0xA3;
    }

}
