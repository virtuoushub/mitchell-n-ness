package com.colapietro.throwback.lwjgl;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Peter Colapietro
 * @since 0.1.0
 */
public enum PS4ControllerButton {

//    GLFW_GAMEPAD_BUTTON_A            = 0,
//    GLFW_GAMEPAD_BUTTON_B            = 1,
//    GLFW_GAMEPAD_BUTTON_X            = 2,
//    GLFW_GAMEPAD_BUTTON_Y            = 3,
//    GLFW_GAMEPAD_BUTTON_LEFT_BUMPER  = 4,
//    GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER = 5,
//    GLFW_GAMEPAD_BUTTON_BACK         = 6,
//    GLFW_GAMEPAD_BUTTON_START        = 7,
//    GLFW_GAMEPAD_BUTTON_GUIDE        = 8,
//    GLFW_GAMEPAD_BUTTON_LEFT_THUMB   = 9,
//    GLFW_GAMEPAD_BUTTON_RIGHT_THUMB  = 10,
//    GLFW_GAMEPAD_BUTTON_DPAD_UP      = 11,
//    GLFW_GAMEPAD_BUTTON_DPAD_RIGHT   = 12,
//    GLFW_GAMEPAD_BUTTON_DPAD_DOWN    = 13,
//    GLFW_GAMEPAD_BUTTON_DPAD_LEFT    = 14,
//    GLFW_GAMEPAD_BUTTON_LAST         = GLFW_GAMEPAD_BUTTON_DPAD_LEFT,
//    GLFW_GAMEPAD_BUTTON_CROSS        = GLFW_GAMEPAD_BUTTON_A,
//    GLFW_GAMEPAD_BUTTON_CIRCLE       = GLFW_GAMEPAD_BUTTON_B,
//    GLFW_GAMEPAD_BUTTON_SQUARE       = GLFW_GAMEPAD_BUTTON_X,
//    GLFW_GAMEPAD_BUTTON_TRIANGLE     = GLFW_GAMEPAD_BUTTON_Y;

    /**
     *
     */
    SQUARE(0),
    /**
     *
     */
    CROSS(1),
    /**
     *
     */
    CIRCLE(2),
    /**
     *
     */
    TRIANGLE(3),
    /**
     *
     */
    LEFT_BUMPER(4), // L1
    /**
     *
     */
    RIGHT_BUMPER(5), // R2
    /**
     *
     */
    LEFT_TRIGGER(6), // L2
    /**
     *
     */
    RIGHT_TRIGGER(7), // R2
    /**
     * BACK
     */
    SHARE(8),
    /**
     * START
     */
    OPTIONS(9),
    /**
     *
     */
    L3(10),
    /**
     *
     */
    R3(11),
    /**
     * GUIDE
     */
    PS4(12),
    /**
     *
     */
    TOUCH_PAD(13),
    /**
     *
     */
    DPAD_UP(14),
    /**
     *
     */
    DPAD_RIGHT(14),
    /**
     *
     */
    DPAD_DOWN(16),
    /**
     *
     */
    DPAD_LEFT(17);

    /**
     *
     */
    private final int controlIndex;

    static final Controller controller = Controller.XBOX_360;

    /**
     *
     */
    private static final Map<Integer, PS4ControllerButton> MAP = new HashMap<>();

    /**
     *
     */
    private static final Map<PS4ControllerButton, Integer> REVERSE_MAP =
            Collections.synchronizedMap(new EnumMap<>(PS4ControllerButton.class));


    static {
        for (PS4ControllerButton controllerEvenIndexEnum : PS4ControllerButton.values()) {
            MAP.put(
                    controllerEvenIndexEnum.getControlIndex(),
                    controllerEvenIndexEnum
            );
            REVERSE_MAP.put(
                    controllerEvenIndexEnum,
                    controllerEvenIndexEnum.getControlIndex()
            );
        }
    }

    /**
     *
     * @param controlIndex controlIndex
     */
    private PS4ControllerButton(int controlIndex) {
        this.controlIndex = controlIndex;
    }

    /**
     *
     * @return controlIndex
     */
    private int getControlIndex() {
        return controlIndex;
    }

    /**
     *
     * @param controlIndex controlIndex
     * @return Xbox360ControllerButton
     */
    public static PS4ControllerButton valueOf(int controlIndex) {
        return MAP.get(controlIndex);
    }

    /**
     *
     * @param controlIndex controlIndex
     * @param isOffByOne isOffByOne
     * @return Xbox360ControllerButton
     */
    public static PS4ControllerButton valueOf(int controlIndex, boolean isOffByOne) {
        if(isOffByOne) {
            return MAP.get(controlIndex - 1);
        } else {
            return valueOf(controlIndex);
        }
    }

    /**
     *
     * @param xbox360ControllerButton xbox360ControllerButton
     * @return controlIndex
     */
    public static int valueOf(PS4ControllerButton xbox360ControllerButton) {
        return REVERSE_MAP.get(xbox360ControllerButton);
    }

    /**
     *
     * @param xbox360ControllerButton xbox360ControllerButton
     * @param isOffByOne isOffByOne
     * @return controlIndex
     */
    public static int valueOf(PS4ControllerButton xbox360ControllerButton, boolean isOffByOne) {
        if(isOffByOne) {
            return REVERSE_MAP.get(xbox360ControllerButton) + 1;
        } else {
            return valueOf(xbox360ControllerButton);
        }
    }

}
