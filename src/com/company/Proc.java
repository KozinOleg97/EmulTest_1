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

    private class regP {
        private byte C; //:1; // carry
        private byte Z; //:1; // zero
        private byte I; //:1; // interrupt 0==enabled
        private byte D; //:1; // decimal mode
        private byte B; //:1; // currently in break(BRK) interrupt
        private byte NU; //:1; // always 1
        private byte V; //:1; // oVerflow
        private byte N; //:1; // negative(?)
    }

    private Memory m;


    Proc(Memory mem)
    {
        regA = regX = regY = regS = 0;
        regPC = 0;
        m=mem;
    }


    // Вход: При входе в эту функцию regPC указывает на первый байт операнда
    // addrmode - три бита, выдранные из инструкции
    // Выход: opAddr - адрес операнда
    // Результат: содержимое операнда
    byte takeoper(int addrmode, Short opAddrL) {
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
                opAddr = (short)(regX + m.getMemAtW( (short)(m.getMemAt(regPC)|m.getMemAt((short)(regPC+1))<<8) ));
                regPC += 2;
                operlo = m.getMemAt(opAddr);
                break;
        }
        return operlo;
    }


    void Step()
    {
        // LDA FF, TAX, INX, STA 1,X, LDY #0
        //char* opcodes = "\xA9\xFE\xAA\xE8\x95\x01\xA4\x00";
       
        byte command = m.getMemAt(regPC++);
        log.log(Level.FINE, "command: %02x\n" + Integer.toHexString(command));

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
                if (comcode == 0 || ((addrmode & 1)==0 && (comcode < 5 || addrmode!=0))) break;
                //if (comcode != 0 && ((addrmode & 1) || (comcode >= 5 && !addrmode))) {
            case 3:
                //undocumented
            case 1:
                log.log(Level.FINE, String.format("operand: %02x(+second byte %02x) ", m.getMemAt(regPC), m.getMemAt((short)(regPC + 1))));
                // TODO: как передавать opaddr как ссылку?????????
                oper = takeoper(addrmode, opaddr);
                log.log(Level.FINE, String.format("decoded: %02x\n", oper));
                break;
        }
        log.log(Level.FINE, "executing ");
        // exec command
        switch (comclass) {
            case 0:
                switch (comcode) {
                    case 5:
                        switch (addrmode) {
                            case 0:
                            case 1:
                            case 3:
                            case 5:
                            case 7:
                                log.log(Level.FINE, String.format("LDY. M(%02x) => Y(%02x)", oper, regY));
                                regY = oper;
                                break;
                            case 2:
                                log.log(Level.FINE, String.format("TAY. A(%02x) => Y(%02x)", regA, regY));
                                regY = regA;
                                break;
                        }
                        break;
                    case 6:
                        switch (addrmode) {
                            case 2:
                                regY++;
                                log.log(Level.FINE, String.format("INY. result: %02x", regY));
                                break;
                        }
                    case 7:
                        switch (addrmode) {
                            case 2:
                                regX++;
                                log.log(Level.FINE, String.format("INX. result: %02x", regX));
                                break;
                        }
                }
                break;
            case 1:
                switch (comcode) {
                    case 4:
                        log.log(Level.FINE, String.format("STA. A(%02x) => M(%02x)", regA, oper));
                        m.setMemAt(opaddr, regA);
                        break;
                    case 5:
                        regA = (byte)oper;
                        log.log(Level.FINE, String.format("LDA. operand: %02x", regA));
                        break;

                }
                break;
            case 2:
                switch (comcode) {
                    case 5:
                        switch (addrmode) {
                            case 0:
                            case 1:
                            case 3:
                            case 5:
                            case 7:
                                log.log(Level.FINE, String.format("LDX. M(%02x) => X(%02x)", oper, regX));
                                regX = oper;
                                break;
                            case 2:
                                log.log(Level.FINE, String.format("TAX. A(%02x) => X(%02x)", regA, regX));
                                regX = regA;
                                break;
                        }
                        break;
                    case 7:
                        if ((addrmode & 1) !=0) {
                            m.setMemAt(opaddr, (byte)(oper+1));
                            log.log(Level.FINE, String.format("INC. result: %02x", m.getMemAt(opaddr)));
                        } else
                            log.log(Level.FINE, "NOP");
                        break;
                }
        }
        log.log(Level.FINE, "\n");
        log.log(Level.FINE, String.format("terminated. A: %02x, X: %02x, Y: %02x, PC: %04x, S: %02x", regA, regX, regY, regPC, regS));
    }
}
