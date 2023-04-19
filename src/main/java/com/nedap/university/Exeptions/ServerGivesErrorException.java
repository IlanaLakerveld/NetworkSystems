package com.nedap.university.Exeptions;

/**
 * This error is thrown when the server gives back an error. The error given is printed.
 */
public class ServerGivesErrorException extends Exception{
    public ServerGivesErrorException(String message){
        super(message);
        System.out.println(message);
    }
}
