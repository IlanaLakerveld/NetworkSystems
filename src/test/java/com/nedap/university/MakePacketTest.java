package com.nedap.university;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class test the functions is the Make Packet class
 */
class MakePacketTest {
    /**
     * Test the checksum with some different input
     */
    @Test
    public void testingTheChecksum(){
        byte [] arr = new byte[100] ;
        int checksum = MakePacket.checksum(arr);
        assertEquals(65535,checksum);
        byte[] arr2 = new byte[64] ;
        arr2[1]=1;
        int check2 = MakePacket.checksum(arr2);
        assertEquals(65534,check2);

        byte[] arr3 = new byte[64] ;
        arr3[1]=1;
        arr3[3]=1;
        int check3 = MakePacket.checksum(arr3);
        assertEquals(65533,check3);

        byte[] arr4 = new byte[64] ;
        Arrays.fill( arr4, (byte) 255 );
        int check4 = MakePacket.checksum(arr4);
        assertEquals(0,check4);

    }

    /**
     * Checks if the function that makes the input for the checksum does this correctly.
     *
     */
    @Test
    public void getInputforChecksumWithoutHeaderTest(){
        byte[] byteArray = new byte[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1} ;
        byte[] output = MakePacket.getInputForChecksumWithoutHeader(byteArray);
        // output 12 and 13 is where the checksum is in the header this should not be givin as input for the checksum and set to zero
        assertEquals(0,output[12]) ;
        assertEquals(0,output[13]);
        // Check if length of the byte array
        assertEquals(byteArray.length,output.length);

    }

    /**
     * Checks if the getSomething functions correspond with the actual file.
     */
    @Test
    public void personalizedHeaderTest(){
        int sequenceNumber = 2838506 ;
        int ack = 3154321;
        byte flags = 0b100 ;
        int windowSize = 4236 ;
        int sessionNumber = 23;

        byte[] output = MakePacket.personalizedHeader(sequenceNumber, ack, flags, windowSize, sessionNumber,new byte[]{1,'a',54});

        assertEquals(sequenceNumber, MakePacket.getSequenceNumber(output));
        assertEquals(ack, MakePacket.getAckNumber(output));
        assertEquals(flags, MakePacket.getFlag(output));
        assertEquals(windowSize,MakePacket.getWindowsize(output));
        assertEquals(sessionNumber,MakePacket.getSessionNumber(output));


    }


    /**
     * Test if the output of test flags correspond with the flags set
     */
    @Test
    public void setFlagsTest(){
        byte flags = MakePacket.setFlags(false, false,false,false,false,false,false);
        assertEquals(0,flags);
        byte flags1 = MakePacket.setFlags(true, false,false,false,false,false,false);
        assertEquals(1,flags1);
        byte flags2 = MakePacket.setFlags(false, true,false,false,false,false,false);
        assertEquals(2,flags2);
        byte flags3 = MakePacket.setFlags(true, true,false,false,false,false,false);
        assertEquals(0b11,flags3);
        assertEquals(0b100,MakePacket.setFlags(false, false,true,false,false,false,false));
        assertEquals(0b1000,MakePacket.setFlags(false, false,false,true,false,false,false));
        assertEquals(0b10000,MakePacket.setFlags(false,false,false,false,true,false,false));
        assertEquals(0b100000,MakePacket.setFlags(false,false,false,false,false,true,false));
        assertEquals(0b1000000,MakePacket.setFlags(false,false,false,false,false,false,true));

    }

}