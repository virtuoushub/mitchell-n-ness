package com.colapietro.throwback.lwjgl;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Peter Colapietro on 11/23/14.
 *
 * @author Peter Colapietro
 * @since 0.1.8
 */
public enum Xbox360ControllerButton {

    /**
     *
     */
    A(0),
    /**
     *
     */
    B(1),
    /**
     *
     */
    X(2),
    /**
     *
     */
    Y(3),
    /**
     *
     */
    LEFT_BUMPER(4),
    /**
     *
     */
    RIGHT_BUMPER(5),
    /**
     *
     */
    L3(6),
    /**
     *
     */
    R3(7),
    /**
     *
     */
    START(8),
    /**
     *
     */
    BACK(9),
    /**
     *
     */
    GUIDE(10),
    /**
     *
     */
    DPAD_UP(11),
    /**
     *
     */
    DPAD_DOWN(12),
    /**
     *
     */
    DPAD_LEFT(13),
    /**
     *
     */
    DPAD_RIGHT(14);

    /**
     *
     */
    private final int controlIndex;

    static final Controller controller = Controller.XBOX_360;

    /**
     *
     */
    private static final Map<Integer, Xbox360ControllerButton> MAP = new HashMap<>();

    /**
     *
     */
    private static final Map<Xbox360ControllerButton, Integer> REVERSE_MAP =
            Collections.synchronizedMap(new EnumMap<>(Xbox360ControllerButton.class));


    static {
        for (Xbox360ControllerButton controllerEvenIndexEnum : Xbox360ControllerButton.values()) {
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
    private Xbox360ControllerButton(int controlIndex) {
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
    public static Xbox360ControllerButton valueOf(int controlIndex) {
        return MAP.get(controlIndex);
    }

    /**
     *
     * @param controlIndex controlIndex
     * @param isOffByOne isOffByOne
     * @return Xbox360ControllerButton
     */
    public static Xbox360ControllerButton valueOf(int controlIndex, boolean isOffByOne) {
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
    public static int valueOf(Xbox360ControllerButton xbox360ControllerButton) {
        return REVERSE_MAP.get(xbox360ControllerButton);
    }

    /**
     *
     * @param xbox360ControllerButton xbox360ControllerButton
     * @param isOffByOne isOffByOne
     * @return controlIndex
     */
    public static int valueOf(Xbox360ControllerButton xbox360ControllerButton, boolean isOffByOne) {
        if(isOffByOne) {
            return REVERSE_MAP.get(xbox360ControllerButton) + 1;
        } else {
            return valueOf(xbox360ControllerButton);
        }
    }

}
