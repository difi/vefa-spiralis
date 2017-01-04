package no.balder.spiralis;

/**
 * Represents a piece of work to be done or undertaken.
 *
 * @author steinar
 *         Date: 07.12.2016
 *         Time: 18.54
 * @parameter <T> the type of input to the task.
 */
public interface Task extends Runnable {

    long getProcessCount();

}
