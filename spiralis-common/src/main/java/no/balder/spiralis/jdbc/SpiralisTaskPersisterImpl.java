package no.balder.spiralis.jdbc;

import com.google.inject.Inject;
import no.balder.spiralis.inbound.SpiralisReceptionTask;
import no.balder.spiralis.transport.ReceptionId;
import no.balder.spiralis.transport.ReceptionMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.net.URI;
import java.sql.*;
import java.util.Optional;

/**
 * @author steinar
 *         Date: 07.02.2017
 *         Time: 13.08
 */
public class SpiralisTaskPersisterImpl implements SpiralisTaskPersister {

    public static final Logger LOGGER = LoggerFactory.getLogger(SpiralisTaskPersisterImpl.class);


    private final DataSource dataSource;

    @Inject
    public SpiralisTaskPersisterImpl(DataSource dataSource) {

        this.dataSource = dataSource;
    }


    @Override
    public Long saveInboundTask(SpiralisReceptionTask spiralisReceptionTask, URI payloadUri, URI evidencUri) {

        LOGGER.debug("Saving " + spiralisReceptionTask);

        final String INSERT_INTO_MESSAGE_SQL =
                "insert into message ( direction, \n" +  // 1
                        "       received,\n" +      // 2
                        "       sender, \n" +       // 3
                        "       receiver, \n" +     // 4
                        "       channel, \n" +      // 5
                        "       message_uuid, \n" + // 6
                        "       document_id, \n" +  // 7
                        "       process_id, \n" +   // 8
                        "       ap_name, \n" +      // 9
                        "       payload_url, \n" +  // 10
                        "       evidence_url, \n" + // 11
                        "       transmission_id, \n" +  // 12
                        "       instance_id, \n" +  // 13
                        "       account_id ) \n" +  // 14
                        "       values(?," + // 1 - direction
                        "           ?" +    // 2 - received
                        "           ,?" +   // 3 - sender
                        "           ,?" +   // 4 - receiver
                        "           ,?" +   // 5 - channel
                        "           ,?" +   // 6 - message_uuid
                        "           ,?" +   // 7 - document_id
                        "           ,?" +   // 8 - process_id (profile)
                        "           ,?" +   // 9 - Access Point identifier (sender's)
                        "           ,?" +   // 10 - URI of payload
                        "           ,?" +   // 11 - URI of evidence (AS2 MDN)
                        "           ,?" +   // 12 - transport id
                        "           ,?" +   // 13 - SBDH instance identifier
                        "           ,coalesce(\n" +
                        "               (select account_id from account_receiver where participant_id=?), null)\n" +   // 14
                        "       )";
        Connection con = null;
        try {
            con = dataSource.getConnection();
            final PreparedStatement ps = con.prepareStatement(INSERT_INTO_MESSAGE_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, "IN");

            // received
            {
                final Timestamp timestamp = new Timestamp(spiralisReceptionTask.getInboundMetadata().getTimestamp().getTime());
                LOGGER.debug("Inserting Timestamp :" + timestamp.toString());
                ps.setTimestamp(2, timestamp);
            }

            ps.setString(3, spiralisReceptionTask.getInboundMetadata().getHeader().getSender().getIdentifier().toString());
            ps.setString(4, spiralisReceptionTask.getInboundMetadata().getHeader().getReceiver().getIdentifier().toString());
            ps.setString(5, "PEPPOL");
            ps.setString(6, spiralisReceptionTask.getReceptionId().value());
            ps.setString(7, spiralisReceptionTask.getInboundMetadata().getHeader().getDocumentType().getIdentifier().toString());
            ps.setString(8, spiralisReceptionTask.getInboundMetadata().getHeader().getProcess().getIdentifier().toString());
            ps.setString(9, spiralisReceptionTask.getSendersApId());
            ps.setString(10, payloadUri.toString());
            ps.setString(11, evidencUri != null ? evidencUri.toString() : null);


            if (spiralisReceptionTask.getInboundMetadata().getTransmissionIdentifier() != null) {
                ps.setString(12, spiralisReceptionTask.getInboundMetadata().getTransmissionIdentifier().getIdentifier());
            } else
                ps.setString(12, null);

            ps.setString(13, spiralisReceptionTask.getInboundMetadata().getHeader().getIdentifier().getIdentifier());

            final String receiverOrgNo = spiralisReceptionTask.getInboundMetadata().getHeader().getReceiver().getIdentifier().toString();
            ps.setString(14, receiverOrgNo);

            ps.executeUpdate();

            long generatedKey = 0;
            if (con.getMetaData().supportsGetGeneratedKeys()) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs != null && rs.next()) {
                    generatedKey = rs.getLong(1);
                    LOGGER.debug("Inserted SpiralisReceptionTask with msg_no={}, message_uuid={}", generatedKey, spiralisReceptionTask.getReceptionId().value());
                } else {
                    LOGGER.error("Seems we were unable to retrieve the auto generated key");
                }
            } else {
                LOGGER.warn("insert into 'messge' table, auto generated keys not supported");
            }

            return generatedKey;

        } catch (SQLException e) {
            throw new IllegalStateException("Unable to persist with " + INSERT_INTO_MESSAGE_SQL + "; reason=" + e.getMessage(), e);
        } finally {
            if (con != null)
                try {
                    con.close();
                } catch (SQLException e) {
                    //noinspection ThrowFromFinallyBlock
                    throw new IllegalStateException("Unable to close connection " + e.getMessage(), e);
                }
        }

    }

    @Override
    public Optional<ReceptionMetaData> findByReceptionId(ReceptionId receptionId) {

        final String sql = "select * from message where message_uuid=?";
        try (Connection con = dataSource.getConnection()) {
            final PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, receptionId.value());
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                final ReceptionMetaData rm = new ReceptionMetaData();
                rm.setMessageNo(rs.getInt("msg_no"));
                rm.setAccountId(rs.getInt("account_id"));
                rm.setDirection(rs.getString("direction"));
                rm.setReceived(rs.getTimestamp("received"));
                rm.setDelivered(rs.getTimestamp("delivered"));
                rm.setSender(rs.getString("sender"));
                rm.setReceiver(rs.getString("receiver"));
                rm.setChannel(rs.getString("channel"));
                rm.setReceptionId(rs.getString("message_uuid"));
                rm.setTransmissionId(rs.getString("transmission_id"));
                rm.setInstanceId(rs.getString("instance_id"));
                rm.setDocumentTypeId(rs.getString("document_id"));
                rm.setProcessTypeId(rs.getString("process_id"));
                rm.setApName(rs.getString("ap_name"));
                rm.setPayloadUrl(rs.getString("payload_url"));
                rm.setEvidenceUrl(rs.getString("evidence_url"));
                return Optional.of(rm);
            } else
                return Optional.empty();

        } catch (SQLException e) {
            throw new IllegalStateException("Unable to find data for receptionId " + receptionId);
        }
    }

}
