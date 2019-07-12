package nes.emulator;

import nes.emulator.cartridge.GenericCartridge;
import nes.emulator.console.Memory;
import nes.emulator.console.PPU;
import nes.emulator.console.Proc;
import nes.emulator.display.SimpleGraphics;

import java.io.FileNotFoundException;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        GenericCartridge c = GenericCartridge.CartridgeFactory("c:\\fceux\\roms\\Super_Mario_Bros._(E).nes");

        Memory.INSTANCE.init(c);

        Proc.INSTANCE.init();

        SimpleGraphics.INSTANCE.init();

        for (int i=0; i<13000; i++)
        {
            long prestep = Proc.INSTANCE.getPPUTickCount();
            Proc.INSTANCE.Step();
            long poststep = Proc.INSTANCE.getPPUTickCount();
            PPU.INSTANCE.Ketchup(poststep-prestep);
        }

    }
}






