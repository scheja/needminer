package de.janscheurenbrand.needminer.filter;

import de.janscheurenbrand.needminer.twitter.Tweet;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.eq;

/**
 * Rejects tweets that contain an URL
 */
public class URLFilter implements Filter {
    @Override
    public boolean accept(Tweet tweet) {
        return !tweet.getBooleanFeatures().getOrDefault("has_url", true);
    }

    @Override
    public Bson asBson() {
        return eq("booleanFeatures.has_url", false);
    }
}
