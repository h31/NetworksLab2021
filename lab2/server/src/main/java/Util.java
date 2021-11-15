import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

public class Util {


    private final static long MILLISECONDS_BETWEEN_1900_AND_1970 = (365 * 70 + 17) * 24L * 60L * 60L * 1000L;


    public static NtpPacket pack(NtpPacket clientRequest, long timeOfReceipt) {
        NtpPacket serverResponse = new NtpPacket();

        //получить данные часов
        long time = getCurrentTime();
        System.out.println(time);


        //преобразования часов к NTP Timestamp
        long ntpTimestamp = convertToNtpTimestamp(time);

        serverResponse.setLeapIndicator((byte) 0b0000_0000);
        serverResponse.setVersionNumber(clientRequest.getVersionNumber());
        serverResponse.setMode((byte) 4);
        serverResponse.setStratum((byte) 3);
        serverResponse.setPoll((byte) 4);
        serverResponse.setPrecision((byte) -15);
        serverResponse.setRootDelay(0);
        serverResponse.setRootDispersion(0);
        serverResponse.setReferenceIdentifier(0);
        serverResponse.setReferenceTimestamp(ntpTimestamp);

        serverResponse.setOriginateTimestamp(clientRequest.getTransmitTimestamp());

        serverResponse.setReceiveTimestamp(convertToNtpTimestamp(timeOfReceipt));

        long dispatchTime = System.currentTimeMillis();
        serverResponse.setTransmitTimestamp(convertToNtpTimestamp(dispatchTime));

        return serverResponse;
    }

    public static NtpPacket unpack(byte[] buf) {
        NtpPacket clientRequest = new NtpPacket();

        byte leapIndicator = (byte) (buf[0] & 0b1100_0000);
        byte versionNumber = (byte) (buf[0] & 0b0011_1000);
        byte mode = (byte) (buf[0] & 0b0000_0111);
        clientRequest.setLeapIndicator(leapIndicator);
        clientRequest.setVersionNumber(versionNumber);
        clientRequest.setMode(mode);
        clientRequest.setStratum(buf[1]);
        clientRequest.setPoll(buf[2]);
        clientRequest.setPrecision(buf[3]);

        byte[] clientTransmitByteArray = Arrays.copyOfRange(buf, 40, 48);
        long clientTransmitTimestamp = ByteBuffer.wrap(clientTransmitByteArray).getLong();

        clientRequest.setTransmitTimestamp(clientTransmitTimestamp);

        return clientRequest;
    }

    private static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    private static long convertToNtpTimestamp(long time) {

        long adjustedTime = time + MILLISECONDS_BETWEEN_1900_AND_1970;

        long seconds = adjustedTime / 1000;
        long fraction = ((adjustedTime % 1000) * 0x100000000L) / 1000;

        return seconds << 32 | fraction;
    }

    //reserved method
    private static ZonedDateTime timestampToUTC(long time) {
        long seconds = (time >>> 32) & 0xffffffffL;
        long fraction = time & 0xffffffffL;
        return LocalDateTime.parse("1900-01-01T00:00:00").atZone(ZoneId.of("UTC"))
                .plusSeconds(seconds)
                .plusNanos((long)(1000000000.0 / (1L << 32) * fraction));
    }


    //reserved method
    public static long bytesToLong(final byte[] b) {
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

    //reserved method
    private static int convertBytesToInteger(int startByte, int endByte, byte[] bytes) {
        byte[] bufferByteArray = new byte[endByte - startByte + 1];

        int j = 0;
        for (int i = startByte; i < endByte + 1; i++) {
            bufferByteArray[j] = bytes[i];
            j++;
        }

        return ByteBuffer.wrap(bufferByteArray).getInt();
    }

}
