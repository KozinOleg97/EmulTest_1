package nes.emulator.console;

public enum PPURegisters {
    INSTANSE;

    public Register[] registers;

    class accessRights {
        boolean write;
        boolean read;

        accessRights(Boolean write, Boolean read) {
            this.write = write;
            this.read = read;
        }
    }

    class Register { //TODO add decompose functions
        private Byte rawValue;

        Register(Byte rawValue) {
            this.rawValue = rawValue;
        }
    }


    PPURegisters() {
        registers = new Register[8];
        for (Register elem : registers) {
            elem.rawValue = 0;
        }


    }

    public void setMemAt(Short addr, Byte val) {
        Integer addrInt = addr & 0x0007;
        if (checkAccsess(addrInt).read) {
            registers[addrInt].rawValue = val;//TODO
        }
    }

    public Byte getMemAt(Short addr) {
        Integer addrInt = addr & 0x0007;

        if (checkAccsess(addrInt).read) {
            return registers[addrInt].rawValue;//TODO
        }

        return 0;
    }

    private accessRights checkAccsess(Integer addr) {
        switch (addr) {
            case 0:
                return new accessRights(true, false);

            case 1:
                return null;//TODO

            case 2:
                return null;

            case 3:
                return null;

            case 4:
                return null;

            case 5:
                return null;

            case 6:
                return null;

            case 7:
                return null;


            default:
                throw new java.lang.IllegalArgumentException("Wrong address in getMemAt PPURegisters");
        }
    }


}
