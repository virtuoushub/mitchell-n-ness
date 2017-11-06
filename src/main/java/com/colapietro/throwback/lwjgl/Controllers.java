package com.colapietro.throwback.lwjgl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
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
import static org.lwjgl.glfw.GLFW.glfwGetJoystickAxes;
import static org.lwjgl.glfw.GLFW.glfwGetJoystickButtons;
import static org.lwjgl.glfw.GLFW.glfwGetJoystickGUID;
import static org.lwjgl.glfw.GLFW.glfwGetJoystickName;
import static org.lwjgl.glfw.GLFW.glfwJoystickIsGamepad;
import static org.lwjgl.glfw.GLFW.glfwJoystickPresent;
import static org.lwjgl.glfw.GLFW.glfwSetJoystickCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwUpdateGamepadMappings;

/**
 * @author Peter Colapietro.
 */
public class Controllers {



    private static final int NUMBER_OF_SUPPORTED_GLFW_JOYSTICKS = GLFW_JOYSTICK_LAST + 1;
    private Set<Integer> controllers;
    private Map<Integer, Boolean> controllersAdded;
    private final long window;
    private final Font font;

    public Controllers(long window, Font font) {
        this.window = window;
        this.font = font;
    }

    void initControllers() {
        updatePS4ControllerGamepadMapping();
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
        final String string = "030000004c050000c405000000010000,PS4 Controller,a:b1,b:b2,back:b8,dpdown:h0.4,dpleft:h0.8,dpright:h0.2,dpup:h0.1,guide:b12,leftshoulder:b4,leftstick:b10,lefttrigger:a3,leftx:a0,lefty:a1,rightshoulder:b5,rightstick:b11,righttrigger:a4,rightx:a2,righty:a5,start:b9,x:b0,y:b3,platform:Mac OS X,";
        assert glfwUpdateGamepadMappings(string);
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
        } else if (controllerButton.equals(Xbox360ControllerButton.LEFT_BUMPER)) {
            font.setKerningEnabled(!font.getKerningEnabled());
        } else if (controllerButton.equals(Xbox360ControllerButton.RIGHT_BUMPER)) {
            font.setLineBoundingBoxEnabled(!font.getLineBoundingBoxEnabled());
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
