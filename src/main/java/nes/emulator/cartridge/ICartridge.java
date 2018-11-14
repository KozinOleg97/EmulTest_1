package nes.emulator.cartridge;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public interface ICartridge {
    static ICartridge CartridgeFactory(String filename) throws FileNotFoundException
    {
        ICartridge ic;
        FileInputStream is = new FileInputStream("c:\\fceux\\roms\\Super_Mario_Bros._(E).nes");
        try {
            int signature = 0;
            byte header[] = new byte[12];
            for (int i = 0; i < 4; i++) {
                signature |= is.read()<<i*8;
            }
            // if not iNES file, fail
            if(signature!=0x1A53454E) return null; //NES+0x1A
            is.read(header);

            // create "memory chips" for
            int romsize = header[0]*16384;
            int chrsize = header[1]*8192;
            ic = new NROM(romsize, chrsize);
        }
        catch (java.io.IOException e)
        {
            return null;
        }
        return ic;
    }

    Byte getMemAt(Short addr);
    Boolean setMemAt(Short addr, Byte val);
}
