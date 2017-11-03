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
public enum PS4ControllerAxis {


    // 0 lx
    // 1 ly
    // 2 rx
    // 3 lt | l2
    // 4 rt | r2
    // 5 ry

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
    RIGHT_X(2),
    /**
     * L2
     */
    LEFT_TRIGGER(3),
    /**
     * R2
     */
    RIGHT_TRIGGER(4),
    /**
     *
     */
    RIGHT_Y(5);

    /**
     *
     */
    private final int axisIndex;

    static final Controller controller = PS4ControllerButton.controller;

    /**
     *
     */
    private static final Map<Integer, PS4ControllerAxis> MAP = new HashMap<>();

    /**
     *
     */
    private static final Map<PS4ControllerAxis, Integer> REVERSE_MAP =
            Collections.synchronizedMap(new EnumMap<>(PS4ControllerAxis.class));


    static {
        for (PS4ControllerAxis controllerEvenIndexEnum : PS4ControllerAxis.values()) {
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
    private PS4ControllerAxis(int axisIndex) {
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
    public static PS4ControllerAxis valueOf(int controlIndex) {
        return MAP.get(controlIndex);
    }

    /**
     *
     * @param controlIndex axisIndex
     * @param isOffByOne isOffByOne
     * @return Xbox360ControllerButton
     */
    public static PS4ControllerAxis valueOf(int controlIndex, boolean isOffByOne) {
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
    public static int valueOf(PS4ControllerAxis xbox360ControllerButton) {
        return REVERSE_MAP.get(xbox360ControllerButton);
    }

    /**
     *
     * @param xbox360ControllerButton xbox360ControllerButton
     * @param isOffByOne isOffByOne
     * @return axisIndex
     */
    public static int valueOf(PS4ControllerAxis xbox360ControllerButton, boolean isOffByOne) {
        if(isOffByOne) {
            return REVERSE_MAP.get(xbox360ControllerButton) + 1;
        } else {
            return valueOf(xbox360ControllerButton);
        }
    }

}
