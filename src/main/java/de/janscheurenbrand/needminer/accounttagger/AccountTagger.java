package de.janscheurenbrand.needminer.accounttagger;/**
 * Created by janscheurenbrand on 12/06/15.
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class AccountTagger extends Application {
    AccountTaggerController accountTaggerController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/accounttagger.fxml"));
        Pane root = loader.load();
        accountTaggerController = loader.getController();
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Account Tagger");
        primaryStage.setMinHeight(625);
        primaryStage.setMinWidth(800);
        primaryStage.show();
    }


}
