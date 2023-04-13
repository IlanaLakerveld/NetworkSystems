package com.nedap.university.Exeptions;

public class ServerGivesErrorException extends Exception{
    public ServerGivesErrorException(String message){
        super(message);
        System.out.println(message);
    }
}
