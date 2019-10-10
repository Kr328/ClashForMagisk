package com.github.kr328.clash;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SimpleHttpClient {
    public static String get(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setInstanceFollowRedirects(true);
        connection.connect();

        if ( connection.getResponseCode() / 100 != 2 ) {
            connection.disconnect();
            return null;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();
        String line;

        while (( line = reader.readLine() ) != null )
            stringBuilder.append(line);

        connection.disconnect();

        return stringBuilder.toString();
    }
}
