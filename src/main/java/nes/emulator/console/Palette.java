package nes.emulator.console;

import java.awt.*;

public class Palette {
    Color[] colors = new Color[4];

    Palette(Color col0, Color col1, Color col2, Color col3) {
        colors[0] = col0;
        colors[1] = col1;
        colors[2] = col2;
        colors[3] = col3;
    }

    public Color getColor(Integer ind) {
        return colors[ind];
    }
}

