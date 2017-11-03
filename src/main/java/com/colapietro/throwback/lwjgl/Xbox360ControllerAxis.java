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
public enum Xbox360ControllerAxis {


    // 0 == left stick x
    // 1 == left stick y
    // 2 == left trigger
    // 3 == right stick x
    // 4 == right stick y
    // 5 == right trigger

    /**
     *
     */
    LEFT_X(0),
    /**
     *
     */
    LEFT_Y(1),
    /**
     *
     */
    LEFT_TRIGGER(2),
    /**
     *
     */
    RIGHT_X(3),
    /**
     *
     */
    RIGHT_Y(4),
    /**
     *
     */
    RIGHT_TRIGGER(5);

    /**
     *
     */
    private final int axisIndex;

    static final Controller controller = Xbox360ControllerButton.controller;

    /**
     *
     */
    private static final Map<Integer, Xbox360ControllerAxis> MAP = new HashMap<>();

    /**
     *
     */
    private static final Map<Xbox360ControllerAxis, Integer> REVERSE_MAP =
            Collections.synchronizedMap(new EnumMap<>(Xbox360ControllerAxis.class));


    static {
        for (Xbox360ControllerAxis controllerEvenIndexEnum : Xbox360ControllerAxis.values()) {
            MAP.put(
                    controllerEvenIndexEnum.getAxisIndex(),
                    controllerEvenIndexEnum
            );
            REVERSE_MAP.put(
                    controllerEvenIndexEnum,
                    controllerEvenIndexEnum.getAxisIndex()
            );
        }
    }

    /**
     *
     * @param axisIndex axisIndex
     */
    private Xbox360ControllerAxis(int axisIndex) {
        this.axisIndex = axisIndex;
    }

    /**
     *
     * @return axisIndex
     */
    private int getAxisIndex() {
        return axisIndex;
    }

    /**
     *
     * @param controlIndex axisIndex
     * @return Xbox360ControllerButton
     */
    public static Xbox360ControllerAxis valueOf(int controlIndex) {
        return MAP.get(controlIndex);
    }

    /**
     *
     * @param controlIndex axisIndex
     * @param isOffByOne isOffByOne
     * @return Xbox360ControllerButton
     */
    public static Xbox360ControllerAxis valueOf(int controlIndex, boolean isOffByOne) {
        if(isOffByOne) {
            return MAP.get(controlIndex - 1);
        } else {
            return valueOf(controlIndex);
        }
    }

    /**
     *
     * @param xbox360ControllerButton xbox360ControllerButton
     * @return axisIndex
     */
    public static int valueOf(Xbox360ControllerAxis xbox360ControllerButton) {
        return REVERSE_MAP.get(xbox360ControllerButton);
    }

    /**
     *
     * @param xbox360ControllerButton xbox360ControllerButton
     * @param isOffByOne isOffByOne
     * @return axisIndex
     */
    public static int valueOf(Xbox360ControllerAxis xbox360ControllerButton, boolean isOffByOne) {
        if(isOffByOne) {
            return REVERSE_MAP.get(xbox360ControllerButton) + 1;
        } else {
            return valueOf(xbox360ControllerButton);
        }
    }

}
