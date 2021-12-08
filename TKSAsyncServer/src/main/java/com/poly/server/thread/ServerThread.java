package com.poly.server.thread;

import com.poly.models.Message;
import com.poly.models.MessageWithContent;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ServerThread extends Thread {

    private final Integer port;
    private final List<SocketChannel> userSocketChannels;
    private final Selector selector;

    private static final Logger LOG = LoggerFactory.getLogger(ServerThread.class);

    public ServerThread(Integer port) throws IOException {
        this.port = port;
        this.userSocketChannels = new LinkedList<>();
        this.selector = Selector.open();
    }

    @Override
    public void run() {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            InetAddress inetAddress = InetAddress.getByName("localhost");
            serverSocketChannel.bind(new InetSocketAddress(inetAddress, port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            SelectionKey key;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    MessageWithContent message;
                    if (selector.select() <= 0) {
                        continue;
                    }
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectedKeys.iterator();
                    ByteBuffer bb = ByteBuffer.allocate(4);
                    while (iterator.hasNext()) {

                        key = iterator.next();
                        iterator.remove();
                        if(key.isValid()) {
                            if (key.isAcceptable()) {
                                SocketChannel sc = serverSocketChannel.accept();
                                sc.configureBlocking(false);
                                sc.register(selector, SelectionKey.OP_READ);
                                userSocketChannels.add(sc);
                            }
                            if (key.isReadable()) {
                                SocketChannel sc = (SocketChannel) key.channel();
                                int size = getSize(sc, bb);
                                if (size > 0) {
                                    message = readMessage(sc, size);
                                    message.getMessage().setDate(LocalDate.now() + " " + LocalTime.now().toString().replace(":", "."));
                                    writeToAll(message);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    LOG.warn("Connection was reset by peer. \n Localized cause: " + e.getLocalizedMessage());
                }
            }
        } catch (IOException e) {
            LOG.error("Something went wrong during socketChannel initialization: " + e.getLocalizedMessage());
        }
    }

    private int getSize(SocketChannel socketChannel, ByteBuffer bb) throws IOException {
        try {
            if (hasMessage(socketChannel, bb)) {
                bb.position(bb.limit());
                bb.limit(4);
                while (bb.hasRemaining()) socketChannel.read(bb);
            }
        } catch (IOException e) {
            userSocketChannels.remove(socketChannel);
            socketChannel.close();
            return 0;
        }
        int size = 0;
        if(bb.position() == 0) {
            return 0;
        }
        for (int i = 0; i < 4; i++) {
            size = size << 8;
            size += bb.get(i) & 0xff;
        }
        return size;
    }

    private MessageWithContent readMessage(SocketChannel socketChannel, int size) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(size);
        while (bb.hasRemaining()) socketChannel.read(bb);
        if (bb.remaining() > 0) {
            throw new IllegalAccessError();
        }
        Message result = new Message();
        result.parseToMessage(StringUtils.newStringUtf8(bb.array()));
        ByteBuffer filBuf = ByteBuffer.allocate(0);
        if (result.getFileSize() != null && result.getFileSize() > 0) {
            filBuf = ByteBuffer.allocate(result.getFileSize());
            while (filBuf.hasRemaining()) socketChannel.read(filBuf);
        }
        MessageWithContent messageWithContent = new MessageWithContent(result, filBuf.array());
        while (bb.hasRemaining()) socketChannel.read(bb);
        return messageWithContent;

    }

    private void writeToAll(MessageWithContent messageWithContent) throws IOException {
        int size = messageWithContent.getMessage().toTransferString().getBytes().length;
        int fileSize = messageWithContent.getMessage().getFileSize() != null ? messageWithContent.getMessage().getFileSize() : 0;
        for (SocketChannel socketChannel : userSocketChannels) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(4 + size + fileSize);
            byteBuffer.put(messageWithContent.serialize());
            byteBuffer.flip();
            try {
                while (byteBuffer.hasRemaining()) socketChannel.write(byteBuffer);
            } catch (Exception e) {
                userSocketChannels.remove(socketChannel);
                socketChannel.close();
            }
        }
    }

    private boolean hasMessage(SocketChannel socketChannel, ByteBuffer bb) throws IOException {
        bb.limit(4);
        socketChannel.read(bb);
        bb.flip();
        return bb.hasRemaining();
    }
}
