import java.io.ByteArrayOutputStream;
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