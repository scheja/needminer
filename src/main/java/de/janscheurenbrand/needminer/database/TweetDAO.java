package de.janscheurenbrand.needminer.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import de.janscheurenbrand.needminer.twitter.Tweet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

/**
 * Tweet Data Access Object for reading and writing to the mongoDB-based tweet store
 */
public class TweetDAO {
    private static final Logger logger = LogManager.getLogger("TwitterDAO");
    private MongoCollection<Tweet> collection;

    public TweetDAO(Database database) {
        this.collection = database.getDatabase().getCollection("tweets", Tweet.class);
    }

    public ArrayList<Tweet> getTweets() {
        logger.debug("Getting all Tweets");
        return collection.find().into(new ArrayList<>());
    }

    public MongoCursor<Tweet> getTweetsIterator() {
        logger.debug("Getting all Tweets");
        return collection.find().batchSize(100).iterator();
    }

    public ArrayList<Tweet> getTweetsInRange(int batch, int nPerBatch) {
        logger.debug(String.format("Getting %s Tweets, starting at %s", nPerBatch, nPerBatch * batch));
        return collection.find()
                .skip(batch > 0 ? ((batch - 1) * nPerBatch) : 0)
                .limit(nPerBatch)
                .into(new ArrayList<>());
    }

    public long getNumberOfTweets() {
        logger.debug("Getting number of Tweets");
        return collection.count();
    }

    public void save(Tweet tweet) {
        collection.insertOne(tweet);
        logger.debug("Inserted tweet " + tweet.getId());
    }

    public void update(Tweet tweet) {
        collection.replaceOne(Filters.eq("_id", tweet.getId()), tweet);
        logger.debug("Updated tweet " + tweet.getId());
    }
}
