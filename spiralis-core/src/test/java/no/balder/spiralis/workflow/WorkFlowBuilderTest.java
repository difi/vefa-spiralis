package no.balder.spiralis.workflow;

import org.testng.annotations.Test;

import static no.balder.spiralis.workflow.WorkFlowBuilder.*;

/**
 * @author steinar
 *         Date: 20.12.2016
 *         Time: 14.51
 */
public class WorkFlowBuilderTest {

    @Test
    public void testPlace() throws Exception {

        createWorkflow("W1")
                .addTransaction("validation");



    }

}