package de.janscheurenbrand.needminer.filter;

import de.janscheurenbrand.needminer.twitter.Tweet;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.eq;

/**
 * Rejects a tweet if it is not written in the specified language. The language annotation by twitter is used here.
 */
public class LanguageFilter implements Filter {
    String language;

    public LanguageFilter(String language) {
        this.language = language;
    }

    @Override
    public boolean accept(Tweet tweet) {
        return tweet.getLanguage().equals(this.getLanguage());
    }

    @Override
    public Bson asBson() {
        return eq("language", language);
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
