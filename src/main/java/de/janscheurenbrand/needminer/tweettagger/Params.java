package de.janscheurenbrand.needminer.tweettagger;

import io.undertow.server.HttpServerExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.HashMap;

/**
 * Created by janscheurenbrand on 28/07/15.
 */
public class Params {
    public static HashMap<String,String> getRequestParams(HttpServerExchange exchange) throws Exception {
        String string = getBody(exchange);
        HashMap<String,String> map = new HashMap<>();
        for (String s : string.split("&")) {
            String[] parts = s.split("=");
            if (parts.length == 2) {
                map.put(parts[0], URLDecoder.decode(parts[1], "UTF-8"));
            }
        }
        return map;
    }

    private static String getBody(HttpServerExchange exchange) {
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();

        try {
            exchange.startBlocking();
            reader = new BufferedReader(new InputStreamReader(exchange.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return builder.toString();
    }
}
