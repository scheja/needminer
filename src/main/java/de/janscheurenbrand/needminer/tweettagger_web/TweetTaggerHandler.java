package de.janscheurenbrand.needminer.tweettagger_web;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.janscheurenbrand.needminer.database.Database;
import de.janscheurenbrand.needminer.database.TweetDAO;
import de.janscheurenbrand.needminer.features.Need;
import de.janscheurenbrand.needminer.features.NeedTagging;
import de.janscheurenbrand.needminer.twitter.Tweet;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by janscheurenbrand on 17/07/15.
 */
public class TweetTaggerHandler implements HttpHandler {
    private static final Logger logger = LogManager.getLogger("TweetTaggerHandler");

    HashMap<String,String> params;
    Session session;
    HashMap<String,String> templateData;

    Database db;
    TweetDAO tweetDAO;

    Gson gson = new Gson();

    public TweetTaggerHandler() {
        db = new Database();
        tweetDAO = db.getTweetDAO();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        params = Params.getRequestParams(exchange);
        templateData = new HashMap<>();

        // To retrive the SessionManager use the attachmentKey
        SessionManager sm = exchange.getAttachment(SessionManager.ATTACHMENT_KEY);
        // same goes to SessionConfig
        SessionConfig sessionConfig = exchange.getAttachment(SessionConfig.ATTACHMENT_KEY);

        // Retrieve or create session
        session = sm.getSession(exchange, sessionConfig);
        if (session == null) {
            session = sm.createSession(exchange, sessionConfig);
        }

        if (session.getAttribute("name") != null) {
            templateData.put("username", (String)session.getAttribute("name"));
        } else {
            templateData.put("username", "");
        }

        logger.info(String.format("Requesting %s", exchange.getRelativePath()));


        if (exchange.getRelativePath().equals("/")) {
            handleIndex(exchange);
        } else if (exchange.getRelativePath().equals("/start"))  {
            handleStart(exchange);
        } else if (exchange.getRelativePath().equals("/save"))  {
            handleSave(exchange);
        } else if (exchange.getRelativePath().equals("/thankyou"))  {
            handleThankYou(exchange);
        } else {
            handleShow(exchange);
        }
    }

    private void handleThankYou(HttpServerExchange exchange) {
        logger.info("thankyou");
        exchange.getResponseSender().send(Template.yield("tweets/thankyou", templateData));
    }

    private void handleIndex(HttpServerExchange exchange) {
        logger.info("index");
        exchange.getResponseSender().send(Template.yield("tweets/index", templateData));
    }

    private void handleStart(HttpServerExchange exchange) throws Exception {
        logger.info("start");
        String name = params.getOrDefault("name","");
        String language = params.getOrDefault("language", "");
        String dataset = params.getOrDefault("dataset","");

        // Validation
        if (name.length() == 0 || language.length() == 0 || dataset.length() == 0) {
            redirectTo("/", exchange);
            return;
        } else {
            session.setAttribute("name", name);
            templateData.put("username", name);
            session.setAttribute("language", language);
            session.setAttribute("dataset", dataset);
        }

        boolean tweetsWithURLs = dataset.endsWith("1") ? false : true;

        List<String> tweetIds = tweetDAO.getTweetsForTagging(10, name, 3, language, tweetsWithURLs).stream().map(t -> t.getId()).collect(Collectors.toList());

        session.setAttribute("tweetIds", tweetIds);

        redirectToNextTweet(exchange);
    }

    private void redirectToNextTweet(HttpServerExchange exchange) {
        List<String> tweetIds = (List<String>) session.getAttribute("tweetIds");
        if (tweetIds.size() > 0) {
            String nextTweetId = tweetIds.remove(0);
            session.setAttribute("currentTweetId", nextTweetId);
            redirectTo("/" + nextTweetId, exchange);
        } else {
            redirectTo("/thankyou", exchange);
        }
    }

    private void handleShow(HttpServerExchange exchange) {
        logger.info("show");
        if (!validSession()) {
            redirectTo("/", exchange);
            return;
        }

        String tweetId = exchange.getRelativePath().substring(1);
        Tweet tweet = tweetDAO.getTweetById(tweetId);

        if (tweet != null) {
            templateData.put("tweetText", tweet.getText());
            templateData.put("tweetId", tweet.getId());
            exchange.getResponseSender().send(Template.yield("tweets/show", templateData));
        } else {
            redirectTo("/", exchange);
        }

    }

    private void handleSave(HttpServerExchange exchange) {
        logger.info("save");
        if (!validSession()) {
            redirectTo("/", exchange);
            return;
        }

        String tweetId = params.getOrDefault("tweetId", "");

        // Validate request. Redirect to root if ID doesn't match
        if (!tweetId.equals(session.getAttribute("currentTweetId"))) {
            redirectTo("/", exchange);
        }

        Tweet tweet = tweetDAO.getTweetById(tweetId);

        String json = params.getOrDefault("needs", "");

        ArrayList<Need> needs = gson.fromJson(json, new TypeToken<ArrayList<Need>>(){}.getType());

        NeedTagging needTagging = new NeedTagging((String) session.getAttribute("name"), needs);

        tweet.addNeedTagging(needTagging);
        tweetDAO.update(tweet);

        redirectToNextTweet(exchange);
    }

    private void redirectTo(String location, HttpServerExchange exchange) {
        logger.info(String.format("Redirecting to %s", location));
        exchange.setResponseCode(StatusCodes.FOUND);
        exchange.getResponseHeaders().put(Headers.LOCATION, location);
        exchange.endExchange();
    }

    private boolean validSession() {
        if (session == null) {
            return false;
        }

        String name = (String) session.getAttribute("name");
        String language = (String) session.getAttribute("language");
        String dataset = (String) session.getAttribute("dataset");

        return ((name != null) && (language != null) && (dataset != null));
    }

}
