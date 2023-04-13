package com.nedap.university.Exeptions;

public class FileNotExistException extends Exception{
    public FileNotExistException(String message){
        super(message);
        System.out.println(message);
    }
}
