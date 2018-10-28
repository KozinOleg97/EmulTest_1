package com.company;


public class Main {

    public static void main(String[] args) {

        Memory.INSTANCE.hello();
        Memory.INSTANCE.hello();

        Memory q = Memory.getInstance();
        q.hello();
        Memory qwe = Memory.INSTANCE;
        qwe.hello();
        q.hello();

        new Graphics().run();

    }
}






