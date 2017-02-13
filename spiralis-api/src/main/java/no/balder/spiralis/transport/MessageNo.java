package no.balder.spiralis.transport;

/**
 * @author steinar
 *         Date: 13.02.2017
 *         Time: 09.56
 */
public class MessageNo {

    private final long msgNo;

    public MessageNo(long msgNo) {
        this.msgNo = msgNo;
    }

    public long longValue() {
        return msgNo;
    }
}
