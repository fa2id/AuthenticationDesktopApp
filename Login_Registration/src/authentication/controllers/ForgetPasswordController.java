package authentication.controllers;

import authentication.Main;
import authentication.email.EmailService;
import authentication.scene.SceneSwitcher;
import authentication.security.Security;
import authentication.user.User;
import authentication.user.UserController;
import authentication.validation.Validation;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;

/**
 * Created by Farid on 2017-08-03.
 */
public class ForgetPasswordController {

    @FXML
    private JFXTextField email;
    @FXML
    private JFXTextField verificationCode;
    @FXML
    private Text warningText;
    @FXML
    private Text resetPasswordWarningText;
    @FXML
    private JFXButton submitButton;
    @FXML
    private JFXPasswordField passwordField;
    @FXML
    private JFXPasswordField rePasswordField;
    @FXML
    private VBox vBoxForPassword;

    private String username;
    private String tempKey;

    private boolean isVerificationStep = false;
    private boolean isPasswordResetStep = false;

    @FXML
    private void switchToLoginScene() throws IOException {

        new SceneSwitcher().switchSceneTo("login.fxml");
    }

    @FXML
    private void sendPasswordToUserEmail() {

        Runnable runnable = () -> {

            submitButton.setDisable(true);

            if (!isPasswordResetStep) {

                if (!isVerificationStep) {
                    Validation validation = new Validation();
                    Security security = new Security();

                    if (!email.getText().trim().isEmpty()) {

                        if (validation.isValid("email", email, warningText)
                                && isEmailExisted()) {

                            warningText.setText("Sending an email... Please wait!");
                            email.setDisable(true);

                            EmailService emailService = new EmailService();

                            tempKey = security.generateVerificationKey();

                            try {
                                emailService.sendMail(email.getText(),
                                        "Verification code",
                                        "We received a request that you have forgot your password." +
                                                "\nEnter this verification code to reset your password." +
                                                "\n\nYour username is: " + username +
                                                "\nYour verification code is: " + tempKey,
                                        warningText,
                                        "We sent your verification code to your email.",
                                        "There is an error to send the email!");

                                isVerificationStep = true;
                                Platform.runLater(() -> {
                                    verificationCode.setDisable(false);
                                    submitButton.setText("Submit Verification Code");
                                });


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    } else warningText.setText("Email field is empty!");

                } else {
                    if (tempKey.equals(verificationCode.getText())) {
                        Platform.runLater(() -> {
                            warningText.setText("VERIFIED");
                            verificationCode.setDisable(true);
                            verificationCode.getParent().setVisible(false);
                            verificationCode.getParent().setDisable(true);
                            vBoxForPassword.setVisible(true);
                            submitButton.setText("Reset password");
                            isVerificationStep = false;
                            isPasswordResetStep = true;
                        });

                    } else warningText.setText("Wrong verification code!");
                }

            } else {
                resetPassword();
            }
            submitButton.setDisable(false);

        };
        new Thread(runnable).start();

    }

    private void resetPassword() {
        Main.getDatabaseConnection().getUserInfoFromDB(username, false);
        User user = UserController.getTempUser();
        UserController.setTempUser(null);

        if (isCheckedPassword()) {
            Security security = new Security();
            Main.getDatabaseConnection().updateUserPasswordInDB(user, security.getFinalHashedPassword(passwordField.getText(), true));
            resetPasswordWarningText.setText("Password reset successfully.");
            resetPasswordWarningText.getParent().setDisable(true);
            submitButton.setVisible(false);
        }
    }

    private boolean isCheckedPassword() {
        Validation validation = new Validation();
        if (!passwordField.getText().trim().isEmpty()
                && !rePasswordField.getText().trim().isEmpty()) {
            if (validation.isValid("password", passwordField, resetPasswordWarningText)) {
                if (passwordField.getText().equals(rePasswordField.getText()))
                    return true;
                else resetPasswordWarningText.setText("Passwords do not match! Try again.");
            }

        } else
            resetPasswordWarningText.setText("Empty fields!");
        return false;
    }

    private boolean isEmailExisted() {

        String info = Main.getDatabaseConnection().getInfoForThisEmail(email.getText());

        if (info != null) {
            int spaceIndex = info.indexOf(" ");

            username = info.substring(0, spaceIndex);

            return true;
        }
        return false;
    }

}
