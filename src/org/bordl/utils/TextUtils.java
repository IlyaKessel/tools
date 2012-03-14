package org.bordl.utils;

/**
 *
 * @author Moxa
 */
public class TextUtils {

    public static boolean isBlank(String s) {
        return s == null ? true : s.length() == 0;
    }

    public static int getInt(String s) {
        return getInt(s, -1);
    }

    public static int getInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            System.out.println("error parsing int: \"" + s + "\"");
            for (char c : s.toCharArray()) {
                System.out.println(c + "  " + (int) c);
            }
        }
        return def;
    }

    public static long getLong(String s, long def) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException ex) {
            System.out.println("error parsing long: \"" + s + "\"");
            for (char c : s.toCharArray()) {
                System.out.println(c + "  " + (int) c);
            }
        }
        return def;
    }

    public static double getDouble(String s) {
        return getDouble(s, -1);
    }

    public static double getDouble(String s, double def) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ex) {
            try {
                return Double.parseDouble(s.replace(",", "."));
            } catch (NumberFormatException e) {
            }
        }
        return def;
    }

    public static String removeAllNbsp(String s) {
        return s.replaceAll("\u00A0", "");
    }
}
