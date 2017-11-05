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
    private static final int NUMBER_OF_SUPPORTED_GLFW_JOYSTICKS = GLFW_JOYSTICK_LAST + 1;

    private long window;
    private Set<Integer> controllers;
    private Map<Integer, Boolean> controllersAdded;
    private Callback debugProc;
    private Font font;
    private Image image;
    private boolean renderFont = true;
    private boolean renderImage = !renderFont;
    private STBTTBakedChar.Buffer cdata;
    private int imageTextureId;

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

    private void destroy() {
        if (debugProc != null) {
            debugProc.free();
        }

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
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
        this.window = glfwCreateWindow(windowWidth, windowHeight, title, NULL, NULL);
        this.font.setWindowHeight(windowHeight);
        this.image.setWindowHeight(windowHeight);
        this.image.setWindowWidth(windowWidth);

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

    private void render() {
        image.render();
//        font.render();
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
        font.setText(controllers.size() + " controllers connected");
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
        if(renderImage) {
            imageTextureId = image.createTexture(); // causing blue color FIXME
        }
        if(renderFont) {
            cdata = font.init(font.BITMAP_WIDTH, font.BITMAP_HEIGHT);
        }

        glEnable(GL_TEXTURE_2D);

        while ( !glfwWindowShouldClose(window) ) {
            detectControllersStates();
            glfwPollEvents();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if(renderImage) {
                image.render();
            }
            if(renderFont) {
                font.render(cdata);
            }
            glfwSwapBuffers(getWindow());
        }
        glDisable(GL_TEXTURE_2D);
        if(renderImage) {
            glDeleteTextures(imageTextureId);
        }
        clearColor(RGBA.BLACK);

        if(renderFont) {
            cdata.free();
        }
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
            font.setText("GREEN");
        } else if(controllerButton.equals(Xbox360ControllerButton.B)) {
            clearColor(RGBA.RED);
            font.setText("RED");
        } else if(controllerButton.equals(Xbox360ControllerButton.Y)) {
            clearColor(RGBA.YELLOW);
            font.setText("YELLOW");
        } else if(controllerButton.equals(Xbox360ControllerButton.X)) {
            clearColor(RGBA.BLUE);
            font.setText("BLUE");
        } else if (controllerButton.equals(Xbox360ControllerButton.BACK)) {
            font.setText("BYE");
            glfwSetWindowShouldClose(window, true);
        } else if (controllerButton.equals(Xbox360ControllerButton.LEFT_BUMPER)) {
            font.setKerningEnabled(!font.getKerningEnabled());
        } else if (controllerButton.equals(Xbox360ControllerButton.RIGHT_BUMPER)) {
            font.setLineBoundingBoxEnabled(!font.getLineBoundingBoxEnabled());
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
