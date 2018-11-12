package com.company;

import java.awt.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public enum PPU {
    INSTANCE;

    //$2000-$2007	$0008	NES PPU registers
    //$2008-$3FFF	$1FF8	Mirrors of $2000-2007 (repeats every 8 bytes)
    //$4000-$4017	$0018	NES APU and I/O registers
////////////////////PPU mem map///////////////////////////////////
    //$0000-$0FFF	$1000	Pattern table 0 CHR
    //$1000-$1FFF	$1000	Pattern Table 1 CHR

    //$2000-$23FF	$0400	Nametable 0  //NES
    //$2400-$27FF	$0400	Nametable 1  //NES

    //$2800-$2BFF	$0400	Nametable 2  // add
    //$2C00-$2FFF	$0400	Nametable 3  // add

    //$3000-$3EFF	$0F00	Mirrors of $2000-$2EFF
    //$3F00-$3F1F	$0020	Palette RAM indexes
    //$3F00-$3F0F фон
    //$3F10-$3F1F Спрайты
    //$3F20-$3FFF	$00E0	Mirrors of $3F00-$3F1F

    private Integer XSize = 256;    //256x240
    private Integer YSize = 240;

    private byte[] PPUMemory;
    public byte[] OAM;   //separate address space
    public Byte[] OAM2;

    private Byte[] paletteSprite, paletteBackground;


    private boolean flagSizeOfSprite = true;

    private Integer OAM2Index = 0;

    private Integer activSprite = -1;

    private Integer curLine = 0;

    private Integer curSpritePixelByX = 0;


    Palette palette0 = new Palette(Color.GRAY, Color.red, Color.BLUE, Color.ORANGE);


    PPU() {
        PPUMemory = new byte[64 * 256]; //16 Kb 16384 Byte

        try {
            SecureRandom.getInstanceStrong().nextBytes((byte[]) PPUMemory);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 64 * 256; i++) {
            PPUMemory[i] = (byte)(PPUMemory[i]&0x7F);//(byte) Math.abs((int) PPUMemory[i]);
        }


        OAM = new byte[256]; //256 Byte; 4 Byte for sprite; sprites 8x8 or 8x16

        try {
            SecureRandom.getInstanceStrong().nextBytes((byte[]) OAM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 256; i++) {
            OAM[i] = (byte)(OAM[i]&0x7F);//(byte) Math.abs((int) OAM[i]);
        }

        OAM2 = new Byte[4 * 8];

        paletteSprite = new Byte[16];
        paletteBackground = new Byte[16];

    }


    private void loadSpriteToOAM2(int spriteNumb) { // 1 спрайт из OAM в OAM2


        for (int i = 0; i < 4; i++) {
            OAM2[OAM2Index++] = OAM[spriteNumb + i];
        }
    }


    private void fillOAMM2(Byte line) { //заполняет oam2 c проверкой на перебор спрайтов в линии
        OAM2Index = 0;
        Byte sizeOfSprite = 8;
        if (!flagSizeOfSprite) {
            sizeOfSprite = 16;
        }

        Integer nubm = 0;
        for (int i = 0; i < 256; i = i + 4) { //перебор ОАМ
            if (line >= OAM[i] && line < OAM[i] + sizeOfSprite) {
                nubm++;
                if (nubm < 8) {
                    loadSpriteToOAM2(i);

                } else if (nubm == 8) {
                    ///TODO set overflow flag
                }

            }

        }

    }

    public void drawScreen() {
        for (int i = 0; i < YSize; i++) {
            fillOAMM2((byte) i);
            drawLine(i);
        }
    }

    private void drawLine(Integer curLine) {

        activSprite=-1;
        for (int i = 0; i < XSize; i++) {
            drawPixel(i, curLine);
        }
    }

    private void drawPixel(Integer curPixel, Integer curLine) {

        decrementXPosition();
        if (activSprite != -1) {
            Integer collorIndex = getActiveSpriteNextPixel(curLine);
            //Integer collor = calcPixelColor(px);

            if(curSpritePixelByX>7) activSprite=-1;
            SimpleGraphics.INSTANCE.addPixel(curPixel, curLine, collorIndex, palette0);
        }

    }

    private Integer getActiveSpriteNextPixel(Integer curScreenLine) {//TODO сделать для спрайтов 8х16

        Integer curSpriteLine = curScreenLine - OAM2[(activSprite * 4) ];

        Integer bit1 = null;

        try {
            bit1 = PPUMemory[OAM2[(activSprite * 4) + 1] * 16 + curSpriteLine] & 0xFF;
        } catch (Exception e) {
            System.out.println("aaaaaaa");
        }

        Integer bit2 = PPUMemory[OAM2[(activSprite * 4) + 1] * 16 + 8 + curScreenLine] & 0xFF;

        bit1 = (bit1 >> (7 - curSpritePixelByX)) & 1;
        bit2 = (bit2 >> (7 - curSpritePixelByX)) & 1;

        // Integer resBit = bit1 + bit2*2;
        if (bit2 != 0) {
            bit2 = 2;
        } else {
            bit2 = 0;
        }
        if (bit1 != 0) {
            bit1 = 1;
        } else {
            bit1 = 0;
        }
        Integer resBit = bit1 + bit2;

        curSpritePixelByX++;
        return resBit;

    }

    private void decrementXPosition() {
        for (int i = 0; i < OAM2Index; i = i + 4) {
            //&0xFF  - для преобразования знакового Bite в беззнаковый Integer


            // int q = (int) OAM2[i + 3] & 0xFF;

            if (( --OAM2[i + 3] & 0xFF) == 0) {
                activSprite = i / 4; ////////////////????????????????? 0 1 2 3 нужны
                curSpritePixelByX = 0;
            } else {
                //OAM2[i + 3]--;
            }


        }
    }





}
