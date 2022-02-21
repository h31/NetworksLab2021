package org.example.util;

import lombok.Data;

import java.io.Serializable;
import java.nio.ByteBuffer;

@Data
public class NtpPacket implements Serializable {
    private byte leapIndicator; // индикатор коррекции (2 бита)
    private byte versionNumber; // номер версии протокола NTP (3 бита)
    private byte mode; // режим работы отправителя пакета (3 бита)
    private byte stratum; // уровень наслоения
    private byte poll; // интервал, с которым могут опрашивать сервер
    private byte precision; // точность системных часов
    private int rootDelay; // время, за которое показания часов доходят до NTP-сервера
    private int rootDispersion; // разброс показаний часов NTP-сервера
    private int referenceIdentifier; // адрес сервера-источника
    private long referenceTimestamp; // последние показания часов на сервере
    private long originateTimestamp; // время, когда пакет был отправлен
    private long receiveTimestamp; // время получения пакета сервером
    private long transmitTimestamp; // время отправки пакета с сервера клиенту

    @Override
    public String toString() {
        return "Leap indicator: " + leapIndicator +
                "\nVersion number: " + versionNumber +
                "\nMode: " + mode +
                "\nStratum: " + stratum +
                "\nPoll: " + poll +
                "\nPrecision: " + precision +
                "\nRoot delay: " + rootDelay +
                "\nRoot dispersion: " + rootDispersion +
                "\nRef id: " + referenceIdentifier +
                "\nReference: " + referenceTimestamp +
                "\nOriginate: " + originateTimestamp +
                "\nReceive: " + receiveTimestamp +
                "\nTransmit: " + transmitTimestamp;
    }

    public byte[] toByteArray() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(48)
                .put((byte) (leapIndicator | versionNumber | mode))
                .put(stratum)
                .put(poll)
                .put(precision)
                .putInt(rootDelay)
                .putInt(rootDispersion)
                .putInt(referenceIdentifier)
                .putLong(referenceTimestamp)
                .putLong(originateTimestamp)
                .putLong(receiveTimestamp)
                .putLong(transmitTimestamp);
        return byteBuffer.array();
    }
}
