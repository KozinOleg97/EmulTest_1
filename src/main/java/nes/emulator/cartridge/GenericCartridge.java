package nes.emulator.cartridge;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.InvalidParameterException;
import java.util.*;

public abstract class GenericCartridge {
    static public GenericCartridge CartridgeFactory(String filename) throws FileNotFoundException {
        GenericCartridge ic;
        FileInputStream is = new FileInputStream(filename);
        try {
            int signature = 0;
            byte header[] = new byte[12];
            for (int i = 0; i < 4; i++) {
                signature |= is.read() << i * 8;
            }
            // if not iNES file, fail
            if (signature != 0x1A53454E) return null; //NES+0x1A
            is.read(header);

            // create "memory chips" for cartridge, mappers are separate
            int romsize = header[0] * 16384;
            int chrsize = header[1] * 8192;
            int ramsize = 8192;
            int chrramsize = 0;
            byte rom[] = new byte[romsize+ramsize];
            byte chr[] = new byte[chrsize+chrramsize];
            //TODO: check for trainer
            if ((header[3] & 4) != 0) is.skip(512);

            is.read(rom, 0, romsize);
            is.read(chr, 0, chrsize);
            ic = new NROM();
            ic.chipsCPUROM.put(0, ic.new MemChip(rom, BankType.CPUROM));
            ic.chipsPPUROM.put(0, ic.new MemChip(chr, BankType.PPUROM));
            ((NROM)ic).init(header[0], header[1], ramsize);
            //ic.PRGRAM = new byte[ramsize];
        } catch (java.io.IOException e) {
            return null;
        }
        return ic;
    }

    protected GenericCartridge() {
        /*banksCPUROM = new ArrayList<Bank>();
        banksPPUROM = new ArrayList<Bank>();
        chipsCPUROM = new ArrayList<MemChip>();
        chipsPPUROM = new ArrayList<MemChip>();*/
        banksCPUROM = new HashMap<Integer, Bank>();
        banksPPUROM = new HashMap<Integer, Bank>();
        chipsCPUROM = new HashMap<Integer, MemChip>();
        chipsPPUROM = new HashMap<Integer, MemChip>();
    }

    protected boolean inRange(int accessaddr, int rangestart, int rangesize) {
        return (accessaddr >= rangestart && accessaddr < rangestart + rangesize);
    }

    protected Bank checkCPUInRange(Short addr) {
        int longaddr = addr & 0xFFFF;
        for (Bank b : banksCPUROM.values()) {
            if (b.HitTest(longaddr)) {
                return b;
            }
        }
        return null;
    }

    public Byte getCPUMemAt(Short addr) {
        // банки не должны быть расположены друг поверх друга. Правда ведь?
        byte res = (byte)0xFF;
        int longaddr = addr & 0xFFFF;
        for (Bank b : banksCPUROM.values()) {
            if(b.HitTest(longaddr))
                res&=b.Resolve(longaddr);
        }
        return res;
    }

    public Boolean setCPUMemAt(Short addr, Byte val) {
        boolean res = false;
        int longaddr = addr & 0xFFFF;
        for (Bank b : banksCPUROM.values()) {
            if(b.HitTest(longaddr))
                res|=b.Resolve(longaddr, val);
        }
        return res;
    }

    protected void createCPUMemChip(int n, int size, BankType b)
    {
        chipsCPUROM.put(n, new MemChip(new byte[size], b));
    }

    protected void setCPUMemChipAccess(int n, boolean readable, boolean writeable)
    {
        MemChip a = chipsCPUROM.get(n);
        a.readable = readable;
        a.writeable = writeable;
    }

    protected void createCPUBank(int n, int CPUAddr, int size, int intAddr, int chipindex)
    {
        banksCPUROM.put(n, new Bank(CPUAddr, size, intAddr, chipsCPUROM.get(chipindex)));
    }

    protected void setCPUBank(int n, int CPUAddr, int size, int intAddr)
    {
        Bank b = banksCPUROM.get(n);
        b.CPUAddr = CPUAddr;
        b.InternalAddress=intAddr;
        b.Size=size;
    }

    protected Bank checkPPUInRange(Short addr) {
        int longaddr = addr & 0xFFFF;
        for (Bank b : banksPPUROM.values()) {
            if (b.HitTest(longaddr)) {
                return b;
            }
        }
        return null;
    }

    public Byte getPPUMemAt(Short addr) {
        // банки не должны быть расположены друг поверх друга. Правда ведь?
        byte res = (byte)0xFF;
        int longaddr = addr & 0xFFFF;
        for (Bank b : banksPPUROM.values()) {
            if(b.HitTest(longaddr))
                res&=b.Resolve(longaddr);
        }
        return res;
    }

    public Boolean setPPUMemAt(Short addr, Byte val) {
        boolean res = false;
        int longaddr = addr & 0xFFFF;
        for (Bank b : banksPPUROM.values()) {
            if(b.HitTest(longaddr))
                res|=b.Resolve(longaddr, val);
        }
        return res;
    }

    protected void createPPUMemChip(int n, int size, BankType b)
    {
        chipsPPUROM.put(n, new MemChip(new byte[size], b));
    }

    protected void setPPUMemChipAccess(int n, boolean readable, boolean writeable)
    {
        MemChip a = chipsPPUROM.get(n);
        a.readable = readable;
        a.writeable = writeable;
    }

    protected void createPPUBank(int n, int PPUAddr, int size, int intAddr, int chipindex)
    {
        banksPPUROM.put(n, new Bank(PPUAddr, size, intAddr, chipsPPUROM.get(chipindex)));
    }

    protected void setPPUBank(int n, int PPUAddr, int size, int intAddr)
    {
        Bank b = banksPPUROM.get(n);
        b.CPUAddr = PPUAddr;
        b.InternalAddress=intAddr;
        b.Size=size;
    }

    enum BankType
    {
        CPUROM,
        CPURAM1,
        PPUROM,
        PPURAM1
    }

    class Bank
    {
        Bank(int a, int b, int c, MemChip reftgt)
        {
            CPUAddr=a;
            Size=b;
            InternalAddress=c;
            AccTgt=reftgt;
        }
        byte Resolve(int longaddr)
        {
            byte res = (byte)0xFF;
            if (inRange(longaddr, CPUAddr, Size)) {
                if(AccTgt.readable)
                    res = AccTgt.data[longaddr - CPUAddr + InternalAddress];
            } else throw new IndexOutOfBoundsException("invalid Resolve() target");
            return res;
        }

        boolean Resolve(int longaddr, byte val)
        {
            boolean writesuccessful = false;
            if (inRange(longaddr, CPUAddr, Size)) {
                if(AccTgt.writeable) {
                    AccTgt.data[longaddr - CPUAddr + InternalAddress] = val;
                    writesuccessful = true;
                }
            } else throw new IndexOutOfBoundsException("invalid Resolve() target");
            return writesuccessful;
        }

        boolean HitTest(int longaddr)
        {
            return inRange(longaddr, CPUAddr, Size);
        }

        int CPUAddr;
        int Size;
        int InternalAddress;
        MemChip AccTgt;
    }

    class MemChip
    {
        MemChip(byte d[], BankType b)
        {
            data=d;
            memclass=b;
            readable = true;
            writeable = !( b==BankType.CPUROM || b==BankType.PPUROM );
        }
        byte data[];
        boolean readable;
        boolean writeable;
        BankType memclass;

    }
    private Map<Integer, Bank> banksCPUROM;
    private Map<Integer, MemChip> chipsCPUROM;

    private Map<Integer, Bank> banksPPUROM;
    private Map<Integer, MemChip> chipsPPUROM;
}