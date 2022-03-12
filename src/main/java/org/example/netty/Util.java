package org.example.netty;

import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;

import java.io.*;
import java.nio.charset.Charset;

@Slf4j
public class Util {

    public static void writeFileContent(RequestData data, ByteBuf in, Charset charset) {
        ByteArrayOutputStream stringByteArray = new ByteArrayOutputStream();
        int contentLength = in.readInt();
        data.setContentLength(contentLength);
        int attNameLength = in.readInt();
        data.setAttName(in.readCharSequence(attNameLength, charset).toString());
        for (int i = 0; i < contentLength; i++) {
            byte contentByte = in.readByte();
            stringByteArray.write(contentByte);
        }
        data.setContent(stringByteArray.toByteArray());
    }

    public static void saveFileToClient(ResponseData requestData){
        byte[] content = requestData.getContent();
        String[] f = requestData.getAttName().split("\\.");
        StringBuilder suffix = new StringBuilder();
        for (int i = 1; i < f.length; i++) {
            suffix.append(".").append(f[i]);
        }
        try (BufferedOutputStream fileReader =
                     new BufferedOutputStream(new FileOutputStream(File.createTempFile(f[0], suffix.toString())))) {
            File file = File.createTempFile(f[0], suffix.toString());
            fileReader.write(content);
            fileReader.flush();
            System.out.println(file.getAbsolutePath());
        } catch (IOException ex) {
            log.error(String.valueOf(Level.ERROR),ex.getMessage());
        }
    }
}
