package com.toocol.termio.core.mosh.core.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/9 23:15
 * @version: 0.0.1
 */
public final class Compressor {

    private static Compressor compressor;
    private static Mode mode = Mode.NO_WRAP;

    private Compressor() {

    }

    static synchronized Compressor get() {
        if (compressor == null) {
            compressor = new Compressor();
        }
        return compressor;
    }

    public static void wrap() {
        mode = Mode.WRAP;
    }

    public static void noWrap() {
        mode = Mode.NO_WRAP;
    }

    public byte[] compress(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return new byte[0];
        }
        byte[] output;

        Deflater compressor = new Deflater(Deflater.DEFAULT_COMPRESSION, mode.compressNowrap);
        compressor.reset();
        compressor.setInput(bytes);
        compressor.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length);

        try {
            byte[] buf = new byte[1024];
            while (!compressor.finished()) {
                int i = compressor.deflate(buf);
                bos.write(buf, 0, i);
            }
            output = bos.toByteArray();
        } catch (Exception e) {
            output = bytes;
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        compressor.end();
        return output;
    }

    public byte[] decompress(byte[] data) {
        byte[] output;

        Inflater decompressor = new Inflater(mode.decompressNowrap);
        decompressor.reset();
        decompressor.setInput(data);

        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!decompressor.finished()) {
                int i = decompressor.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        decompressor.end();
        return output;
    }

    /**
     * When we send packet to mosh-server and receive packet from mosh-server, we have to set the nowrap to false.
     */
    public enum Mode {
        WRAP(true, true),
        NO_WRAP(false, false);
        private final boolean compressNowrap;
        private final boolean decompressNowrap;

        Mode(boolean compressNowrap, boolean decompressNowrap) {
            this.compressNowrap = compressNowrap;
            this.decompressNowrap = decompressNowrap;
        }
    }

}