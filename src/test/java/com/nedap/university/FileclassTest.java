package com.nedap.university;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class FileclassTest {

    /**
     * This test tests if the filesize of the file is correct. The actual file size of the file (are the file of the example files I found when inspecting the files with finder. )
     */
    @Test
    public void loadFile() {
        File file = new File("example_files/tiny.pdf");
        byte[] bytes = Fileclass.loadFile(file);
        assertEquals(bytes.length,24286);

        File file2 = new File("example_files/medium.pdf");
        byte[] bytes2 = Fileclass.loadFile(file2);
        assertEquals(bytes2.length,475664);


        File file3 = new File("example_files/large.pdf");
        byte[] bytes3 = Fileclass.loadFile(file3);
        assertEquals(bytes3.length,31498458);

    }
}