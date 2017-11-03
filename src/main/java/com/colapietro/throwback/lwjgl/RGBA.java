package com.colapietro.throwback.lwjgl;

/**
 * @author Peter Colapietro.
 */
public enum RGBA {
    RED(1.0f, 0.0f, 0.0f, 0.0f),
    GREEN(0.0f, 1.0f, 0.0f, 0.0f),
    BLUE(0.0f, 0.0f, 1.0f, 0.0f);

    final float red;
    final float green;
    final float blue;
    final float alpha;

    /**
     *
     * @param red   the value to which to clear the R channel of the color buffer
     * @param green the value to which to clear the G channel of the color buffer
     * @param blue  the value to which to clear the B channel of the color buffer
     * @param alpha the value to which to clear the A channel of the color buffer
     */
    RGBA(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }
}
