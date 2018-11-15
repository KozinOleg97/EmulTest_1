package nes.emulator.console;

import nes.emulator.display.SimpleGraphics;

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
    public Byte[] OAM2XCounters;

    private boolean flagSizeOfSprite = true;
    private boolean flagTableOfSprites = true;

    private Integer OAM2Index = 0;

    private Integer activSprite = -1;
    Palette palette0 = new Palette(Color.GRAY, Color.red, Color.BLUE, Color.ORANGE);


    PPU() {
        PPUMemory = new byte[64 * 256]; //16 Kb 16384 Byte



        try {
            SecureRandom.getInstanceStrong().nextBytes((byte[]) PPUMemory);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        OAM = new byte[256]; //256 Byte; 4 Byte for sprite; sprites 8x8 or 8x16

        try {
            SecureRandom.getInstanceStrong().nextBytes((byte[]) OAM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        OAM2 = new Byte[4 * 8];
        OAM2XCounters = new Byte[8];


    }

    private void PPUMemRnd() {
        try {
            SecureRandom.getInstanceStrong().nextBytes((byte[]) PPUMemory);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }



        try {
            SecureRandom.getInstanceStrong().nextBytes((byte[]) OAM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


    }

    private void loadSpriteToOAM2(int spriteNumb) { // 1 спрайт из OAM в OAM2


        OAM2XCounters[OAM2Index / 4] = OAM[spriteNumb + 3];

        for (int i = 0; i < 4; i++) {
            OAM2[OAM2Index++] = OAM[spriteNumb + i];
        }


    }


    private void fillOAMM2(Integer line) { //заполняет oam2 c проверкой на перебор спрайтов в линии
        OAM2Index = 0;
        Byte sizeOfSprite = 8;
        if (!flagSizeOfSprite) {
            sizeOfSprite = 16;
        }

        Integer nubm = 0;
        for (int i = 0; i < 256; i = i + 4) { //перебор ОАМ
            if ((line >= (OAM[i]&0xFF)) && (line < (OAM[i]&0xFF) + sizeOfSprite)) {
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
        PPUMemRnd();
        for (int i = 0; i < YSize; i++) {
            fillOAMM2(i);
            drawLine(i);
        }
    }

    private void drawLine(Integer curLine) {


        for (int i = 0; i < XSize; i++) {
            drawPixel(i, curLine);
        }
    }

    private void drawPixel(Integer curPixel, Integer curLine) {
        checkXPosition();
        if (activSprite != -1) {
            Integer collorIndex = getActiveSpriteNextPixel(curPixel, curLine);

            SimpleGraphics.INSTANCE.addPixel(curPixel, curLine, collorIndex, palette0);
        }
        decrementXPosition();
    }

    private Integer getActiveSpriteNextPixel(Integer curPixelOnScreen, Integer curScreenLine) {

        Integer curSpriteLine = null;

        curSpriteLine = curScreenLine - (OAM2[(activSprite * 4)]&0xFF);

        Integer spriteX = (curPixelOnScreen - OAM2[(activSprite * 4) + 3] & 0xFF);

        Integer addr = OAM2[(activSprite * 4) + 1] & 0xFF;

        if (flagSizeOfSprite) {   // if 8x8
            addr*=16; addr+=curSpriteLine;
            addr += flagTableOfSprites == false ? 0 : 4096;
        } else {//if 8x16
            Integer addr1High = addr & ~0x01;
            Integer addr1Low = addr & ~0xfe;
            if(curSpriteLine>=8) addr1High |= 1;
            addr1High*=16; addr1High+=curSpriteLine;

            addr = addr1Low * 4096 + addr1High;
        }


        Integer bit1 = PPUMemory[addr] & 0xFF;
        Integer bit2 = PPUMemory[addr + 8] & 0xFF;

        bit1 = (bit1 >> (7 - spriteX)) & 1;
        bit2 = (bit2 >> (7 - spriteX)) & 1;

        Integer resBit = bit1 + bit2 * 2;

        if (spriteX == 7 || curPixelOnScreen>=XSize-1) {
            activSprite = -1;
        }

        return resBit;

    }

    private void decrementXPosition() {
        for (int i = 0; i < OAM2Index; i = i + 4) {
            //&0xFF  - для преобразования знакового Bite в беззнаковый Integer

            OAM2XCounters[i / 4]--;
        }
    }

    private void checkXPosition() {
        for (int i = 0; i < OAM2Index; i = i + 4) {

            if ((OAM2XCounters[i / 4] & 0xFF) == 0) {
                activSprite = i / 4; ////////////////????????????????? 0 1 2 3 нужны
            }
        }

    }
}
