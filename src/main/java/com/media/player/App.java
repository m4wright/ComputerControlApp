package com.media.player;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class App extends Application
{
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        URL url = getClass().getClassLoader().getResource("app.fxml");
        if (url != null)
        {
            Parent root = FXMLLoader.load(url);
            primaryStage.setTitle("Media Player");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        }
    }


    public static void main(String[] args)
    {
        launch(args);
    }
}
