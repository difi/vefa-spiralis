package no.balder.spiralis.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * @author steinar
 *         Date: 03.02.2017
 *         Time: 13.36
 */
public final class SpiralisConfigProperty {

    public static final String SPIRALIS_HOME            = "spiralis.home";
    public static final String SPIRALIS_INBOUND_DIR = "spiralis.inbound.directory";
    public static final String SPIRALIS_AZURE_ACCOUNT   = "spiralis.azure.account";
    public static final String SPIRALIS_AZURE_KEY       = "spiralis.azure.access-key";
    public static final String SPIRALIS_AZURE_CONNECT   = "spiralis.azure.connect";
    public static final String SPIRALIS_ARCHIVE_DIR     = "spirlias.archive.dir";
    /**
     * The glob pattern to match inbound files against.
     */
    public static final String SPIRALIS_INBOUND_GLOB = "spiralis.inbound.glob";

    private SpiralisConfigProperty() {
    }

    public static List<String> getPropertyNames() {


        Field[] declaredFields = SpiralisConfigProperty.class.getDeclaredFields();

        int publicStaticFinal = (Modifier.STATIC | Modifier.FINAL | Modifier.PUBLIC);

        List<String> result = new ArrayList<>();
        for (Field field : declaredFields) {
            int modifiers = field.getModifiers();

            // We are only interested in public static final String
            if ((field.getModifiers() & publicStaticFinal) == publicStaticFinal
                    && field.getType() == String.class) {

                try {
                    String propertyName = (String) field.get(null); // this is ok for static fields
                    result.add(propertyName);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("OOps! " + e.getMessage(), e);
                }
            }
        }
        return result;
    }
}
