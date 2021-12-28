package com.colapietro.throwback.lwjgl;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;
import org.slf4j.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.colapietro.throwback.lwjgl.GLHelper.glClearColor;
import static org.lwjgl.glfw.GLFW.GLFW_CONNECTED;
import static org.lwjgl.glfw.GLFW.GLFW_DISCONNECTED;
import static org.lwjgl.glfw.GLFW.GLFW_JOYSTICK_1;
import static org.lwjgl.glfw.GLFW.GLFW_JOYSTICK_LAST;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwGetJoystickAxes;
import static org.lwjgl.glfw.GLFW.glfwGetJoystickButtons;
import static org.lwjgl.glfw.GLFW.glfwGetJoystickGUID;
import static org.lwjgl.glfw.GLFW.glfwGetJoystickName;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwJoystickIsGamepad;
import static org.lwjgl.glfw.GLFW.glfwJoystickPresent;
import static org.lwjgl.glfw.GLFW.glfwSetJoystickCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowMonitor;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwUpdateGamepadMappings;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * @author Peter Colapietro.
 */
public class Controllers {

    private static final Logger LOGGER = LoggerFactory.getLogger(Image.class);

    private static final int NUMBER_OF_SUPPORTED_GLFW_JOYSTICKS = GLFW_JOYSTICK_LAST + 1;
    private Set<Integer> controllers;
    private Map<Integer, Boolean> controllersAdded;
    private final long window;
    private final Font font;
    private final Image image;
    private final float angleScalar = 2.0f;
    private final float movementScalar = angleScalar;
    private final float movementSpeed = movementScalar * 1.0f;
    private final float rotationSpeed = angleScalar * 1.0f;
    private final Map<Integer, Integer> previousState = new ConcurrentHashMap<>(Controller.XBOX_360.buttonLimit); //FIXME
    private boolean fullscreen = false; // FIXME
    int xpos; // FIXME
    int ypos; // FIXME
    int width = 800; // FIXME
    int height = 600; // FIXME



    public Controllers(long window, Font font, Image image) {
        this.window = window;
        this.font = font;
        this.image = image;
    }

    void initControllers() {
//        FIXME: not sure why this try/catch is needed. When running in IDEs
//        using git hash c97c351168bbfe2e30540df1a823a5d852b980de
//        the following error is observed:
//
//        java.lang.IllegalArgumentException: Missing termination
//        at org.lwjgl.system.Checks.assertNT(Checks.java:196)
//        at org.lwjgl.system.Checks.checkNT1(Checks.java:227)
//        at org.lwjgl.glfw.GLFW.glfwUpdateGamepadMappings(GLFW.java:4588)
//        at com.colapietro.throwback.lwjgl.Controllers.updatePS4ControllerGamepadMapping(Controllers.java:93)
//        at com.colapietro.throwback.lwjgl.Controllers.initControllers(Controllers.java:76)
//        at com.colapietro.throwback.lwjgl.HelloWorld.init(HelloWorld.java:108)
//        at com.colapietro.throwback.lwjgl.HelloWorld.run(HelloWorld.java:68)
//        at com.colapietro.throwback.lwjgl.HelloWorld.main(HelloWorld.java:61)
        try {
            updatePS4ControllerGamepadMapping();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("AFAIK following stack trace is safe to ignore, although code was unable to map controllers via updatePS4ControllerGamepadMapping", e);
        }
        assert NUMBER_OF_SUPPORTED_GLFW_JOYSTICKS == 16;
        controllers = new HashSet<>(NUMBER_OF_SUPPORTED_GLFW_JOYSTICKS);
        controllersAdded = new ConcurrentHashMap<>(NUMBER_OF_SUPPORTED_GLFW_JOYSTICKS);
        for (int jid = GLFW_JOYSTICK_1; jid < GLFW_JOYSTICK_LAST; jid++) {
            if(glfwJoystickPresent(jid)) {
                updateConnectedControllers(jid, GLFW_CONNECTED);
            }
        }
        glfwSetJoystickCallback(this::updateConnectedControllers);
    }

    private void updatePS4ControllerGamepadMapping() {
        final String ps4ControllerGamepadMapping = "030000004c050000c405000000010000,PS4 Controller,a:b1,b:b2,back:b8,dpdown:h0.4,dpleft:h0.8,dpright:h0.2,dpup:h0.1,guide:b12,leftshoulder:b4,leftstick:b10,lefttrigger:a3,leftx:a0,lefty:a1,rightshoulder:b5,rightstick:b11,righttrigger:a4,rightx:a2,righty:a5,start:b9,x:b0,y:b3,platform:Mac OS X,";
        assert glfwUpdateGamepadMappings(ByteBuffer.wrap(ps4ControllerGamepadMapping.getBytes(StandardCharsets.UTF_8)));
        final String xboxOneControllerGamepadMapping = "030000005e040000d102000000000000,Xbox One Wired Controller,a:b0,b:b1,back:b9,dpdown:b12,dpleft:b13,dpright:b14,dpup:b11,guide:b10,leftshoulder:b4,leftstick:b6,lefttrigger:a2,leftx:a0,lefty:a1,rightshoulder:b5,rightstick:b7,righttrigger:a5,rightx:a3,righty:a4,start:b8,x:b2,y:b3,platform:Mac OS X,";
        assert glfwUpdateGamepadMappings(ByteBuffer.wrap(xboxOneControllerGamepadMapping.getBytes(StandardCharsets.UTF_8)));
    }

    private void updateConnectedControllers(int jid, int event) {
        if (event == GLFW_CONNECTED) {
            addController(jid);
            final boolean isGamepad = glfwJoystickIsGamepad(jid);
            final String joystickGUID = glfwGetJoystickGUID(jid);
            final String joystickName = glfwGetJoystickName(jid);
            final String s = joystickGUID + ' ' + isGamepad + ' ' + joystickName;
            LOGGER.debug(s);
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
            LOGGER.debug("Added");
        }
    }

    private void removeController(int jid) {
        if (controllersAdded.getOrDefault(jid, false)) {
            controllers.remove(jid);
            controllersAdded.put(jid, false);
            LOGGER.debug("Removed");
        }
    }

    void detectControllersStates() {
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
            final int buttonState = joystickButtons.get();
            if (buttonState == GLFW_RELEASE) {
                if (previousState.getOrDefault(buttonIndex, GLFW_RELEASE) == GLFW_PRESS) {
                    doSomething(buttonIndex);
                } else {
                    final Xbox360ControllerButton controllerButton = Xbox360ControllerButton.valueOf(buttonIndex);
                    if (controllerButton.equals(Xbox360ControllerButton.DPAD_LEFT)) {
                        image.x += movementSpeed;
                    } else if (controllerButton.equals(Xbox360ControllerButton.DPAD_RIGHT)) {
                        image.x -= movementSpeed;
                    } else if (controllerButton.equals(Xbox360ControllerButton.DPAD_UP)) {
                        image.y += movementSpeed;
                    } else if (controllerButton.equals(Xbox360ControllerButton.DPAD_DOWN)) {
                        image.y -= movementSpeed;
                    }
                }
            }
            previousState.put(buttonIndex, buttonState);
        }
        final FloatBuffer joystickAxes = glfwGetJoystickAxes(jid);
        assert joystickAxes.limit() == 6;
        while (joystickAxes.hasRemaining()) {
            final int axisPosition = joystickAxes.position();
            final float axisState = joystickAxes.get();
                final Xbox360ControllerAxis controllerAxis = Xbox360ControllerAxis.valueOf(axisPosition);
                final boolean isAxisLeftTrigger = controllerAxis.equals(Xbox360ControllerAxis.LEFT_TRIGGER);
                final boolean isAxisRightTrigger = controllerAxis.equals(Xbox360ControllerAxis.RIGHT_TRIGGER);
                final boolean isAxisTrigger = isAxisLeftTrigger || isAxisRightTrigger;
                if (isAxisTrigger) {
                    if (Float.compare(axisState, 1.0f) == 0) {
//                        LOGGER.debug(controllerAxis + " fully pressed");
                        if(isAxisLeftTrigger) {
                            image.angle -= rotationSpeed;
                        } else {
                            image.angle += rotationSpeed;
                        }
                    }
                } else {
                    final boolean isAxisLeftX = controllerAxis.equals(Xbox360ControllerAxis.LEFT_X);
                    final boolean isAxisLeftY = controllerAxis.equals(Xbox360ControllerAxis.LEFT_Y);
                    final boolean isAxisLeft = isAxisLeftX || isAxisLeftY;
//                        final boolean isAxisRightX = controllerAxis.equals(Xbox360ControllerAxis.RIGHT_X);
//                        final boolean isAxisRightY = controllerAxis.equals(Xbox360ControllerAxis.RIGHT_Y);
//                        final boolean isAxisRight = isAxisRightX || isAxisRightY;
                    final boolean isAsixPastHalfway = axisState > 0.5f || axisState < -0.5f;
                    if (isAxisLeft) {
                        if (isAsixPastHalfway) {
                            LOGGER.debug("left stick moved"); // PS4
                        }
                    } else {
                        if (isAsixPastHalfway) {
                            LOGGER.debug("right stick moved"); //PS4?
                        }
                    }
                }
            }
    }

    private void doSomething(int buttonIndex) {
        final Xbox360ControllerButton controllerButton = Xbox360ControllerButton.valueOf(buttonIndex);
        if(controllerButton.equals(Xbox360ControllerButton.A)) {
            glClearColor(RGBA.GREEN);
            font.setText("GREEN");
        } else if(controllerButton.equals(Xbox360ControllerButton.B)) {
            glClearColor(RGBA.RED);
            font.setText("RED");
        } else if(controllerButton.equals(Xbox360ControllerButton.Y)) {
            glClearColor(RGBA.YELLOW);
            font.setText("YELLOW");
        } else if(controllerButton.equals(Xbox360ControllerButton.X)) {
            glClearColor(RGBA.BLUE);
            font.setText("BLUE");
        } else if (controllerButton.equals(Xbox360ControllerButton.BACK)) {
            font.setText("BYE");
            glfwSetWindowShouldClose(window, true);
        } else if (controllerButton.equals(Xbox360ControllerButton.LEFT_BUMPER) ||
                controllerButton.equals(Xbox360ControllerButton.RIGHT_BUMPER)) {
            font.setLineBoundingBoxEnabled(!font.getLineBoundingBoxEnabled());
            image.lineBoundingBoxEnabled = !image.lineBoundingBoxEnabled;
        } else if (controllerButton.equals(Xbox360ControllerButton.GUIDE)) {
            fullscreen = !fullscreen;
            if(fullscreen) {
                long monitor = glfwGetPrimaryMonitor();
                GLFWVidMode vidmode = glfwGetVideoMode(monitor);
                if (glfwGetWindowMonitor(window) == NULL) {
                    try (MemoryStack s = stackPush()) {
                        IntBuffer a = s.ints(0);
                        IntBuffer b = s.ints(0);

                        glfwGetWindowPos(window, a, b);
                        xpos = a.get(0);
                        ypos = b.get(0);

                        glfwGetWindowSize(window, a, b);
                        width = a.get(0);
                        height = b.get(0);
                    }
                    glfwSetWindowMonitor(window, monitor, 0, 0, vidmode.width(), vidmode.height(), vidmode.refreshRate());
                    glfwSwapInterval(1);
                }
            } else {
                if (glfwGetWindowMonitor(window) != NULL) {
                    glfwSetWindowMonitor(window, NULL, xpos, ypos, width, height, 0);
                }
            }
        }
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

}
