package com.company;

public class Memory {

    private Byte[] mainMemory;

    Memory() {
        mainMemory = new Byte[64 * 256];
    }

    Byte getMemAt(Short addr)
    {
        return mainMemory[addr];
    }

}
