package com.simple.player.lyrics;

import android.util.Log;

import com.simple.player.util.FileUtil;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import okio.internal.ZipKt;

public class KRCDecoder {

    public static final String TAG = "KRCDecoder";

    private final char[] key = {
             '@', 'G', 'a', 'w', '^', '2', 't', 'G', 'Q', '6', '1', '-', 'Î', 'Ò', 'n', 'i'
    };

    @NotNull
    public static final KRCDecoder INSTANCE = new KRCDecoder();

    @NotNull
    public String decode(@NotNull InputStream inputStream) {
        byte[] data = FileUtil.INSTANCE.readBytes(inputStream);
        byte[] out = new byte[data.length - 4];
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) (data[i + 4] ^ key[i % 16]);
        }
        Inflater inflater = new Inflater();
        inflater.reset();
        inflater.setInput(out);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(out.length);
        try {
            byte[] buff = new byte[1024];
            while (!inflater.finished()) {
                int i = inflater.inflate(buff);
                byteArrayOutputStream.write(buff, 0, i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            inflater.end();
        }
        out = byteArrayOutputStream.toByteArray();
        return new String(out, 0, out.length, StandardCharsets.UTF_8);
    }

}
