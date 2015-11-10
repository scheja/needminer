package de.janscheurenbrand.needminer.filter;

import de.janscheurenbrand.needminer.twitter.Tweet;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.exists;

/**
 * Created by janscheurenbrand on 13/09/15.
 */
public class TaggedFilter implements Filter {

    @Override
    public boolean accept(Tweet tweet) {
        return (tweet.getNeedTaggings() != null && tweet.getNeedTaggings().size() > 3);
    }

    @Override
    public Bson asBson() {
        return exists("needTaggings.2");
    }
}
