package no.balder.spiralis.workflow;

import no.balder.spiralis.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * @author steinar
 *         Date: 19.12.2016
 *         Time: 20.24
 */
public class WorkFlowBuilder implements End {

    static List<String> places = new ArrayList<>();
    static List<Class<? extends Task>> tasks = new ArrayList<>();

    static TransactionBuilder createWorkflow(String name) {
        return new TransactionBuilder();
    }

    @Override
    public Workflow end() {
        return new Workflow() {
        };
    }


    public static class TransactionBuilder {

        TaskChoice addTransaction(String name, WorkflowTransaction ... transactions) {
            return new TaskChoice(this);
        }
    }

    public static class TaskChoice {

        private final TransactionBuilder transactionBuilder;

        public TaskChoice(TransactionBuilder transactionBuilder) {

            this.transactionBuilder = transactionBuilder;
        }

        public ValidateTaskBuilder validateTask() {
            return new ValidateTaskBuilder(this);
        }

        public TransactionBuilder getTransactionBuilder() {
            return transactionBuilder;
        }

        public void transmissionTask() {

        }
    }

    public static class ValidateTaskBuilder extends AbstractTaskBuilder implements TaskBuilder {


        public ValidateTaskBuilder(TaskChoice taskChoice) {
            super(taskChoice);
        }

    }


}

