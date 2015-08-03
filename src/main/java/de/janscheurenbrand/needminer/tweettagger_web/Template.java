package de.janscheurenbrand.needminer.tweettagger_web;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;

/**
 * Created by janscheurenbrand on 28/07/15.
 */
public class Template {
    public static String yield(String partial, HashMap<String,String> data) {
        HashMap<String,String> map = new HashMap<>();
        if (data != null) {
            map.putAll(data);
        }
        map.put("content",evaluate(partial,data));
        return evaluate("application",map);
    }

    public static String toTable(ResultSet rs) {
        StringBuilder sb = new StringBuilder();
        try {
            ResultSetMetaData md = rs.getMetaData();
            int count = md.getColumnCount();
            sb.append("<table class='table'>");
            sb.append("<tr>");
            for (int i=1; i<=count; i++) {
                sb.append("<th>");
                sb.append(md.getColumnLabel(i));
            }
            sb.append("</tr>");
            while (rs.next()) {
                sb.append("<tr>");
                for (int i=1; i<=count; i++) {
                    sb.append("<td>");
                    sb.append(rs.getString(i));
                }
                sb.append("</tr>");
            }
            sb.append("</table>");
        } catch (Exception e) {}
        return sb.toString();
    }

    private static String evaluate (String partial, HashMap<String,String> data) {
        final String[] s = {getFile(partial)};
        if (data != null) {
            data.forEach((key,value) -> {
                s[0] = s[0].replace("$" + key, value);
            });
        }

        return s[0];
    }

    private static String getFile(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get("static", "partials", path + ".html")));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}