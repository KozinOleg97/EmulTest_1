package nes.emulator.cartridge;

import java.security.InvalidParameterException;

public class CNROM extends GenericCartridge
{
    int realchrsize;

    void init(int rompages, int chrpages, int ramsize) throws InvalidParameterException
    {
        if (rompages != 2 && rompages != 1) throw new InvalidParameterException("Invalid rompages count");
        createCPUBank(0, 0x8000, rompages * 0x4000, 0, 0);
        if (rompages == 1) createCPUBank(1, 0x8000 + 0x4000, rompages * 0x4000, 0, 0);

        createPPUBank(0, 0, 0x2000, 0, 0);
        realchrsize=chrpages;
    }

    @Override
    public Boolean setCPUMemAt(Short addr, Byte val)
    {
        int longaddr = addr & 0xFFFF;
        if (inRange(longaddr, 0x8000, 0x8000))
        {
            int valcorr = getCPUMemAt(addr) & val;
            int tgtbank = valcorr&0x3;
            // should crash with chip sizes lower than 32K
            setPPUBank(0, 0, 0x2000, tgtbank*0x2000);
        }
        return super.setCPUMemAt(addr, val);
    }
}
