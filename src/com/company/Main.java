package com.company;

public class Main {

    public static void main(String[] args) {



    }
}




        int _tmain(int argc, _TCHAR* argv[])
        {	regA=regX=regY=regS=0;
        regPC=0;
        ZeroMemory(&regP, sizeof(regP));
        // LDA FF, TAX, INX, STA 1,X, LDY #0
        //char* opcodes = "\xA9\xFE\xAA\xE8\x95\x01\xA4\x00";
        char opcodes[1000];
        wscanf_s(L"%02x %04x", opcodes+regPC, opcodes+regPC+1);
        while(opcodes[regPC])
        {
        BYTE command = opcodes[regPC++];
        wprintf(L"command: %02x\n", command);
        BYTE oper = 0;
        BYTE* opaddr = NULL;
        // XXXYYYZZ
        // XXX: код команды
        // YYY: режим адресации
        // ZZ: класс команды

        BYTE addrmode = (command>>2)&7;
        BYTE comcode = (command>>5)&7;
        BYTE comclass = command&3;

        // interpret command's code to ascertain addressing mode
        switch(comclass)
        {
        case 3:
        //undocumented
        break;
        case 2:
        if(!(addrmode&1)) break;
        //commands either work as expected or halt/nop
        case 1:
        wprintf(L"operand: %02x(+second byte %02x) ", int(opcodes[regPC]), int(opcodes[regPC+1]));
        oper=takeoper(opcodes, addrmode, opaddr);
        wprintf(L"decoded: %02x\n", oper);
        break;
        case 0:
        if(comcode!=0&&((addrmode&1)||(comcode>=5&&!addrmode)))
        {
        wprintf(L"operand: %02x(+second byte %02x) ", int(opcodes[regPC]), int(opcodes[regPC+1]));
        oper=takeoper(opcodes, addrmode, opaddr);
        wprintf(L"decoded: %02x\n", oper);
        }
        break;
        }
        wprintf(L"executing ");
        // exec command
        switch(comclass)
        {
        case 0:
        switch(comcode)
        {
        case 5:
        switch(addrmode)
        {
        case 0:
        case 1:
        case 3:
        case 5:
        case 7:
        wprintf(L"LDY. M(%02x) => Y(%02x)", oper, regY);
        regY=oper;
        break;
        case 2:
        wprintf(L"TAY. A(%02x) => Y(%02x)", regA, regY);
        regY=regA;
        break;
        }
        break;
        case 6:
        switch(addrmode)
        {
        case 2:
        regY++;
        wprintf(L"INY. result: %02x", regY);
        break;
        }
        case 7:
        switch(addrmode)
        {
        case 2:
        regX++;
        wprintf(L"INX. result: %02x", regX);
        break;
        }
        }
        break;
        case 1:
        switch(comcode)
        {
        case 4:
        wprintf(L"STA. A(%02x) => M(%02x)", regA, *opaddr);
        *opaddr=regA;
        break;
        case 5:
        regA=BYTE(oper);
        wprintf(L"LDA. operand: %02x", regA);
        break;

        }
        break;
        case 2:
        switch(comcode)
        {
        case 5:
        switch(addrmode)
        {
        case 0:
        case 1:
        case 3:
        case 5:
        case 7:
        wprintf(L"LDX. M(%02x) => X(%02x)", oper, regX);
        regX=oper;
        break;
        case 2:
        wprintf(L"TAX. A(%02x) => X(%02x)", regA, regX);
        regX=regA;
        break;
        }
        break;
        case 7:
        if(addrmode&1)
        {
        (*opaddr)++;
        wprintf(L"INC. result: %02x", (*opaddr));
        }
        else
        wprintf(L"NOP");
        break;
        }
        }
        wprintf(L"\n\n");
        wscanf_s(L"%02x %04x", opcodes+regPC, opcodes+regPC+1);
        }
        wprintf(L"terminated. A: %02x, X: %02x, Y: %02x, PC: %04x, S: %02x", regA, regX, regY, regPC, regS);
        while(1);
        return 0;
        }

