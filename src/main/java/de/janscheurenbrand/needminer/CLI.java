package de.janscheurenbrand.needminer;

import de.janscheurenbrand.needminer.tasks.*;
import de.janscheurenbrand.needminer.twitter.Tweet;
import de.janscheurenbrand.needminer.util.TweetExcelExport;
import de.janscheurenbrand.needminer.worker.WorkerPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Command line interface for running various tasks
 */
public class CLI {
    private static final Logger logger = LogManager.getLogger("CLI");

    private static ArrayList<Option> options = new ArrayList<>();
    private static WorkerPool workerPool;

    public static void main(String[] args) {
        logger.info("Starting CLI");

        // create the Options
        options.add(new Option("import", "Imports Tweets from a MySQL Database", "importTweets"));
        options.add(new Option("language", "Detects the Language of the tweets with several language detection frameworks", "languageDetection"));
        options.add(new Option("languagestats", "Calculates and displays some stats about tweets", "languageStats"));
        options.add(new Option("accountstats", "Calculates and displays some stats about user accounts", "accountStats"));
        options.add(new Option("hash", "Hashes different parts of the tweet text", "hashing"));
        options.add(new Option("markduplicates", "Mark dupicate tweets. Run hash before!", "markDuplicates"));
        options.add(new Option("analyze", "Analyzes content of the tweet", "analyzeContent"));
        options.add(new Option("taggingratio", "Mark tagged tweets with their need/noneed ratio", "taggingRatio"));
        options.add(new Option("tagginginfo", "Show stats about the taggings", "taggingInfo"));
        options.add(new Option("sample", "Gets a sample of the tweets in the DB"));
        options.add(new Option("stats", "Get stats about the dataset"));
        options.add(new Option("exit", "Stops all tasks and exits the application"));
        options.add(new Option("help", "Prints this list", "printHelp"));

        printHelp();

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line;
        // call exit() to exit the loop...
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                line = in.readLine();
                parse(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void importTweets() throws Exception {
        logger.info("Starting Tweet import");
        long start = System.currentTimeMillis();
        ImportTweetsTask importTweetsTask = new ImportTweetsTask();
        importTweetsTask.call();
        long end = System.currentTimeMillis();
        logger.info(String.format("%s completed in %dms%n", "Tweet import", end - start));
    }

    // Run hashing before!
    public static void markDuplicates() throws Exception {
        logger.info("Starting marking of duplicates");
        long start = System.currentTimeMillis();
        MarkDuplicatesTask markDuplicatesTask = new MarkDuplicatesTask();
        markDuplicatesTask.call();
        long end = System.currentTimeMillis();
        logger.info(String.format("%s completed in %dms%n", "Duplicate marking", end - start));
    }

    public static void languageDetection() throws Exception{
        logger.info("Starting Language detection");
        long start = System.currentTimeMillis();
        workerPool = new WorkerPool(LanguageTask.class, 10);
        workerPool.start();
        long end = System.currentTimeMillis();
        logger.info(String.format("%s completed in %dms%n", "Language detection", end - start));
    }

    public static void languageStats() throws Exception {
        logger.info("Starting calculation of language stats");
        long start = System.currentTimeMillis();
        workerPool = new WorkerPool(LanguageStatsTask.class, 10);
        workerPool.start();
        LanguageStatsTask.stats();
        long end = System.currentTimeMillis();
        logger.info(String.format("%s completed in %dms%n", "Language stats calculation", end - start));
    }

    public static void accountStats() throws Exception {
        logger.info("Starting calculation of language stats");
        long start = System.currentTimeMillis();
        workerPool = new WorkerPool(AccountStatsTask.class, 10);
        workerPool.start();
        AccountStatsTask.stats();
        long end = System.currentTimeMillis();
        logger.info(String.format("%s completed in %dms%n", "Account stats calculation", end - start));
    }

    public static void hashing() throws Exception {
        logger.info("Starting hashing");
        long start = System.currentTimeMillis();
        workerPool = new WorkerPool(HashTask.class, 10);
        workerPool.start();
        long end = System.currentTimeMillis();
        logger.info(String.format("%s completed in %dms%n", "Hashing", end - start));
    }

    public static void analyzeContent() throws Exception {
        logger.info("Starting content analyzer");
        long start = System.currentTimeMillis();
        workerPool = new WorkerPool(ContentAnalyzerTask.class, 10);
        workerPool.start();
        long end = System.currentTimeMillis();
        logger.info(String.format("%s completed in %dms%n", "Analyzing Content", end - start));
    }

    public static void taggingInfo() throws Exception {
        logger.info("Starting tagging info");
        long start = System.currentTimeMillis();

        TaggingInfoTask taggingInfoTask = new TaggingInfoTask();
        taggingInfoTask.call();

        long end = System.currentTimeMillis();
        logger.info(String.format("%s completed in %dms%n", "Tagging Info", end - start));
    }

    public static void taggingRatio() throws Exception {
        logger.info("Starting tagging ratio task");
        long start = System.currentTimeMillis();

        TaggingRatioTask taggingRatioTask = new TaggingRatioTask();
        taggingRatioTask.call();

        long end = System.currentTimeMillis();
        logger.info(String.format("%s completed in %dms%n", "Tagging ratio task", end - start));
    }

    public static void stats() throws Exception {
        logger.info("Starting stats");
        long start = System.currentTimeMillis();

        CombinedStatsTask combinedStatsTask = new CombinedStatsTask();
        combinedStatsTask.call();

        long end = System.currentTimeMillis();
        logger.info(String.format("%s completed in %dms%n", "Stats", end - start));
    }

    public static void sample() throws Exception {
        logger.info("Starting sampling");
        long start = System.currentTimeMillis();
        SampleTask sampleTask = new SampleTask(100);
        Collection<Tweet> tweets = sampleTask.call();
        TweetExcelExport export = new TweetExcelExport(
                new String[]{"Document Group","Document Name", "Tweet"},
                "tweets-batch-4",
                tweets.iterator(),
                new File(System.getProperty("user.home")+"/Desktop/tweets-"+String.valueOf(System.currentTimeMillis())+".xls"));
        export.export();
        long end = System.currentTimeMillis();
        logger.info(String.format("%s completed in %dms%n", "Sampling", end - start));
    }

    public static void exit() {
        if (workerPool != null) {
            logger.info("Stopping Workers...");
            workerPool.shutdown();
        }
        System.exit(0);
    }

    private static void parse(String line) {
        if(line.equals("")) {
            showProgress();
            return;
        }
        for (Option option : options) {
            if (line.trim().equals(option.getName())) {
                new Thread(() -> {
                    try {
                        CLI.class.getMethod(option.getMethodName()).invoke(null);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }).start();
                return;
            }
        }
        System.out.println("Command not found");
    }


    private static void showProgress() {
        if (workerPool != null) {
            workerPool.progress();
        }
    }

    public static void printHelp() {
        System.out.println("Usage:");
        String[][] table = new String[options.size()][2];
        for (int i = 0; i < options.size(); i++) {
            table[i][0] = options.get(i).getName();
            table[i][1] = options.get(i).getDescription();
        }
        printTable(table);
    }

    // http://stackoverflow.com/a/275438
    private static void printTable(String[][] table) {
        // Find out what the maximum number of columns is in any row
        int maxColumns = 0;
        for (String[] aTable : table) {
            maxColumns = Math.max(aTable.length, maxColumns);
        }

        // Find the maximum length of a string in each column
        int[] lengths = new int[maxColumns];
        for (String[] aTable : table) {
            for (int j = 0; j < aTable.length; j++) {
                lengths[j] = Math.max(aTable[j].length(), lengths[j]);
            }
        }

        // Generate a format string for each column
        String[] formats = new String[lengths.length];
        for (int i = 0; i < lengths.length; i++) {
            formats[i] = "%1$-" + lengths[i] + "s" + (i + 1 == lengths.length ? "\n" : " ");
        }

        // Print 'em out
        for (String[] aTable : table) {
            for (int j = 0; j < aTable.length; j++) {
                System.out.printf(formats[j], aTable[j]);
            }
        }
    }

    private static class Option {
        String name;
        String description;
        String methodName;

        public Option(String name, String description) {
            this.name = name;
            this.description = description;
            this.methodName = name;
        }

        public Option(String name, String description, String methodName) {
            this.name = name;
            this.description = description;
            this.methodName = methodName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }
    }

}
