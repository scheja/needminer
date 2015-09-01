package de.janscheurenbrand.needminer.tweettagger_web;

import com.google.gson.Gson;
import de.janscheurenbrand.needminer.database.Database;
import de.janscheurenbrand.needminer.database.TweetDAO;
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

import java.security.MessageDigest;
import java.util.HashMap;

/**
 * Created by janscheurenbrand on 17/07/15.
 */
public class TweetTaggerHandler implements HttpHandler {
    private static final Logger logger = LogManager.getLogger("TweetTaggerHandler");

    final int tagSetSize = 100;

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
        int twitterKnowledge = Integer.valueOf(params.getOrDefault("twitterKnowledge", "0"));
        int emobilityKnowledge = Integer.valueOf(params.getOrDefault("emobilityKnowledge", "0"));

        // Validation
        if (name.length() == 0) {
            redirectTo("/", exchange);
            return;
        } else {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(name.getBytes());
            byte[] digest = md.digest();
            StringBuffer sb = new StringBuffer();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }

            session.setAttribute("name", sb.toString());
            templateData.put("username", name);
            session.setAttribute("twitterKnowledge", twitterKnowledge);
            session.setAttribute("emobilityKnowledge", emobilityKnowledge);
            session.setAttribute("alreadyTagged", 0);
        }

        redirectToNextTweet(exchange);
    }

    private void redirectToNextTweet(HttpServerExchange exchange) {

        int alreadyTagged = (Integer) session.getAttribute("alreadyTagged");

        if (alreadyTagged < 1000) {
            String nextTweetId = tweetDAO.getNextTweetIdForTagging((String) session.getAttribute("name"), 5);
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

        int alreadyTagged = (Integer) session.getAttribute("alreadyTagged");

        if (tweet != null) {
            templateData.put("tweetText", tweet.getText());
            templateData.put("tweetId", tweet.getId());
            templateData.put("tagSetSize", String.valueOf(tagSetSize));
            templateData.put("progress", String.valueOf(alreadyTagged+1));
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
        Tweet tweet = tweetDAO.getTweetById(tweetId);
        String tag = params.getOrDefault("tag", "");

        NeedTagging needTagging = new NeedTagging((String) session.getAttribute("name"), tag);
        needTagging.setEmobilityKnowledge((Integer)session.getAttribute("emobilityKnowledge"));
        needTagging.setTwitterKnowledge((Integer) session.getAttribute("twitterKnowledge"));

        tweet.addNeedTagging(needTagging);
        tweetDAO.update(tweet);

        int alreadyTagged = (Integer) session.getAttribute("alreadyTagged");
        alreadyTagged++;
        session.setAttribute("alreadyTagged", alreadyTagged);

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

        return name != null;
    }

}
