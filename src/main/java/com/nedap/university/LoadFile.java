package com.nedap.university;

import java.io.File;
import java.io.FileInputStream;

public final class LoadFile {

    //Todo from challege 2
    public static byte[] loadFile(File fileToTransmit) {

        if(!fileToTransmit.exists()){
            System.out.println("file does not exist");
            return null ;
        }
        else{
            try (FileInputStream fileStream = new FileInputStream(fileToTransmit)) {
                byte[] fileContents = new byte[(int) fileToTransmit.length()];

                for (int i = 0; i < fileContents.length; i++) {
                    int nextByte = fileStream.read();
                    if (nextByte == -1) {
                        throw new Exception("File size is smaller than reported");
                    }

                    fileContents[i] = (byte) nextByte;
                }
                return fileContents ;

            } catch (Exception e) {
                System.err.println(e.getMessage());
                System.err.println(e.getStackTrace());

            }
        }
        return null ;
    }
}
