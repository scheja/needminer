package de.janscheurenbrand.needminer.database;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import de.janscheurenbrand.needminer.filter.Filter;
import de.janscheurenbrand.needminer.twitter.Tweet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mongodb.client.model.Filters.*;

/**
 * Tweet Data Access Object for reading and writing to the mongoDB-based tweet store
 */
public class TweetDAO {
    private static final Logger logger = LogManager.getLogger("TwitterDAO");
    private MongoCollection<Tweet> collection;
    private Database database;

    public TweetDAO(Database database) {
        this.database = database;
        this.collection = database.getDatabase().getCollection("tweets", Tweet.class);
    }

    public void setCollection(String collection) {
        this.collection = this.database.getDatabase().getCollection(collection, Tweet.class);
    }

    public ArrayList<Tweet> getTweets() {
        logger.debug("Getting all Tweets");
        return collection.find().into(new ArrayList<>());
    }

    public ArrayList<Tweet> getGermanTweets() {
        logger.debug("Getting German Tweets");
        return collection.find(new BasicDBObject("language","de")).into(new ArrayList<>());
    }

    public Tweet getTweetById(String id) {
        logger.debug("Getting Tweets");
        return collection.find(new BasicDBObject("_id",id)).first();
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

    /*
     * expects tweets to have originalText hash set
     */
    public ArrayList<Tweet> getTweetsForTagging(int n, String tagger, int maxPreviousTaggings, String language, boolean with_url) {
        logger.debug("Getting German Tweets without url");
        ArrayList<Bson> filters = new ArrayList<>();
        // See that we have enough taggings for each tweet
        // Fill them up from the start
        filters.add(exists(String.format("needTaggings.%s", maxPreviousTaggings), false));
        // Never let someone tag a tweet more than once
        filters.add(nin("needTaggings.tagger", tagger));
        // Language
        filters.add(eq("language", language));
        // URLs
        filters.add(eq("booleanFeatures.has_url", with_url));
        // URLs
        filters.add(eq("booleanFeatures.duplicate", false));
        // If a tweet was tagged exactly twice without needs, it should not get displayed again
        filters.add(or(ne("needTaggings.0.needCount", 0), ne("needTaggings.1.needCount", 0), exists("needTaggings.2", true)));

        return collection.find(and(filters))
                .sort(new BasicDBObject("hashes.originalText", 1))
                .limit(n)
                .into(new ArrayList<>());
    }

    /*
     * expects tweets to have originalText hash set
     * only german
     * with and without url
    */
    public String getNextTweetIdForTagging(String tagger) {
        logger.debug("Getting next tweet id for tagger "+ tagger);

        collection.find(
                elemMatch(
                        "needTaggings",
                        and(
                                eq("tagger", tagger),
                                eq("tag", "currentlyInTagging")
                        )
                )
        ).into(new ArrayList<>()).forEach(tweet -> {
            tweet.removeNeedTaggingByTagger(tagger);
            logger.debug("Remove previous currentlyInTagging tag from tweet " + tweet.getId());
            this.update(tweet);
        });

        ArrayList<Bson> filters = new ArrayList<>();
        // See that we have enough taggings for each tweet
        // Fill them up from the start
        int maxTaggings = 3;
        filters.add(exists(String.format("needTaggings.%s", maxTaggings - 1), false));
        // Never let someone tag a tweet more than once
        filters.add(nin("needTaggings.tagger", tagger));
        // Language
        filters.add(eq("language", "de"));
        // No URLs
        filters.add(eq("booleanFeatures.has_url", false));
        // No duplicates
        filters.add(eq("booleanFeatures.duplicate", false));
        // If a tweet was tagged exactly twice without needs, it should not get displayed again
        // filters.add(or(ne("needTaggings.0.needCount", 0), ne("needTaggings.1.needCount", 0), exists("needTaggings.2", true)));

        logger.debug("Remaining tweets: " + collection.count(and(filters)));

        Tweet tweetOrNull =  collection.find(and(filters))
                .sort(new BasicDBObject("hashes.originalText", 1))
                .first();

        if (tweetOrNull != null) {
            return tweetOrNull.getId();
        } else {
            return null;
        }

    }

    public ArrayList<Tweet> getTweetsWithTaggings() {
        logger.debug("Getting Tweets with Tagging");
        ArrayList<Bson> filters = new ArrayList<>();
        filters.add(exists("needTaggings"));

        return collection.find(and(filters))
                .sort(new BasicDBObject("hashes.originalText", 1))
                .into(new ArrayList<>());
    }


    public ArrayList<Tweet> getTweetsByUsername(String username) {
        logger.debug(String.format("Getting all Tweets from user %s", username));
        return collection.find(eq("user.screenName", username)).into(new ArrayList<>());
    }

    public ArrayList<Tweet> getTweetsByTextHash(String hash) {
        logger.debug(String.format("Getting all Tweets with hash %s", hash));
        return collection.find(eq("hashes.textHash", hash)).into(new ArrayList<>());
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

    public long getCount(Bson bson) {
        logger.debug("Getting number of Tweets");
        return collection.count(bson);
    }

    public void save(Tweet tweet) {
        try {
            collection.insertOne(tweet);
            logger.debug("Inserted tweet " + tweet.getId());
        } catch (MongoWriteException e) {
            logger.warn(e);
        }
    }

    public void update(Tweet tweet) {
        collection.replaceOne(eq("_id", tweet.getId()), tweet);
        logger.debug("Updated tweet " + tweet.getId());
    }

    public ArrayList<Tweet> getTaggedAsNeedTweets(double percentage) {
        logger.debug("Getting Tweets with need Taggings");
        ArrayList<Bson> filters = new ArrayList<>();
        filters.add(gt("codeRatio", percentage));

        return collection.find(and(filters))
                .sort(new BasicDBObject("codeRatio", -1))
                .into(new ArrayList<>());

    }

    public ArrayList<Tweet> getTaggedAsNothingTweets(double percentage) {
        logger.debug("Getting Tweets with nothing Taggings");
        ArrayList<Bson> filters = new ArrayList<>();
        filters.add(lt("codeRatio", percentage));

        return collection.find(and(filters))
                .sort(new BasicDBObject("codeRatio", 1))
                .into(new ArrayList<>());

    }

    public ArrayList<Tweet> getTweetsWithFilters(List<Filter> rawFilters) {
        logger.debug("Getting Tweets with Filters");
        ArrayList<Bson> filters = new ArrayList<>();
        rawFilters.forEach(rawFilter -> filters.add(rawFilter.asBson()));
        return collection.find(and(filters)).into(new ArrayList<>());
    }
}
