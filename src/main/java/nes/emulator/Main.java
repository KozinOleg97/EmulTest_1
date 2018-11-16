package nes.emulator;

import nes.emulator.cartridge.GenericCartridge;
import nes.emulator.console.Memory;
import nes.emulator.console.Proc;

import java.io.FileNotFoundException;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        GenericCartridge c = GenericCartridge.CartridgeFactory("c:\\fceux\\roms\\Super_Mario_Bros._(E).nes");
        Memory m = new Memory(c);
        Proc cpu = new Proc(m);

        for (int i=0; i<10; i++)
        {
            cpu.Step();
        }
    }
}






