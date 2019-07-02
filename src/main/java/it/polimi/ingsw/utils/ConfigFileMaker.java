package it.polimi.ingsw.utils;

import java.io.*;
import java.util.logging.Logger;

/**
 * Configuration file loader and writer
 *
 * @author Carlo Dell'Acqua
 */
public class ConfigFileMaker {

    /**
     * Logging utility
     */
    protected static Logger logger = Logger.getLogger(ConfigFileMaker.class.getName());

    private ConfigFileMaker() { }

    /**
     * Overload of {@link #load(String, String, boolean)} with default createFs parameter
     *
     * @param fsPath the file system path
     * @param resPath the resource-relative path
     * @return a Reader of the file data
     */
    public static Reader load(String fsPath, String resPath) {
        return load(fsPath, resPath, true);
    }

    /**
     * This method try to load a file from a file system path. If it's missing, it uses a resource-relative
     * path to read it and, if the createFs flag is set to true, it writes the same content to a new file
     * with the corresponding fsPath
     *
     * @param fsPath the file system path
     * @param resPath the resource-relative path
     * @param createFs if true and the file on the file system is not found, using the fsPath a new file is created with the same content of the file located in the resPath
     * @return a Reader of the file data
     */
    public static Reader load(String fsPath, String resPath, boolean createFs) {
        try {
            return new FileReader(new File(fsPath));
        } catch (FileNotFoundException e) {

            if (createFs) {
                try {
                    byte[] fileData = ConfigFileMaker.class.getResourceAsStream(resPath).readAllBytes();
                    File fsFile = new File(fsPath);
                    if (fsFile.getParentFile().exists() || fsFile.getParentFile().mkdirs()) {
                        try (FileOutputStream outputStream = new FileOutputStream(fsFile)) {
                            outputStream.write(fileData);
                            logger.info("Wrote configuration file to " + fsPath);
                        } catch (IOException ex) {
                            logger.warning("Unable to write configuration file to " + fsPath + "\n" + ex.toString());
                        }
                    } else {
                        logger.warning("Unable to create directories for configuration file at path: " + fsPath);
                    }
                } catch (IOException ex) {
                    logger.warning("Unable to read configuration file from resources at path: " + resPath + "\n" + ex.toString());
                }
            }

            return new InputStreamReader(ConfigFileMaker.class.getResourceAsStream(resPath));
        }
    }
}
