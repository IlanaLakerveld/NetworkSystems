package com.nedap.university;

import java.io.*;

/**
 * This class changes file into bytes or bytes into files
 */
public final class Fileclass {

    // TODO will je hier andere manier exceptions opvangen
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

    public static void makeFileFromBytes(String filename, byte[] fileInBytes) {
        String filenametest = filename.trim();
        File fileToWrite = new File(filenametest);
        // because file does not exist
        try {
            fileToWrite.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (FileOutputStream fileStream = new FileOutputStream(fileToWrite)) {
            for (byte fileContent : fileInBytes) {
                fileStream.write(fileContent);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

}
