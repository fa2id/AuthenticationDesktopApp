package authentication.controllers;

import authentication.scene.SceneSwitcher;
import authentication.user.User;
import authentication.validation.Validation;
import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Farid on 2017-08-03.
 */
public class RegistrationController implements Initializable {

    private boolean isDuplicateUserError = false;
    @FXML
    private JFXTextField username;
    @FXML
    private JFXTextField email;
    @FXML
    private JFXTextField firstName;
    @FXML
    private JFXTextField lastName;
    @FXML
    private JFXPasswordField password;
    @FXML
    private JFXPasswordField rePassword;
    @FXML
    private JFXDatePicker datePicker;
    @FXML
    private JFXComboBox<Character> sex;
    @FXML
    private Text warningText;
    @FXML
    private Text passwordWarningText;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private JFXButton submitButton;
    @FXML
    private JFXButton loginButton;
    @FXML
    private GridPane gridPane;
    @FXML
    private JFXSpinner spinner;

    private boolean doesPasswordMatch() {

        return password.getText().equals(rePassword.getText());
    }

    public void setDuplicateUserError(boolean duplicateUserError) {
        this.isDuplicateUserError = duplicateUserError;
    }

    @FXML
    private void switchToLoginScene() throws IOException {

        new SceneSwitcher().switchSceneTo("login.fxml");
    }

    @FXML
    private void registerUser() {

        warningText.setText("Please wait ...");
        setDuplicateUserError(false);
        spinner.setVisible(true);
        submitButton.setDisable(true);
        loginButton.setDisable(true);

        Runnable runnable = () -> {

            Validation validation = new Validation();

            printPasswordMatchText();


            if (!areEmptyFields()
                    && validation.isValid("email", email, warningText)
                    && validation.isValid("password", password, warningText)
                    && validation.isValidAge(datePicker, warningText)
                    && validation.isValid("username", username, warningText)
                    && validation.isValid("firstName", firstName, warningText)
                    && validation.isValid("lastName", firstName, warningText)
                    && doesPasswordMatch()) {

                try {

                    User newUser = new User(username.getText(),
                            password.getText(),
                            firstName.getText().substring(0, 1).toUpperCase() + firstName.getText().substring(1).toLowerCase(),
                            lastName.getText().substring(0, 1).toUpperCase() + lastName.getText().substring(1).toLowerCase(),
                            email.getText(),
                            datePicker.getValue().toString(),
                            sex.getValue().toString().charAt(0));

                    newUser.addUserToDB(this);
                    Platform.runLater(this::printResult);

                } catch (Exception e) {
                    warningText.setText("Registration failed!");
                    spinner.setVisible(false);
                    submitButton.setDisable(false);
                    loginButton.setDisable(false);
                    e.printStackTrace();
                }
            } else if (!doesPasswordMatch()) {
                warningText.setText("Passwords do not match!");
                spinner.setVisible(false);
                submitButton.setDisable(false);
                loginButton.setDisable(false);
            } else {
                spinner.setVisible(false);
                submitButton.setDisable(false);
                loginButton.setDisable(false);
            }

        };
        Thread thread = new Thread(runnable);
        thread.start();

    }

    private void emptyAllFields() {
        username.setText("");
        password.setText("");
        rePassword.setText("");
        email.setText("");
        firstName.setText("");
        lastName.setText("");
        sex.setValue(null);
        datePicker.setValue(null);
    }

    @FXML
    private void printPasswordMatchText() {

        if (!password.getText().trim().isEmpty() && !rePassword.getText().trim().isEmpty()) {
            if (doesPasswordMatch()) {
                passwordWarningText.setFill(Color.GREEN);
                passwordWarningText.setText("Passwords match!");
            } else {
                passwordWarningText.setFill(Color.RED);
                passwordWarningText.setText("Passwords do not match!");
            }
        } else {
            passwordWarningText.setFill(Color.RED);
            passwordWarningText.setText("Passwords must match!");
        }
    }

    private boolean areEmptyFields() {

        if (username.getText().trim().isEmpty() ||
                password.getText().trim().isEmpty() ||
                rePassword.getText().trim().isEmpty() ||
                email.getText().trim().isEmpty() ||
                firstName.getText().trim().isEmpty() ||
                lastName.getText().trim().isEmpty() ||
                sex.getSelectionModel().isEmpty() ||
                datePicker.getValue() == null) {
            warningText.setText("Empty fields!");
            return true;
        }
        return false;

    }

    private void printResult() {

        if (!doesPasswordMatch()) {
            warningText.setText("Password does not match!");
            spinner.setVisible(false);
            submitButton.setDisable(false);
            loginButton.setDisable(false);
        } else if (isDuplicateUserError) {
            warningText.setText("Username or Email address exist!");
            spinner.setVisible(false);
            submitButton.setDisable(false);
            loginButton.setDisable(false);
        } else {
            warningText.setText(" ");
            anchorPane.setVisible(true);
            gridPane.setDisable(true);
            submitButton.setVisible(false);
            warningText.setVisible(false);
            emptyAllFields();
            spinner.setVisible(false);
            loginButton.setDisable(false);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sex.getItems().addAll('M', 'F');

    }
}