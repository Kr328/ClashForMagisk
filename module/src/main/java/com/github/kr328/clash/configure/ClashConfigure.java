package com.github.kr328.clash.configure;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class ClashConfigure {
    public String portHttp;
    public String portSocks;
    public String portRedir;
    public String portDns;
    public String externalController;
    public String secret;

    public static ClashConfigure loadFromFile(File file) throws IOException {
        Map root = new Yaml(new SafeConstructor()).load(new FileReader(file));
        ClashConfigure result = new ClashConfigure();

        result.portHttp = valueOfOrNull(root.get("port"));
        result.portSocks = valueOfOrNull(root.get("socks-port"));
        result.portRedir = valueOfOrNull(root.get("redir-port"));
        result.externalController = valueOfOrNull(root.get("external-controller"));
        result.secret = valueOfOrNull(root.get("secret"));

        Map dns = (Map) root.get("dns");
        if ( dns != null && (boolean) dns.get("enable") && dns.containsKey("listen") ) {
            result.portDns = String.valueOf(dns.get("listen")).split(":")[1];
        }

        return result;
    }

    private static String valueOfOrNull(Object object) {
        if ( object == null )
            return null;
        return String.valueOf(object);
    }
}
