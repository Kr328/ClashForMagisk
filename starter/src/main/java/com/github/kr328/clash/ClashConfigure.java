package com.github.kr328.clash;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

class ClashConfigure {
    String portHttp;
    String portSocks;
    String portRedirect;
    Dns dns;

    static ClashConfigure loadFromFile(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        Constructor constructor = new Constructor(ClashConfigure.class);

        constructor.setPropertyUtils(new PropertyUtils() {
            {
                setSkipMissingProperties(true);
            }

            @Override
            public Property getProperty(Class<?> type, String name) {
                switch (name) {
                    case "port":
                        name = "portHttp";
                        break;
                    case "socks-port":
                        name = "portSocks";
                        break;
                    case "redir-port":
                        name = "portRedirect";
                        break;
                }
                return super.getProperty(type, name);
            }
        });

        ClashConfigure result = new Yaml(constructor).loadAs(inputStream, ClashConfigure.class);

        inputStream.close();

        return result;
    }

    static class Dns {
        boolean enable;
        String listen;
    }
}
