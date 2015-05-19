package de.janscheurenbrand.needminer.tasks;

import de.janscheurenbrand.needminer.twitter.Tweet;

/**
 * Interface for Tasks intended to be run via Worker and WorkerPool
 * Each Task is executed for the specified set of tweets (e.g. all or training set),
 * therefore the Task is called with a Tweet as parameter, executed and returns the
 * modified tweet for persistence (back in WorkerPool).
 */
public interface TweetTask {
    Tweet call(Tweet tweet) throws Exception;
}
