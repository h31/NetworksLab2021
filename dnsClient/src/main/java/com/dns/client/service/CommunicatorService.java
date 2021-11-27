package com.dns.client.service;


import com.dns.client.model.QueryType;
import com.poly.dnshelper.model.DNSFlags;
import com.poly.dnshelper.model.DNSMessage;
import com.poly.dnshelper.model.DNSQuery;

import java.io.IOException;
import java.net.*;
import java.util.Collections;

public class CommunicatorService {

    private DatagramSocket socket;
    private InetAddress address;

    private byte[] buf;


    public CommunicatorService(String dnsServerHost) throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        address = InetAddress.getByName(dnsServerHost);
    }

    public DNSMessage sendQuery(String hostToResolve, QueryType queryType) throws IOException {
        DNSMessage preparedMessage = prepareMessage(hostToResolve, queryType);

        buf = preparedMessage.getMessageBytes();
        DatagramPacket packet
                = new DatagramPacket(buf, buf.length, address, 53);
        socket.send(packet);

        buf = new byte[2048];
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);

        DNSMessage message = new DNSMessage();
        message.mapperMessage(packet.getData(), preparedMessage);
        return message;
    }

    private DNSMessage prepareMessage(String hostToResolve, QueryType queryType) {
        DNSMessage dnsMessage = new DNSMessage();
        dnsMessage.setDnsFlags(new DNSFlags(
                false,
                (byte) 0,
                false,
                false,
                true,
                false,
                (byte) 0));
        dnsMessage.setTransactionId((short) 111);
        dnsMessage.setNumOfQuestions((short) 1);
        dnsMessage.setQuestions(Collections.singletonList(new DNSQuery(hostToResolve, (short) QueryType.valueOf(queryType), (short) 1)));
        return dnsMessage;
    }

}
