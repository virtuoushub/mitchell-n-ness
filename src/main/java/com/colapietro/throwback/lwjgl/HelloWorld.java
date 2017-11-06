package com.colapietro.throwback.lwjgl;


import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.system.*;

import java.nio.*;

import static com.colapietro.throwback.lwjgl.GLHelper.glClearColor;
import static com.colapietro.throwback.lwjgl.GLHelper.glColor;
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
 *
 * <a href="https://stackoverflow.com/q/2225737">ERROR: JDWP Unable to get JNI 1.2 environment</a>
 */
public class HelloWorld {

    private int windowWidth = 800;
    private int windowHeight = 600;
    private long window;

    private Callback debugProcess;
    private Font font;
    private Image image;
    private boolean isFontRendered = true;
    private boolean isImageRendered = isFontRendered;
    private STBTTBakedChar.Buffer cdata;
    private int[] textures;
    private Controllers controllers;
    static boolean boundingBoxesEnabled = false;

    public static void main(String[] args) {
        new HelloWorld().run();
    }

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
        image = new Image("images/idle.png");
        font = new Font();
        if ( !glfwInit() ) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        GLFWErrorCallback.createPrint(System.err).set();

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
        font.setWindowHeight(windowHeight);
        image.setWindowHeight(windowHeight);
        image.setWindowWidth(windowWidth);
        controllers = new Controllers(window, font, image);
        controllers.initControllers();

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
        }
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glfwSwapInterval(1);
        glfwShowWindow(window);
        debugProcess = GLUtil.setupDebugMessageCallback();
        glfwInvoke(window, this::windowSizeChanged, HelloWorld::framebufferSizeChanged);
    }

    private void loop() {
        glEnable(GL_TEXTURE_2D);
        glGenTextures(textures);
        if(isImageRendered) {
            glBindTexture(GL_TEXTURE_2D, textures[0]);//FIXME
            image.createTexture();
        }
        if(isFontRendered) {
            glBindTexture(GL_TEXTURE_2D, textures[1]);//FIXME
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            cdata = font.init();
        }
        glClearColor(RGBA.WHITE);//        glClearColor(43f / 255f, 43f / 255f, 43f / 255f, 0f); // BG color
        while ( !glfwWindowShouldClose(window) ) {
            controllers.detectControllersStates();
            glfwPollEvents();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            render();
            glfwSwapBuffers(getWindow());
        }
        glDisable(GL_TEXTURE_2D);

    }

    private void render() {
        if(isImageRendered) {
            glBindTexture(GL_TEXTURE_2D, textures[0]);//FIXME
            image.render();
        }
        if(isFontRendered) {
            glBindTexture(GL_TEXTURE_2D, textures[1]);//FIXME
            glColor(RGB.BLACK);//        glColor3f(169f / 255f, 183f / 255f, 198f / 255f); // Text color
            font.render(cdata);
        }
    }

    private void destroy() {
        glDeleteTextures(textures);
        if(isFontRendered) {
            cdata.free();
        }
        if (debugProcess != null) {
            debugProcess.free();
        }
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
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
