package Server;

import Utils.Command;
import Utils.MessageCreator;
import Utils.MessageReader;
import Utils.Parser;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.util.*;

public class Server {
    private static LinkedList<SocketChannel> userSocketChannels;
    private static HashMap<SocketChannel, String> userNames;

    public static void main(String[] args) throws IOException {
        userSocketChannels = new LinkedList<>();
        userNames = new HashMap<>();

        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress("localhost", 1111));
        serverSocketChannel.configureBlocking(false);
        int ops = serverSocketChannel.validOps();
        serverSocketChannel.register(selector, ops, null);

        while (true) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();

                if (key.isAcceptable()) {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    userSocketChannels.add(socketChannel);
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                }
                if (key.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    try {
                        boolean res = readMessage(key);
                        if (!res) break;
                    } catch (Exception e) {
                        String serverDate = getServerDate();
                        byte[] bytesToSend = MessageCreator.createMessage(userNames.get(socketChannel),
                                'c', "", serverDate);
                        closeSocket((SocketChannel) key.channel(), userNames.get(socketChannel));
                        writeToClients(bytesToSend);
                        break;
                    }
                }
                iterator.remove();
            }
        }
    }

    private static String getServerDate() {
        return Long.toString(Instant.now().toEpochMilli());
    }

    private static boolean readMessage(SelectionKey key) throws IOException {

        SocketChannel socketChannel = (SocketChannel) key.channel();
        byte[] bb = MessageReader.readMessage(socketChannel);

        Parser p = Parser.getAll(bb);
        String userName = p.getUserName();
        char command = p.getCommand();
        String text = p.getText();
        String fileName = p.getFileName();
        byte[] bytesFile = p.getFileBytes();
        byte[] bytesToSend = null;
        long time = Instant.now().toEpochMilli();
        String serverDate = Long.toString(time);
        if (command == Command.GREETING.getSymbol()) {
            bytesToSend = MessageCreator.createMessage(userName, Command.GREETING.getSymbol(), "", serverDate);
            userNames.put(socketChannel, userName);
        }
        if (command == Command.TEXT.getSymbol()) {
            bytesToSend = MessageCreator.createMessage(userName, Command.TEXT.getSymbol(), serverDate,
                    fileName, bytesFile.length, bytesFile, text);
        }
        if (command == Command.CLOSE.getSymbol()) {
            bytesToSend = MessageCreator.createMessage(userName, Command.CLOSE.getSymbol(), "", serverDate);
            closeSocket(socketChannel, userName);
        }

        writeToClients(bytesToSend);
        return true;
    }

    private static void closeSocket(SocketChannel socket, String userName) {
        userNames.remove(socket, userName);
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        userSocketChannels.remove(socket);
    }

    private static void writeToClients(byte[] bytesToSend) throws IOException {
        for (SocketChannel sc : userSocketChannels) {
            sc.write(ByteBuffer.wrap(bytesToSend));
        }
    }
}