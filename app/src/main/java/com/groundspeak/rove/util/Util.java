package com.groundspeak.rove.util;

import java.util.Locale;

public class Util {

    private static final int METRE_CUTOFF = 500;

    public static String getDistanceString(double meters) {
        String result;
        if (meters < METRE_CUTOFF) {
            result = String.format(Locale.getDefault(), "%.0fm", meters);
        } else {
            double km = meters / 1000;
            result = String.format(Locale.getDefault(), "%.1fkm", km);
        }
        return result;
    }
}
