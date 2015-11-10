package de.janscheurenbrand.needminer.filter;

import de.janscheurenbrand.needminer.twitter.Tweet;
import org.bson.conversions.Bson;

/**
 * Inetrface for Filters that reject non-matching tweets
 */
public interface Filter {

    /*
    * Returns whether a tweet matches the filter or not
    */
    boolean accept(Tweet tweet);

    Bson asBson();

}
