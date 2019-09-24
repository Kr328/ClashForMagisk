package com.github.kr328.clash;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

class StarterConfigure {
    String mode;

    static StarterConfigure loadFromFile(File file) throws IOException {
        Map root = new Yaml(new SafeConstructor()).load(new FileReader(file));

        StarterConfigure result = new StarterConfigure();

        result.mode = valueOfOrDefault(root.get("mode"), "proxy-only");

        return result;
    }

    private static String valueOfOrDefault(Object object, String fallback) {
        if ( object == null )
            return fallback;
        return String.valueOf(object);
    }
}
