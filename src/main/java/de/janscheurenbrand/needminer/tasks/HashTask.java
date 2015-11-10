package de.janscheurenbrand.needminer.tasks;

import de.janscheurenbrand.needminer.twitter.Tweet;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Task for Hashing parts of a Tweet's text
 * One intended usage is deduplication
 */
public class HashTask implements TweetTask {

    @Override
    public Tweet call(Tweet tweet) throws InterruptedException {
        String hash = hash(tweet.getText());
        tweet.addHash("originalText", hash);

        hash = hash(textWithoutPrefixes(tweet.getText()));
        tweet.addHash("originalTextWithoutRT", hash);
        return tweet;
    }

    private String textWithoutPrefixes(String text) {
        if (text.startsWith("RT ")) {
            text = text.substring(3);
        }
        while (text.startsWith("@") && text.indexOf(" ") > 0) {
            int firstSpace = text.indexOf(" ");
            text = text.substring(firstSpace+1);
        }
        return text;
    }

    private String hash(String text) {
        String hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(text.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            hash = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hash;
    }
}
