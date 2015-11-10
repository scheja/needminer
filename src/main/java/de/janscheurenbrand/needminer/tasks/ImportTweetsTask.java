package de.janscheurenbrand.needminer.tasks;

import de.janscheurenbrand.needminer.database.Database;
import de.janscheurenbrand.needminer.database.TweetDAO;
import de.janscheurenbrand.needminer.twitter.Tweet;
import de.janscheurenbrand.needminer.twitter.User;

import java.sql.*;
import java.util.concurrent.Callable;

/**
 * Imports tweets from an existing mysql tweet store
 */
public class ImportTweetsTask implements Callable {
    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private Database db;
    private TweetDAO tweetDAO;

    @Override
    public Void call() throws Exception {
        db = new Database();
        tweetDAO = db.getTweetDAO();

        connect();

        try {
            resultSet = statement.executeQuery("select * from elektro7");

            while (resultSet.next()) {
                Tweet tweet = new Tweet();
                tweet.setId(String.valueOf(resultSet.getLong("tweetId")));
                tweet.setText(resultSet.getString("text"));
                tweet.setTimestamp(resultSet.getDate("createdAt"));
                tweet.setRetweetCount(resultSet.getInt("retweetCount"));
                tweet.setFavoriteCount(resultSet.getInt("favoriteCount"));
                tweet.setLanguage(resultSet.getString("tweetLanguage"));
                tweet.setLatitude(resultSet.getDouble("tweetLatitude"));
                tweet.setLongitude(resultSet.getDouble("tweetLongitude"));
                User user = new User();
                user.setId(resultSet.getLong("userId"));
                user.setUserName(resultSet.getString("userName"));
                user.setScreenName(resultSet.getString("screenName"));
                user.setLocation(resultSet.getString("userLocation"));
                user.setLanguage(resultSet.getString("userLanguage"));
                user.setFollowers(resultSet.getLong("followersCount"));
                user.setFriends(resultSet.getLong("friendsCount"));
                tweet.setUser(user);
                tweetDAO.save(tweet);
            }

        } finally {
            close();
            db.close();
        }

        return null;
    }


    private void connect() throws ClassNotFoundException, SQLException {
        // This will load the MySQL driver, each DB has its own driver
        Class.forName("com.mysql.jdbc.Driver");
        // Setup the connection with the DB
        connection = DriverManager.getConnection("jdbc:mysql://localhost/ststweets?user=root&password=");
        statement = connection.createStatement();
    }

    /**
     * MySQL
     */
    private void close() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }

            if (statement != null) {
                statement.close();
            }

            if (connection != null) {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
