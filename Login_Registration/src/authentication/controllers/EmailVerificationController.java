package authentication.controllers;

import authentication.Main;
import authentication.scene.SceneSwitcher;
import authentication.security.Security;
import authentication.user.UserController;
import authentication.validation.Validation;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class EmailVerificationController implements Initializable {

    @FXML
    private JFXTextField emailAddressField;
    @FXML
    private JFXButton changeEmailButton;
    @FXML
    private JFXTextField verificationCodeField;
    @FXML
    private Text emailChangeHelpText;
    @FXML
    private Text warningText;

    @FXML
    public void changeEmailAddress() {

        if (emailAddressField.isDisable()) {

            emailAddressField.setDisable(false);
            changeEmailButton.setText("Save new email address");

        } else {
            if (!emailAddressField.getText().equals(UserController.getUserSignedIn().getEmail())) {

                if (!emailAddressField.getText().trim().isEmpty()) {
                    Validation validation = new Validation();

                    if (validation.isValid("email", emailAddressField, warningText)) {

                        if (!Main.getDatabaseConnection().isDuplicateEmail(UserController.getUserSignedIn(), emailAddressField.getText())) {
                            Main.getDatabaseConnection().deleteQuery("faridsen_javaApp.verification", "email", UserController.getUserSignedIn().getEmail());
                            Main.getDatabaseConnection().verifyEmailOfUser(UserController.getUserSignedIn().getUsername(), false);
                            Main.getDatabaseConnection().sendVerificationDetailsToUser(emailAddressField.getText(), false);
                            Main.getDatabaseConnection().updateQuery("faridsen_javaApp.user", "username", UserController.getUserSignedIn().getUsername(), "email", emailAddressField.getText());

                            changeEmailButton.setText("Saved");
                            changeEmailButton.setDisable(true);
                            emailAddressField.setDisable(true);
                            emailChangeHelpText.setText("If you need to change your email address AGAIN, sign out and login again to be able to change your email address again.");
                            warningText.setText("Email changed successfully.");
                            Main.getDatabaseConnection().getUserInfoFromDB(UserController.getUserSignedIn().getUsername(), true);
                        } else
                            warningText.setText("Duplicate email address! try another email address.");
                    }
                } else {
                    warningText.setText("New email address field is empty!");
                }
            } else {
                warningText.setText("You have entered your current email address. If you need to change your email address, choose another email address.");
            }
        }
    }

    @FXML
    public void resendVerificationCode() {

        warningText.setText(" ");

        Runnable runnable = () -> {
            Main.getDatabaseConnection().sendVerificationDetailsToUser(UserController.getUserSignedIn().getEmail(), true);
            Platform.runLater(() -> warningText.setText("We sent a new verification code. Please enter your new code."));

        };
        new Thread(runnable).start();

    }

    @FXML
    public void signOutUser() throws IOException {

        UserController.signOut();
        new SceneSwitcher().switchSceneTo("login.fxml");

    }

    @FXML
    public void verifyEmailOfUser() throws IOException {

        warningText.setText(" ");

        Security security = new Security();

        String verificationCode = Main.getDatabaseConnection().getVerificationCode(UserController.getUserSignedIn().getEmail());

        String tempSalt = verificationCode.substring(0, verificationCode.indexOf(":"));

        String passwordToCheck = security.getFinalHashedPassword(
                tempSalt + verificationCodeField.getText(), false);

        if (passwordToCheck.equals(
                verificationCode.substring(
                        verificationCode.indexOf(":") + 1))) {
            Main.getDatabaseConnection().verifyEmailOfUser(UserController.getUserSignedIn().getUsername(), true);
            warningText.setText("Email address verified successfully! Please wait...!");

            new SceneSwitcher().switchSceneTo("profile.fxml");

        } else warningText.setText("Incorrect verification code!");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        emailAddressField.setText(UserController.getUserSignedIn().getEmail());
    }
}