package com.dns.server.service;

import com.dns.server.model.QueryType;
import com.poly.dnshelper.model.DNSMessage;
import com.poly.dnshelper.model.answer.*;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import static com.dns.server.model.QueryType.*;

public class CommunicatorService implements Runnable {

    private DatagramSocket socket;

    private static Logger LOG = LoggerFactory.getLogger(CommunicatorService.class);

    public CommunicatorService() throws SocketException {
        socket = new DatagramSocket(53);
    }

    public void run() {
        boolean running = true;
        DNSMessage dnsMessage = new DNSMessage();
        while (running) {
            byte[] buf = new byte[2048];
            DatagramPacket packet
                    = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
                dnsMessage = DNSMessage(Arrays.copyOf(buf, packet.getLength()), null);
            } catch (IOException e) {
                LOG.error("Error during receiving");
                LOG.info("Clearing buffer");
                buf = new byte[2048];
                continue;
            }

            dnsMessage.getDnsFlags().setResponse(true);
            dnsMessage.setAnswers(new LinkedList<>());
            dnsMessage.getAnswers().add(getAnswer(dnsMessage));
            dnsMessage.setAnswerRRs((short) 1);
            byte[] messageBytes = dnsMessage.getMessageBytes();
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(messageBytes, messageBytes.length, address, port);
            try {
                socket.send(packet);
            } catch (IOException e) {
                LOG.warn("Error during sending");
            }
        }
        socket.close();
    }

    private DNSAnswer getAnswer(DNSMessage message) {
        switch (message.getQuestions().get(0).getType()) {
            case (short) 1: {
                DNSAnswerA dnsAnswer = new DNSAnswerA();
                dnsAnswer.setDnsClass((short) 1);
                dnsAnswer.setType((short) QueryType.valueOf(A));
                dnsAnswer.setTimeToLive(Integer.MAX_VALUE);
                dnsAnswer.setDataLength((short) 4);
                dnsAnswer.setResourceData(getRandomByteArr(4));
                return dnsAnswer;
            }
            case (short) 28: {
                DNSAnswerAAAA dnsAnswer = new DNSAnswerAAAA();
                dnsAnswer.setDnsClass((short) 1);
                dnsAnswer.setType((short) QueryType.valueOf(AAAA));
                dnsAnswer.setTimeToLive(Integer.MAX_VALUE);
                dnsAnswer.setDataLength((short) 16);
                dnsAnswer.setResourceData(getRandomByteArr(16));
                return dnsAnswer;
            }
            case (short) 15: {
                DNSAnswerMX dnsAnswer = new DNSAnswerMX();
                dnsAnswer.setDnsClass((short) 1);
                dnsAnswer.setType((short) QueryType.valueOf(MX));
                dnsAnswer.setTimeToLive(Integer.MAX_VALUE);
                dnsAnswer.setDataLength((short) 18);
                dnsAnswer.setPreference((short) 30);
                dnsAnswer.setResourceData(hexStringToByteArray("03747874037478740374787403747874"));
                return dnsAnswer;
            }
            case (short) 16: {

                DNSAnswerTXT dnsAnswer = new DNSAnswerTXT();
                dnsAnswer.setDnsClass((short) 1);
                dnsAnswer.setType((short) QueryType.valueOf(TXT));
                dnsAnswer.setTimeToLive(Integer.MAX_VALUE);
                dnsAnswer.setDataLength((short) 16);
                dnsAnswer.setTxtLength((byte) 15);
                dnsAnswer.setResourceData("txttxttxttxttxt".getBytes());
                return dnsAnswer;
            }
            default:
                throw new IllegalArgumentException();
        }
    }

    private byte[] getRandomByteArr(int size) {
        byte[] arr = new byte[size];
        Random random = new Random();
        random.nextBytes(arr);
        return arr;
    }

    //SOURCE: https://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java/140861#140861

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
