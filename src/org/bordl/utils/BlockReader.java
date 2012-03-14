/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bordl.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Moxa
 */
public class BlockReader {

    private PushbackInputStream in;
    private byte[] buffer, dynamicBuffer;
    private boolean close = false;
    private byte[] separator;
    private boolean wait = true;
    private int findedIndex = -1;
    private int offset = 0;
    private int r, endimg, buffered;
    private BoyerMoore bm;
    private long blockLength = 0;

    public BlockReader(InputStream in, byte[] separator) {
        this.separator = Arrays.copyOf(separator, separator.length);
        this.in = new PushbackInputStream(in, 1);
        buffer = new byte[separator.length];
        bm = new BoyerMoore(separator);
    }

    public boolean hashNext() throws IOException {
        return dynamicBuffer != null || ready();
    }

    public void next() {
        wait = false;
        blockLength = 0;
    }

    public int read(byte[] b) throws IOException {
        if (b.length < separator.length) {
            throw new IllegalArgumentException("byte array MUST be bigger then separator");
        }
        if (wait) {
            return -1;
        }
        if (dynamicBuffer != null) {
            int k = dynamicBuffer.length - offset;
            if (k >= b.length) {
                System.arraycopy(dynamicBuffer, offset, b, 0, b.length);
                offset += b.length;
                if (offset == dynamicBuffer.length) {
                    dynamicBuffer = null;
                    offset = 0;
                }
                r = b.length;
            } else {
                System.arraycopy(dynamicBuffer, offset, b, 0, k);
                dynamicBuffer = null;
                offset = 0;
                r = k;
            }
        } else {
            if (buffered > 0) {
                System.arraycopy(buffer, 0, b, 0, buffered);
                r = in.read(b, buffered, b.length - buffered);
                if (r != -1) {
                    r += buffered;
                } else {
                    r = buffered;
                }
                buffered = 0;
            } else {
                if (ready()) {
                    r = in.read(b);
                } else {
                    r = -1;
                }
            }
        }
        if (r != -1) {
            findedIndex = bm.search(b, 0, r);
//            findedIndex = endOfBlock(b, 0, r);
            if (findedIndex != -1) {
                wait = true;
                int length = r - findedIndex - separator.length;
                if (length != 0) {
                    dynamicBuffer = new byte[length];
                    System.arraycopy(b, findedIndex + separator.length, dynamicBuffer, 0, dynamicBuffer.length);
                }
                blockLength += findedIndex;
                return findedIndex;
            } else {
                endimg = isEnding(b, r, separator);
                if (endimg != -1) {
                    buffered = r - endimg;
                    System.arraycopy(b, endimg, buffer, 0, buffered);
                    blockLength += endimg;
                    return endimg;
                } else {
                    blockLength += r;
                    return r;
                }
            }
        }
        return -1;
    }

    public long getBlockLength() {
        return blockLength;
    }

//    public String readLine(String encoding) throws IOException {
//        String s = null;
//        while ((r = in.read(buffer)) != -1) {
//            int sl = -1;
//            if ((sl = endOfLine(buffer, r)) != -1) {
//                s = new String(buffer, 0, sl, encoding);
//                byte[] bb = new byte[10240];
//                System.arraycopy(buffer, 0, bb, 0, sl);
//                buffer = bb;
//                r = r - sl;
//                break;
//            }
//        }
//
//        if (s != null) {
//            s = s.trim();
//        }
//        return s;
//    }
    public void close() throws IOException {
        in.close();
        close = true;
        in = null;
    }

    public static int isEnding(byte[] b, int length, byte[] endsWith) {
        int i = length - endsWith.length;
        if (i < 0) {
            i = 0;
        }
        outer:
        for (; i < length; i++) {
            int j = 0;
            while (j < endsWith.length && j + i < length && b[i + j] == endsWith[j]) {
                j++;
            }
            if (j + i == length) {
                return i;
            }
        }
        return -1;
    }

    private int endOfBlock(byte[] b, int offset, int length) {
        int i = offset;
        if (i < 0) {
            i = 0;
        }
        outer:
        while (i <= length - separator.length) {
            int j = 0;
            while (j < separator.length && b[i + j] == separator[j]) {
                j++;
            }
            if (j == separator.length) {
                return i;
            }
            i++;
        }
        return -1;
    }

//    private int endOfLine(byte[] b, int length) {
//        int i = 0;
//        while (i < length) {
//            if (b[i] == 13 || b[i] == 10) {
//                return i;
//            }
//            i++;
//        }
//        return -1;
//    }
    public boolean ready() throws IOException {
        if (!close) {
            long wait = 0;
            int r;
            while ((r = in.read()) == -1 && wait < 5000) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BlockReader.class.getName()).log(Level.SEVERE, null, ex);
                }
                wait++;
            }
            in.unread(r);
            return true;
        } else {
            return false;
        }
    }
}
