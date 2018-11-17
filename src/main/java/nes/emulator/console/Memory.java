package nes.emulator.console;

import nes.emulator.cartridge.GenericCartridge;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum Memory {
    INSTANCE;

    /*
    Address range	Size	Device
$0000-$07FF	$0800	2KB internal RAM
$0800-$0FFF	$0800	Mirrors of $0000-$07FF
$1000-$17FF	$0800
$1800-$1FFF	$0800
$2000-$2007	$0008	NES PPU registers
$2008-$3FFF	$1FF8	Mirrors of $2000-2007 (repeats every 8 bytes)
$4000-$4017	$0018	NES APU and I/O registers
$4018-$401F	$0008	APU and I/O functionality that is normally disabled. See CPU Test Mode.
$4020-$FFFF	$BFE0	Cartridge space: PRG ROM, PRG RAM, and mapper registers (See Note)
See Sample RAM map for an example allocation strategy for the 2KB of internal RAM at $0000-$0800.

Note: Most common boards and iNES mappers address ROM and Save/Work RAM in this format:

$6000-$7FFF = Battery Backed Save or Work RAM
$8000-$FFFF = Usual ROM, commonly with Mapper Registers (see MMC1 and UxROM for example)
The CPU expects interrupt vectors in a fixed place at the end of the cartridge space:

$FFFA-$FFFB = NMI vector
$FFFC-$FFFD = Reset vector
$FFFE-$FFFF = IRQ/BRK vector
If a mapper doesn't fix $FFFA-$FFFF to some known bank (typically, along with the rest of the bank containing them, e.g. $C000-$FFFF for a 16KiB banking mapper) or use some sort of reset detection, the vectors need to be stored in all banks.
     */
    private Byte[] mainMemory;
    private Logger log;
    private GenericCartridge mapperMemory;
    byte[] PPUMemory; // TODO check this

    public void init(GenericCartridge c) {
        mapperMemory = c;
    }

    Memory() {
        mainMemory = new Byte[8 * 256];
        for (int i = 0; i < mainMemory.length; i++) {
            mainMemory[i] = 0;
        }
        PPUMemory = PPU.INSTANCE.PPUMemory; // TODO check this 2
        log = Logger.getLogger("memory.java");

    }

    Byte getMemAt(Short addr) {
        switch (addr & 0xF000) {
            case 0:
            case 0x1000:
                if ((addr & 0x1800) != 0) log.log(Level.FINE, "RAM mirroring at " + Integer.toHexString(addr));
                return mainMemory[addr & 0x07FF];
            case 0x2000:
                if ((addr & 0x0FF8) != 0) log.log(Level.FINE, "PPU register mirroring at " + Integer.toHexString(addr));
                return (byte)0x00;
                //return PPUMemory[addr & 0x0007];
            case 0x3000:
                throw new java.lang.UnsupportedOperationException("Not supported yet.");
            default:
                return mapperMemory.getCPUMemAt(addr);
        }
    }

    Short getMemAtW(Short addr) {
        Short lo = getMemAt(addr).shortValue();
        Short hi = getMemAt((short) (addr + 1)).shortValue();
        return (short) ((hi << 8) | lo);
    }

    Short getMemAtWarpedW(Short addr) {
        log.log(Level.FINE, "getMemAtWarpedW " + Integer.toHexString(addr));
        Short lo = getMemAt(addr).shortValue();
        Short hi = (short) (getMemAt((short) (addr & 0xff00 | addr + 1 & 0xff)) << 8);
        return (short) (lo | hi);
    }

    Boolean setMemAt(Short addr, Byte val) {

        switch (addr & 0xF000) {
            case 0:
            case 0x1000:
                if ((addr & 0x1800) != 0) log.log(Level.FINE, "RAM mirroring at " + Integer.toHexString(addr));
                mainMemory[addr & 0x7FF] = val;
                return true;
            case 0x2000:
                if ((addr & 0x0FF8) != 0) log.log(Level.FINE, "PPU register mirroring at " + Integer.toHexString(addr));
                PPUMemory[addr & 0x0007] = val;
                return true;
            case 0x3000:
                throw new java.lang.UnsupportedOperationException("Not supported yet.");
            default:
                return mapperMemory.setCPUMemAt(addr, val);
        }
    }

    public void Push(Short a[]) {
        for (int i = 0; i < a.length; i++) {
            mainMemory[i] = a[i].byteValue();
        }
    }

}
