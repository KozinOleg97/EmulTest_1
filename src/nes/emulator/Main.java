package nes.emulator;

import nes.emulator.console.Memory;
import nes.emulator.console.Proc;

public class Main {

    public static void main(String[] args) {
        Memory m = new Memory();
        Proc cpu = new Proc(m);

        // LDA FF, TAX, INX, STA 1,X, LDY #0
        //char* opcodes = "\xA9\xFE\xAA\xE8\x95\x01\xA4\x00";
        // https://skilldrick.github.io/easy6502/
        // LDA #$01
        // STA $0200
        // TAX
        // INX
        // STX $0201
        // LDA $0201
        // a9 01 8d 00 02 aa e8 8e 01 02 ad 01 02
        Short codetemp[] = {0xa9,0x01,0x8d,0x00,0x02,0xaa,0xe8,0x8e,0x01,0x02,0xad,0x01,0x02};
        m.Push(codetemp);
        for (int i=0; i<6; i++)
        {
            cpu.Step();
        }
    }
}






