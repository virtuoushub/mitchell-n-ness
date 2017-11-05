package com.colapietro.throwback.lwjgl;


import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.*;

import java.io.IOException;
import java.nio.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.colapietro.throwback.lwjgl.demo.GLFWUtil.glfwInvoke;
import static com.colapietro.throwback.lwjgl.demo.IOUtil.ioResourceToByteBuffer;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBTruetype.stbtt_BakeFontBitmap;
import static org.lwjgl.stb.STBTruetype.stbtt_GetBakedQuad;
import static org.lwjgl.stb.STBTruetype.stbtt_GetCodepointHMetrics;
import static org.lwjgl.stb.STBTruetype.stbtt_GetCodepointKernAdvance;
import static org.lwjgl.stb.STBTruetype.stbtt_GetFontVMetrics;
import static org.lwjgl.stb.STBTruetype.stbtt_InitFont;
import static org.lwjgl.stb.STBTruetype.stbtt_ScaleForPixelHeight;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * @author Peter Colapietro.
 */
public class HelloWorld {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private int ww = 800;
    private int wh = 600;
    private int lineOffset;
    private final int lineCount = 1;
    private float lineHeight;
    private final int scale  = 0;
    private boolean kerningEnabled = true;
    private boolean lineBBEnabled;
    protected String text;
    private final STBTTFontinfo info;
    private final int ascent;
    private final int descent;
    private final int lineGap;

    private final ByteBuffer ttf;
    private long window;
    private Set<Integer> controllers;
    private Map<Integer, Boolean> controllersAdded;
    private Callback debugProc;
    private final int fontHeight = 24;
    private static final int NUMBER_OF_SUPPORTED_GLFW_JOYSTICKS = GLFW_JOYSTICK_LAST + 1;
    int BITMAP_W = 512;
    int BITMAP_H = 512;


    public HelloWorld() {
        try {
            ttf = ioResourceToByteBuffer("FiraSans-Regular.ttf", 160 * 1024);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        info = STBTTFontinfo.create();
        if (!stbtt_InitFont(info, ttf)) {
            throw new IllegalStateException("Failed to initialize font information.");
        }
        text = "Bar";
        try (MemoryStack stack = stackPush()) {
            IntBuffer pAscent  = stack.mallocInt(1);
            IntBuffer pDescent = stack.mallocInt(1);
            IntBuffer pLineGap = stack.mallocInt(1);

            stbtt_GetFontVMetrics(info, pAscent, pDescent, pLineGap);

            ascent = pAscent.get(0);
            descent = pDescent.get(0);
            lineGap = pLineGap.get(0);
        }
    }

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

        final String title = "Hello World!";
        this.window = glfwCreateWindow(WIDTH, HEIGHT, title, NULL, NULL);
        if ( window == NULL ) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

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

        //
        final String string = "030000004c050000c405000000010000,PS4 Controller,a:b1,b:b2,back:b8,dpdown:h0.4,dpleft:h0.8,dpright:h0.2,dpup:h0.1,guide:b12,leftshoulder:b4,leftstick:b10,lefttrigger:a3,leftx:a0,lefty:a1,rightshoulder:b5,rightstick:b11,righttrigger:a4,rightx:a2,righty:a5,start:b9,x:b0,y:b3,platform:Mac OS X,";
        assert glfwUpdateGamepadMappings(string);
        assert NUMBER_OF_SUPPORTED_GLFW_JOYSTICKS == 16;
        controllers = new HashSet<>(NUMBER_OF_SUPPORTED_GLFW_JOYSTICKS);
        controllersAdded = new ConcurrentHashMap<>(NUMBER_OF_SUPPORTED_GLFW_JOYSTICKS);
        for (int jid = GLFW_JOYSTICK_1; jid < GLFW_JOYSTICK_LAST; jid++) {
            if(glfwJoystickPresent(jid)) {
                updateConnectedControllers(jid, GLFW_CONNECTED);
            }
        }
        glfwSetJoystickCallback(this::updateConnectedControllers);

        glfwInvoke(window, this::windowSizeChanged, HelloWorld::framebufferSizeChanged);
    }

    private void updateConnectedControllers(int jid, int event) {
        if (event == GLFW_CONNECTED) {
            addController(jid);
            final boolean isGamepad = glfwJoystickIsGamepad(jid);
            final String joystickGUID = glfwGetJoystickGUID(jid);
            final String joystickName = glfwGetJoystickName(jid);
            final String s = joystickGUID + ' ' + isGamepad + ' ' + joystickName;
            System.out.println(s);
        } else if (event == GLFW_DISCONNECTED) {
            removeController(jid);
        }
        text = controllers.size() + " controllers connected";
    }

    private void addController(int jid) {
        final boolean controllerAdded = controllers.add(jid);
        if (controllerAdded) {
            controllersAdded.put(jid, controllerAdded);
            final String joystickName = glfwGetJoystickName(GLFW_JOYSTICK_1);
//            testing ps4 controller
//            assert joystickName.equals(Controller.XBOX_360.name);
//            assert joystickName.equals(Xbox360ControllerButton.controller.name);
            System.out.println("Added");
        }
    }

    private void removeController(int jid) {
        if (controllersAdded.getOrDefault(jid, false)) {
            controllers.remove(jid);
            controllersAdded.put(jid, false);
            System.out.println("Removed");
        }
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the clear color
        clearColor(RGBA.BLACK);


        STBTTBakedChar.Buffer cdata = init(BITMAP_W, BITMAP_H);


        while ( !glfwWindowShouldClose(window) ) {
            detectControllersStates();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

//            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();

            float scaleFactor = 1.0f + getScale() * 0.25f;


            glPushMatrix();
            // Zoom
            glScalef(scaleFactor, scaleFactor, 1f);
            // Scroll
            glTranslatef(4.0f, getFontHeight() * 0.5f + 4.0f - getLineOffset() * getFontHeight(), 0f);

            renderText(cdata, BITMAP_W, BITMAP_H);

            glPopMatrix();

            glfwSwapBuffers(getWindow());
        }
        cdata.free();
    }

    private void detectControllersStates() {
        for(int glfwJoystickIndex : controllers) {
            dectectControllerState(glfwJoystickIndex);
        }
    }

    private void dectectControllerState(int jid) {
//        if(glfwGetJoystickGUID(jid).equals("030000005e0400008e02000000000000")) {
            final ByteBuffer joystickButtons = glfwGetJoystickButtons(jid);
//            assert joystickButtons.limit() == Controller.XBOX_360.buttonLimit;
//            assert joystickButtons.limit() == Controller.PS4.buttonLimit;
            while (joystickButtons.hasRemaining()) {
                final int buttonIndex = joystickButtons.position();
                final byte buttonState = joystickButtons.get();
                if (buttonState == GLFW_PRESS) {
                    doSomething(buttonIndex);
                }
            }
            final FloatBuffer joystickAxes = glfwGetJoystickAxes(jid);
            assert joystickAxes.limit() == 6;
            while (joystickAxes.hasRemaining()) {
                final int axisPosition = joystickAxes.position();
                final float axisState = joystickAxes.get();

            }
//                final Xbox360ControllerAxis controllerAxis = Xbox360ControllerAxis.valueOf(axisPosition);
//                final boolean isAxisLeftTrigger = controllerAxis.equals(Xbox360ControllerAxis.LEFT_TRIGGER);
//                final boolean isAxisRightTrigger = controllerAxis.equals(Xbox360ControllerAxis.RIGHT_TRIGGER);
//                final boolean isAxisTrigger = isAxisLeftTrigger || isAxisRightTrigger;
//                if (isAxisTrigger) {
//                    if (Float.compare(axisState, 1.0f) == 0) {
//                        System.out.println(controllerAxis + " fully pressed");
//                    }
//                } else {
//                    final boolean isAxisLeftX = controllerAxis.equals(Xbox360ControllerAxis.LEFT_X);
//                    final boolean isAxisLeftY = controllerAxis.equals(Xbox360ControllerAxis.LEFT_Y);
//                    final boolean isAxisLeft = isAxisLeftX || isAxisLeftY;
////                        final boolean isAxisRightX = controllerAxis.equals(Xbox360ControllerAxis.RIGHT_X);
////                        final boolean isAxisRightY = controllerAxis.equals(Xbox360ControllerAxis.RIGHT_Y);
////                        final boolean isAxisRight = isAxisRightX || isAxisRightY;
//                    final boolean isAsixPastHalfway = axisState > 0.5f || axisState < -0.5f;
//                    if (isAxisLeft) {
//                        if (isAsixPastHalfway) {
//                            System.out.println("left stick moved"); // PS4
//                        }
//                    } else {
//                        if (isAsixPastHalfway) {
//                            System.out.println("right stick moved"); //PS4?
//                        }
//                    }
//                }
//            }
//        }
    }

    private void doSomething(int buttonIndex) {
        final Xbox360ControllerButton controllerButton = Xbox360ControllerButton.valueOf(buttonIndex);
        if(controllerButton.equals(Xbox360ControllerButton.A)) {
            clearColor(RGBA.GREEN);
            text = "GREEN";
        } else if(controllerButton.equals(Xbox360ControllerButton.B)) {
            clearColor(RGBA.RED);
            text = "RED";
        } else if(controllerButton.equals(Xbox360ControllerButton.Y)) {
            clearColor(RGBA.YELLOW);
            text = "YELLOW";
        } else if(controllerButton.equals(Xbox360ControllerButton.X)) {
            clearColor(RGBA.BLUE);
            text = "BLUE";
        } else if (controllerButton.equals(Xbox360ControllerButton.BACK)) {
            text = "BYE";
            glfwSetWindowShouldClose(window, true);
        }
    }

    public static void main(String[] args) {
        new HelloWorld().run();
    }

    private static void clearColor(RGBA color) {
        glClearColor(color.red, color.green, color.blue, color.alpha);
    }

//    private void foo() {
        // ...
//        Configuration.DISABLE_CHECKS.set(true);
//        final String fileName = "pc_game_controller_db.txt"; //"gamecontrollerdb.txt";
//         try (final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(fileName)) {
//            final byte[] byteArray = IOUtils.toByteArray(inputStream);
//            final ByteBuffer byteBuffer0 = ByteBuffer.wrap(byteArray);
//            glfwUpdateGamepadMappings(byteBuffer0);
//            final String string = IOUtils.toString(inputStream, StandardCharsets.US_ASCII);
//         } catch (IOException e) {
//             e.printStackTrace();
//         } finally {}
//    }


    private void windowSizeChanged(long window, int width, int height) {
        this.ww = width;
        this.wh = height;

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0.0, width, height, 0.0, -1.0, 1.0);
        glMatrixMode(GL_MODELVIEW);

        setLineOffset(round(lineOffset));
    }

    private void setLineOffset(int offset) {
        lineOffset = max(0, min(offset, lineCount - (int)(wh / lineHeight)));
    }

    private static void framebufferSizeChanged(long window, int width, int height) {
        glViewport(0, 0, width, height);
    }

    private STBTTBakedChar.Buffer init(int BITMAP_W, int BITMAP_H) {
        int                   texID = glGenTextures();
        STBTTBakedChar.Buffer cdata = STBTTBakedChar.malloc(96);

        ByteBuffer bitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H);
        stbtt_BakeFontBitmap(this.ttf, getFontHeight(), bitmap, BITMAP_W, BITMAP_H, 32, cdata);

        glBindTexture(GL_TEXTURE_2D, texID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, BITMAP_W, BITMAP_H, 0, GL_ALPHA, GL_UNSIGNED_BYTE, bitmap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        glClearColor(43f / 255f, 43f / 255f, 43f / 255f, 0f); // BG color
        glColor3f(169f / 255f, 183f / 255f, 198f / 255f); // Text color

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        return cdata;
    }

    public int getFontHeight() {
        return fontHeight;
    }

    public long getWindow() {
        return window;
    }

    public int getScale() {
        return scale;
    }

    public int getLineOffset() {
        return lineOffset;
    }

    private void renderText(STBTTBakedChar.Buffer cdata, int BITMAP_W, int BITMAP_H) {
        float scale = stbtt_ScaleForPixelHeight(info, getFontHeight());

        try (MemoryStack stack = stackPush()) {
            IntBuffer pCodePoint = stack.mallocInt(1);

            FloatBuffer x = stack.floats(0.0f);
            FloatBuffer y = stack.floats(0.0f);

            STBTTAlignedQuad q = STBTTAlignedQuad.mallocStack(stack);

            int lineStart = 0;

            int i  = 0;
            int to = text.length();

            glBegin(GL_QUADS);
            while (i < to) {
                i += getCP(text, to, i, pCodePoint);

                int cp = pCodePoint.get(0);
                if (cp == '\n') {
                    if (isLineBBEnabled()) {
                        glEnd();
                        renderLineBB(lineStart, i - 1, y.get(0), scale);
                        glBegin(GL_QUADS);
                    }

                    y.put(0, y.get(0) + (ascent - descent + lineGap) * scale);
                    x.put(0, 0.0f);

                    lineStart = i;
                    continue;
                } else if (cp < 32 || 128 <= cp) {
                    continue;
                }

                stbtt_GetBakedQuad(cdata, BITMAP_W, BITMAP_H, cp - 32, x, y, q, true);
                if (isKerningEnabled() && i < to) {
                    getCP(text, to, i, pCodePoint);
                    x.put(0, x.get(0) + stbtt_GetCodepointKernAdvance(info, cp, pCodePoint.get(0)) * scale);
                }

                glTexCoord2f(q.s0(), q.t0());
                glVertex2f(q.x0(), q.y0());

                glTexCoord2f(q.s1(), q.t0());
                glVertex2f(q.x1(), q.y0());

                glTexCoord2f(q.s1(), q.t1());
                glVertex2f(q.x1(), q.y1());

                glTexCoord2f(q.s0(), q.t1());
                glVertex2f(q.x0(), q.y1());
            }
            glEnd();
            if (isLineBBEnabled()) {
                renderLineBB(lineStart, text.length(), y.get(0), scale);
            }
        }
    }

    private void renderLineBB(int from, int to, float y, float scale) {
        glDisable(GL_TEXTURE_2D);
        glPolygonMode(GL_FRONT, GL_LINE);
        glColor3f(1.0f, 1.0f, 0.0f);

        float width = getStringWidth(info, text, from, to, getFontHeight());
        y -= descent * scale;

        glBegin(GL_QUADS);
        glVertex2f(0.0f, y);
        glVertex2f(width, y);
        glVertex2f(width, y - getFontHeight());
        glVertex2f(0.0f, y - getFontHeight());
        glEnd();

        glEnable(GL_TEXTURE_2D);
        glPolygonMode(GL_FRONT, GL_FILL);
        glColor3f(169f / 255f, 183f / 255f, 198f / 255f); // Text color
    }

    private float getStringWidth(STBTTFontinfo info, String text, int from, int to, int fontHeight) {
        int width = 0;

        try (MemoryStack stack = stackPush()) {
            IntBuffer pCodePoint       = stack.mallocInt(1);
            IntBuffer pAdvancedWidth   = stack.mallocInt(1);
            IntBuffer pLeftSideBearing = stack.mallocInt(1);

            int i = from;
            while (i < to) {
                i += getCP(text, to, i, pCodePoint);
                int cp = pCodePoint.get(0);

                stbtt_GetCodepointHMetrics(info, cp, pAdvancedWidth, pLeftSideBearing);
                width += pAdvancedWidth.get(0);

                if (isKerningEnabled() && i < to) {
                    getCP(text, to, i, pCodePoint);
                    width += stbtt_GetCodepointKernAdvance(info, cp, pCodePoint.get(0));
                }
            }
        }

        return width * stbtt_ScaleForPixelHeight(info, fontHeight);
    }

    private static int getCP(String text, int to, int i, IntBuffer cpOut) {
        char c1 = text.charAt(i);
        if (Character.isHighSurrogate(c1) && i + 1 < to) {
            char c2 = text.charAt(i + 1);
            if (Character.isLowSurrogate(c2)) {
                cpOut.put(0, Character.toCodePoint(c1, c2));
                return 2;
            }
        }
        cpOut.put(0, c1);
        return 1;
    }

    public boolean isKerningEnabled() {
        return kerningEnabled;
    }

    public boolean isLineBBEnabled() {
        return lineBBEnabled;
    }
}
