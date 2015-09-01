package de.janscheurenbrand.needminer.bluemix;

import de.janscheurenbrand.needminer.twitter.Tweet;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Created by janscheurenbrand on 20/08/15.
 */
public class TwitterInsightsAPI {
    private static final Logger logger = LogManager.getLogger("TwitterInsightsAPI");

    final String BASE_URI = "https://6a0d00e3a1fdc7b72629aa1dfde610e0:Bxx5MH8CPh@cdeservice.eu-gb.mybluemix.net/api/v1/messages/search?";
    ArrayList<String> keywords = new ArrayList<>();
    String start;
    String end;
    int from = 0;
    int size = 5;

    public ArrayList<Tweet> getTweets() {
        return new ArrayList<>();
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getStart() {
        return start;
    }

    // yyyy-mm-dd
    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    // yyyy-mm-dd
    public void setEnd(String end) {
        this.end = end;
    }

    public void addKeyword(String keyword) {
        this.keywords.add(keyword);
    }

    public String request() {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(BASE_URI + encodedParams());
        CloseableHttpResponse response = null;
        String result = "";
        try {
            logger.info(String.format("Requesting %s", httpGet.getURI()));
            response = httpclient.execute(httpGet);
            logger.info(response.getStatusLine());

            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                buffer.append(line);
            }
            result = buffer.toString();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public static void main(String[] args) {
        TwitterInsightsAPI api = new TwitterInsightsAPI();
        api.addKeyword("Elektromobilität");
        api.addKeyword("Emobilität");
        api.addKeyword("Elektromobilitaet");
        api.addKeyword("Emobilitaet");
        api.addKeyword("eMobility");
        api.addKeyword("e-Mobility");
        api.addKeyword("eMobilität");
        api.addKeyword("eMobilitaet");
        api.addKeyword("e-Mobilität");
        api.addKeyword("e-Mobilitaet");
        api.addKeyword("electric mobility");
        api.addKeyword("electric vehicle");
        api.addKeyword("Elektroauto");
        api.addKeyword("Elektrofahrzeug");
        api.addKeyword("eMobil");
        api.addKeyword("eAuto");
        api.addKeyword("eCar");
        api.addKeyword("E-Tankstelle");
        api.addKeyword("Ladesäule");
        api.addKeyword("Ladesaeule");
        api.addKeyword("fortwo electric drive");
        api.addKeyword("BMW i3");
        api.addKeyword("Nissan Leaf");
        api.addKeyword("Renault Zoe");
        api.addKeyword("Opel Ampera");
        api.addKeyword("eup");
        api.addKeyword("e-up");
        api.addKeyword("e-up!");
        api.addKeyword("eGolf");
        api.addKeyword("Golf GTE");
        api.addKeyword("miev");
        api.addKeyword("i-MiEV");
        api.addKeyword("Tesla Model S");
        api.addKeyword("Peugeot iON");
        api.setStart("2011-01-01");
        api.setEnd("2015-02-28");
        System.out.println(api.request());
    }

    public String encodedParams() {
        return String.format("q=%s&size=%s&from=%s", encodeURIComponent(queryString()), size, from);
    }

    // q=ibm%2B-apple%2Bfollowers_count%253A500&size=5&from=0 11.905.163
    // q=ibm%2520-apple%2520followers_count%253A500%250A&size=5 71.475.082
    // q=ibm+-apple+followers_count%3A500&size=5&from=0 293.878

    // Tesla or musk   86.277.502
    // 515287
    // 143219

    private String queryString() {
        StringBuilder builder = new StringBuilder();

        builder.append("(");
        builder.append(this.keywords.stream().map(keyword -> String.format("\"%s\"",keyword)).collect(Collectors.joining(" OR ")));
        builder.append(")");

        if (this.start != null) {
            builder.append(" posted:" + this.start);
        }

        if (this.end != null) {
            builder.append("," + this.end);
        }

        builder.append(" lang:de");

        logger.info(builder.toString());

        return builder.toString();
    }

    public static String encodeURIComponent(String input) {
        int l = input.length();
        StringBuilder o = new StringBuilder(l * 3);
        try {
            for (int i = 0; i < l; i++) {
                String e = input.substring(i, i + 1);
                if ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.!~*'()".indexOf(e) == -1) {
                    byte[] b = e.getBytes("utf-8");
                    o.append(getHex(b));
                    continue;
                }
                o.append(e);
            }
            return o.toString();
        } catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return input;
    }

    private static String getHex(byte buf[]) {
        StringBuilder o = new StringBuilder(buf.length * 3);
        for (int i = 0; i < buf.length; i++) {
            int n = (int) buf[i] & 0xff;
            o.append("%");
            if (n < 0x10) {
                o.append("0");
            }
            o.append(Long.toString(n, 16).toUpperCase());
        }
        return o.toString();
    }
}
