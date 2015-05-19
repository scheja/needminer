package de.janscheurenbrand.needminer.tasks;

import de.janscheurenbrand.needminer.database.Database;
import de.janscheurenbrand.needminer.database.TweetDAO;
import de.janscheurenbrand.needminer.twitter.Tweet;

import java.util.HashMap;
import java.util.Map;

/**
 * Evaluation Task. Acquires language feature data and prints that
 */
public class LanguageStatsTask implements TweetTask {
    private Database db;
    private TweetDAO tweetDAO;
    private static HashMap<String,Integer> twitterLanguageMap = new HashMap<>();
    private static HashMap<String,Integer> detectedLanguageMap = new HashMap<>();

    @Override
    public Tweet call(Tweet tweet) throws Exception {
        String lang = tweet.getLanguage();
        int count = twitterLanguageMap.containsKey(lang) ? twitterLanguageMap.get(lang) : 0;
        twitterLanguageMap.put(lang, count + 1);

        lang = tweet.getDetectedLanguages().get(0).getLang();
        count = detectedLanguageMap.containsKey(lang) ? detectedLanguageMap.get(lang) : 0;
        detectedLanguageMap.put(lang, count + 1);
        return null;
    }

    public static void stats() {
        twitterLanguageMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(System.out::println);

        detectedLanguageMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(System.out::println);
    }

}
