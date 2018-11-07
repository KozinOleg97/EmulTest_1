package com.company;


//http://www.emuverse.ru/wiki/MOS_Technology_6502/Система_команд
//http://nparker.llx.com/a2/opcodes.html
//https://www.atariarchives.org/alp/appendix_1.php

import java.util.logging.Level;
import java.util.logging.Logger;

public class Proc {


    private byte regA;
    private byte regX;
    private byte regY;
    private short regPC;
    private byte regS;

    private Logger log;

        private byte C; //:1; // carry
        private byte Z; //:1; // zero
        private byte I; //:1; // interrupt 0==enabled
        private byte D; //:1; // decimal mode
        //private byte B; //:1; // currently in break(BRK) interrupt
        //private byte NU; //:1; // always 1
        private byte V; //:1; // oVerflow
        private byte N; //:1; // negative(?)


    public void setZ(byte z) {
        Z = (byte)(z==0?1:0);
    }

    public void setN(byte n) {
        N = (byte)((n&0x80)==0?0:1);
    }

    private Memory m;


    Proc(Memory mem)
    {
        regA = regX = regY = 0;
        regS = (byte)0xFF;
        regPC = 0;
        m=mem;
        log = Logger.getLogger("proc.java");
        log.setLevel(Level.ALL);
    }


    // Вход: При входе в эту функцию regPC указывает на первый байт операнда
    // addrmode - три бита, выдранные из инструкции
    // Выход: opAddr - адрес операнда
    // Результат: содержимое операнда
    short takeoperaddr(int addrmode) {
	/*
	    Для ZZ=01 и ZZ=11(недокументированные инструкции)

       000   ind,x
       001   zp
       010   immed
       011   abs
       100   ind,y
       101   zp,x
       110   abs,y
       111   abs,x
	*/
	/*
	    Для ZZ=10

		000   immed
		010   impl
		100   ИНВАЛИД/КРЕШ
		110   impl
	*/
	/*
	    Для ZZ=00

		000   immed
		010   impl
		100   rel(инструкции перехода)
		110   impl
	*/

        short opAddr = 0;
        Byte operlo = 0;
        switch (addrmode) {
            case 0:
                opAddr = (short)(255 & (regX + m.getMemAt(regPC++)));
                operlo = m.getMemAt(opAddr);
                break;
            case 1:
                opAddr = m.getMemAt(regPC++);
                operlo = m.getMemAt(opAddr);
                break;
            case 2:
                opAddr = regPC++;
                operlo = m.getMemAt(opAddr);
                break;
            case 3:
                opAddr = (short)(m.getMemAt(regPC)|m.getMemAt((short)(regPC+1))<<8);
                operlo = m.getMemAt(opAddr);
                regPC += 2;
                break;
            case 4:
                opAddr = (short)(m.getMemAtW(m.getMemAt(regPC++).shortValue()) + regY);
                operlo = m.getMemAt(opAddr);
                break;
            case 5:
                byte pc_i = m.getMemAt((short)(regPC - 1)), pc_0 = m.getMemAt(regPC);
                opAddr = m.getMemAt((short)(255 & (pc_0 + (pc_i == 0x96 || pc_i == 0xB6 ? regY : regX))));
                regPC++;
                operlo = m.getMemAt(opAddr);
                break;
            case 6:
                opAddr = (short)(regY + m.getMemAtW( (short)(m.getMemAt(regPC)|m.getMemAt((short)(regPC+1))<<8) ));
                regPC += 2;
                operlo = m.getMemAt(opAddr);
                break;
            case 7:
                pc_i = m.getMemAt((short)(regPC - 1));
                opAddr = (short)((pc_i==0xBE?regY:regX) + m.getMemAtW( (short)(m.getMemAt(regPC)|m.getMemAt((short)(regPC+1))<<8) ));
                regPC += 2;
                operlo = m.getMemAt(opAddr);
                break;
        }
        return opAddr;
    }


    void Step()
    {
        // LDA FF, TAX, INX, STA 1,X, LDY #0
        //char* opcodes = "\xA9\xFE\xAA\xE8\x95\x01\xA4\x00";
       
        byte command = m.getMemAt(regPC++);
        log.log(Level.INFO, String.format("command: %02x\n", command));

        byte oper = 0;
        Short opaddr = 0;

        // XXXYYYZZ
        // XXX: код команды
        // YYY: режим адресации
        // ZZ: класс команды

        int addrmode = (command >> 2) & 7;
        int comcode = (command >> 5) & 7;
        int comclass = command & 3;

        // interpret command's code to ascertain addressing mode
        switch (comclass) {
            case 2:
                //if ((addrmode & 1)==0) break;
                //commands either work as expected or halt/nop
            case 0:
                // сложнааааааа
                if (comcode == 0 || ((addrmode & 1)==0 && (comcode < 5 || addrmode!=0))) break;
                //if (comcode != 0 && ((addrmode & 1) || (comcode >= 5 && !addrmode))) {
            case 3:
                //undocumented
            case 1:
                log.log(Level.INFO, String.format("operand: %02x(+second byte %02x) ", m.getMemAt(regPC), m.getMemAt((short)(regPC + 1))));
                // TODO: как передавать opaddr как ссылку?????????
                opaddr = takeoperaddr(addrmode);
                oper = m.getMemAt(opaddr);
                log.log(Level.INFO, String.format("decoded: %02x", oper));
                break;
        }
        log.log(Level.INFO, "executing ");
        // exec command
        switch (comclass) {
            case 0:
                switch (comcode) {
                    case 0:
                        switch(addrmode)
                        {
                            case 2:
                                short stackaddr = (short)(0x100 + (regS&0xFF));
                                int P = C | (Z<<1) | (I<<2) | (D<<3) | (1<<5) | (V<<6) | (N<<7);
                                log.log(Level.INFO, String.format("PHP. P(%02x) => [S](%02x)", P, m.getMemAt(stackaddr)));
                                m.setMemAt(stackaddr, (byte)(P));
                                regS--;
                                break;
                            case 6:
                                log.log(Level.INFO, String.format("CLC. C(%02x)", C));
                                C=0;
                                break;
                        }
                        break;
                    case 1:
                        switch(addrmode)
                        {
                            case 1:
                            case 3:
                                log.log(Level.INFO, String.format("BIT. M(%02x) <&> A(%02x)", oper, regA));
                                int bitimterm = regA&oper&0xFF;
                                byte bitimtermb = (byte)bitimterm;
                                setZ(bitimtermb);
                                setN(bitimtermb);
                                V = (byte)((bitimterm&0x40)==0?0:1);
                                break;
                            case 2:
                                regS++;
                                short stackaddr = (short)(0x100 + (regS&0xFF));
                                int P = C | (Z<<1) | (I<<2) | (D<<3) | (1<<5) | (V<<6) | (N<<7);
                                byte NP = m.getMemAt(stackaddr);
                                log.log(Level.INFO, String.format("PLP. [S](%02x) => P(%02x)", NP, P));
                                C = (byte)(NP&1);
                                Z = (byte)(NP>>1&1);
                                I = (byte)(NP>>2&1);
                                D = (byte)(NP>>3&1);
                                V = (byte)(NP>>6&1);
                                N = (byte)(NP>>7&1);
                                break;
                            case 6:
                                log.log(Level.INFO, String.format("SEC. C(%02x)", C));
                                C=1;
                                break;
                        }
                        break;
                    case 2:
                        switch (addrmode)
                        {
                            case 2:
                                short stackaddr = (short)(0x100 + (regS&0xFF));
                                log.log(Level.INFO, String.format("PHA. A(%02x) => [S](%02x)", regA, m.getMemAt(stackaddr)));
                                m.setMemAt(stackaddr, regA);
                                regS--;
                                break;
                            case 6:
                                log.log(Level.INFO, String.format("CLI. I(%02x)", I));
                                I=0;
                                break;
                        }
                        break;
                    case 3:
                        switch(addrmode)
                        {
                            case 2:
                                regS++;
                                short stackaddr = (short)(0x100 + (regS&0xFF));
                                byte NA = m.getMemAt(stackaddr);
                                log.log(Level.INFO, String.format("PLA. [S](%02x) => A(%02x)", NA, regA));
                                regA=NA;
                                setZ(regA);
                                setN(regA);
                                break;
                            case 6:
                                log.log(Level.INFO, String.format("SEI. I(%02x)", I));
                                I=1;
                                break;
                        }
                        break;
                    case 4:
                        switch(addrmode)
                        {
                            case 1:
                            case 3:
                            case 5:
                                log.log(Level.INFO, String.format("STY. Y(%02x) => M(%02x)", regY, oper));
                                m.setMemAt(opaddr, regY);
                                break;
                            case 2:
                                regY--;
                                log.log(Level.INFO, String.format("DEY. result: %02x", regY));
                                setZ(regY);
                                setN(regY);
                                break;
                            case 6:
                                log.log(Level.INFO, String.format("TYA. Y(%02x) => A(%02x)", regY, regA));
                                regA = regY;
                                setZ(regA);
                                setN(regA);
                                break;
                        }
                    case 5:
                        switch (addrmode) {
                            case 0:
                            case 1:
                            case 3:
                            case 5:
                            case 7:
                                log.log(Level.INFO, String.format("LDY. M(%02x) => Y(%02x)", oper, regY));
                                regY = oper;
                                setZ(regY);
                                setN(regY);
                                break;
                            case 2:
                                log.log(Level.INFO, String.format("TAY. A(%02x) => Y(%02x)", regA, regY));
                                regY = regA;
                                setZ(regY);
                                setN(regY);
                                break;
                            case 6:
                                log.log(Level.INFO, String.format("CLV. V(%02x)", V));
                                V=0;
                                break;
                        }
                        break;
                    case 6:
                        switch (addrmode) {
                            case 2:
                                regY++;
                                log.log(Level.INFO, String.format("INY. result: %02x", regY));
                                setZ(regY);
                                setN(regY);
                                break;
                        }
                        break;
                    case 7:
                        switch (addrmode) {
                            case 2:
                                regX++;
                                log.log(Level.INFO, String.format("INX. result: %02x", regX));
                                setZ(regX);
                                setN(regX);
                                break;
                        }
                }
                break;
            case 1:
                switch (comcode) {
                    case 0:
                        log.log(Level.INFO, String.format("ORA. M(%02x) |> A(%02x)", oper, regA));
                        regA |= oper;
                        setZ(regA);
                        setN(regA);
                        break;
                    case 1:
                        log.log(Level.INFO, String.format("AND. M(%02x) &> A(%02x)", oper, regA));
                        regA &= oper;
                        setZ(regA);
                        setN(regA);
                        break;
                    case 2:
                        log.log(Level.INFO, String.format("EOR. M(%02x) ^> A(%02x)", oper, regA));
                        regA ^= oper;
                        setZ(regA);
                        setN(regA);
                        break;
                    case 3:
                        log.log(Level.INFO, String.format("ADC. M(%02x) +> A(%02x)", oper, regA));
                        // bytes get sign-extended into ints. mask the lowest byte to get carry
                        int intermediate = ((regA&0xff)+(oper&0xff)+C);
                        C = (byte)(intermediate>>8);
                        regA = (byte)intermediate;
                        //Overflow occurs if (M^result)&(N^result)&0x80 is nonzero. That is, if the sign of both inputs is different from the sign of the result.
                        V = (byte)(((regA^intermediate)&(oper^intermediate)&80)==0?0:1);
                        setZ(regA);
                        setN(regA);
                        break;
                    case 4:
                        log.log(Level.INFO, String.format("STA. A(%02x) => M(%02x)", regA, oper));
                        m.setMemAt(opaddr, regA);
                        break;
                    case 5:
                        regA = (byte)oper;
                        log.log(Level.INFO, String.format("LDA. operand: %02x", regA));
                        setZ(oper);
                        setN(oper);
                        break;
                    case 6:
                        log.log(Level.INFO, String.format("CMP. M(%02x) <> A(%02x)", oper, regA));
                        int cmp = ((regA&0xff)-(oper&0xff));
                        byte cmpb = (byte)cmp;
                        C = (byte)(cmp>>8&1);
                        setZ(cmpb);
                        setN(cmpb);
                        break;
                    case 7:
                        log.log(Level.INFO, String.format("SBC. M(%02x) -> A(%02x)", oper, regA));
                        // http://www.righto.com/2012/12/the-6502-overflow-flag-explained.html
                        // bytes get sign-extended into ints. mask the lowest byte to get borrow.
                        // borrow is complement of carry. 1^1=0; 1^0=1
                        // M - N - B
                        // = M + (ones complement of N) + C
                        int sbcinterm = ((regA&0xff)+(~oper&0xff)+C);
                        // get borrow value
                        C = (byte)(sbcinterm>>8&1);
                        //Overflow occurs if (M^result)&(N^result)&0x80 is nonzero. That is, if the sign of both inputs is different from the sign of the result.
                        V = (byte)(((regA^sbcinterm)&(~oper^sbcinterm)&0x80)==0?0:1);
                        regA = (byte)sbcinterm;
                        setZ(regA);
                        setN(regA);
                        break;

                }
                break;
            case 2:
                switch (comcode) {
                    case 0:
                        switch(addrmode)
                        {
                            case 1:
                            case 3:
                            case 5:
                            case 7:
                                log.log(Level.INFO, String.format("ASL. M: %02x", oper));
                                m.setMemAt(opaddr, oper<<=1);
                                setZ(oper);
                                setN(oper);
                                break;
                            case 2:
                                log.log(Level.INFO, String.format("ASL. A: %02x", regA));
                                regA<<=1;
                                setZ(regA);
                                setN(regA);
                                break;
                        }
                        break;
                    case 1:
                        switch(addrmode)
                        {
                            case 1:
                            case 3:
                            case 5:
                            case 7:
                                log.log(Level.INFO, String.format("ROL. M: %02x", oper));
                                // to roll left: shift left normally
                                // shift right 7 then mask the lowest bit (java sign-extends bytes into ints. for negative numbers this means all leftmost bits become 1's)
                                // operator precedence: <</>> > & > |
                                oper = (byte)(oper<<1 | oper>>7&0x1);
                                m.setMemAt(opaddr, oper);
                                setZ(oper);
                                setN(oper);
                                break;
                            case 2:
                                log.log(Level.INFO, String.format("ROL. A: %02x", regA));
                                regA = (byte)(regA<<1 | regA>>7&0x1);
                                setZ(regA);
                                setN(regA);
                                break;
                        }
                        break;
                    case 2:
                        switch(addrmode)
                        {
                            case 1:
                            case 3:
                            case 5:
                            case 7:
                                log.log(Level.INFO, String.format("ASR. M: %02x", oper));
                                oper = (byte)((oper&0xff)>>1);
                                m.setMemAt(opaddr, oper);
                                setZ(oper);
                                setN(oper);
                                break;
                            case 2:
                                log.log(Level.INFO, String.format("ASR. A: %02x", regA));
                                regA = (byte)((regA&0xff)>>1);
                                setZ(regA);
                                setN(regA);
                                break;
                        }
                        break;
                    case 3:
                        switch(addrmode)
                        {
                            case 1:
                            case 3:
                            case 5:
                            case 7:
                                log.log(Level.INFO, String.format("ROR. M: %02x", oper));
                                // to roll right: shift right  then mask the lowest 7 bits due to sign-extension
                                // shift left 7 normally
                                // operator precedence: <</>> > & > |
                                oper = (byte)(oper>>1&0x7F | oper<<7);
                                m.setMemAt(opaddr, oper);
                                setZ(oper);
                                setN(oper);
                                break;
                            case 2:
                                log.log(Level.INFO, String.format("ROR. A: %02x", regA));
                                regA = (byte)(regA>>1&0x7F | regA<<7);
                                setZ(regA);
                                setN(regA);
                                break;
                        }
                        break;
                    case 4:
                        switch(addrmode)
                        {
                            case 1:
                            case 3:
                            case 5:
                                log.log(Level.INFO, String.format("STX. X(%02x) => M(%02x)", regX, oper));
                                m.setMemAt(opaddr, regX);
                                break;
                            case 2:
                                log.log(Level.INFO, String.format("TXA. X(%02x) => A(%02x)", regX, regA));
                                regA=regX;
                                setZ(regA);
                                setN(regA);
                                break;
                            case 6:
                                log.log(Level.INFO, String.format("TXS. X(%02x) => S(%02x)", regX, regS));
                                regS=regX;
                                break;
                        }
                        break;
                    case 5:
                        switch (addrmode) {
                            case 0:
                            case 1:
                            case 3:
                            case 5:
                            case 7:
                                log.log(Level.INFO, String.format("LDX. M(%02x) => X(%02x)", oper, regX));
                                regX = oper;
                                setZ(regX);
                                setN(regX);
                                break;
                            case 2:
                                log.log(Level.INFO, String.format("TAX. A(%02x) => X(%02x)", regA, regX));
                                regX = regA;
                                setZ(regX);
                                setN(regX);
                                break;
                            case 6:
                                log.log(Level.INFO, String.format("TSX. S(%02x) => X(%02x)", regS, regX));
                                regX=regS;
                                setZ(regX);
                                setN(regX);
                                break;
                        }
                        break;
                    case 6:
                        if ((addrmode & 1) !=0) {
                            m.setMemAt(opaddr, (byte)(oper-1));
                            oper = m.getMemAt(opaddr);
                            log.log(Level.INFO, String.format("DEC. result: %02x", m.getMemAt(opaddr)));
                            setZ(oper);
                            setN(oper);
                        } else if(addrmode==2)
                        {
                            regX--;
                            log.log(Level.INFO, String.format("DEX. result: %02x", regY));
                            setZ(regX);
                            setN(regX);
                        } else log.log(Level.INFO, "NOP");
                        break;
                    case 7:
                        if ((addrmode & 1) !=0) {
                            m.setMemAt(opaddr, (byte)(oper+1));
                            oper = m.getMemAt(opaddr);
                            log.log(Level.INFO, String.format("INC. result: %02x", oper));
                            setZ(oper);
                            setN(oper);
                        } else
                            log.log(Level.INFO, "NOP");
                        break;
                }
        }
        log.log(Level.INFO, "\n");
        log.log(Level.INFO, String.format("terminated. A: %02x, X: %02x, Y: %02x, PC: %04x, S: %02x", regA, regX, regY, regPC, regS));
    }
}
