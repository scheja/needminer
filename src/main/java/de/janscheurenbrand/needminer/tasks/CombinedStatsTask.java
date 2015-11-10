package de.janscheurenbrand.needminer.tasks;

import de.janscheurenbrand.needminer.database.Database;
import de.janscheurenbrand.needminer.database.TweetDAO;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import static com.mongodb.client.model.Filters.*;

/**
 * Created by janscheurenbrand on 10/09/15.
 */
public class CombinedStatsTask implements Callable {
    @Override
    public Void call() throws Exception {
        Database db = new Database();
        TweetDAO tweetDAO = db.getTweetDAO();
        ArrayList<Bson> filters = new ArrayList<>();

        System.out.print("All: ");
        System.out.println(tweetDAO.getNumberOfTweets());

        filters.add(eq("language", "de"));
        System.out.print("German: ");
        System.out.println(tweetDAO.getCount(and(filters)));

        filters.add(eq("booleanFeatures.duplicate", false));
        System.out.print("Without dups: ");
        System.out.println(tweetDAO.getCount(and(filters)));

        filters.add(eq("booleanFeatures.has_url", false));
        System.out.print("Without URLs: ");
        System.out.println(tweetDAO.getCount(and(filters)));

        return null;
    }
}
