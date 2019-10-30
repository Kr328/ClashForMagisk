package com.github.kr328.clash;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

class StarterConfigure {
    String mode;
    List<String> blacklist;

    static StarterConfigure loadFromFile(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        Constructor constructor = new Constructor(StarterConfigure.class);

        constructor.setPropertyUtils(new PropertyUtils() {
            {
                setSkipMissingProperties(true);
            }
        });

        StarterConfigure result = new Yaml(constructor).loadAs(inputStream, StarterConfigure.class);

        inputStream.close();

        return result;
    }

}
