package com.company;


public class Proc {


    private Byte regA;
    private Byte regX;
    private Byte regY;
    private Short regPC; // Был word
    private Byte regS;


    private class regP {
        private Byte C; //:1; // carry
        private Byte Z; //:1; // zero
        private Byte I; //:1; // interrupt 0==enabled
        private Byte D; //:1; // decimal mode
        private Byte B; //:1; // currently in break(BRK) interrupt
        private Byte NU; //:1; // always 1
        private Byte V; //:1; // oVerflow
        private Byte N; //:1; // negative(?)
    }




    //private Byte NESMem[][] = new Byte[64][256];


    Byte takeoper(Byte[] opCodes, Byte addrmode, Byte opAddr) {
	/*
		000   ind,x
		001   zp
		010   immed
		011   abs
		100   ind,y
		101   zp,x
		110   abs,y
		111   abs,x
	*/


        Byte operlo = 0;
        switch (addrmode) {
            case 0:
                opAddr =&NESMem[0][255 & (regX + opCodes[regPC++])];
                operlo =*opAddr;
                break;
            case 1:
                opAddr =&NESMem[0][opCodes[regPC++]];
                operlo =*opAddr;
                break;
            case 2:
                opAddr = (Byte *) & opCodes[regPC++];
                operlo =*opAddr;
                break;
            case 3:
                opAddr =&NESMem[opCodes[regPC + 1]][opCodes[regPC]];
                operlo =*opAddr;
                regPC += 2;
                break;
            case 4: {
                WORD addr;
                addr =*((WORD *) & NESMem[0][opCodes[regPC++]]) + regY;
                Byte addrlo = addr >> 8, addrhi = addr & 255;
                opAddr =&NESMem[addrhi][addrlo];
                operlo =*opAddr;
            }
            break;
            case 5:
                opAddr =&NESMem[0][255 & (opCodes[regPC] + (opCodes[regPC - 1] == 0x96 || opCodes[regPC - 1] == 0xB6 ? regY : regX))];
                regPC++;
                operlo =*opAddr;
                break;
            case 6: {
                WORD addr = regY +*(WORD *) & NESMem[opCodes[regPC + 1]][opCodes[regPC]];
                regPC += 2;
                Byte addrlo = addr >> 8, addrhi = addr & 255;
                opAddr =&NESMem[addrhi][addrlo];
                operlo =*opAddr;
            }
            break;
            case 7: {
                WORD addr = regX +*(WORD *) & NESMem[opCodes[regPC + 1]][opCodes[regPC]];
                regPC += 2;
                Byte addrlo = addr >> 8, addrhi = addr & 255;
                opAddr =&NESMem[addrhi][addrlo];
                operlo =*opAddr;
            }
            break;
        }
        return operlo;
    }


}
