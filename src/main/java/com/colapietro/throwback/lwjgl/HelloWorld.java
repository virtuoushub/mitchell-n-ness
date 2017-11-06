package com.colapietro.throwback.lwjgl;


import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.colapietro.throwback.lwjgl.GLHelper.clearColor;
import static com.colapietro.throwback.lwjgl.demo.GLFWUtil.glfwInvoke;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * @author Peter Colapietro.
 */
public class HelloWorld {

    private int windowWidth = 800;
    private int windowHeight = 600;
    private long window;

    private Callback debugProc;
    private Font font;
    private Image image;
    private boolean isFontRendered = true;
    private boolean isImageRendered = !isFontRendered;
    private STBTTBakedChar.Buffer cdata;
    private int[] textures;
    private Controllers controllers;

    private void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        try {
            init();
            loop();
        } finally {
            try {
                destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    private void init() {
        textures = new int[2];
        image = new Image("images/lwjgl32.png");
        font = new Font();
        GLFWErrorCallback.createPrint(System.err).set();
        if ( !glfwInit() ) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwSetErrorCallback((error, description) -> {
            System.out.println("error " + error);
            System.out.println("description " + GLFWErrorCallback.getDescription(description));
        });

        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);

        final String title = "Hello World!";
        window = glfwCreateWindow(windowWidth, windowHeight, title, NULL, NULL);
        controllers = new Controllers(window, font);
        font.setWindowHeight(windowHeight);
        image.setWindowHeight(windowHeight);
        image.setWindowWidth(windowWidth);

        if ( window == NULL ) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetWindowRefreshCallback(window, window -> render());
        glfwSetWindowSizeCallback(window, this::windowSizeChanged);
        glfwSetFramebufferSizeCallback(window, HelloWorld::framebufferSizeChanged);


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
        GL.createCapabilities();
        debugProc = GLUtil.setupDebugMessageCallback();

        glfwSwapInterval(1);
        glfwShowWindow(window);

        controllers.initControllers();

        glfwInvoke(window, this::windowSizeChanged, HelloWorld::framebufferSizeChanged);
    }

    private void loop() {
        if(isImageRendered) {
            image.createTexture(textures); // causing blue color FIXME
        }
        if(isFontRendered) {
            cdata = font.init(textures);
        }

        glEnable(GL_TEXTURE_2D);

        while ( !glfwWindowShouldClose(window) ) {
            controllers.detectControllersStates();
            glfwPollEvents();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            render();
            glfwSwapBuffers(getWindow());
        }
    }

    private void destroy() {
        glDeleteTextures(textures);
        if(isFontRendered) {
            cdata.free();
        }
        if (debugProc != null) {
            debugProc.free();
        }
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void render() {
        if(isImageRendered) {
            image.render();
        }
        if(isFontRendered) {
            font.render(cdata);
        }
    }

    public static void main(String[] args) {
        new HelloWorld().run();
    }

    private void windowSizeChanged(long window, int width, int height) {
        this.window = window;
        this.windowWidth = width;
        this.windowHeight = height;
        this.font.setWindowHeight(height);


        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0.0, width, height, 0.0, -1.0, 1.0);
        glMatrixMode(GL_MODELVIEW);

        setLineOffset(round(font.lineOffset));
    }

    private void setLineOffset(int offset) {
        font.lineOffset = max(0, min(offset, font.lineCount - (int)(windowHeight / font.lineHeight)));
    }

    private static void framebufferSizeChanged(long window, int width, int height) {
        glViewport(0, 0, width, height);
    }

    public long getWindow() {
        return window;
    }
}
