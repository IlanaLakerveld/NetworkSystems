package com.nedap.university;

/**
 * This class create header according to  my protocol
 */

public final class MakePacket {
    public static int udpHeaderLength = 8;
    public static int persionalizedHeaderLength = 16;


    /**
     *
     * @param payload actual data
     * @return the packet you can send nicely ordered.
     */
    public static byte[] makePacket(byte[] payload){
//       byte[] file =filename.getBytes();



        // todo
        return null ;
    }

    /**
     * This makes an array bytes that created a header as described in my report.
     * |            SequenceNumber          |
     * |                 Ack                |
     * |data offset|flags|   Window size    |
     * |    checksum     |   Session number |
     *
     * @param sequenceNumber
     * @param ack
     * @param flags
     * @param windowSize
     * @param sessionNumber
     * @return The personalizedHeader as a byte array
     */
    // todo add a checksum
    private static byte[] personalizedHeader(int sequenceNumber, int ack, byte flags , int windowSize, int sessionNumber){
        byte[] header = new byte[persionalizedHeaderLength] ;
        // sequence number
        header[0] = (byte) ((sequenceNumber >> 24) & 0xff);
        header[1] = (byte) ((sequenceNumber >> 16) & 0xff);
        header[2] = (byte) ((sequenceNumber >> 8) & 0xff);
        header[3] = (byte) (((sequenceNumber) & 0xff));
        // ack
        header[4] = (byte) ((ack >> 24) & 0xff);
        header[5] = (byte) ((ack >> 16) & 0xff);
        header[6] = (byte) ((ack >> 8) & 0xff);
        header[7] = (byte) ((ack) & 0xff);
        // data offset
        header[8] = (byte) ((persionalizedHeaderLength/ 4) << 4);
        // flags
        header[9] = flags ;
        // window size
        header[10] = (byte) (windowSize >> 8);
        header[11] = (byte) (windowSize & 0xff);
        // checksum
        header[12] = 0 ;
        header[13] = 0 ;
        // session number
        header[14] = (byte) (sessionNumber >> 8) ;
        header[15]= (byte) (sessionNumber & 0xff) ;
        return null;
    }



    private int[] createTCPHeader(int TCPHeaderSize, int sequenceNumber, int ack, int flag, int sourcePort, int destinationPort, int windowSize) {

        int[] TCPHeader = new int[TCPHeaderSize];
        // source port
        TCPHeader[0] = sourcePort >> 8;
        TCPHeader[1] = sourcePort & 0xff;
        // destination port
        TCPHeader[2] = destinationPort >> 8;
        TCPHeader[3] = destinationPort & 0xff;

        // SEQUENCE NUMBER  ;
        TCPHeader[4] = (sequenceNumber >> 24) & 0xff;
        TCPHeader[5] = (sequenceNumber >> 16) & 0xff;
        TCPHeader[6] = (sequenceNumber >> 8) & 0xff;
        TCPHeader[7] = (sequenceNumber) & 0xff;

        // ACK
        TCPHeader[8] = (ack >> 24) & 0xff;
        TCPHeader[9] = (ack >> 16) & 0xff;
        TCPHeader[10] = (ack >> 8) & 0xff;
        TCPHeader[11] = (ack) & 0xff;
        // Data offset
        TCPHeader[12] = (TCPHeaderSize / 4) << 4;

        //flags
        TCPHeader[13] = flag;

        // Todo do not hard code is.
        // Window Size
        TCPHeader[14] = windowSize >> 8;
        TCPHeader[15] = windowSize & 0xff;


        // Todo dit klopt nog niet hier pseudoheader maken voor checksum  ;)
//        int[] psuedoHeader = new int[36];
//        int[] arrSourceAddress = getArrayOfAddresses(sourceAddress);
//        int[] arrDestinationAddress = getArrayOfAddresses(destinationAddress);
//        System.arraycopy(arrSourceAddress, 0, psuedoHeader, 0, 16);
//        System.arraycopy(arrDestinationAddress, 0, psuedoHeader, 16, 16);
//        psuedoHeader[32] = 0 ;
//        psuedoHeader[33]= 253 ;
//        psuedoHeader[34] = 0  ;
//        psuedoHeader[35] = TCPHeaderSize ;
//        int[] forChecksum = new int[64] ;
//        int[] a = Arrays.copyOfRange(TCPHeader, 0, 16) ;
//        System.arraycopy( a , 0, forChecksum, 0, 16);
//        System.arraycopy(psuedoHeader  , 0, forChecksum, 16, 36);


//        int checksum = checksum(forChecksum);
//        TCPHeader[16] = (checksum >> 8 ) & 0xff;
//        TCPHeader[17] = checksum & 0xff ;

        return  TCPHeader;
    }




    // todo version en header length zijn nu hard coded
    public static Integer[] ipv4Header(byte[] data,boolean lastData){
        Integer[] ipv4Header = new Integer[20];
        ipv4Header[0] = (4 << 4) | (ipv4Header.length/4) ; // Version en Header length
        // byte 1 is TOS

        // length of data gram
        int lengthOfDatagram = data.length + ipv4Header.length ;
        ipv4Header[2]=  lengthOfDatagram >>8 ;
        ipv4Header[3]= lengthOfDatagram & 0xff ;

        // 4 -5 is ident en
        if(lastData){  // 6 is flags
            ipv4Header[6] = 0 ;
        }
        else{
            ipv4Header[6] = 1 ;
        }
        //and 7 is offset


        ipv4Header[8] = 30 ; // TODO make here an none magic number time to life
        ipv4Header[9] = 17 ; // udp protocol number
//        ipv4Header[10] & ipv4Header[11] = checksum //Todo checksum ? of CRC en wat de input : Header ipv4

        // 12 - 15 zijn source address
        // 16 -19 zijn destination address

       return ipv4Header ;
    }


    public static Integer[] udpHeader(int scrPort, int destPort, Integer[] data){

        Integer[] udpHeader = new Integer[udpHeaderLength];
        udpHeader[0]= scrPort  >> 8 ;
        udpHeader[1]= scrPort  & 0xff;
        udpHeader[2] = destPort >> 8 ;
        udpHeader[3] = destPort & 0xff ;
        udpHeader[4] = ( data.length + udpHeaderLength) >>8;
        udpHeader[5] = ( data.length + udpHeaderLength) & 0xff;

        udpHeader[6]= 0  >> 8 ; //Todo checkup ;
        udpHeader[7]= 0  & 0xff; //Todo checkup ;
        return udpHeader;
    }


    /**
     * This is a one's complement arithmetic checksum
     * @param checksumInput input for the checksum
     * @return a 2 byte array containing the answer of the checksum
     */
    private static int checksum(int[] checksumInput) {

        int sum = 0;
        int length = checksumInput.length;
        int j = 0;
        while (length > 1) {
            sum += ((checksumInput[j] << 8) & 0xFF00) | ((checksumInput[j + 1]) & 0xFF);
            if ((sum & 0xFFFF0000) > 0) {
                sum &= 0xFFFF; //
                sum++;
            }
            j += 2;
            length -= 2;

        }
        if (length == 1) {
            sum += (checksumInput[j] << 8 & 0xFF00);
            if ((sum & 0xFFFF0000) > 0) {
                sum &= 0xFFFF;
                sum++;
            }
        }

        sum = ~sum & 0xFFFF;

        return sum;
    }


}
