package de.janscheurenbrand.needminer.twitter;

import com.google.gson.annotations.SerializedName;
import de.janscheurenbrand.needminer.features.Language;
import de.janscheurenbrand.needminer.features.Need;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tweet POJO
 * Gets serialized via Gson and persisted in MongoDB. Therefore no special serialization necessary.
 * Features can be implemented with simple POJOs and get persisted automatically
 */
public class Tweet {
    @SerializedName("_id")
    private String id;
    private String text;
    private User user;
    private Date timestamp;
    private int retweetCount;
    private int favoriteCount;
    private String language;
    private double latitude;
    private double longitude;
    private String place;
    private List<Language> detectedLanguages;
    private List<Language> detectedTweets;
    private List<Need> needs;
    private HashMap<String,String> hashes;
    private HashMap<String,Boolean> booleanFeatures;

    public Tweet() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getRetweetCount() {
        return retweetCount;
    }

    public void setRetweetCount(int retweetCount) {
        this.retweetCount = retweetCount;
    }

    public int getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(int favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public List<Language> getDetectedLanguages() {
        return detectedLanguages;
    }

    public void setDetectedLanguages(List<Language> detectedLanguages) {
        this.detectedLanguages = detectedLanguages;
    }

    public List<Language> getDetectedTweets() {
        return detectedTweets;
    }

    public void setDetectedTweets(List<Language> detectedTweets) {
        this.detectedTweets = detectedTweets;
    }

    public HashMap<String, String> getHashes() {
        return hashes;
    }

    public void setHashes(HashMap<String, String> hashes) {
        this.hashes = hashes;
    }

    public HashMap<String, Boolean> getBooleanFeatures() {
        return booleanFeatures;
    }

    public void setBooleanFeatures(HashMap<String, Boolean> booleanFeatures) {
        this.booleanFeatures = booleanFeatures;
    }

    public List<Need> getNeeds() {
        return needs;
    }

    public void setNeeds(List<Need> needs) {
        this.needs = needs;
    }

    @Override
    public String toString() {
        return "Tweet{" +
                "text='" + text + '\'' +
                ", id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tweet)) return false;

        Tweet tweet = (Tweet) o;

        return id.equals(tweet.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public void addDetectedLanguages(List<Language> languages) {
        if (this.getDetectedLanguages() == null) {
            this.setDetectedLanguages(languages);
        } else {
            this.getDetectedLanguages().addAll(languages);
        }
    }

    public void addBooleanFeature(String key, boolean value) {
        if (this.booleanFeatures == null) {
            this.booleanFeatures = new HashMap<>();
        }
        this.booleanFeatures.put(key, value);
    }


    public void addHash(String key, String value) {
        if (hashes == null) {
            hashes = new HashMap<>();
        }
        hashes.put(key,value);
    }

    public void addNeed(Need need) {
        if (this.needs == null) {
            this.needs = new ArrayList<>();
        }
        this.needs.add(need);
    }

    public List<String> getNeedTexts() {
        if (this.needs != null) {
            return this.needs.stream().map( need -> {
                return this.getText().substring(need.getStart(),need.getEnd());
            }).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }


    }
}
