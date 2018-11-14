package com.company.cartridges;

public class NROM implements ICartridge {
    NROM(int a, int b)
    {

    }

    public Byte getMemAt(Short addr)
    {
        return 1;
    }

    public Boolean setMemAt(Short addr, Byte val)
    {

        return true;
    }
}
