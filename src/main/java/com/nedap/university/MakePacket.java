package com.nedap.university;

import java.util.Arrays;

/**
 * This class create header according to my protocol
 */

public final class MakePacket {
    public static int personalizedHeaderLength = 16;

    public static final byte ackFlagByte = MakePacket.setFlags(false, true, false, false, false, false, false);
    public static final byte sendFlagByte = MakePacket.setFlags(false, false, true, false, false, false, false);
    public static final byte getFlagByte = MakePacket.setFlags(false, false, false, true, false, false, false);
    public static final byte removeFlagByte = MakePacket.setFlags(false, false, false, false, true, false, false);
    public static final byte listFlagByte = MakePacket.setFlags(false, false, false, false, false, false, true);
    public static final byte replaceFlagByte = MakePacket.setFlags(false, false, true, false, true, false, false);
    public static final byte errorFlagByte = MakePacket.setFlags(false, false, false, false, false, true, false);
    public static final byte finFlagByte = MakePacket.setFlags(true, false, false, false, false, false, false);


    /**
     * Makes a packet with  a header
     *
     * @param payload actual data
     * @return the packet you can send nicely ordered.
     */
    public static byte[] makePacket(byte[] payload, int sequenceNumber, int ack, byte flags, int windowSize, int sessionNumber) {
        byte[] header = personalizedHeader(sequenceNumber, ack, flags, windowSize, sessionNumber, payload);
        byte[] packet = new byte[payload.length + personalizedHeaderLength];
        System.arraycopy(header, 0, packet, 0, personalizedHeaderLength);
        System.arraycopy(payload, 0, packet, personalizedHeaderLength, payload.length);
        return packet;
    }

    /**
     * This makes an array bytes that created a header as described in my report.
     * This is the structure of the header
     * |            SequenceNumber          |
     * |                 Ack                |
     * |data offset|flags|   Window size    |
     * |    checksum     |   Session number |
     *
     * @param sequenceNumber sequence number this packet
     * @param ack            Acknowledgement number of packet that acknowledges (if flag is set otherwise zero)
     * @param flags          Flags
     * @param windowSize     The current window size
     * @param sessionNumber  The current session number
     * @return The personalizedHeader as a byte array
     */

    public static byte[] personalizedHeader(int sequenceNumber, int ack, byte flags, int windowSize, int sessionNumber, byte[] payload) {
        byte[] header = new byte[personalizedHeaderLength];
        // sequence number
        header[0] = (byte) ((sequenceNumber >> 24) & 0xff);
        header[1] = (byte) ((sequenceNumber >> 16) & 0xff);
        header[2] = (byte) ((sequenceNumber >> 8) & 0xff);
        header[3] = (byte) (((sequenceNumber) & 0xff));
        // acknowledgement
        header[4] = (byte) ((ack >> 24) & 0xff);
        header[5] = (byte) ((ack >> 16) & 0xff);
        header[6] = (byte) ((ack >> 8) & 0xff);
        header[7] = (byte) ((ack) & 0xff);
        // data offset
        header[8] = (byte) ((personalizedHeaderLength / 4) << 4);
        // flags

        header[9] = flags;
        // Window size
        header[10] = (byte) (windowSize >> 8);
        header[11] = (byte) (windowSize & 0xff);


        // session number
        header[14] = (byte) (sessionNumber >> 8);
        header[15] = (byte) (sessionNumber & 0xff);

        // make input for checksum
        byte[] inputChecksum = new byte[header.length + payload.length];
        System.arraycopy(header, 0, inputChecksum, 0, header.length);
        System.arraycopy(payload, 0, inputChecksum, header.length, payload.length);
        int outputChecksum = checksum(inputChecksum);

        // checksum
        header[12] = (byte) (outputChecksum >> 8);
        header[13] = (byte) (outputChecksum & 0xff);

        return header;
    }


    /**
     * This is a one's complement arithmetic checksum
     *
     * @param checksumInput input for the checksum
     * @return a 2 byte array containing the answer of the checksum
     */
    public static int checksum(byte[] checksumInput) {

        int sum = 0;
        int length = checksumInput.length;
        int j = 0;
        // The output is 2 bytes, so 2 bytes should be put together
        while (length > 1) {
            sum += ((checksumInput[j] << 8) & 0xFF00) | ((checksumInput[j + 1]) & 0xFF);
            // If answer get larger than 2 bytes
            if ((sum & 0xFFFF0000) > 0) {
                sum &= 0xFFFF; //
                sum++;
            }
            j += 2;
            length -= 2;

        }
        // only if you have an unequal amount of bytes
        if (length == 1) {
            sum += (checksumInput[j] << 8 & 0xFF00);
            if ((sum & 0xFFFF0000) > 0) {
                sum &= 0xFFFF;
                sum++;
            }
        }
        // make inverse
        sum = ~sum & 0xFFFF;

        return sum;
    }


    /**
     * @param packet input inclusive the own created header
     * @return sequence number
     */
    public static int getSequenceNumber(byte[] packet) {

        return (((packet[0] & 0xff) << 24) | ((packet[1] & 0xff) << 16) | ((packet[2] & 0xff) << 8) | packet[3] & 0xff);
    }

    /**
     * @param packet input including the own created header
     * @return checksum value
     */
    public static int getCheckSumInteger(byte[] packet) {

        return ((packet[12] & 0xff) << 8) | (packet[13] & 0xff);
    }

    /**
     * @param packet input including the own created header
     * @return acknowledgement number
     */
    public static int getAckNumber(byte[] packet) {
        return ((packet[4] << 24) | ((packet[5] & 0xff) << 16) | ((packet[6] & 0xff) << 8) | (packet[7] & 0xff));
    }

    /**
     * @param packet input including the own created header
     * @return session number
     */
    public static int getSessionNumber(byte[] packet) {
        return ((packet[14] & 0xff) << 8) | (packet[15] & 0xff);
    }

    /**
     * @param packet input including the own created header
     * @return window size
     */
    public static int getWindowsize(byte[] packet) {

        return ((packet[10] & 0xff) << 8) | (packet[11] & 0xff);
    }


    /**
     * removes the values of the checksum to get the required input for the checksum
     *
     * @param packet input including the own created header
     * @return a byte array you can use as input for the function checksum
     */
    public static byte[] getInputForChecksumWithoutHeader(byte[] packet) {
        byte[] bytesForChecksum = Arrays.copyOfRange(packet, 0, MakePacket.personalizedHeaderLength);
        // The answer of the checksum should not be included as input for the checksum
        bytesForChecksum[12] = 0;
        bytesForChecksum[13] = 0;


        byte[] inputChecksum = new byte[packet.length];
        System.arraycopy(bytesForChecksum, 0, inputChecksum, 0, bytesForChecksum.length);
        System.arraycopy(packet, MakePacket.personalizedHeaderLength, inputChecksum, bytesForChecksum.length, (packet.length - MakePacket.personalizedHeaderLength));
        return inputChecksum;
    }

    public static byte getFlag(byte[] packet) {
        return packet[9];
    }


    /**
     * @param FIN true if fin flag needs to be set
     * @param ACK true if ack flag needs to be set
     * @return byte flag for the header
     */
    public static byte setFlags(boolean FIN, boolean ACK, boolean SEND, boolean GET, boolean REMOVE, boolean ERROR, boolean LIST) {
        byte flags = 0;
        if (FIN) {
            flags += 1;
        }
        if (ACK) {
            flags += 1 << 1;
        }
        if (SEND) {
            flags += 1 << 2;
        }
        if (GET) {
            flags += 1 << 3;
        }
        if (REMOVE) {
            flags += 1 << 4;
        }
        if (ERROR) {
            flags += 1 << 5;
        }
        if (LIST) {
            flags += 1 << 6;
        }
        return flags;
    }


}
