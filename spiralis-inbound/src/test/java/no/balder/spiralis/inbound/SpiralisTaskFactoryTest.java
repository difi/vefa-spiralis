package no.balder.spiralis.inbound;

import no.balder.spiralis.payload.WellKnownFileTypeSuffix;
import no.balder.spiralis.testutil.DummyFiles;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 25.02.2017
 *         Time: 11.53
 */
public class SpiralisTaskFactoryTest {

    private Path inboundDir;

    @BeforeMethod
    public void setUp() throws Exception {
        final Path rootDir = DummyFiles.createInboundDummyFilesInRootWithOptionalSubdirs("IN");
        inboundDir = rootDir.resolve("IN");
    }

    @Test
    public void testInsepctInbound() throws Exception {

        final List<Path> jsonMetaData = DummyFiles.locateJsonMetaData(inboundDir);

        for (Path jsonMetaDatum : jsonMetaData) {
            final SpiralisReceptionTask spiralisReceptionTask = SpiralisTaskFactory.insepctInbound(jsonMetaDatum);
        }
    }

}