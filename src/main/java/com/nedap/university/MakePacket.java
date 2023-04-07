package com.nedap.university;

/**
 * This class create header according to  my protocol
 */

public final class MakePacket {
    public static int persionalizedHeaderLength = 16;


    /**
     *
     * @param payload actual data
     * @return the packet you can send nicely ordered.
     */
    public static byte[] makePacket(byte[] payload ,int sequenceNumber, int ack, byte flags , int windowSize, int sessionNumber){
       byte[] header = personalizedHeader(sequenceNumber,ack, flags ,windowSize,sessionNumber) ;
       byte[] packet = new byte[payload.length+persionalizedHeaderLength];
        System.arraycopy(header,0,packet,0,persionalizedHeaderLength);
        System.arraycopy(payload,0,packet,persionalizedHeaderLength,payload.length);
        return packet ;
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
