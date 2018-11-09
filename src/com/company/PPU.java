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

    //256x240

    private Byte[] PPUMemory;
    public Byte[] OAM;   //separate address space
    public Byte[] OAM2;

    private Byte[] paletteSprite, paletteBackground;


    private boolean flagSizeOfSprite = true;

    private Byte OAM2Index = 0;

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

    }


    private void chooseSpritesToDraw(Byte line) { //заполняет oam2
        Byte sizeOfSprite = 8;
        if (!flagSizeOfSprite) {
            sizeOfSprite = 16;
        }

        Integer nubm = 0;
        for (int i = 0; i < 256; i = i + 4) {
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

    private void drawPixel() {

    }


    private Byte calcColor(int n, int x, int y) {


        return null;
    }


}
