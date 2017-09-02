package authentication.scene;

import authentication.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneSwitcher {

    public void switchSceneTo(String sceneName) throws IOException {

        Parent parent = FXMLLoader.load(SceneSwitcher.class.getResource(sceneName));
        Scene scene = new Scene(parent);
        Stage window = Main.thisStage;

        window.setScene(scene);
        window.show();
    }
}
