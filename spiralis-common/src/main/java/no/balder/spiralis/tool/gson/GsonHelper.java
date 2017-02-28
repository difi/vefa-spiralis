package no.balder.spiralis.tool.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import no.difi.oxalis.api.inbound.InboundMetadata;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.util.Date;


/**
 * @author steinar
 *         Date: 21.02.2017
 *         Time: 08.02
 */
public class GsonHelper {

    private static final Gson gson;

    static {

        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(InboundMetadata.class, new InboundMetaDataSerializer());
        gsonBuilder.registerTypeAdapter(InboundMetadata.class, new InboundMetaDataDeserializer());
        gsonBuilder.registerTypeAdapter(Date.class, new DateSerializer());
        gsonBuilder.registerTypeAdapter(Date.class, new DateDeSerializer());

        gsonBuilder.setDateFormat(DateFormat.FULL, DateFormat.FULL);
        gson = gsonBuilder.create();
    }


    public static String toJson(InboundMetadata inboundMetadata) {
        return gson.toJson(inboundMetadata, InboundMetadata.class);
    }

    public static InboundMetadata fromJson(Path path) {

        try(Reader reader = Files.newBufferedReader(path)) {
            final InboundMetadata inboundMetadata = gson.fromJson(reader, InboundMetadata.class);
            return inboundMetadata;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read Json from " + path);
        }
    }

    public static InboundMetadata fromJson(String s) {
        return gson.fromJson(s, InboundMetadata.class);
    }
}