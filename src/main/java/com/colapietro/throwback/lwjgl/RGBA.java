package com.colapietro.throwback.lwjgl;

/**
 * @author Peter Colapietro.
 */
public enum RGBA {
    RED(RGB.RED, 0.0f),
    GREEN(RGB.GREEN, 0.0f),
    BLUE(RGB.BLUE, 0.0f),
    WHITE(RGB.WHITE, 0.0f),
    BLACK(RGB.BLACK, 0.0f),
    YELLOW(RGB.YELLOW, 0.0f),
    PINK(RGB.PINK, 0.0f),;

    final float red;
    final float green;
    final float blue;
    final float alpha;

    /**
     *
     * @param rgb the enum to which
     * @param alpha the value of the color buffer
     */
    RGBA(RGB rgb, float alpha) {
        this.red = rgb.red;
        this.green = rgb.green;
        this.blue = rgb.blue;
        this.alpha = alpha;
    }
}
