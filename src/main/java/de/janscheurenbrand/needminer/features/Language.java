package de.janscheurenbrand.needminer.features;

/**
 * Language Features represented as follows:
 * String lang: the detected language
 * double score: score of the classification (e.g. accuracy, precision)
 * String detector: the detector/classifier used to get the classification
 */
public class Language {
    private String lang;
    private double score;
    private String detector;

    public Language() {
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getDetector() {
        return detector;
    }

    public void setDetector(String detector) {
        this.detector = detector;
    }
}
