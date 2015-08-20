package de.janscheurenbrand.needminer.tasks;

import de.janscheurenbrand.needminer.database.Database;
import de.janscheurenbrand.needminer.database.TweetDAO;
import de.janscheurenbrand.needminer.twitter.Tweet;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * Created by janscheurenbrand on 11/08/15.
 */
public class TaggingInfoTask implements Callable {

    @Override
    public Void call() throws InterruptedException {
        Database db = new Database();
        TweetDAO tweetDAO = db.getTweetDAO();
        ArrayList<Tweet> tweets = tweetDAO.getTweetsWithTaggings();

        tweets.stream().forEach(this::print);

        return null;
    }

    private void print(Tweet tweet) {
        StringBuilder sb = new StringBuilder();

        sb.append(tweet.getText());
        sb.append("\n");

        tweet.getNeedTaggings().stream()
                .sorted((t1, t2) -> Integer.compare(t2.getNeedCount(), t1.getNeedCount()))
                .forEach(tagging -> {
                    if (tagging.getNeedCount() > 0) {
                        tagging.getNeeds().stream()
                                //.sorted((t1, t2) -> Integer.compare(t1.getStart(), t2.getStart()))
                                .forEach(need -> {

                                    if (need != null) {
                                        // Print the need
                                        for (int i = 0; i < need.getStart(); i++) {
                                            sb.append(" ");
                                        }
                                        sb.append("|");
                                        for (int i = need.getStart(); i < need.getEnd() - 1; i++) {
                                            sb.append("-");
                                        }
                                        sb.append("|");
                                        sb.append(" -- ");
                                        sb.append(tagging.getTagger());
                                        sb.append("\n");
                            }

                        });
            } else {
                sb.append("No needs -- ");
                sb.append(tagging.getTagger());
                sb.append("\n");
            }
        });

        System.out.println(sb.toString());
    }
}


