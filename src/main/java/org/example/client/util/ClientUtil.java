package org.example.client.util;

import org.example.util.NtpPacket;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ClientUtil {
    private static final long OFFSET_MILLISECONDS_1900_TO_1970 = (365 * 70 + 17) * 24L * 60L * 60L * 1000L;

    public static byte[] pack(long receiveTime) {
        byte[] ntpPacket = new byte[48];
        ntpPacket[0] = (byte) 0b0000_1011;
        long requestTime = convertToNtpTimestamp(receiveTime);
        byte[] clientTime = longToBytes(requestTime);
        System.arraycopy(clientTime, 0, ntpPacket, 40, 8);
        return ntpPacket;
    }

    public static NtpPacket unpack(byte[] buf) {
        NtpPacket clientNtpPacket = new NtpPacket();
        clientNtpPacket.setLeapIndicator((byte) (buf[0] & 0b1100_0000));
        clientNtpPacket.setVersionNumber((byte) (buf[0] & 0b0011_1000));
        clientNtpPacket.setMode((byte) (buf[0] & 0b0000_0111));
        clientNtpPacket.setStratum(buf[1]);
        clientNtpPacket.setPoll(buf[2]);
        clientNtpPacket.setPrecision(buf[3]);

        byte[] clientReferenceTimestampByteArray = Arrays.copyOfRange(buf, 16, 24);
        byte[] clientOriginateTimestampByteArray = Arrays.copyOfRange(buf, 24, 32);
        byte[] clientReceiveTimestampByteArray = Arrays.copyOfRange(buf, 32, 40);
        byte[] clientTransmitTimestampByteArray = Arrays.copyOfRange(buf, 40, 48);

        long clientReferenceTimestamp = ByteBuffer.wrap(clientReferenceTimestampByteArray).getLong();
        long clientOriginateTimestamp = ByteBuffer.wrap(clientOriginateTimestampByteArray).getLong();
        long clientReceiveTimestamp = ByteBuffer.wrap(clientReceiveTimestampByteArray).getLong();
        long clientTransmitTimestamp = ByteBuffer.wrap(clientTransmitTimestampByteArray).getLong();

        clientNtpPacket.setReferenceTimestamp(clientReferenceTimestamp);
        clientNtpPacket.setOriginateTimestamp(clientOriginateTimestamp);
        clientNtpPacket.setReceiveTimestamp(clientReceiveTimestamp);
        clientNtpPacket.setTransmitTimestamp(clientTransmitTimestamp);
        return clientNtpPacket;
    }

    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public static long convertToNtpTimestamp(long time) {

        long adjustedTime = time + OFFSET_MILLISECONDS_1900_TO_1970;

        long seconds = adjustedTime / 1000;
        long fraction = ((adjustedTime % 1000) * 0x100000000L) / 1000;

        return seconds << 32 | fraction;
    }
}
