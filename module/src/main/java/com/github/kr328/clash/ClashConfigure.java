package com.github.kr328.clash;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

class ClashConfigure {
    String portHttp;
    String portSocks;
    String portRedirect;
    String portDns;

    static ClashConfigure loadFromFile(File file) throws IOException {
        Map root = new Yaml(new SafeConstructor()).load(new FileReader(file));
        ClashConfigure result = new ClashConfigure();

        result.portHttp = validPortOrNull(valueOfOrNull(root.get("port")));
        result.portSocks = validPortOrNull(valueOfOrNull(root.get("socks-port")));
        result.portRedirect = validPortOrNull(valueOfOrNull(root.get("redir-port")));

        Map dns = (Map) root.get("dns");
        if ( dns != null && (boolean) dns.get("enable") && dns.containsKey("listen") ) {
            result.portDns = validPortOrNull(String.valueOf(dns.get("listen")).split(":")[1]);
        }

        return result;
    }

    private static String valueOfOrNull(Object object) {
        if ( object == null )
            return null;
        return String.valueOf(object);
    }

    private static String validPortOrNull(String data) {
        if ( data == null )
            return null;

        if ( Integer.parseInt(data) <= 1024)
            return null;

        return data;
    }
}
