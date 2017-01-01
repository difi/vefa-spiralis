package no.balder.spiralis.workflow;

/**
 * @author steinar
 *         Date: 28.12.2016
 *         Time: 23.24
 */
public interface TaskBuilder {


    TaskBuilder inputPlace(String place);

    TaskBuilder outputPlace(String place);


    WorkFlowBuilder.TransactionBuilder addTransaction(String transmission);
}
