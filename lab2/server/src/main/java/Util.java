import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;

public class Util {


    private final static long MILLISECONDS_BETWEEN_1900_AND_1970 = (365 * 70 + 17) * 24L * 60L * 60L * 1000L;
    private static byte[] clientByteArray;


    public static NtpPacket pack(NtpPacket clientRequest, long timeOfReceipt) {
        NtpPacket serverResponse = new NtpPacket();

        //получить последние показания часов на сервере
        long time = getCurrentTime();

        //преобразования часов к NTP Timestamp
        long ntpTimestamp = convertToNtpTimestamp(time);

        System.out.println(timestampToUTC(ntpTimestamp));


        serverResponse.setLeapIndicator((byte) 0b0000_0000);
        serverResponse.setVersionNumber(clientRequest.getVersionNumber());
        serverResponse.setMode((byte) 4);
        serverResponse.setStratum((byte) 3);
        serverResponse.setPoll((byte) 2);
        serverResponse.setPrecision((byte) 0); // уточняем?
        serverResponse.setRootDelay(0);
        serverResponse.setRootDispersion(0);
        serverResponse.setReferenceIdentifier(0);
        serverResponse.setReferenceTimestamp(ntpTimestamp);

        byte[] originByteArray = Arrays.copyOfRange(clientByteArray, 40, 48);
        long originTimestamp = ByteBuffer.wrap(originByteArray).getLong();
        serverResponse.setOriginateTimestamp(originTimestamp);
        System.out.println("client transmit = " + clientRequest.getTransmitTimestamp());
        serverResponse.setReceiveTimestamp(convertToNtpTimestamp(timeOfReceipt));
        long dispatchTime = System.currentTimeMillis();
        serverResponse.setTransmitTimestamp(convertToNtpTimestamp(dispatchTime));

        return serverResponse;
    }

    public static NtpPacket unpack(byte[] buf) {
        NtpPacket clientRequest = new NtpPacket();
        clientByteArray = buf;

        System.out.println("изначальный массив байтов = " + Arrays.toString(buf));

        byte leapIndicator = (byte) (buf[0] & 0b1100_0000);
        byte versionNumber = (byte) (buf[0] & 0b0011_1000);
        byte mode = (byte) (buf[0] & 0b0000_0111);
        clientRequest.setLeapIndicator(leapIndicator);
        clientRequest.setVersionNumber(versionNumber);
        clientRequest.setMode(mode);
        clientRequest.setStratum(buf[1]);
        clientRequest.setPoll(buf[2]);
        clientRequest.setPrecision(buf[3]);
        clientRequest.setRootDelay(convertBytesToInteger(4, 7, buf));
        clientRequest.setRootDispersion(convertBytesToInteger(8, 11, buf));
        clientRequest.setReferenceIdentifier(convertBytesToInteger(12, 15, buf));
/*        clientRequest.setReferenceTimestamp(convertBytesToLong(16, 23, buf));
        clientRequest.setOriginateTimestamp(convertBytesToLong(24, 31, buf));
        clientRequest.setReceiveTimestamp(convertBytesToLong(32, 39, buf));*/
        clientRequest.setTransmitTimestamp(convertBytesToLong(40, 47, buf));

        convertBytesToString(40, 47, buf);

        System.out.println(clientRequest);

        return clientRequest;
    }

    private static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    private static long convertToNtpTimestamp(long time) {

        long adjustedTime = time + MILLISECONDS_BETWEEN_1900_AND_1970;

        long seconds = adjustedTime / 1000;
        long fraction = ((adjustedTime % 1000) * 0x100000000L) / 1000;

        System.out.println("мои секунды = " + seconds);
        System.out.println("мои fraction = " + fraction);

        //seconds |= 0x80000000L;

        return seconds << 32 | fraction;
    }

    private static ZonedDateTime timestampToUTC(long time) {
        long seconds = (time >>> 32) & 0xffffffffL;
        long fraction = time & 0xffffffffL;
        return LocalDateTime.parse("1900-01-01T00:00:00").atZone(ZoneId.of("UTC"))
                .plusSeconds(seconds)
                .plusNanos((long)(1000000000.0 / (1L << 32) * fraction));
    }

    private static ZonedDateTime convertBytesToString(int startByte, int endByte, byte[] bytes) {
        byte[] secondsByteArray = new byte[4];
        byte[] fractionByteArray = new byte[] {bytes[endByte-3], bytes[endByte-2], bytes[endByte-1], bytes[endByte]};
        long result = 0L;
        StringBuilder str = new StringBuilder();
        StringBuilder str1 = new StringBuilder();

        secondsByteArray[0] = (bytes[startByte]);
        secondsByteArray[1] = (bytes[startByte+1]);
        secondsByteArray[2] = (bytes[startByte+2]);
        secondsByteArray[3] = (bytes[startByte+3]);

        fractionByteArray[0] = (bytes[endByte-3]);
        fractionByteArray[1] = (bytes[endByte-2]);
        fractionByteArray[2] = (bytes[endByte-1]);
        fractionByteArray[3] = (bytes[endByte]);


        for (int i = startByte; i < endByte + 1; i++) {
            str.append(bytes[i] & 0xFF);
            str1.append(Integer.toHexString(bytes[i] & 0xFF ));
            System.out.println(Integer.toHexString(bytes[i] & 0xFF ));
        }

        long seconds = bytesToLong(secondsByteArray);
        long fraction = bytesToLong(fractionByteArray);

        System.out.println("какие должны быть секунды = " + seconds);
        System.out.println("какие должны быть fraction = " + fraction);
        System.out.println("а в сумме должно быть = " + (seconds << 32 | fraction));

        return LocalDateTime.parse("1900-01-01T00:00:00").atZone(ZoneId.of("UTC"))
                .plusSeconds(seconds)
                .plusNanos((long)(1000000000.0 / (1L << 32) * fraction));
    }



    public static long bytesToLong(final byte[] b) {
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

    private static long convertBytesToLong(int startByte, int endByte, byte[] bytes) {
        byte[] bufferByteArray = new byte[endByte - startByte + 1];
        long result = 0L;
        StringBuilder str = new StringBuilder();

        int j = 0;
        for (int i = startByte; i < endByte + 1; i++) {
            str.append(bytes[i] & 0xFF);
        }

        long seconds = Long.parseLong(str.toString().substring(0, 8), 16);
        return seconds * 1000L;
    }

    private static int convertBytesToInteger(int startByte, int endByte, byte[] bytes) {
        byte[] bufferByteArray = new byte[endByte - startByte + 1];
        int result = 0;

        int j = 0;
        for (int i = startByte; i < endByte + 1; i++) {
            bufferByteArray[j] = bytes[i];
            j++;
        }

        System.out.println("байты из которых надо слепить инт " + Arrays.toString(bufferByteArray));
        result = ByteBuffer.wrap(bufferByteArray).getInt();
        return result;
    }

}
