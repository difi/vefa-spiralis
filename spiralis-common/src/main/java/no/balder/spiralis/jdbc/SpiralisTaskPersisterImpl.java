package no.balder.spiralis.jdbc;

import com.google.inject.Inject;
import no.balder.spiralis.inbound.SpiralisTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.net.URI;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
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
    public Long saveInboundTask(SpiralisTask spiralisTask, URI payloadUri, Optional<URI> evidencUri) {

        LOGGER.debug("Saving " + spiralisTask);

        // TODO: rewrite with insert into .... select
        //                                                             1           2           3       4       5       6               7           8           9           10          11                  12            13
        final String INSERT_INTO_MESSAGE_SQL = "insert into message (account_id, direction, sender, receiver, channel, message_uuid, document_id, process_id, ap_name, payload_url, native_evidence_url, received, transmission_id ) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
        Connection con = null;
        try {
            con = dataSource.getConnection();
            final PreparedStatement ps = con.prepareStatement(INSERT_INTO_MESSAGE_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setNull(1, Types.INTEGER);   // This will be set later
            ps.setString(2, "IN");
            ps.setString(3, spiralisTask.getHeader().getSender().getIdentifier().toString());
            ps.setString(4, spiralisTask.getHeader().getReceiver().getIdentifier().toString());
            ps.setString(5, "PEPPOL");
            ps.setString(6, spiralisTask.getOurMessageId());
            ps.setString(7, spiralisTask.getHeader().getDocumentType().toString());
            ps.setString(8, spiralisTask.getHeader().getProcess().toString());
            ps.setString(9, spiralisTask.getSendersApId());
            ps.setString(10, payloadUri.toString());
            if (evidencUri.isPresent()) {
                ps.setString(11, evidencUri.get().toString());
            } else
                ps.setString(11, null);


            final TemporalAccessor received = spiralisTask.getReceived();
            final LocalDateTime localDateTime = LocalDateTime.from(received);
            final Timestamp timestamp = Timestamp.valueOf(localDateTime);

            ps.setTimestamp(12, timestamp);

            if (spiralisTask.getTransmissionId() != null) {
                ps.setString(13, spiralisTask.getTransmissionId());
            } else
                ps.setString(13, null);

            ps.executeUpdate();

            long generatedKey = 0;
            if (con.getMetaData().supportsGetGeneratedKeys()) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs != null && rs.next()) {
                    generatedKey = rs.getLong(1);
                    LOGGER.debug("Inserted SpiralisTask with msg_no={}", generatedKey);
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
                    throw new IllegalStateException("Unable to close connection " + e.getMessage(), e);
                }
        }

    }
}
