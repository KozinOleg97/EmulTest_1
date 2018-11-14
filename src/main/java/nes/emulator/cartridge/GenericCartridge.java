package nes.emulator.cartridge;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public abstract class GenericCartridge {
    static GenericCartridge CartridgeFactory(String filename) throws FileNotFoundException
    {
        GenericCartridge ic;
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

    GenericCartridge(int prgromsize, int chrromsize)
    {
        PRGROM = new byte[prgromsize];
        // как-то читаем
    }

    Byte getMemAt(Short addr)
    {
        if(addr.inRange(BankCPUAddr, size))
        {
            return PRGROM[addr-CPUAddr+intAddr];
        }
    }
    abstract Boolean setMemAt(Short addr, Byte val);

    protected void setBank(int n, int CPUAddr, int size, int intAddr)
    {
        // во внутренние переменные
        if(n>banks.size())
            banks.add(new Bank(CPUAddr, size, intAddr));
    }

    class Bank
    {
        Bank(int a, int b, int c)
        {
            CPUAddr=a;
            Size=b;
            InternalAddress=c;
        }
        int CPUAddr;
        int Size;
        int InternalAddress;
    }
    ArrayList<Bank> banks;

    byte PRGROM[];
}