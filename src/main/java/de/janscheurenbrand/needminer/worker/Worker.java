package de.janscheurenbrand.needminer.worker;

import de.janscheurenbrand.needminer.tasks.TweetTask;
import de.janscheurenbrand.needminer.twitter.Tweet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

/**
 * Container for running a Task for a specific tweet
 */
public class Worker implements Callable<Tweet> {
    private Constructor<? extends TweetTask> ctor;
    private TweetTask task;
    private Tweet tweet;

    Worker(Class<? extends TweetTask> impl, Tweet tweet) {
        this.tweet = tweet;
        try {
            this.ctor = impl.getConstructor();
            this.task = ctor.newInstance();
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Tweet call() throws Exception {
        return task.call(tweet);
    }
}
