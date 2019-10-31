package com.github.kr328.clash;

import android.annotation.SuppressLint;
import android.util.Log;
import android.util.Pair;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

class Utils {
    static void waitForUserUnlocked() {
        @SuppressLint("SdCardPath")
        File file = new File("/sdcard/Android");

        while (!file.isDirectory()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }

            Log.i(Constants.TAG, "Wait 1s for user unlock");
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    static void deleteFiles(String baseDir, String... files) {
        for (String f : files)
            new File(baseDir, f).delete();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static String readFile(File file) throws IOException {
        InputStream inputStream = new FileInputStream(file);

        byte[] buffer = new byte[inputStream.available()];
        inputStream.read(buffer);
        String result = new String(buffer);

        inputStream.close();

        return result;
    }

    private static void writeFile(File file, String data) throws IOException {
        OutputStream outputStream = new FileOutputStream(file);

        outputStream.write(data.getBytes());

        outputStream.close();
    }

    static StarterConfigure loadOrRenderStarter(String baseDir, String dataDir) throws IOException {
        File starter = new File(dataDir, "starter.yaml");
        File starterTemplate = new File(baseDir, "starter.template.yaml");

        if (!starter.exists() || starter.lastModified() < starterTemplate.lastModified()) {
            StarterConfigure result = starter.exists() ? StarterConfigure.loadFromFile(starter) : StarterConfigure.defaultValue();

            String template = readFile(starterTemplate);
            Yaml yaml = new Yaml();

            for (Map.Entry<String, Map> replace : result.replaceList().entrySet() ) {
                template = template.replace(replace.getKey(), yaml.dumpAsMap(replace.getValue()));
            }

            writeFile(starter, template);

            return result;
        }

        return StarterConfigure.loadFromFile(starter);
    }
}
