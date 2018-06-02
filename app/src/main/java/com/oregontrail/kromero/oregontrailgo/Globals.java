package com.oregontrail.kromero.oregontrailgo;

public class Globals {
    private static Globals instance;

    // Global variable
    //private String data;

    // Restrict the constructor from being instantiated
    private Globals(){}

    public String getData(){
        // cade
        return "http://192.168.43.126:8080/";

        // emulator
        //return "http://10.0.2.2:8080/";
    }

    public static synchronized Globals getInstance(){
        if(instance==null){
            instance=new Globals();
        }
        return instance;
    }
}
