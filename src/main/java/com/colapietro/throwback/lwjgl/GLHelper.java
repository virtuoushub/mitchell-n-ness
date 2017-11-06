package com.colapietro.throwback.lwjgl;

import static org.lwjgl.opengl.GL11.glClearColor;

/**
 * @author Peter Colapietro.
 */
public class GLHelper {
     static void clearColor(RGBA color) {
        glClearColor(color.red, color.green, color.blue, color.alpha);
    }
}
