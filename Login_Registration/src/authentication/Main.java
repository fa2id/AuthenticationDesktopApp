package authentication;

import authentication.database.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static Stage thisStage;
    private static DatabaseConnection databaseConnection;

    public static DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        databaseConnection = new DatabaseConnection();
        Parent root = FXMLLoader.load(getClass().getResource("scene/login.fxml"));
        primaryStage.setTitle("Authorization application - By Farid");
        primaryStage.setScene(new Scene(root));
        thisStage = primaryStage;
        primaryStage.show();
    }
}
