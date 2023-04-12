package com.nedap.university;

import java.io.*;

public final class Fileclass {

    // Todo from challenge 2
    public static byte[] loadFile(File fileToTransmit) {

        try (FileInputStream fileStream = new FileInputStream(fileToTransmit)) {
            byte[] fileContents = new byte[(int) fileToTransmit.length()];

            for (int i = 0; i < fileContents.length; i++) {
                int nextByte = fileStream.read();
                fileContents[i] = (byte) nextByte;
            }
            fileStream.close();
            return fileContents;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    public static void makeFileFromBytes(String filename, byte[] fileInBytes) {
        String filenametest = filename.trim();
        File fileToWrite = new File(filenametest);
        try {
            fileToWrite.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (FileOutputStream fileStream = new FileOutputStream(fileToWrite)) {
            for (byte fileContent : fileInBytes) {
                fileStream.write(fileContent);
            }
            fileStream.close(); //toDO why is this redundant?
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
