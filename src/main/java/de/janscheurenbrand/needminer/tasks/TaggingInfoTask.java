package de.janscheurenbrand.needminer.tasks;

import de.janscheurenbrand.needminer.database.Database;
import de.janscheurenbrand.needminer.database.TweetDAO;
import de.janscheurenbrand.needminer.twitter.Tweet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * Created by janscheurenbrand on 11/08/15.
 */
public class TaggingInfoTask implements Callable {
    HashMap<String,Integer> countMap = new HashMap<>();
    HashMap<String, HashMap<String, Integer>> taggerMap = new HashMap<>();
    ArrayList<Tweet> onlyNeed = new ArrayList<>();
    ArrayList<Tweet> mixed2Need = new ArrayList<>();
    ArrayList<Tweet> mixed2Nothing = new ArrayList<>();
    ArrayList<Tweet> onlyNothing = new ArrayList<>();
    int tweetSum = 0;
    int tagSum = 0;

    @Override
    public Void call() throws InterruptedException {
        Database db = new Database();
        TweetDAO tweetDAO = db.getTweetDAO();
        ArrayList<Tweet> tweets = tweetDAO.getTweetsWithTaggings();

        tweets.stream().forEach(this::analyzeAndPrint);

        System.out.println("#### NEED ####");
        onlyNeed.forEach(this::printTweet);

        System.out.println("#### MIXED: TWO NEED ####");
        mixed2Need.forEach(this::printTweet);

        System.out.println("#### MIXED: TWO NOTHING ####");
        mixed2Nothing.forEach(this::printTweet);

        System.out.println("#### NOTHING ####");
        onlyNothing.forEach(this::printTweet);

        printMap(countMap);
        printMap(taggerMap);
        analyzeTaggers(taggerMap);

        System.out.println("All tags: " + tagSum);
        System.out.println("All tweets: " + tweetSum);
        System.out.println("All three needs: " + onlyNeed.size());
        System.out.println("Two needs, one nothing: " + mixed2Need.size());
        System.out.println("Two nothing, one need: " + mixed2Nothing.size());
        System.out.println("All three nothing: " + onlyNothing.size());

        return null;
    }

    private void analyzeTaggers(HashMap<String, HashMap<String, Integer>> taggerMap) {
        StringBuilder sb = new StringBuilder();
        taggerMap.entrySet().stream()
                .sorted((t1, t2) -> t1.getKey().compareTo(t2.getKey()))
                .forEach(entry -> {
                    if (entry.getValue().containsKey("nothing") && entry.getValue().containsKey("need")) {
                        sb.append(entry.getKey());
                        sb.append(" = ");
                        int needs = entry.getValue().get("need");
                        int nothing = entry.getValue().get("nothing");
                        float percent = (needs * 100f) / (needs + nothing);
                        sb.append(String.format("%.2f", percent));
                        sb.append("% tweets with needs");
                    }
                    sb.append("\n");
                });
        System.out.println(sb.toString());
    }


    private void analyzeAndPrint(Tweet tweet) {
        StringBuilder sb = new StringBuilder();

        sb.append(tweet.getText());
        sb.append(" -- ");
        sb.append(tweet.getId());

        HashMap<String, String> miniMap = new HashMap<>();
        HashMap<String,Integer> values = new HashMap<>();

        System.out.println(tweet);

        // necessary to fix double entries
        tweet.getNeedTaggings().stream()
                .forEach(tagging -> {
                    miniMap.put(tagging.getTagger(), tagging.getTag());
                    int old = values.getOrDefault(tagging.getTag(), 0);
                    values.put(tagging.getTag(), old + 1);
                });

        miniMap.keySet().stream().forEach(key -> {
            inc(key);
            inc(miniMap.get(key));
            incTagger(key, miniMap.get(key));
            this.tagSum++;
        });

        this.tweetSum++;

        if (values.get("need") != null && values.get("need") > 2) {
            this.onlyNeed.add(tweet);
        }

        if (values.get("need") != null && values.get("need") == 2) {
            this.mixed2Need.add(tweet);
        }

        if (values.get("nothing") != null && values.get("nothing") > 2) {
            this.onlyNothing.add(tweet);
        }

        if (values.get("nothing") != null && values.get("nothing") == 2) {
            this.mixed2Nothing.add(tweet);
        }


        System.out.println(sb.toString());
        printMap(miniMap);
    }

    private void printTweet(Tweet tweet) {
        StringBuilder sb = new StringBuilder();

        sb.append(tweet.getText());
        sb.append(" -- ");
        sb.append(tweet.getId());

        HashMap<String, String> miniMap = new HashMap<>();

        // necessary to fix double entries
        tweet.getNeedTaggings().stream()
                .forEach(tagging -> {
                    miniMap.put(tagging.getTagger(), tagging.getTag());
                });

        System.out.println(sb.toString());
        printMap(miniMap);
    }

    private void printMap(HashMap<String, ? extends Object> map) {
        StringBuilder sb = new StringBuilder();
        map.entrySet().stream()
        .sorted((t1, t2) -> t1.getKey().compareTo(t2.getKey()))
        .forEach(entry -> {
            sb.append(entry.getKey());
            sb.append(" = ");
            sb.append(entry.getValue());
            sb.append("\n");
        });
        System.out.println(sb.toString());
    }

    // increase counter
    private void inc(String key) {
        int curVal;
        if (countMap.containsKey(key)) {
            curVal = countMap.get(key);
            countMap.put(key, curVal + 1);
        } else {
            countMap.put(key, 1);
        }
    }

    private void incTagger(String tagger, String value) {
        if (!taggerMap.containsKey(tagger)) {
            taggerMap.put(tagger, new HashMap<>());
        }

        HashMap taggerCounts = taggerMap.get(tagger);
        int curVal;
        if (taggerCounts.containsKey(value)) {
            curVal = (int)taggerCounts.get(value);
            taggerCounts.put(value, curVal + 1);
        } else {
            taggerCounts.put(value, 1);
        }
    }

}


