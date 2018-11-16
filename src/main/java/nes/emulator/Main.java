package nes.emulator;

import nes.emulator.cartridge.GenericCartridge;
import nes.emulator.console.Memory;
import nes.emulator.console.Proc;
import nes.emulator.display.SimpleGraphics;

import java.io.FileNotFoundException;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        /*GenericCartridge c = GenericCartridge.CartridgeFactory("c:\\fceux\\roms\\Super_Mario_Bros._(E).nes");

        Memory.INSTANCE.init(c);

        Proc.INSTANCE.init();
        for (int i=0; i<10; i++)
        {
            Proc.INSTANCE.Step();
        }*/

        SimpleGraphics.INSTANCE.init();
    }
}






