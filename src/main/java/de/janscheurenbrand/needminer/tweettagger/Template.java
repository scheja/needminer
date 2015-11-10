package de.janscheurenbrand.needminer.tweettagger;

import java.io.IOException;
import java.io.InputStream;
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
            System.out.println(Template.class.getClassLoader().getResource("static/partials/"+path+".html").getFile());
            return readFile(Template.class.getClassLoader().getResourceAsStream("static/partials/" + path + ".html"));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String readFile(InputStream stream) throws IOException {
        java.util.Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}