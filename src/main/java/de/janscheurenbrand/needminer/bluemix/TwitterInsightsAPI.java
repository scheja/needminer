package de.janscheurenbrand.needminer.bluemix;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.janscheurenbrand.needminer.database.Database;
import de.janscheurenbrand.needminer.database.TweetDAO;
import de.janscheurenbrand.needminer.twitter.Tweet;
import de.janscheurenbrand.needminer.twitter.User;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Created by janscheurenbrand on 20/08/15.
 */
public class TwitterInsightsAPI {
    private static final Logger logger = LogManager.getLogger("TwitterInsightsAPI");

    final String BASE_URI = "https://6a0d00e3a1fdc7b72629aa1dfde610e0:Bxx5MH8CPh@cdeservice.eu-gb.mybluemix.net/api/v1/messages/";
    ArrayList<String> keywords = new ArrayList<>();
    String start;
    String end;
    String language;
    int offset = 0;
    int batchSize = 500;
    int resultSize = 0;
    boolean remaining = true;

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
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

    public JsonObject request(boolean count) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String searchOrCount = count ? "count?" : "search?";
        HttpGet httpGet = new HttpGet(BASE_URI + searchOrCount + encodedParams());
        CloseableHttpResponse response = null;
        try {
            logger.info(String.format("Requesting %s", httpGet.getURI()));
            response = httpclient.execute(httpGet);
            logger.info(response.getStatusLine());
            Reader reader = new InputStreamReader(response.getEntity().getContent());
            return new JsonParser().parse(reader).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public ArrayList<Tweet> getAll() {

        ArrayList<Tweet> tweets = new ArrayList<>();

        while(this.remaining) {
            JsonObject json = this.request(false);
            tweets.addAll(this.processResponse(json));
            this.offset = this.offset + this.batchSize;
        }

        return tweets;
    }

    public int count() {
        JsonObject json = this.request(true);
        return json.get("search").getAsJsonObject().get("results").getAsInt();
    }

    public static void main(String[] args) {
        Database db = new Database();
        TweetDAO tweetDAO = db.getTweetDAO();
        ArrayList<String> keywords = new ArrayList<>();
        //keywords.add("Elektromobilität");
        //keywords.add("Emobilität");
        keywords.add("Elektromobilitaet");
        keywords.add("Emobilitaet");
        keywords.add("eMobility");
        keywords.add("e-Mobility");
        keywords.add("eMobilität");
        keywords.add("eMobilitaet");
        keywords.add("e-Mobilität");
        keywords.add("e-Mobilitaet");
        keywords.add("electric mobility");
        keywords.add("electric vehicle");
        keywords.add("Elektroauto");
        keywords.add("Elektrofahrzeug");
        keywords.add("eMobil");
        keywords.add("eAuto");
        keywords.add("eCar");
        keywords.add("E-Tankstelle");
        keywords.add("Ladesäule");
        keywords.add("Ladesaeule");
        keywords.add("fortwo electric drive");
        keywords.add("BMW i3");
        keywords.add("Nissan Leaf");
        keywords.add("Renault Zoe");
        keywords.add("Opel Ampera");
        keywords.add("eup");
        keywords.add("e-up");
        keywords.add("e-up!");
        keywords.add("eGolf");
        keywords.add("Golf GTE");
        keywords.add("miev");
        keywords.add("i-MiEV");
        keywords.add("Tesla Model S");
        //keywords.add("Peugeot iON");


        keywords.stream().forEach(keyword -> {
            logger.debug(String.format("%s starting", keyword));
            TwitterInsightsAPI api = new TwitterInsightsAPI();
            api.addKeyword(keyword);
            api.setStart("2011-01-01");
            api.setEnd("2015-09-08");

            logger.debug(String.format("%s: %s", keyword, api.count()));

            ArrayList<Tweet> tweets = api.getAll();

            tweets.stream().forEach(tweet -> {
                tweetDAO.save(tweet);
            });
            logger.debug(String.format("%s finished", keyword));
        });



    }

    public String encodedParams() {
        return String.format("q=%s&size=%s&from=%s", encodeURIComponent(queryString()), batchSize, offset);
    }

    // q=ibm%2B-apple%2Bfollowers_count%253A500&batchSize=5&offset=0 11.905.163
    // q=ibm%2520-apple%2520followers_count%253A500%250A&batchSize=5 71.475.082
    // q=ibm+-apple+followers_count%3A500&batchSize=5&offset=0 293.878

    // Tesla or musk   86.277.502
    // 515287
    // 143219

    private String queryString() {
        StringBuilder builder = new StringBuilder();

        builder.append("(");
        builder.append(this.keywords.stream().map(keyword -> String.format("\"%s\"", keyword)).collect(Collectors.joining(" OR ")));
        builder.append(")");

        if (this.start != null) {
            builder.append(" posted:" + this.start);
        }

        if (this.end != null) {
            builder.append("," + this.end);
        }

        if (this.language != null) {
            builder.append(" lang:" + this.language);
        }

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
        } catch (UnsupportedEncodingException e) {
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

    private ArrayList<Tweet> processResponse(JsonObject json) {
        String test = "{\"search\":{\"results\":6125,\"current\":5},\"tweets\":[{\"message\":{\"body\":\"Crowd-Sourcing-Idee für #Elektromobilität: #BMBF fördert Forschungsprojekt „CrowdStrom“. http://t.co/bewFK1fPqQ @stadtwerke_ms @WWU_Muenster\",\"favoritesCount\":0,\"link\":\"http://twitter.com/3E_Ticker/statuses/419084549180882944\",\"retweetCount\":1,\"twitter_lang\":\"de\",\"postedTime\":\"2014-01-03T12:35:17.000Z\",\"provider\":{\"link\":\"http://www.twitter.com\",\"displayName\":\"Twitter\",\"objectType\":\"service\"},\"actor\":{\"twitterTimeZone\":\"Berlin\",\"summary\":\"Hier twittert die PR-Agentur pr/omotion über tagesaktuelle Themen aus den Bereichen #Energieeffizienz, #Elektromobilität und #erneuerbare #Energien.\",\"friendsCount\":724,\"favoritesCount\":186,\"location\":{\"displayName\":\"Hannover, Hamburg, Deutschland\",\"objectType\":\"place\"},\"link\":\"http://www.twitter.com/3E_Ticker\",\"postedTime\":\"2011-03-23T10:44:01.000Z\",\"image\":\"https://pbs.twimg.com/profile_images/2986872166/cba3dc961e02e0efe5ef5352b90a609c_normal.png\",\"links\":[{\"rel\":\"me\",\"href\":\"http://www.pr-omotion.de\"}],\"listedCount\":24,\"id\":\"id:twitter.com:270842937\",\"languages\":[\"de\"],\"verified\":false,\"followersCount\":747,\"utcOffset\":\"3600\",\"statusesCount\":7193,\"displayName\":\"pr-omotion GmbH\",\"preferredUsername\":\"3E_Ticker\",\"objectType\":\"person\"},\"object\":{\"id\":\"object:search.twitter.com,2005:419084549180882944\",\"summary\":\"Crowd-Sourcing-Idee für #Elektromobilität: #BMBF fördert Forschungsprojekt „CrowdStrom“. http://t.co/bewFK1fPqQ @stadtwerke_ms @WWU_Muenster\",\"link\":\"http://twitter.com/3E_Ticker/statuses/419084549180882944\",\"postedTime\":\"2014-01-03T12:35:17.000Z\",\"objectType\":\"note\"},\"twitter_entities\":{\"trends\":[],\"symbols\":[],\"urls\":[{\"expanded_url\":\"http://ow.ly/seG8u\",\"indices\":[89,111],\"display_url\":\"ow.ly/seG8u\",\"url\":\"http://t.co/bewFK1fPqQ\"}],\"hashtags\":[{\"text\":\"Elektromobilität\",\"indices\":[24,41]},{\"text\":\"BMBF\",\"indices\":[43,48]}],\"user_mentions\":[{\"id\":122052435,\"name\":\"Stadtwerke Münster\",\"indices\":[112,126],\"screen_name\":\"stadtwerke_ms\",\"id_str\":\"122052435\"},{\"id\":24677217,\"name\":\"Universität Münster\",\"indices\":[127,140],\"screen_name\":\"WWU_Muenster\",\"id_str\":\"24677217\"}]},\"twitter_filter_level\":\"low\",\"id\":\"tag:search.twitter.com,2005:419084549180882944\",\"verb\":\"post\",\"generator\":{\"link\":\"http://www.hootsuite.com\",\"displayName\":\"Hootsuite\"},\"objectType\":\"activity\"},\"cde\":{\"content\":{\"sentiment\":{\"polarity\":\"NEUTRAL\",\"evidence\":[]}},\"author\":{\"location\":{\"state\":\"\",\"country\":\"GERMANY\",\"city\":\"Hannover\"},\"parenthood\":{\"evidence\":\"\",\"isParent\":\"unknown\"},\"gender\":\"unknown\",\"maritalStatus\":{\"isMarried\":\"unknown\",\"evidence\":\"\"}}}},{\"message\":{\"gnip\":{\"urls\":[{\"expanded_url\":\"http://www.umwelt-monitor.de/2014/04/solarpanels-als-sprungbrett-der-elektromobilitaet/?utm_source=twitterfeed&utm_medium=twitter\",\"expanded_status\":200,\"url\":\"http://t.co/sxEPtMfiI7\"}],\"language\":{\"value\":\"de\"}},\"body\":\"Solarpanels als Sprungbrett der Elektromobilität?: Verminderte Einspeisevergütung hin oder her: Solarstrom ble... http://t.co/sxEPtMfiI7\",\"favoritesCount\":0,\"link\":\"http://twitter.com/umweltmonitor/statuses/450822588390834177\",\"retweetCount\":0,\"twitter_lang\":\"de\",\"postedTime\":\"2014-04-01T02:30:55.000Z\",\"provider\":{\"link\":\"http://www.twitter.com\",\"displayName\":\"Twitter\",\"objectType\":\"service\"},\"actor\":{\"twitterTimeZone\":\"Berlin\",\"summary\":\"Laufend die aktuellen Veröffentlichungen und Meldungen von ausgewählten Institutionen, Ministerien, Verbänden und Medien. Noch mehr auf:\",\"friendsCount\":326,\"favoritesCount\":0,\"location\":{\"displayName\":\"Erde\",\"objectType\":\"place\"},\"link\":\"http://www.twitter.com/umweltmonitor\",\"postedTime\":\"2011-09-25T08:43:44.000Z\",\"image\":\"https://pbs.twimg.com/profile_images/1558961556/twitter-logo-umwelt-monitor_normal.gif\",\"links\":[{\"rel\":\"me\",\"href\":\"http://www.umwelt-monitor.de\"}],\"listedCount\":43,\"id\":\"id:twitter.com:379628540\",\"languages\":[\"de\"],\"verified\":false,\"followersCount\":997,\"utcOffset\":\"7200\",\"statusesCount\":12976,\"displayName\":\"umwelt-monitor\",\"preferredUsername\":\"umweltmonitor\",\"objectType\":\"person\"},\"object\":{\"id\":\"object:search.twitter.com,2005:450822588390834177\",\"summary\":\"Solarpanels als Sprungbrett der Elektromobilität?: Verminderte Einspeisevergütung hin oder her: Solarstrom ble... http://t.co/sxEPtMfiI7\",\"link\":\"http://twitter.com/umweltmonitor/statuses/450822588390834177\",\"postedTime\":\"2014-04-01T02:30:55.000Z\",\"objectType\":\"note\"},\"twitter_entities\":{\"symbols\":[],\"urls\":[{\"expanded_url\":\"http://bit.ly/1ljvqeO\",\"indices\":[114,136],\"display_url\":\"bit.ly/1ljvqeO\",\"url\":\"http://t.co/sxEPtMfiI7\"}],\"hashtags\":[],\"user_mentions\":[]},\"twitter_filter_level\":\"medium\",\"id\":\"tag:search.twitter.com,2005:450822588390834177\",\"verb\":\"post\",\"generator\":{\"link\":\"http://twitterfeed.com\",\"displayName\":\"twitterfeed\"},\"objectType\":\"activity\"},\"cdeInternal\":{\"tracks\":[{\"id\":\"2713720a-f341-4ca0-acd1-f8b1e6c0d00f\"}]},\"cde\":{\"content\":{\"sentiment\":{\"polarity\":\"NEUTRAL\",\"evidence\":[]}},\"author\":{\"location\":{\"state\":\"\",\"country\":\"\",\"city\":\"\"},\"parenthood\":{\"evidence\":\"\",\"isParent\":\"unknown\"},\"gender\":\"unknown\",\"maritalStatus\":{\"isMarried\":\"unknown\",\"evidence\":\"\"}}}},{\"message\":{\"gnip\":{\"urls\":[{\"expanded_url\":\"http://www.greencarreports.com/news/1091199_plug-in-electric-sales-in-march\",\"expanded_status\":200,\"url\":\"http://t.co/q6YfmCas2P\"}],\"language\":{\"value\":\"en\"}},\"body\":\"Plug-In Electric Car Sales In March: Nissan Leaf Has Best March Ever http://t.co/q6YfmCas2P\",\"favoritesCount\":0,\"link\":\"http://twitter.com/ElectricPledge/statuses/451044068177371136\",\"retweetCount\":0,\"twitter_lang\":\"de\",\"postedTime\":\"2014-04-01T17:11:00.000Z\",\"provider\":{\"link\":\"http://www.twitter.com\",\"displayName\":\"Twitter\",\"objectType\":\"service\"},\"actor\":{\"twitterTimeZone\":\"Pacific Time (US & Canada)\",\"summary\":\"Pledge To Drive An Electric Car Or If You Drive An Electric Car #ElectricCars #EV #PHEV #DriveElectricCars #ElectricCarPledge\",\"friendsCount\":2001,\"favoritesCount\":280,\"link\":\"http://www.twitter.com/ElectricPledge\",\"postedTime\":\"2014-02-06T15:24:06.000Z\",\"image\":\"https://pbs.twimg.com/profile_images/435283120334446593/JkgwOinj_normal.png\",\"links\":[{\"rel\":\"me\",\"href\":\"http://www.ElectricCarPledge.com\"}],\"listedCount\":8,\"id\":\"id:twitter.com:2330455399\",\"languages\":[\"en\"],\"verified\":false,\"followersCount\":556,\"utcOffset\":\"-25200\",\"statusesCount\":669,\"displayName\":\"Electric Car Pledge\",\"preferredUsername\":\"ElectricPledge\",\"objectType\":\"person\"},\"object\":{\"id\":\"object:search.twitter.com,2005:451044068177371136\",\"summary\":\"Plug-In Electric Car Sales In March: Nissan Leaf Has Best March Ever http://t.co/q6YfmCas2P\",\"link\":\"http://twitter.com/ElectricPledge/statuses/451044068177371136\",\"postedTime\":\"2014-04-01T17:11:00.000Z\",\"objectType\":\"note\"},\"twitter_entities\":{\"symbols\":[],\"urls\":[{\"expanded_url\":\"http://www.greencarreports.com/news/1091199_plug-in-electric-sales-in-march\",\"indices\":[69,91],\"display_url\":\"greencarreports.com/news/1091199_p…\",\"url\":\"http://t.co/q6YfmCas2P\"}],\"hashtags\":[],\"user_mentions\":[]},\"twitter_filter_level\":\"medium\",\"id\":\"tag:search.twitter.com,2005:451044068177371136\",\"verb\":\"post\",\"generator\":{\"link\":\"http://twitter.com/tweetbutton\",\"displayName\":\"Tweet Button\"},\"objectType\":\"activity\"},\"cdeInternal\":{\"tracks\":[{\"id\":\"2713720a-f341-4ca0-acd1-f8b1e6c0d00f\"}]},\"cde\":{\"content\":{\"sentiment\":{\"polarity\":\"NEUTRAL\",\"evidence\":[]}},\"author\":{\"location\":{},\"parenthood\":{\"evidence\":\"\",\"isParent\":\"unknown\"},\"gender\":\"unknown\",\"maritalStatus\":{\"isMarried\":\"unknown\",\"evidence\":\"\"}}}},{\"message\":{\"gnip\":{\"profileLocations\":[{\"geo\":{\"type\":\"point\",\"coordinates\":[11.64222,48.05139]},\"address\":{\"region\":\"Bavaria\",\"countryCode\":\"DE\",\"locality\":\"Landkreis München\",\"country\":\"Germany\"},\"displayName\":\"Landkreis München, Bavaria, Germany\",\"objectType\":\"place\"}],\"urls\":[{\"expanded_url\":\"http://adacemobility.wordpress.com/2014/03/31/nach-branden-tesla-verstarkt-unterboden/\",\"expanded_status\":200,\"url\":\"http://t.co/8htP95BxWc\"}],\"language\":{\"value\":\"de\"}},\"body\":\"Tesla reagiert auf Brände im Model S und verstärkt den Unterboden dreifach -&gt; http://t.co/8htP95BxWc #emobility\",\"favoritesCount\":0,\"link\":\"http://twitter.com/ADAC/statuses/450905132713410560\",\"retweetCount\":0,\"twitter_lang\":\"de\",\"postedTime\":\"2014-04-01T07:58:55.000Z\",\"provider\":{\"link\":\"http://www.twitter.com\",\"displayName\":\"Twitter\",\"objectType\":\"service\"},\"actor\":{\"twitterTimeZone\":\"Berlin\",\"summary\":\"Hier twittert Europas größter Automobilclub über Themen wie Mobilität, Auto, Verbraucherschutz, Reise und Verkehr.\",\"friendsCount\":441,\"favoritesCount\":117,\"location\":{\"displayName\":\"München\",\"objectType\":\"place\"},\"link\":\"http://www.twitter.com/ADAC\",\"postedTime\":\"2010-06-30T09:35:25.000Z\",\"image\":\"https://pbs.twimg.com/profile_images/1064577918/Logo_340_tcm11-225485_normal.jpg\",\"links\":[{\"rel\":\"me\",\"href\":\"http://www.adac.de\"}],\"listedCount\":296,\"id\":\"id:twitter.com:161234186\",\"languages\":[\"de\"],\"verified\":false,\"followersCount\":9292,\"utcOffset\":\"7200\",\"statusesCount\":6364,\"displayName\":\"ADAC \",\"preferredUsername\":\"ADAC\",\"objectType\":\"person\"},\"object\":{\"id\":\"object:search.twitter.com,2005:450905132713410560\",\"summary\":\"Tesla reagiert auf Brände im Model S und verstärkt den Unterboden dreifach -&gt; http://t.co/8htP95BxWc #emobility\",\"link\":\"http://twitter.com/ADAC/statuses/450905132713410560\",\"postedTime\":\"2014-04-01T07:58:55.000Z\",\"objectType\":\"note\"},\"twitter_entities\":{\"symbols\":[],\"urls\":[{\"expanded_url\":\"http://bit.ly/1fJeOZl\",\"indices\":[81,103],\"display_url\":\"bit.ly/1fJeOZl\",\"url\":\"http://t.co/8htP95BxWc\"}],\"hashtags\":[{\"text\":\"emobility\",\"indices\":[104,114]}],\"user_mentions\":[]},\"twitter_filter_level\":\"medium\",\"id\":\"tag:search.twitter.com,2005:450905132713410560\",\"verb\":\"post\",\"generator\":{\"link\":\"https://about.twitter.com/products/tweetdeck\",\"displayName\":\"TweetDeck\"},\"objectType\":\"activity\"},\"cdeInternal\":{\"tracks\":[{\"id\":\"2713720a-f341-4ca0-acd1-f8b1e6c0d00f\"}]},\"cde\":{\"content\":{\"sentiment\":{\"polarity\":\"NEUTRAL\",\"evidence\":[]}},\"author\":{\"location\":{\"state\":\"Bavaria\",\"country\":\"Germany\",\"city\":\"Landkreis München\"},\"parenthood\":{\"evidence\":\"\",\"isParent\":\"unknown\"},\"gender\":\"unknown\",\"maritalStatus\":{\"isMarried\":\"unknown\",\"evidence\":\"\"}}}},{\"message\":{\"gnip\":{\"urls\":[{\"expanded_url\":\"http://et-tutorials.de/4991/berechnung-der-stromstarke-durch-ein-starthilfekabel/\",\"expanded_status\":200,\"url\":\"http://t.co/f3TxDQtSTl\"}],\"language\":{\"value\":\"de\"}},\"body\":\"RT @etTutorials: Berechnung der Stromstärke durch ein Starthilfekabel... http://t.co/f3TxDQtSTl #emobility\",\"favoritesCount\":0,\"link\":\"http://twitter.com/bimpress1/statuses/451589762214674432\",\"retweetCount\":0,\"twitter_lang\":\"de\",\"postedTime\":\"2014-04-03T05:19:24.000Z\",\"provider\":{\"link\":\"http://www.twitter.com\",\"displayName\":\"Twitter\",\"objectType\":\"service\"},\"actor\":{\"twitterTimeZone\":\"Berlin\",\"summary\":\"Impressive Communication; B2B-Content; MarCom; PR 2.0; Social Media #SMM; Editor/Journalist; 20Y in media; #mobile IC #healthcare #cleantech, fun of consulting\",\"friendsCount\":658,\"favoritesCount\":153,\"location\":{\"displayName\":\"Greater Munich, #Augsburg\",\"objectType\":\"place\"},\"link\":\"http://www.twitter.com/bimpress1\",\"postedTime\":\"2010-02-09T17:55:09.000Z\",\"image\":\"https://pbs.twimg.com/profile_images/2999742622/a91929aa6d97ec384f4ff8fd46b50206_normal.jpeg\",\"links\":[{\"rel\":\"me\",\"href\":\"http://www.bimpress.com\"}],\"listedCount\":17,\"id\":\"id:twitter.com:112781003\",\"languages\":[\"de\"],\"verified\":false,\"followersCount\":301,\"utcOffset\":\"7200\",\"statusesCount\":1915,\"displayName\":\"Robert Brunner\",\"preferredUsername\":\"bimpress1\",\"objectType\":\"person\"},\"object\":{\"id\":\"object:search.twitter.com,2005:451589762214674432\",\"summary\":\"RT @etTutorials: Berechnung der Stromstärke durch ein Starthilfekabel... http://t.co/f3TxDQtSTl #emobility\",\"link\":\"http://twitter.com/bimpress1/statuses/451589762214674432\",\"postedTime\":\"2014-04-03T05:19:24.000Z\",\"objectType\":\"note\"},\"twitter_entities\":{\"symbols\":[],\"urls\":[{\"expanded_url\":\"http://et-tutorials.de/4991/berechnung-der-stromstarke-durch-ein-starthilfekabel/\",\"indices\":[73,95],\"display_url\":\"et-tutorials.de/4991/berechnun…\",\"url\":\"http://t.co/f3TxDQtSTl\"}],\"hashtags\":[{\"text\":\"emobility\",\"indices\":[96,106]}],\"user_mentions\":[{\"id\":52036568,\"name\":\"E-Technik Tutorials\",\"indices\":[3,15],\"screen_name\":\"etTutorials\",\"id_str\":\"52036568\"}]},\"twitter_filter_level\":\"medium\",\"id\":\"tag:search.twitter.com,2005:451589762214674432\",\"verb\":\"post\",\"generator\":{\"link\":\"https://about.twitter.com/products/tweetdeck\",\"displayName\":\"TweetDeck\"},\"objectType\":\"activity\"},\"cdeInternal\":{\"tracks\":[{\"id\":\"2713720a-f341-4ca0-acd1-f8b1e6c0d00f\"}]},\"cde\":{\"content\":{\"sentiment\":{\"polarity\":\"NEUTRAL\",\"evidence\":[]}},\"author\":{\"location\":{\"state\":\"\",\"country\":\"GERMANY\",\"city\":\"Munich\"},\"parenthood\":{\"evidence\":\"\",\"isParent\":\"unknown\"},\"gender\":\"male\",\"maritalStatus\":{\"isMarried\":\"unknown\",\"evidence\":\"\"}}}}],\"related\":{\"next\":{\"href\":\"https://cdeservice.eu-gb.mybluemix.net:443/api/v1/tweets/search?q=%28%22Elektromobilit%C3%A4t%22+OR+%22Emobilit%C3%A4t%22+OR+%22Elektromobilitaet%22+OR+%22Emobilitaet%22+OR+%22eMobility%22+OR+%22e-Mobility%22+OR+%22eMobilit%C3%A4t%22+OR+%22eMobilitaet%22+OR+%22e-Mobilit%C3%A4t%22+OR+%22e-Mobilitaet%22+OR+%22electric+mobility%22+OR+%22electric+vehicle%22+OR+%22Elektroauto%22+OR+%22Elektrofahrzeug%22+OR+%22eMobil%22+OR+%22eAuto%22+OR+%22eCar%22+OR+%22E-Tankstelle%22+OR+%22Lades%C3%A4ule%22+OR+%22Ladesaeule%22+OR+%22fortwo+electric+drive%22+OR+%22BMW+i3%22+OR+%22Nissan+Leaf%22+OR+%22Renault+Zoe%22+OR+%22Opel+Ampera%22+OR+%22eup%22+OR+%22e-up%22+OR+%22e-up%21%22+OR+%22eGolf%22+OR+%22Golf+GTE%22+OR+%22miev%22+OR+%22i-MiEV%22+OR+%22Tesla+Model+S%22+OR+%22Peugeot+iON%22%29+posted%3A2011-01-01%2C2015-02-28+lang%3Ade&from=5&batchSize=5\"}}}\n";

        if (json == null) {
            json = new JsonParser().parse(test).getAsJsonObject();
        }

        ArrayList<Tweet> tweets = new ArrayList<>();

        json.get("tweets").getAsJsonArray().iterator().forEachRemaining((jsonElement) -> {
            Tweet tweet = tweetFromJson(jsonElement);
            tweets.add(tweet);
        });

        this.resultSize = json.get("search").getAsJsonObject().get("results").getAsInt();
        int currentBatch = json.get("search").getAsJsonObject().get("current").getAsInt();
        logger.debug("And another " + currentBatch);
        this.remaining =  currentBatch> 0;

        return tweets;
    }

    private Tweet tweetFromJson(JsonElement jsonElement) {
        JsonObject messageObject = jsonElement.getAsJsonObject().get("message").getAsJsonObject();
        String text = messageObject.get("body").getAsString();
        String iri = messageObject.get("id").getAsString();
        String id = iri.substring(iri.lastIndexOf(":")+1);
        String language = messageObject.get("twitter_lang").getAsString();
        Date timestamp = getDate(messageObject.get("postedTime").getAsString());
        int retweetCount = messageObject.get("retweetCount").getAsInt();
        int favoriteCount = messageObject.get("favoritesCount").getAsInt();

        JsonObject userObject = messageObject.get("actor").getAsJsonObject();
        String userName = userObject.get("preferredUsername").getAsString();
        String screenName = userObject.get("displayName").getAsString();
        String location = "";
        if (userObject.get("location") != null) {
            location = userObject.get("location").getAsJsonObject().get("displayName").getAsString();
        }

        String userLanguage = "";
        if (userObject.get("language") != null && userObject.get("language").getAsJsonArray().size() > 0 ) {
            userLanguage = userObject.get("language").getAsJsonArray().get(0).getAsString();
        }

        long followers = userObject.get("followersCount").getAsLong();
        long friends = userObject.get("friendsCount").getAsLong();

        User user = new User();
        user.setUserName(userName);
        user.setScreenName(screenName);
        user.setLocation(location);
        user.setLanguage(userLanguage);
        user.setFollowers(followers);
        user.setFriends(friends);

        Tweet tweet = new Tweet();
        tweet.setUser(user);
        tweet.setText(text);
        tweet.setId(id);
        tweet.setLanguage(language);
        tweet.setTimestamp(timestamp);
        tweet.setRetweetCount(retweetCount);
        tweet.setFavoriteCount(favoriteCount);

        return tweet;
    }

    private Date getDate(String string) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        try {
            return sdf.parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
