package de.janscheurenbrand.needminer.filter;

import de.janscheurenbrand.needminer.twitter.Tweet;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.eq;

/**
 * Rejects tweets that were marked as duplicates
 */
public class DuplicateFilter implements Filter {
    @Override
    public boolean accept(Tweet tweet) {
        return !tweet.getBooleanFeatures().getOrDefault("duplicate",true);
    }

    @Override
    public Bson asBson() {
        return eq("booleanFeatures.duplicate", false);
    }
}
