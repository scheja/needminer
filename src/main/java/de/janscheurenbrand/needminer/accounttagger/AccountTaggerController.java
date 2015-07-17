package de.janscheurenbrand.needminer.accounttagger;

import de.janscheurenbrand.needminer.database.Database;
import de.janscheurenbrand.needminer.database.TweetDAO;
import de.janscheurenbrand.needminer.twitter.Tweet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebView;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by janscheurenbrand on 12/06/15.
 */
public class AccountTaggerController {

    @FXML private WebView webView;
    @FXML private Button isInstitutionButton;
    @FXML private Button isPrivateButton;

    Database db;
    TweetDAO tweetDAO;
    ArrayList<Tweet> tweetsToProcess;

    Tweet currentTweet;

    Set<String> usernamesWithInfo = new HashSet<>();

    @FXML
    protected void initialize() {
        setWebViewUrl("http://google.com");

        db = new Database();
        tweetDAO = db.getTweetDAO();

        tweetsToProcess = tweetDAO.getRandomTweets(100);
        displayNextTweet();

        // Todo: refactor
        try {
            FileReader fileReader = new FileReader("/Users/janscheurenbrand/Desktop/institutional_users.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                usernamesWithInfo.add(line);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileReader fileReader = new FileReader("/Users/janscheurenbrand/Desktop/private_users.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                usernamesWithInfo.add(line);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Already known users: " + usernamesWithInfo.size());

    }

    @FXML
    private void setIsInstitution(ActionEvent event) {
        updateTweet("institutional");
    }

    @FXML
    private void setIsPrivate(ActionEvent event) {
        updateTweet("private");
    }

    private void setWebViewUrl(String url) {
        webView.getEngine().load(url);
    }

    @FXML
    public void processKeyEvent(KeyEvent event) {
        switch (event.getCode()) {
            case LEFT:
                updateTweet("institutional");
                break;

            case RIGHT:
                updateTweet("private");
                break;

            default:
                return;
        }
    }

    private void updateTweet(String type) {
        Tweet tweet = currentTweet;
        tweet.getUser().setType(type);
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("/Users/janscheurenbrand/Desktop/" + type + "_users.txt", true)))) {
            out.println(tweet.getUser().getScreenName());
        }catch (IOException e) {
            System.out.println(e);
        }
        tweetDAO.update(tweet);

        tweetDAO.getTweetsByUsername(tweet.getUser().getScreenName()).forEach(tweet1 -> {
            tweet1.getUser().setType(type);
            tweetDAO.update(tweet1);
        });

        usernamesWithInfo.add(tweet.getUser().getScreenName());
        displayNextTweet();
    }

    private void displayNextTweet() {
        if (tweetsToProcess.size() > 0) {
            currentTweet = tweetsToProcess.remove(0);
            if (usernamesWithInfo.contains(currentTweet.getUser().getScreenName())) {
                displayNextTweet();
            }
            setWebViewUrl(String.format("http://twitter.com/%s", currentTweet.getUser().getScreenName()));
        }
    }
}
