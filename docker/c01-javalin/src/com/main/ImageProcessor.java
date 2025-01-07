package com.main;

import java.util.Arrays;

public class ImageProcessor {

    private static final double[] predefinedZoomInFactors = {2.0, 4.0, 8.0, 16.0};
    private static final double[] predefinedZoomOutFactors = {0.5, 0.25, 0.1};

    public static boolean isValidZoomInFactor(double scaleFactor) {
        return Arrays.stream(predefinedZoomInFactors).anyMatch(factor -> factor == scaleFactor);
    }

    public static boolean isValidZoomOutFactor(double scaleFactor) {
        return Arrays.stream(predefinedZoomOutFactors).anyMatch(factor -> factor == scaleFactor);
    }
}
