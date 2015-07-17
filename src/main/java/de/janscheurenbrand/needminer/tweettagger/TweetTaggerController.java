package de.janscheurenbrand.needminer.tweettagger;

import de.janscheurenbrand.needminer.database.Database;
import de.janscheurenbrand.needminer.database.TweetDAO;
import de.janscheurenbrand.needminer.features.Need;
import de.janscheurenbrand.needminer.twitter.Tweet;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

/**
 * Created by janscheurenbrand on 10/07/15.
 */
public class TweetTaggerController {
    private static final Logger logger = LogManager.getLogger("TweetTaggerController");

    @FXML private TextArea tweetDisplay;
    @FXML private ListView<String> listView;
    @FXML private Button markBtn;
    @FXML private Button nextBtn;

    Database db;
    TweetDAO tweetDAO;
    ArrayList<Tweet> tweetsToProcess;

    Tweet currentTweet;

    @FXML protected void initialize() {
        db = new Database();
        tweetDAO = db.getTweetDAO();

        tweetsToProcess = tweetDAO.getRandomTweets(100);
        nextTweet();

        listView.setItems(FXCollections.observableArrayList(currentTweet.getNeedTexts()));
    }

    @FXML protected void nextTweet() {
        logger.debug("Next tweet");
        if (tweetsToProcess.size() > 0) {
            currentTweet = tweetsToProcess.remove(0);
        }
        tweetDisplay.setText(currentTweet.getText());
        listView.setItems(FXCollections.observableArrayList(currentTweet.getNeedTexts()));
    }

    @FXML protected void markNeed() {
        logger.debug("Next tweet");
        int start = tweetDisplay.getSelection().getStart();
        int end = tweetDisplay.getSelection().getEnd();
        if (end > start) {
            Need need = new Need(start, end, "manual");
            currentTweet.addNeed(need);
        }
        listView.setItems(FXCollections.observableArrayList(currentTweet.getNeedTexts()));
    }

    @FXML protected void processKeyEvent(KeyEvent event) {
        logger.debug(String.format("%s key pressed", event.getCharacter()));
    }

}