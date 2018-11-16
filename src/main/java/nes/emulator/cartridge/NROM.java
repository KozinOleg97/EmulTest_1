package nes.emulator.cartridge;

import java.security.InvalidParameterException;

public class NROM extends GenericCartridge {
    NROM() {

    }

    void init(int rompages, int chrpages, int ramsize) throws InvalidParameterException {
        if (rompages != 2 && rompages != 1) throw new InvalidParameterException("Invalid rompages count");
        createCPUBank(0, 0x8000, rompages * 0x4000, 0, 0);
        if (rompages == 1) createCPUBank(1, 0x8000 + 0x4000, rompages * 0x4000, 0, 0);
        createCPUMemChip(10, ramsize, BankType.CPURAM1);
        createCPUBank(2, 0x6000, ramsize, 0, 10);

        createPPUBank(0, 0, 0x2000, 0, 0);
    }

}
