package com.company;

public class PPU {


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

    private Byte[] PPUMemory;
    public Byte[] OAM;   //separate address space
    public Byte[] OAM2;

    private Byte[] paletteSprite, paletteBackground;


    private boolean flagSizeOfSprite = true;

    private Integer OAM2Index = 0;

    private Integer activSprite = -1;

    private Integer curLine = 0;

    PPU() {
        PPUMemory = new Byte[64 * 256]; //16 Kb 16384 Byte
        OAM = new Byte[256]; //256 Byte; 4 Byte for sprite; sprites 8x8 or 8x16
        OAM2 = new Byte[4 * 8];

        paletteSprite = new Byte[16];
        paletteBackground = new Byte[16];

    }


    private void loadSpriteToOAM2(int spriteNumb) { // 1 спрайт из OAM в OAM2


        for (int i = 0; i < 4; i++) {
            OAM2[OAM2Index + i] = OAM[spriteNumb + i];
        }

        OAM2Index++;// +4))))
        OAM2Index++;
        OAM2Index++;
        OAM2Index++;

    }


    private void fillOAMM2(Byte line) { //заполняет oam2 c проверкой на перебор спрайтов в линии
        Byte sizeOfSprite = 8;
        if (!flagSizeOfSprite) {
            sizeOfSprite = 16;
        }

        Integer nubm = 0;
        for (int i = 0; i < 256; i = i + 4) { //перебор ОАМ
            if (line - OAM[i] < sizeOfSprite) {
                nubm++;
                if (nubm < 9) {
                    loadSpriteToOAM2(i);
                } else if (nubm == 9) {
                    ///TODO set overflow flag
                }

            }

        }

    }

    private void drawScreen() {
        for (int i = 0; i < YSize; i++) {
            drawLine(i);
        }
    }

    private void drawLine(Integer curLine) {

        for (int i = 0; i < XSize; i++) {
            drawPixel(i, curLine);
        }
    }

    private void drawPixel(Integer curPixel, Integer curLine) {

        decrementXPosition();
        if (activSprite != -1) {
            Integer px = getActiveSpriteNextPixel(curLine);
            Integer collor = calcPixelColor(px);

            //  drawPPixel()//jframe
        }

    }

    private Integer getActiveSpriteNextPixel(Integer curScreenLine) {//TODO сделать для спрайтов 8х16

        Integer curSpriteLine = curScreenLine - OAM2[(activSprite * 4) + 3];

        Integer bit1 = PPUMemory[OAM2[(activSprite * 4) + 1] * 16] >> 7 & 1;
        Integer bit2 = PPUMemory[OAM2[(activSprite * 4) + 1] * 16 + 8] >> 7 & 1;
        Integer resBit = bit1 | bit2 << 1;
        return resBit;

    }

    private void decrementXPosition() {
        for (int i = 0; i < 32; i = i + 4) {
            //&0xFF  - для преобразования знакового Bite в беззнаковый Integer
            if (((int) OAM2[i + 3] & 0xFF) == 0) {
                activSprite = i / 4; ////////////////????????????????? 0 1 2 3 нужны

            } else {
                OAM2[i]--;
            }
        }
    }


    private Integer calcPixelColor(Integer curPx) {


        return null;
    }


}
