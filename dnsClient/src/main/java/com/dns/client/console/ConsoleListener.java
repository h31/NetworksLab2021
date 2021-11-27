package com.dns.client.console;

import com.dns.client.model.QueryType;
import com.dns.client.service.CommunicatorService;
import com.poly.dnshelper.model.DNSMessage;
import com.poly.dnshelper.model.answer.DNSAnswerTXT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleListener {

    private CommunicatorService communicatorService;

    public ConsoleListener(CommunicatorService communicatorService) {
        this.communicatorService = communicatorService;
    }

    public void startListen() throws IOException {
        while (true) {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in));

            String line = reader.readLine();
            String name = line.split(" ")[0];
            String type = line.split(" ")[1];

            if (name.equals(";;;")) {
                return;
            }

            // Printing the read line
            DNSMessage dnsMessage = communicatorService.sendQuery(name, QueryType.valueOf(type.toUpperCase()));
            for (int i = 0; i < dnsMessage.getAnswers().size(); i++) {
                byte[] byteData = dnsMessage.getAnswers().get(i).getResourceData();
                switch (QueryType.valueOf(dnsMessage.getQuestions().get(0).getType())) {
                    case A:
                        System.out.println(getA(byteData));
                        break;
                    case MX:
                        System.out.println(getMX(byteData, name));
                        break;
                    case AAAA:
                        System.out.println(getAAAA(byteData));
                        break;
                    case TXT:
                        System.out.println(getTXT(byteData, ((DNSAnswerTXT) dnsMessage.getAnswers().get(i)).getTxtLength()));
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown type");
                }
            }
        }
    }

    private String getA(byte[] bytesIp) {
        StringBuilder sb = new StringBuilder();
        for (Byte b : bytesIp) {
            sb.append(".");
            sb.append(Byte.toUnsignedInt(b));
        }
        return sb.substring(1);
    }

    private String getAAAA(byte[] bytesIp) {
        StringBuilder sb = new StringBuilder();
        byte[] pair = new byte[2];
        for (int i = 0; i < bytesIp.length; i += 2) {
            sb.append(":");
            pair[0] = bytesIp[i];
            pair[1] = bytesIp[i + 1];
            sb.append(bytesToHex(pair));
        }
        return sb.substring(1);
    }

    private String getMX(byte[] byteData, String host) {
        int i = 0;
        StringBuilder sb = new StringBuilder();
        while (i < byteData.length - 4) {
            int size = byteData[i];
            byte[] tempArray = new byte[size];
            i++;
            System.arraycopy(byteData, i, tempArray, 0, i + size - i);
            i += size;
            sb.append(new String(tempArray));
            sb.append(".");
        }
        sb.append(host);
        return sb.toString();
    }

    private String getTXT(byte[] byteData, int textLength) {
        return new String(byteData).substring(0, textLength);
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
