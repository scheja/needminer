package de.janscheurenbrand.needminer.tasks;

import de.janscheurenbrand.needminer.database.Database;
import de.janscheurenbrand.needminer.database.TweetDAO;
import de.janscheurenbrand.needminer.twitter.Tweet;

import java.util.HashMap;
import java.util.Map;

/**
 * Evaluation Task. Acquires account data and prints that
 */
public class AccountStatsTask implements TweetTask {
    private Database db;
    private TweetDAO tweetDAO;
    private static HashMap<String,Integer> accounts = new HashMap<>();

    @Override
    public Tweet call(Tweet tweet) throws Exception {
        String user = tweet.getUser().getScreenName();
        int count = accounts.containsKey(user) ? accounts.get(user) : 0;
        accounts.put(user, count + 1);

        return null;
    }

    public static void stats() {
        accounts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(System.out::println);
    }

}
