package de.janscheurenbrand.needminer.database;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import de.janscheurenbrand.needminer.twitter.Tweet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;

import static com.mongodb.client.model.Filters.*;

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

    public ArrayList<Tweet> getGermanTweets() {
        logger.debug("Getting German Tweets");
        return collection.find(new BasicDBObject("language","de")).into(new ArrayList<>());
    }

    public ArrayList<Tweet> getGermanTweetsWithoutUserType() {
        logger.debug("Getting German Tweets without user type");
        return collection.find(and(eq("user.type", "unknown"),eq("language", "de"))).into(new ArrayList<>());
    }

    public ArrayList<Tweet> getGermanTweetsFromNonInstitutionalUsers() {
        logger.debug("Getting German Tweets without user type");
        return collection.find(and(eq("user.type", "private"),eq("language", "de"))).into(new ArrayList<>());
    }

    public ArrayList<Tweet> getGermanTweetsWithoutURL() {
        logger.debug("Getting German Tweets without url");
        return collection.find(and(eq("booleanFeatures.has_url", false),eq("language", "de"))).into(new ArrayList<>());
    }

    public ArrayList<Tweet> getTweetsByUsername(String username) {
        logger.debug(String.format("Getting all Tweets from user %s", username));
        return collection.find(eq("user.screenName", username)).into(new ArrayList<>());
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

    public ArrayList<Tweet> getRandomTweets(int limit) {
        long count = getNumberOfTweets();
        logger.debug(String.format("Getting %s random Tweets", limit));

        ArrayList<Tweet> tweets1 = getGermanTweetsWithoutUserType();
        Collections.shuffle(tweets1);

        return new ArrayList<>(tweets1.subList(0,100));
    }

    public ArrayList<Tweet> getRandomTweetsFromGermanUsersWithoutURL(int limit) {
        long count = getNumberOfTweets();
        logger.debug(String.format("Getting %s random Tweets", limit));

        ArrayList<Tweet> tweets1 = getGermanTweetsWithoutURL();
        Collections.shuffle(tweets1);

        return new ArrayList<>(tweets1.subList(0,100));
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
        collection.replaceOne(eq("_id", tweet.getId()), tweet);
        logger.debug("Updated tweet " + tweet.getId());
    }
}
