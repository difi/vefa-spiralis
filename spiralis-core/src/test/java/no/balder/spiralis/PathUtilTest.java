package no.balder.spiralis;

import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 15.12.2016
 *         Time: 15.54
 */
public class PathUtilTest {


    @Test
    public void testReplaceExtension() throws Exception {

        Path newPath = PathUtil.replaceExtensionWith(Paths.get("/tmp/xyz.XML"), "-sbdh.xml");
        assertEquals(newPath, Paths.get("/tmp/xyz-sbdh.xml"));

        Path path = PathUtil.replaceExtensionWith(Paths.get("/tmp/Out/hfcEHF_P205044_P3746797_5684_HF_PTI_161107_2115_35734841_206616380229.XML"), "-sbdh.xml");
        assertEquals(path.toString(), "/tmp/Out/hfcEHF_P205044_P3746797_5684_HF_PTI_161107_2115_35734841_206616380229-sbdh.xml");
    }
}