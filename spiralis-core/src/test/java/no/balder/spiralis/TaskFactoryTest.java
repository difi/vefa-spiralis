package no.balder.spiralis;

import com.google.inject.Inject;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 14.12.2016
 *         Time: 20.14
 */
@Guice(moduleFactory = TestModuleFactory.class)
public class TaskFactoryTest {

    @Inject
    TaskFactory taskFactory;

    @Test
    public void testCreateSbdhInspectionTasks() throws Exception {
        List<SbdhInspectionTask> sbdhInspectionTasks = taskFactory.createSbdhInspectionTasks(1);
    }

}