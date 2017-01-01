package no.balder.spiralis.workflow;

/**
 * @author steinar
 *         Date: 28.12.2016
 *         Time: 23.30
 */
abstract class AbstractTaskBuilder implements TaskBuilder {

    private final WorkFlowBuilder.TaskChoice taskChoice;

    public AbstractTaskBuilder(WorkFlowBuilder.TaskChoice taskChoice) {
        this.taskChoice = taskChoice;
    }


    @Override
    public WorkFlowBuilder.TransactionBuilder addTransaction(String transmission) {
        return taskChoice.getTransactionBuilder();
    }

    @Override
    public TaskBuilder inputPlace(String place) {
        return this;
    }

    @Override
    public TaskBuilder outputPlace(String place) {
        return this;
    }
}
