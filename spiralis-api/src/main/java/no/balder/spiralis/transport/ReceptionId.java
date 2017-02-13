package no.balder.spiralis.transport;

import java.util.UUID;

/**
 * @author steinar
 *         Date: 13.02.2017
 *         Time: 08.22
 */
public class ReceptionId {

    String receptionId;

    public ReceptionId(String receptionId) {
        this.receptionId = receptionId;
    }

    public ReceptionId() {
        this.receptionId = UUID.randomUUID().toString();
    }

    public String value() {
        return receptionId;
    }

    public String toString() {
        return value();
    }
}
