package de.janscheurenbrand.needminer.tweettagger;
/**
 * Created by janscheurenbrand on 26/06/15.
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class TweetTagger extends Application {
    TweetTaggerController tweetTaggerController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/tweettagger.fxml"));
        Pane root = loader.load();
        tweetTaggerController = loader.getController();
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Tweet Tagger");
        primaryStage.setMinHeight(625);
        primaryStage.setMinWidth(800);
        primaryStage.show();
    }


}
