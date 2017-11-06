package com.colapietro.throwback.lwjgl;

import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glColor3f;

/**
 * @author Peter Colapietro.
 */
public class GLHelper {
     static void clearColor(RGBA color) {
        glClearColor(color.red, color.green, color.blue, color.alpha);
    }

    static void glColor(RGB rgb) {
        glColor3f(rgb.red, rgb.green, rgb.blue);
    }
}
