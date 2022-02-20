package org.example.util;

import java.nio.ByteBuffer;
import java.util.Arrays;


public class Utils {
    private static final long OFFSET_MILLISECONDS_1900_TO_1970 = (365 * 70 + 17) * 24L * 60L * 60L * 1000L;
    private static final byte LEAP_INDICATOR = 0;
    private static final byte MODE = 4;
    private static final byte STRATUM = 2;
    private static final byte POLL = 0;
    private static final byte PRECISION = 20;
    private static final int ROOT_DELAY = 0;
    private static final int ROOT_DISPERSION = 4;

    public static NtpPacket pack(NtpPacket clientNtpPacket, long timeOfReceive, byte[] hostAddress) {
        NtpPacket serverNtpPacket = new NtpPacket();

        long receiveTimestamp = convertToNtpTimestamp(System.currentTimeMillis());
        int serverAddress = ByteBuffer.wrap(hostAddress).getInt();

        serverNtpPacket.setLeapIndicator(LEAP_INDICATOR);
        serverNtpPacket.setVersionNumber(clientNtpPacket.getVersionNumber());
        serverNtpPacket.setMode(MODE);
        serverNtpPacket.setStratum(STRATUM);
        serverNtpPacket.setPoll(POLL);
        serverNtpPacket.setPrecision(PRECISION);
        serverNtpPacket.setRootDelay(ROOT_DELAY);
        serverNtpPacket.setRootDispersion(ROOT_DISPERSION);
        serverNtpPacket.setReferenceIdentifier(serverAddress);
        serverNtpPacket.setReferenceTimestamp(receiveTimestamp);
        serverNtpPacket.setOriginateTimestamp(clientNtpPacket.getOriginateTimestamp());
        serverNtpPacket.setReceiveTimestamp(convertToNtpTimestamp(timeOfReceive));
        serverNtpPacket.setTransmitTimestamp(convertToNtpTimestamp(System.currentTimeMillis()));

        return serverNtpPacket;
    }

    public static NtpPacket unpack(byte[] buf) {
        NtpPacket clientNtpPacket = new NtpPacket();

        clientNtpPacket.setLeapIndicator((byte) (buf[0] & 0b1100_0000));
        clientNtpPacket.setVersionNumber((byte) (buf[0] & 0b0011_1000));
        clientNtpPacket.setMode((byte) (buf[0] & 0b0000_0111));
        clientNtpPacket.setStratum(buf[1]);
        clientNtpPacket.setPoll(buf[2]);
        clientNtpPacket.setPrecision(buf[3]);

        byte[] clientTransmitByteArray = Arrays.copyOfRange(buf, 40, 48);

        long clientTransmitTimestamp = ByteBuffer.wrap(clientTransmitByteArray).getLong();

        clientNtpPacket.setTransmitTimestamp(clientTransmitTimestamp);

        return clientNtpPacket;
    }

    public static long convertToNtpTimestamp(long time) {

        long adjustedTime = time + OFFSET_MILLISECONDS_1900_TO_1970;

        long seconds = adjustedTime / 1000;
        long fraction = ((adjustedTime % 1000) * 0x100000000L) / 1000;

        return seconds << 32 | fraction;
    }
}
