package com.oregontrail.kromero.oregontrailgo;

public class Globals{
    private static Globals instance;

    // Global variable
    //private String data;

    // Restrict the constructor from being instantiated
    private Globals(){}

    public String getData(){
        return "http://149.142.227.146:8080/";
    }

    public static synchronized Globals getInstance(){
        if(instance==null){
            instance=new Globals();
        }
        return instance;
    }
}
