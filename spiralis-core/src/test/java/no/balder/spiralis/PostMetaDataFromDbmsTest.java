package no.balder.spiralis;

import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import eu.peppol.identifier.PeppolProcessTypeIdAcronym;
import eu.peppol.identifier.WellKnownParticipant;
import eu.peppol.persistence.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.jms.*;

import java.io.InputStream;
import java.net.URISyntaxException;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 25.11.2016
 *         Time: 20.43
 */
@Guice(moduleFactory = TestModuleFactory.class)
public class PostMetaDataFromDbmsTest {

    @Inject
    MessageRepository messageRepository;
    private Long msgNo;

    @com.google.inject.Inject Connection connection;

    @BeforeTest
    public void setUp() throws URISyntaxException, OxalisMessagePersistenceException {

        InputStream resource = PostMetaDataFromDbmsTest.class.getClassLoader().getResourceAsStream("hafslund-test-1.xml");
        assertNotNull(resource);
        MessageMetaData.Builder builder = new MessageMetaData.Builder(TransferDirection.OUT, WellKnownParticipant.DUMMY, WellKnownParticipant.DUMMY, PeppolDocumentTypeIdAcronym.EHF_INVOICE.getDocumentTypeIdentifier(), ChannelProtocol.SREST);
        builder.processTypeId(PeppolProcessTypeIdAcronym.INVOICE_ONLY.getPeppolProcessTypeId());
        builder.accountId(new AccountId(1));

        MessageMetaData messageMetaData = builder.build();

        msgNo = messageRepository.saveOutboundMessage(messageMetaData, resource);
        assertNotNull(msgNo);
    }

    @Test
    public void test() throws JMSException {

        MessageMetaData messageMetaData = messageRepository.findByMessageNo(msgNo);
        assertNotNull(messageMetaData);

        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("outbound.reception");
        MessageProducer producer = session.createProducer(queue);

        Message message = session.createMessage();
        message.setStringProperty("no.balder.spiralis.payload_url", messageMetaData.getPayloadUri().toString());

        producer.send(message);
    }
}
