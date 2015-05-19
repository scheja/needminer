package de.janscheurenbrand.needminer.worker;

import com.mongodb.client.MongoCursor;
import de.janscheurenbrand.needminer.database.Database;
import de.janscheurenbrand.needminer.database.TweetDAO;
import de.janscheurenbrand.needminer.tasks.TweetTask;
import de.janscheurenbrand.needminer.twitter.Tweet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages the execution of a Task for a given set of Tweets
 */
public class WorkerPool {
    private static final Logger logger = LogManager.getLogger(WorkerPool.class);
    ThreadPoolExecutor executor;
    private LimitedQueue<Runnable> blockingQueue;
    private int tweetsToProcess;
    private AtomicInteger tweetsProcessed;
    private TweetDAO tweetDAO;
    private Class<? extends TweetTask> tasktype;
    private boolean stop = false;

    public WorkerPool(Class<? extends TweetTask> tasktype, int threads) {
        logger.debug("Initializing WorkerPool");
        this.tasktype = tasktype;
        Database db = new Database();
        tweetDAO = db.getTweetDAO();
        tweetsToProcess = (int) tweetDAO.getNumberOfTweets();
        blockingQueue = new LimitedQueue<>(50);
        executor = new ThreadPoolExecutor(10, 20, 5000, TimeUnit.MILLISECONDS, blockingQueue);
        tweetsProcessed = new AtomicInteger(0);
    }

    public void start() {
        logger.debug("Starting Workers");

        MongoCursor<Tweet> iterator = tweetDAO.getTweetsIterator();

        while (iterator.hasNext() && !stop) {
            Future future = executor.submit(new Worker(tasktype, iterator.next()));
            try {
                Tweet tweetToUpdate = (Tweet) future.get();
                if (tweetToUpdate != null) {
                    tweetDAO.update(tweetToUpdate);
                }
                tweetsProcessed.incrementAndGet();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        shutdown();
    }

    public void shutdown() {
        logger.debug("Stopping Workers");
        this.stop = true;
        try {
            executor.awaitTermination(10000, TimeUnit.MILLISECONDS);
            executor.shutdown();
            logger.info("Tweets processed: " + tweetsProcessed.get());
            System.gc();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void progress() {
        logger.info(String.format("%(,.2f", 100*(tweetsProcessed.doubleValue()/(double)tweetsToProcess)) + "%");
    }

    public class LimitedQueue<E> extends LinkedBlockingQueue<E> {
        public LimitedQueue(int maxSize) {
            super(maxSize);
        }

        @Override
        public boolean offer(E e) {
            // turn offer() and add() into a blocking calls (unless interrupted)
            try {
                put(e);
                return true;
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            return false;
        }

    }
}