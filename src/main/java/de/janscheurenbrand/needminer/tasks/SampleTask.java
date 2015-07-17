package de.janscheurenbrand.needminer.tasks;

import de.janscheurenbrand.needminer.database.Database;
import de.janscheurenbrand.needminer.database.TweetDAO;
import de.janscheurenbrand.needminer.twitter.Tweet;

import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * Returns sample Tweets
 */
public class SampleTask implements Callable<Collection<Tweet>> {
    int numberOfTweetsToLoad;

    public SampleTask(int numberOfTweetsToLoad) {
        this.numberOfTweetsToLoad = numberOfTweetsToLoad;
    }

    @Override
    public Collection<Tweet> call() throws Exception {
        Database db = new Database();
        TweetDAO tweetDAO = db.getTweetDAO();
        return tweetDAO.getRandomTweetsFromGermanUsersWithoutURL(numberOfTweetsToLoad);
    }
}
