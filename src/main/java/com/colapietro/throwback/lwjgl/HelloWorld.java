package com.colapietro.throwback.lwjgl;


import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * @author Peter Colapietro.
 */
public class HelloWorld {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    // The window handle
    private long window;
    private Set<Integer> controllers;
    private Map<Integer, Boolean> controllersAdded;
    private static final int NUMBER_OF_SUPPORTED_GLFW_JOYSTICKS = GLFW_JOYSTICK_LAST + 1;

    private void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() ) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(WIDTH, HEIGHT, "Hello World!", NULL, NULL);
        if ( window == NULL ) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE ) {
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
            }
        });

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
        assert NUMBER_OF_SUPPORTED_GLFW_JOYSTICKS == 16;
        controllers = new HashSet<>(NUMBER_OF_SUPPORTED_GLFW_JOYSTICKS);
        controllersAdded = new ConcurrentHashMap<>(NUMBER_OF_SUPPORTED_GLFW_JOYSTICKS);
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the clear color
        clearColor(RGBA.BLUE);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) ) {
            detectControllers();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    private void detectControllers() {
        for (int glfwJoystickIndex = GLFW_JOYSTICK_1; glfwJoystickIndex < GLFW_JOYSTICK_LAST; glfwJoystickIndex++) {
            if (glfwJoystickPresent(glfwJoystickIndex)) {
                final boolean controllerAdded = controllers.add(glfwJoystickIndex);
                if(controllerAdded) {
                    controllersAdded.put(glfwJoystickIndex, controllerAdded);
                    final String joystickName = glfwGetJoystickName(GLFW_JOYSTICK_1);
                    assert joystickName.equals(KnownControllers._360.name);
                    System.out.println("Added");
                }
            } else {
                if (controllersAdded.getOrDefault(glfwJoystickIndex, false)) {
                    controllers.remove(glfwJoystickIndex);
                    controllersAdded.put(glfwJoystickIndex, false);
                    System.out.println("Removed");
                }
            }
        }
    }

    public static void main(String[] args) {
        new HelloWorld().run();
    }

    private static void clearColor(RGBA color) {
        glClearColor(color.red, color.green, color.blue, color.alpha);
    }

}
