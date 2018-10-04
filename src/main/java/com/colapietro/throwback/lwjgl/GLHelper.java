package com.colapietro.throwback.lwjgl;

import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.glColor3f;

/**
 * @author Peter Colapietro.
 */
public class GLHelper {
     static void glClearColor(RGBA color) {
        GL11.glClearColor(color.red, color.green, color.blue, color.alpha);
    }

    static void glColor(RGB rgb) {
        glColor3f(rgb.red, rgb.green, rgb.blue);
    }
}
