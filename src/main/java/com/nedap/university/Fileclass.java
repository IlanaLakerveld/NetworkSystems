package com.nedap.university;

import java.io.*;

/**
 * This class changes file into bytes or bytes into files
 */
public final class Fileclass {

    public static byte[] loadFile(File file) {

        try (FileInputStream fileStream = new FileInputStream(file)) {
            byte[] fileContents = new byte[(int) file.length()];
            for (int i = 0; i < fileContents.length; i++) {
                fileContents[i] = (byte) fileStream.read();
            }
            fileStream.close();
            return fileContents;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    public static void makeFileFromBytes(String filename, byte[] fileInBytes) throws IOException {
        String filenameTrimmed = filename.trim();
        File fileToWrite = new File(filenameTrimmed);

        // because file does not exist
        fileToWrite.createNewFile();

        try (FileOutputStream fileStream = new FileOutputStream(fileToWrite)) {
            for (byte fileContent : fileInBytes) {
                fileStream.write(fileContent);
            }
        }


    }

}
