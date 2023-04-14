package com.nedap.university.Exeptions;

/**
 * This Exception is thrown when there is asked for a file that does not exist
 */
public class FileNotExistException extends Exception{
    public FileNotExistException(String message){
        super(message);
        System.out.println(message);
    }
}
