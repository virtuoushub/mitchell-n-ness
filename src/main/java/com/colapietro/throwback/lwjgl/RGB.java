package com.colapietro.throwback.lwjgl;

/**
 * @author Peter Colapietro.
 */
public enum RGB {
    RED(1.0f, 0.0f, 0.0f),
    GREEN(0.0f, 1.0f, 0.0f),
    BLUE(0.0f, 0.0f, 1.0f),
    WHITE(1.0f, 1.0f, 1.0f),
    BLACK(0.0f, 0.0f, 0.0f),
    YELLOW(1.0f, 1.0f, 0.0f),
    PINK(1.0f, 0.0f, 1.0f),;

    final float red;
    final float green;
    final float blue;

    /**
     *
     * @param red   the value to which to clear the R channel of the color buffer
     * @param green the value to which to clear the G channel of the color buffer
     * @param blue  the value to which to clear the B channel of the color buffer
     */
    RGB(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }
}
