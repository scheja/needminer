package de.janscheurenbrand.needminer.tasks;

import de.janscheurenbrand.needminer.features.Language;
import de.janscheurenbrand.needminer.twitter.Tweet;
import me.champeau.ld.LangDetector;
import me.champeau.ld.UberLanguageDetector;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Task for detecting the language of a tweet with different language detectors
 */
public class LanguageTask implements TweetTask {

    @Override
    public Tweet call(Tweet tweet) throws InterruptedException {
        tweet.addDetectedLanguages(detectLanguage(tweet));
        return tweet;
    }

    private List<Language> detectLanguage(Tweet tweet) {
        UberLanguageDetector detector = UberLanguageDetector.getInstance();
        Collection<LangDetector.Score> scores = detector.scoreLanguages(tweet.getText());
        return scores.stream().map(score -> {
            Language language = new Language();
            language.setDetector("jlanguagedetect");
            language.setLang(score.getLanguage());
            language.setScore(score.getScore());
            return language;
        }).collect(Collectors.toList());
    }
}
