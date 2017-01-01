package no.balder.spiralis.workflow;

import no.balder.spiralis.Task;

/**
 * @author steinar
 *         Date: 20.12.2016
 *         Time: 15.01
 */
public interface TransitionBuilder extends End {

    TransitionBuilder transition(Class<? extends Task> taskClass);

    PlaceBuilder outputPlaces(String... places);


}
