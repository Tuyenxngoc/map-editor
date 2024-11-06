package com.teamobi.map.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author tuyen
 */
public class Utils {

    public static short bytesToShort(byte[] byteArray, int offset) {
        return (short) ((byteArray[offset] & 0xff) << 8 | byteArray[offset + 1] & 0xff);
    }

    public static void shortToBytes(byte[] byteArray, int index, short value) {
        byteArray[index] = (byte) (value / 255);
        byteArray[index + 1] = (byte) value;
    }

    public static byte[] getFile(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            byte[] ab = new byte[fis.available()];
            fis.read(ab, 0, ab.length);
            fis.close();
            return ab;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveFile(String url, byte[] ab) {
        try {
            File f = new File(url);
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(url);
            fos.write(ab);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
