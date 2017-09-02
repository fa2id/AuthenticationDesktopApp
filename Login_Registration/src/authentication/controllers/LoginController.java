package authentication.controllers;

import authentication.Main;
import authentication.scene.SceneSwitcher;
import authentication.security.Security;
import authentication.user.UserController;
import authentication.validation.Validation;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

import java.io.IOException;

/**
 * Created by Farid on 2017-08-03.
 */

public class LoginController {

    @FXML
    private Text warningText;
    @FXML
    private JFXPasswordField password;
    @FXML
    private JFXTextField userName;
    @FXML
    private JFXSpinner spinner;

    private boolean userIsAuthorized = false;

    @FXML
    public void switchToRegistrationScene() throws IOException {

        new SceneSwitcher().switchSceneTo("registration.fxml");
    }

    @FXML
    public void switchToForgetPasswordScene() throws IOException {

        new SceneSwitcher().switchSceneTo("forgetPassword.fxml");
    }

    @FXML
    public void checkLogin() {

        warningText.getParent().getParent().setDisable(true);
        Validation validation = new Validation();

        Runnable runnable = () -> {

            if (validation.isValid("username", userName, warningText)
                    && validation.isValid("password", password, warningText)) {
                Security security = new Security();

                try {
                    Main.getDatabaseConnection().getUserInfoFromDB(userName.getText().toLowerCase(), true);

                    String tempPSD = UserController.getUserSignedIn().getPassword().
                            substring(0, UserController.getUserSignedIn().getPassword().indexOf(":"));

                    String passwordToCheck = security.getFinalHashedPassword(
                            tempPSD + password.getText(), false);

                    if (passwordToCheck.equals(
                            UserController.getUserSignedIn().getPassword().substring(
                                    UserController.getUserSignedIn().getPassword().indexOf(":") + 1))) {

                        userIsAuthorized = true;
                        Platform.runLater(() -> {
                            try {
                                switchToProfileScene();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                    } else warningText.setText("Incorrect password!");

                } catch (Exception e) {
                    warningText.setText("User not found!");
                    e.printStackTrace();
                } finally {
                    if (!userIsAuthorized)
                        UserController.signOut();
                }
            }
        };
        new Thread(runnable).start();
        spinner.setVisible(false);
        warningText.getParent().getParent().setDisable(false);
    }

    private void switchToProfileScene() throws IOException {

        SceneSwitcher sceneSwitcher = new SceneSwitcher();

        if (userIsVerified()) {
            if (userIsAuthorized) {

                sceneSwitcher.switchSceneTo("profile.fxml");
            }
        } else
            sceneSwitcher.switchSceneTo("emailVerification.fxml");

    }

    private boolean userIsVerified() {

        return Main.getDatabaseConnection().isUserEmailVerified(userName.getText().toLowerCase());
    }
}