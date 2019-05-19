package it.polimi.ingsw.utils;

import java.io.*;

public class ConfigFileMaker {
    private ConfigFileMaker() { }

    public static Reader load(String fsPath, String resPath) {
        return load(fsPath, resPath, true);
    }

    public static Reader load(String fsPath, String resPath, boolean createFs) {
        try {
            return new FileReader(new File(fsPath));
        } catch (FileNotFoundException e) {

            if (createFs) {
                try {
                    byte[] fileData = ConfigFileMaker.class.getResourceAsStream(resPath).readAllBytes();
                    File fsFile = new File(fsPath);
                    if (fsFile.getParentFile().mkdirs()) {
                        try (FileOutputStream outputStream = new FileOutputStream(fsFile)) {
                            outputStream.write(fileData);
                        } catch (IOException ignored) { }
                    }
                } catch (IOException ignored) { }
            }

            return new InputStreamReader(ConfigFileMaker.class.getResourceAsStream(resPath));
        }
    }
}
