package com.modules;

import java.awt.Color;

public final class AppColors {

    public static final Color PRIMARY_DARK = new Color(0x13, 0x29, 0x3D);
    public static final Color PRIMARY = new Color(0x00, 0x64, 0x94);
    public static final Color PRIMARY_HOVER = new Color(0x24, 0x7B, 0xA0);
    public static final Color PRIMARY_LIGHT = new Color(0x1B, 0x98, 0xE0);
    public static final Color PRIMARY_SUBTLE = new Color(0xE8, 0xF1, 0xF2);

    public static final Color SUCCESS_DARK = new Color(0x0F, 0x76, 0x3D);
    public static final Color SUCCESS = new Color(0x16, 0xA3, 0x4A);
    public static final Color SUCCESS_LIGHT = new Color(0xDC, 0xFC, 0xE7);

    public static final Color WARNING_DARK = new Color(0x92, 0x60, 0x10);
    public static final Color WARNING = new Color(0xF5, 0x9E, 0x0B);
    public static final Color WARNING_LIGHT = new Color(0xFE, 0xF3, 0xC7);

    public static final Color ERROR_DARK = new Color(0xB9, 0x1C, 0x1C);
    public static final Color ERROR = new Color(0xDC, 0x26, 0x26);
    public static final Color ERROR_LIGHT = new Color(0xFE, 0xE2, 0xE2);


    public static final Color BACKGROUND = new Color(0xF8, 0xFA, 0xFC);
    public static final Color SURFACE = Color.WHITE;

    public static final Color TEXT_PRIMARY = new Color(0x0F, 0x17, 0x2A);
    public static final Color TEXT_SECONDARY = new Color(0x47, 0x55, 0x69);

    public static final Color BORDER = new Color(0xDE, 0xE3, 0xE8);

    public static final Color ACTION_SOFT_BG = new Color(0xE8, 0xF1, 0xF2);
    public static final Color ACTION_SOFT_HOVER_BG = new Color(0xD6, 0xEC, 0xF7);
    public static final Color ACTION_SOFT_FG = PRIMARY;
    public static final Color ACTION_SOFT_BORDER = new Color(0xB9, 0xDD, 0xED);
    public static final Color ACTION_DANGER_SOFT_BG = ERROR_LIGHT;
    public static final Color ACTION_DANGER_SOFT_FG = ERROR_DARK;
    public static final Color ACTION_DANGER_SOFT_HOVER_BG = new Color(0xFD, 0xD4, 0xD4);

    public static final Color SEAT_HARD_FILL = new Color(0xFF, 0xD6, 0xA5);
    public static final Color SEAT_HARD_BORDER = new Color(0xFB, 0x92, 0x24);
    public static final Color SEAT_SOFT_FILL = new Color(0xA0, 0xC4, 0xFF);
    public static final Color SEAT_BED_FILL = new Color(0xCA, 0xFF, 0xBF);

    public static final Color SEAT_AVAILABLE_BORDER = new Color(0xBD, 0xB2, 0xFF);
    public static final Color SEAT_UNAVAILABLE_FILL = new Color(0xFD, 0xFF, 0xB6);
    public static final Color SEAT_UNAVAILABLE_BORDER = new Color(0xFF, 0xAD, 0xAD);
    public static final Color SEAT_SELECTED_FILL = PRIMARY;
    public static final Color SEAT_SELECTED_BORDER = PRIMARY_HOVER;

    public static final Color SHADOW_LIGHT = new Color(0, 0, 0, 18);
    public static final Color SHADOW_MEDIUM = new Color(0, 0, 0, 30);

    public static Color primaryAlpha(int alpha) {
        return withAlpha(PRIMARY_DARK, alpha);
    }

    public static Color errorAlpha(int alpha) {
        return withAlpha(ERROR_DARK, alpha);
    }

    public static Color surfaceAlpha(int alpha) {
        return withAlpha(SURFACE, alpha);
    }

    public static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    private AppColors() {}
}
