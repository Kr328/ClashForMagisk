package com.github.kr328.clash;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    static StarterConfigure defaultValue() {
        StarterConfigure result = new StarterConfigure();

        result.mode = "redirect";
        result.blacklist = Arrays.asList("package:com.github.shadowsocks",
                "package:com.github.shadowsocks:1000",
                "user:system",
                "uid:1000");

        return result;
    }

    Map<String, Map> replaceList() {
        HashMap<String, Map> result = new HashMap<>();

        result.put("%%MODE%%", modeMap());
        result.put("%%BLACKLIST%%", blacklistMap());

        return result;
    }

    private Map<String, String> modeMap() {
        return new HashMap<String, String>() {
            {
                put("mode", mode);
            }
        };
    }

    private Map<String, List<String>> blacklistMap() {
        return new HashMap<String, List<String>>() {{
            put("blacklist", blacklist);
        }};
    }
}
