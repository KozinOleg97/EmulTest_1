package nes.emulator.console;

import nes.emulator.display.SimpleGraphics;

import java.awt.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

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

    public byte[] PPUMemory;
    public byte[] OAM;   //separate address space

    private static class OAM2Struct {
        public static Byte[] mem;
        public static Byte[] xCounters;

        public static Integer index;

        OAM2Struct() {
            mem = new Byte[4 * 8];
            xCounters = new Byte[8];
            index = 0;
        }

    }

    OAM2Struct OAM2 = new OAM2Struct();

    private boolean flagSizeOfSprite = true;
    private boolean flagTableOfSprites = true;

    private Integer activeSprite = -1;
    Palette palette0 = new Palette(Color.GRAY, Color.red, Color.BLUE, Color.ORANGE);
    Palette palette1 = new Palette(Color.BLACK, Color.WHITE, Color.DARK_GRAY, Color.GRAY);


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


        OAM2.xCounters[OAM2.index / 4] = OAM[spriteNumb + 3];

        for (int i = 0; i < 4; i++) {
            OAM2.mem[OAM2.index++] = OAM[spriteNumb + i];
        }

    }


    private void fillOAMM2(Integer line) { //заполняет oam2 c проверкой на перебор спрайтов в линии
        OAM2.index = 0;
        Byte sizeOfSprite = 8;
        if (!flagSizeOfSprite) {
            sizeOfSprite = 16;
        }

        Integer nubm = 0;
        for (int i = 0; i < 256; i = i + 4) { //перебор ОАМ
            if ((line >= (OAM[i] & 0xFF)) && (line < (OAM[i] & 0xFF) + sizeOfSprite)) {
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

    Integer x=0, y=0;
    Integer oddFramePixel=0;


    public void Ketchup(long cycles)
    {
        for (int i=0; i<cycles; i++)
        {
            if(x<256 && y>0 && y<241)
                drawPixel(x, y);
            else if(x>=340)
            {
                x=0;
                if(y++>=261+oddFramePixel)
                {
                    y=0;
                    oddFramePixel = -1 - oddFramePixel;
                }
            } else x++;
        }
    }

    private void drawPixel(Integer curPixel, Integer curLine) {
        checkXPosition();
        if (activeSprite != -1) {
            Integer spriteColor = getActiveSpriteNextPixel(curPixel, curLine);

            if (spriteColor == 0) {                            // if transparent --> draw background pixel --> return
                Integer bgColor = getBackgroundPixel(curPixel, curLine);
                SimpleGraphics.INSTANCE.addPixel(curPixel, curLine, bgColor, palette1);

                decrementXPosition();
                return;
            }

            SimpleGraphics.INSTANCE.addPixel(curPixel, curLine, spriteColor, palette0);
        } else {                                            // if no sprite --> draw background pixel
            Integer bgColor = getBackgroundPixel(curPixel, curLine);
            SimpleGraphics.INSTANCE.addPixel(curPixel, curLine, bgColor, palette1);
        }
        decrementXPosition();
    }

    private Integer getBackgroundPixel(Integer curPixelOnScreen, Integer curScreenLine) {
        //TODO add func to calculate current BG pixel address (+need some scrolling attribute)
        //8192 - NameTable1 index
        //1024 - NameTable size

        return 0;
    }

    private Integer getActiveSpriteNextPixel(Integer curPixelOnScreen, Integer curScreenLine) {

        Integer curSpriteLine = null;

        curSpriteLine = curScreenLine - (OAM2.mem[(activeSprite * 4)] & 0xFF);

        Integer spriteX = (curPixelOnScreen - OAM2.mem[(activeSprite * 4) + 3] & 0xFF);

        Integer addr = OAM2.mem[(activeSprite * 4) + 1] & 0xFF;

        if (flagSizeOfSprite) {   // if 8x8
            addr *= 16;
            addr += curSpriteLine;
            addr += flagTableOfSprites == false ? 0 : 4096;
        } else {//if 8x16
            Integer addr1High = addr & ~0x01;
            Integer addr1Low = addr & ~0xfe;
            if (curSpriteLine >= 8) addr1High |= 1;
            addr1High *= 16;
            addr1High += curSpriteLine;

            addr = addr1Low * 4096 + addr1High;
        }


        Integer bit1 = PPUMemory[addr] & 0xFF;
        Integer bit2 = PPUMemory[addr + 8] & 0xFF;

        bit1 = (bit1 >> (7 - spriteX)) & 1;
        bit2 = (bit2 >> (7 - spriteX)) & 1;

        Integer resBit = bit1 + bit2 * 2;

        if (spriteX == 7 || curPixelOnScreen >= XSize - 1) {
            activeSprite = -1;
        }

        return resBit;

    }

    private void decrementXPosition() {
        for (int i = 0; i < OAM2.index; i = i + 4) {
            //&0xFF  - для преобразования знакового Bite в беззнаковый Integer

            OAM2.xCounters[i / 4]--;
        }
    }

    private void checkXPosition() {
        for (int i = 0; i < OAM2.index; i = i + 4) {

            if ((OAM2.xCounters[i / 4] & 0xFF) == 0) {
                activeSprite = i / 4; ////////////////????????????????? 0 1 2 3 нужны
            }
        }

    }
}
