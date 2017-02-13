package no.balder.spiralis;

import com.google.inject.Inject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * @author steinar
 *         Date: 03.01.2017
 *         Time: 18.10
 */
class TransmissionTaskBuilder {

    URL endpointUrl = null;
    Place inputPlace = Place.OUTBOUND_WORKFLOW_START;
    Place errorPlace = Place.OUTBOUND_TRANSMISSION_ERROR;

    TaskFactory taskFactory;


    @Inject
    TransmissionTaskBuilder(TaskFactory taskFactory) {
        this.taskFactory = taskFactory;
    }


    TaskFactory getTaskFactory() {
        return taskFactory;
    }

    TransmissionTaskBuilder overrideEndpointUrl(String url) {

        try {
            endpointUrl = new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Unable to create URL from string: " + url);
        }
        return this;
    }

    TransmissionTaskBuilder inputPlace(Place inputPlace) {
        this.inputPlace = inputPlace;
        return this;
    }

    TransmissionTaskBuilder errorPlace(Place errorPlace) {
        this.errorPlace = errorPlace;
        return this;
    }

    /**
     * Builds a single transport task
     * @return
     */
    List<TransmissionTask> build() {
        return build(1);
    }

    /**
     * Builds number of transport tasks indicated
     * @param instances number of identical tasks to build.
     *
     * @return
     */
    List<TransmissionTask> build(int instances) {

        List<TransmissionTask> result = null;

        if (endpointUrl != null) {
            result = taskFactory.createTransmissionTasks(instances, endpointUrl, inputPlace, errorPlace);
        } else {
            result = taskFactory.createTransmissionTasks(instances, inputPlace, errorPlace);
        }
        return result;
    }



}
