package de.janscheurenbrand.needminer.filter;

import de.janscheurenbrand.needminer.twitter.Tweet;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.lt;

/**
 * Created by janscheurenbrand on 13/09/15.
 */
public class TagRatioFilter implements Filter {
    double ratio = 0.5;
    boolean lessThan = false;

    public TagRatioFilter(double ratio, boolean lessThan) {
        this.ratio = ratio;
        this.lessThan = lessThan;
    }

    @Override
    public boolean accept(Tweet tweet) {
        if (lessThan) {
            return tweet.getCodeRatio() < ratio;
        } else {
            return  tweet.getCodeRatio() > ratio;
        }
    }

    @Override
    public Bson asBson() {
        if (lessThan) {
            return gt("codeRatio", ratio);
        } else {
            return  lt("codeRatio", ratio);
        }
    }



}
