package de.janscheurenbrand.needminer.tasks;

import de.janscheurenbrand.needminer.twitter.Tweet;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.Properties;

/**
 * Created by janscheurenbrand on 22/06/15.
 */
public class ContentAnalyzerTask implements TweetTask {

    @Override
    public Tweet call(Tweet tweet) throws InterruptedException {
        checkForURLs(tweet);
        checkForRT(tweet);
        //nlp(tweet);
        return tweet;
    }

    private void checkForRT(Tweet tweet) {
        tweet.addBooleanFeature("retweet", tweet.getText().startsWith("RT"));
    }

    private void checkForURLs(Tweet tweet) {
        tweet.addBooleanFeature("has_url", tweet.getText().contains("http"));
    }

    private void nlp(Tweet tweet) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation annotation =  pipeline.process(tweet.getText());
    }

    // Follow links to their final host (folllow up to n 301 Redirects
    // Save the final url(s)
    // load the blacklist from a file
    // Analyze the final urls, check whether the host is in the pre-defined blacklist

}
