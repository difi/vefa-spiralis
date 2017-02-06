package no.balder.spiralis.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static no.balder.spiralis.config.SpiralisConfigProperty.*;

/**
 * @author steinar
 *         Date: 31.01.2017
 *         Time: 09.44
 */
public class SpiralisHomeDirectory {

    public static final Logger log = LoggerFactory.getLogger(SpiralisHomeDirectory.class);
    public static final String HOME_PROPERTY_NAME = SPIRALIS_HOME;
    public static final String HOME_ENV_NAME = "SPIRALIS_HOME";
    public static final String RELATIVE_DIR_NAME = ".spiralis";

    public static Path locateSpiralisHomeDir() {

        log.info("Attempting to locate home dir ....");

        Path homeDir = locateHomeDirFromJavaSystemProperty();
        if (homeDir == null) homeDir = locateHomeDirFromEnvironmentVariable();
        if (homeDir == null) homeDir = locateHomeDirRelativeToUserHome();

        validate(homeDir);

        return homeDir;

    }

    private static void validate(Path homeDir) {
        if (homeDir == null) {
            throw new IllegalStateException("No " + HOME_ENV_NAME + " directory could be determined. Application will probably fail");
        }
        if (!Files.exists(homeDir)) {
            throw new IllegalStateException(homeDir + " does not exist");
        } else if (!Files.isDirectory(homeDir)) {
            throw new IllegalStateException(homeDir + " is not a directory");
        } else if (!Files.isReadable(homeDir)) {
            throw new IllegalStateException(homeDir + " exists and is a directory, but there is no read access");
        }
    }

    protected static Path locateHomeDirRelativeToUserHome() {

        String userHome = System.getProperty("user.home");

        Path userHomePath = Paths.get(userHome);
        if (!Files.isDirectory(userHomePath)) {
            return null;
        }
        Path homePath = userHomePath.resolve(RELATIVE_DIR_NAME);
        if (!Files.isDirectory(homePath)) {
            return null;
        }
        
        log.info("Using " + homePath + " as home directory");
        return homePath;
    }

    protected static Path locateHomeDirFromEnvironmentVariable() {
        String homeDirName = System.getenv(HOME_ENV_NAME);
        if (homeDirName != null && homeDirName.trim().length() > 0) {
            log.info("Using value of environment variable " + HOME_ENV_NAME + " as home directory reference : " + homeDirName);
            return Paths.get(homeDirName);
        }
        return null;
    }

    protected static Path locateHomeDirFromJavaSystemProperty() {
        String homeDirName = System.getProperty(HOME_PROPERTY_NAME);
        if (homeDirName != null && homeDirName.trim().length() > 0) {
            log.info("Using home directory specified with Java System property -D" + HOME_PROPERTY_NAME + ": " + homeDirName);
            return Paths.get(homeDirName);
        } else
            return null;
    }
}
