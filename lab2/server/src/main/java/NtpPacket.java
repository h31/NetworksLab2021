import java.io.Serializable;
import java.nio.ByteBuffer;

public class NtpPacket implements Serializable {


    //dont forget to add gitignore

    public byte leapIndicator; // индикатор секунды коррекции - 2 бита
    public byte versionNumber; // версия протокола - 3 бита
    public byte mode; // режим работы - 3 бита
    public byte stratum; // уровень в иерархии
    public byte poll; // интервал, с которым могут опрашивать сервер
    public byte precision; // точность системных часов
    public int rootDelay; // время за которое показатель эталлоных часов доходит до сервера
    public int rootDispersion; // разброс показанний сервера
    public int referenceIdentifier; // адрес сервера-источника
    public long referenceTimestamp; // последние показание часов на сервере
    public long originateTimestamp; // время отправки пакета с сервера
    public long receiveTimestamp; // время получения пакета сервером
    public long transmitTimestamp; // время отправки ответа от клиента

    public NtpPacket() {
    }

    public NtpPacket(byte leapIndicator,
                     byte versionNumber,
                     byte mode,
                     byte stratum,
                     byte poll,
                     byte precision,
                     int rootDelay,
                     int rootDispersion,
                     int referenceIdentifier,
                     long referenceTimestamp,
                     long originateTimestamp,
                     long receiveTimestamp,
                     long transmitTimestamp) {
        this.leapIndicator = leapIndicator;
        this.versionNumber = versionNumber;
        this.mode = mode;
        this.stratum = stratum;
        this.poll = poll;
        this.precision = precision;
        this.rootDelay = rootDelay;
        this.rootDispersion = rootDispersion;
        this.referenceIdentifier = referenceIdentifier;
        this.referenceTimestamp = referenceTimestamp;
        this.originateTimestamp = originateTimestamp;
        this.receiveTimestamp = receiveTimestamp;
        this.transmitTimestamp = transmitTimestamp;
    }

    public void setLeapIndicator(byte leapIndicator) {
        this.leapIndicator = leapIndicator;
    }

    public void setVersionNumber(byte versionNumber) {
        this.versionNumber = versionNumber;
    }

    public void setMode(byte mode) {
        this.mode = mode;
    }

    public void setStratum(byte stratum) {
        this.stratum = stratum;
    }

    public void setPoll(byte poll) {
        this.poll = poll;
    }

    public void setPrecision(byte precision) {
        this.precision = precision;
    }

    public void setRootDelay(int rootDelay) {
        this.rootDelay = rootDelay;
    }

    public void setRootDispersion(int rootDispersion) {
        this.rootDispersion = rootDispersion;
    }

    public void setReferenceIdentifier(int referenceIdentifier) {
        this.referenceIdentifier = referenceIdentifier;
    }

    public void setReferenceTimestamp(long referenceTimestamp) {
        this.referenceTimestamp = referenceTimestamp;
    }

    public void setOriginateTimestamp(long originateTimestamp) {
        this.originateTimestamp = originateTimestamp;
    }

    public void setReceiveTimestamp(long receiveTimestamp) {
        this.receiveTimestamp = receiveTimestamp;
    }

    public void setTransmitTimestamp(long transmitTimestamp) {
        this.transmitTimestamp = transmitTimestamp;
    }

    public byte getLeapIndicator() {
        return leapIndicator;
    }

    public byte getVersionNumber() {
        return versionNumber;
    }

    public byte getMode() {
        return mode;
    }

    public byte getStratum() {
        return stratum;
    }

    public byte getPoll() {
        return poll;
    }

    public byte getPrecision() {
        return precision;
    }

    public int getRootDelay() {
        return rootDelay;
    }

    public int getRootDispersion() {
        return rootDispersion;
    }

    public int getReferenceIdentifier() {
        return referenceIdentifier;
    }

    public long getReferenceTimestamp() {
        return referenceTimestamp;
    }

    public long getOriginateTimestamp() {
        return originateTimestamp;
    }

    public long getReceiveTimestamp() {
        return receiveTimestamp;
    }

    public long getTransmitTimestamp() {
        return transmitTimestamp;
    }


    public byte[] toByteArray() {
        byte[] byteArray = new byte[48];
        byteArray[0] = (byte) (leapIndicator | versionNumber | mode);
        byteArray[1] = stratum;
        byteArray[2] = poll;
        byteArray[3] = precision;
        System.arraycopy(ByteBuffer.allocate(4).putInt(rootDelay).array(), 0, byteArray, 4, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(rootDispersion).array(), 0, byteArray, 8, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(referenceIdentifier).array(), 0, byteArray, 12, 4);
        System.arraycopy(ByteBuffer.allocate(8).putLong(referenceTimestamp).array(), 0, byteArray, 16, 8);
        System.arraycopy(ByteBuffer.allocate(8).putLong(originateTimestamp).array(), 0, byteArray, 24, 8);
        System.arraycopy(ByteBuffer.allocate(8).putLong(receiveTimestamp).array(), 0, byteArray, 32, 8);
        System.arraycopy(ByteBuffer.allocate(8).putLong(transmitTimestamp).array(), 0, byteArray, 40, 8);

        return byteArray;

    }

    @Override
    public String toString() {
        return "NtpPacket{" +
                "leapIndicator=" + leapIndicator +
                ", versionNumber=" + versionNumber +
                ", mode=" + mode +
                ", stratum=" + stratum +
                ", poll=" + poll +
                ", precision=" + precision +
                ", rootDelay=" + rootDelay +
                ", rootDispersion=" + rootDispersion +
                ", referenceIdentifier=" + referenceIdentifier +
                ", referenceTimestamp=" + referenceTimestamp +
                ", originateTimestamp=" + originateTimestamp +
                ", receiveTimestamp=" + receiveTimestamp +
                ", transmitTimestamp=" + transmitTimestamp +
                '}';
    }
}