package no.balder.spiralis.inbound;

import no.balder.spiralis.payload.ReceptionPathUtil;
import no.balder.spiralis.payload.WellKnownFileTypeSuffix;
import no.balder.spiralis.tool.Grep;
import no.balder.spiralis.tool.gson.GsonHelper;
import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.vefa.peppol.sbdh.SbdReader;
import no.difi.vefa.peppol.sbdh.lang.SbdhException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import no.difi.vefa.peppol.common.model.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inspects a file reference by a supplied {@link Path}, figures out various meta data properties and
 * creates a {@link SpiralisReceptionTask}
 * <p>
 *     In future releases, this class should be refactored to handle other protocols than AS2.
 * </p>
 * 
 * @author steinar
 *         Date: 06.02.2017
 *         Time: 19.26
 */
class SpiralisTaskFactory {

    public static final Logger LOGGER = LoggerFactory.getLogger(SpiralisTaskFactory.class);


    static SpiralisReceptionTask insepctInbound(Path metaPath) {

        // Ensures that we only process files containing meta data
        if (ReceptionPathUtil.classify(metaPath) != WellKnownFileTypeSuffix.META_JSON ){
            throw new IllegalArgumentException("Can not process " + metaPath + ", unknown type");
        }


        // Grabs the meta data
        final InboundMetadata inboundMetadata = GsonHelper.fromJson(metaPath);
        Header header = inboundMetadata.getHeader();

        // Locates all files belonging to this reception, i.e. files having the same baseFileName
        // Locates all the other files
        final List<Path> associatedFiles = ReceptionPathUtil.associatedFiles(metaPath);

        // Creates the task
        final SpiralisReceptionTask spiralisReceptionTask = new SpiralisReceptionTask(associatedFiles, inboundMetadata);

        return spiralisReceptionTask;
    }
}
