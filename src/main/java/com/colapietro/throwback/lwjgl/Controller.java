package com.colapietro.throwback.lwjgl;

/**
 * @author Peter Colapietro.
 */
public enum Controller {
    XBOX_360("Xbox 360 Wired Controller", new String[]{"030000005e0400008e02000000000000"}, 15),
    PS4("Wireless Controller", new String[]{"030000004c050000c405000000010000"}, 18);

    final String name;
    final String[] guids;
    final int buttonLimit;

    private Controller(String name, String[] guids, int buttonLimit) {
        this.name = name;
        this.guids = guids;
        this.buttonLimit = buttonLimit;
    }
}
