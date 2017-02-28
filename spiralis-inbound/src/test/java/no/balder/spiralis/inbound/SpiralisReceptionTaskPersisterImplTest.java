package no.balder.spiralis.inbound;

import com.google.inject.Inject;
import no.balder.spiralis.config.SpiralisInboundTestModuleFactory;
import no.balder.spiralis.jdbc.SpiralisTaskPersister;
import no.balder.spiralis.testutil.DummyFiles;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * @author steinar
 *         Date: 07.02.2017
 *         Time: 18.12
 */
@Guice(moduleFactory = SpiralisInboundTestModuleFactory.class)
public class SpiralisReceptionTaskPersisterImplTest {

    private Path rootPath;


    @Inject
    SpiralisTaskPersister spiralisTaskPersister;

    @BeforeMethod
    public void setUp() throws Exception {
        rootPath = DummyFiles.createInboundDummyFilesInRootWithOptionalSubdirs();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        DummyFiles.removeAll(rootPath);
    }

    @Test
    public void testPersistTask() throws Exception {

        // Traverses the dummy files ..
        final List<Path> paths = DummyFiles.locateJsonMetaData(rootPath);

        // Creates the SpiralisReceptionTask based upon the contents in the sample dummy files
        final SpiralisReceptionTask spiralisReceptionTask = SpiralisTaskFactory.insepctInbound(paths.get(0));


        spiralisTaskPersister.saveInboundTask(spiralisReceptionTask, new URI("azure", "microsoft", "/inbound/test", null),
                (new URI("azure", "microsoft", "/inbound/test/x.smime", null)));
    }


}