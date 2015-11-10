package de.janscheurenbrand.needminer.tasks;

import de.janscheurenbrand.needminer.database.Database;
import de.janscheurenbrand.needminer.database.TweetDAO;
import de.janscheurenbrand.needminer.features.NeedTagging;
import de.janscheurenbrand.needminer.twitter.Tweet;

import java.util.concurrent.Callable;

/**
 * Marks tagged tweets with their need/noneed ratio
 */
public class TaggingRatioTask implements Callable {

    @Override
    public Void call() throws Exception {
        Database database = new Database();
        TweetDAO tweetDAO = database.getTweetDAO();
        tweetDAO.setCollection("tweets");
        tweetDAO.getTweetsWithTaggings().forEach(tweet -> {
            setRatio(tweet);
            tweetDAO.update(tweet);
        });
        return null;
    }

    public static Tweet setRatio(Tweet tweet) {
        if (tweet.getNeedTaggings() != null && tweet.getNeedTaggings().size() > 0) {
            int need = 0;
            int nothing = 0;

            System.out.println(tweet);

            for (NeedTagging tagging : tweet.getNeedTaggings()) {
                if (tagging.getTag().equals("need")) {
                    need++;
                }
                if (tagging.getTag().equals("nothing")) {
                    nothing++;
                }
            }

            float codeRatio = (float) need / (float) (need+nothing);
            if (codeRatio >= 0) {
                tweet.setCodeRatio(codeRatio);
            } else {
                System.out.println(tweet);
            }
        }
        return tweet;
    }
}
