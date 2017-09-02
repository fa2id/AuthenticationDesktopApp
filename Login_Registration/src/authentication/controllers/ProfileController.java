package authentication.controllers;

import authentication.Main;
import authentication.scene.SceneSwitcher;
import authentication.security.Security;
import authentication.user.User;
import authentication.user.UserController;
import authentication.validation.Validation;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Created by Farid on 2017-08-03.
 */
public class ProfileController implements Initializable {

    private final ArrayList<Pane> panes = new ArrayList<>();
    @FXML
    private Text firstNameToShowInProfile;
    @FXML
    private Text pageStatus;
    @FXML
    private Text newsTitle;
    @FXML
    private Text newsText;
    @FXML
    private Text warningText_home;
    @FXML
    private Text warningText_changeInfo;
    @FXML
    private Text warningText_changePass;
    @FXML
    private Text passwordMatchText;
    @FXML
    private Text username;
    @FXML
    private JFXTextField firstName;
    @FXML
    private JFXTextField lastName;
    @FXML
    private JFXTextField email;
    @FXML
    private JFXComboBox<Character> sex;
    @FXML
    private JFXDatePicker datePicker;
    @FXML
    private JFXPasswordField currentPasswordField;
    @FXML
    private JFXPasswordField newPasswordField;
    @FXML
    private JFXPasswordField confirmPasswordField;
    @FXML
    private TextArea note;
    @FXML
    private Pane homePane;
    @FXML
    private Pane changeInfoPane;
    @FXML
    private Pane changePassPane;
    private String newsTitleString;
    private String newsTextString;
    private User user;

    public User getUser() {
        return user;
    }

    public void setNewsComponents(String newsTitleString, String newsTextString) {

        this.newsTitleString = newsTitleString;
        this.newsTextString = newsTextString;
    }

    @FXML
    private void switchToLoginScene() throws IOException {

        UserController.signOut();
        new SceneSwitcher().switchSceneTo("login.fxml");

    }

    @FXML
    private void saveNote() {
        warningText_home.setText(" ");
        warningText_home.getParent().setDisable(true);
        Runnable runnable = () -> {
            Main.getDatabaseConnection().addNoteToDB(user, note.getText());
            Platform.runLater(() -> warningText_home.setText("Note saved."));
        };
        new Thread(runnable).start();
        warningText_home.getParent().setDisable(false);
    }

    @FXML
    private boolean doesNewPasswordsMatch() {

        if (!newPasswordField.getText().trim().isEmpty()
                && !confirmPasswordField.getText().trim().isEmpty()) {
            if (newPasswordField.getText().equals(confirmPasswordField.getText())) {
                passwordMatchText.setFill(Color.GREEN);
                passwordMatchText.setText("New passwords match!");
                return true;
            } else {
                passwordMatchText.setFill(Color.RED);
                passwordMatchText.setText("New passwords do not match!");
            }
        } else {
            passwordMatchText.setFill(Color.RED);
            passwordMatchText.setText("New passwords must match!");
        }

        return false;
    }

    @FXML
    private void changeInfoOfUser() throws IOException {

        warningText_changeInfo.setText(" ");
        warningText_changeInfo.getParent().setDisable(true);
        if (areFieldsValid()) {
            Runnable runnable = () -> {
                boolean switchSceneToEmailVerification = false;
                if (!email.getText().equals(UserController.getUserSignedIn().getEmail())) {
                    if (!isDuplicateEmail(UserController.getUserSignedIn(), email.getText())) {

                        Main.getDatabaseConnection().verifyEmailOfUser(UserController.getUserSignedIn().getUsername(), false);
                        Main.getDatabaseConnection().sendVerificationDetailsToUser(email.getText(), false);

                        switchSceneToEmailVerification = true;
                    }
                }
                boolean isDuplicated = isDuplicateEmail(UserController.getUserSignedIn(), email.getText());

                Main.getDatabaseConnection().changeInfoOfUserInDB(username.getText(),
                        firstName.getText().substring(0, 1).toUpperCase() + firstName.getText().substring(1).toLowerCase(),
                        lastName.getText().substring(0, 1).toUpperCase() + lastName.getText().substring(1).toLowerCase(),
                        email.getText().equals(UserController.getUserSignedIn().getEmail())
                                ?
                                UserController.getUserSignedIn().getEmail() :
                                (isDuplicateEmail(UserController.getUserSignedIn(), email.getText())
                                        ?
                                        UserController.getUserSignedIn().getEmail()
                                        :
                                        email.getText())
                        ,
                        sex.getValue().toString(),
                        datePicker.getValue().toString());

                Platform.runLater(() -> warningText_changeInfo.setText(isDuplicated ? "Information changed successfully except email address which is duplicated! " +
                        "try other email address." : "Information changed successfully."));


                if (switchSceneToEmailVerification) {

                    Main.getDatabaseConnection().getUserInfoFromDB(user.getUsername(), true);

                    Platform.runLater(() -> {
                        try {
                            new SceneSwitcher().switchSceneTo("resources/emailVerification.fxml");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            };
            new Thread(runnable).start();

        }
        warningText_changeInfo.getParent().setDisable(false);
    }

    private boolean areFieldsValid() {

        Validation validation = new Validation();
        return validation.isValid("firstName", firstName, warningText_changeInfo)
                && validation.isValid("lastName", lastName, warningText_changeInfo)
                && validation.isValid("email", email, warningText_changeInfo)
                && validation.isValidAge(datePicker, warningText_changeInfo);
    }

    @FXML
    private void switchToHomePane() {

        warningText_home.setText("");
        switchPageTo(homePane);
        getNote();
        getNews();
        pageStatus.setText("Home");
    }

    @FXML
    private void switchToChangeInfoPane() {

        warningText_changeInfo.setText("");
        initFields();
        switchPageTo(changeInfoPane);
        pageStatus.setText("Change info");
    }

    @FXML
    private void switchToChangePassPane() {

        warningText_changePass.setText("");
        switchPageTo(changePassPane);
        pageStatus.setText("Change password");
    }

    @FXML
    private void resetPassword() {

        warningText_changePass.setText(" ");
        warningText_changePass.getParent().setDisable(true);

        if (doesCurrentPasswordMatch()
                && doesNewPasswordsMatch()
                && isNewPasswordValid()) {
            Runnable runnable = () -> {
                Security security = new Security();
                Main.getDatabaseConnection().updateUserPasswordInDB(user, security.getFinalHashedPassword(newPasswordField.getText(), true));

                Platform.runLater(() -> warningText_changePass.setText("Password has been changed successfully."));
            };
            new Thread(runnable).start();


        } else {
            printChangingPasswordNotSuccessfulResult();
        }

        warningText_changePass.getParent().setDisable(false);
        currentPasswordField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");

    }

    private boolean isDuplicateEmail(User userToCheck, String emailAddress) {

        if (Main.getDatabaseConnection().isDuplicateEmail(userToCheck, emailAddress)) {
            warningText_changeInfo.setText("Duplicate email address! try another one.");
            return true;
        }
        return false;
    }

    private void initFields() {

        Runnable runnable = () -> {

            Main.getDatabaseConnection().getUserInfoFromDB(user.getUsername(), false);

            Platform.runLater(() -> {

                user = UserController.getTempUser();
                UserController.setTempUser(null);
                username.setText(user.getUsername());
                firstName.setText(user.getFirstName());
                lastName.setText(user.getLastName());
                email.setText(user.getEmail());
                sex.setValue(user.getSex());
                LocalDate localDate = LocalDate.parse(user.getBirthDate());
                datePicker.setValue(localDate);
            });
        };
        new Thread(runnable).start();
    }

    private void switchPageTo(Pane pane) {

        ArrayList<Pane> tempPanes = new ArrayList<>();
        tempPanes.addAll(panes);
        tempPanes.remove(pane);
        for (Pane paneToSetVisibility : tempPanes) {
            paneToSetVisibility.setVisible(false);
        }
        pane.setVisible(true);
    }

    private void printChangingPasswordNotSuccessfulResult() {

        if (isEmptyPasswordFields())
            warningText_changePass.setText("Empty field error!");

        else if (!doesCurrentPasswordMatch())
            warningText_changePass.setText("Incorrect current password!");

        else if (!doesNewPasswordsMatch())
            warningText_changePass.setText("New passwords do not match!");

    }

    private boolean isEmptyPasswordFields() {

        return currentPasswordField.getText().trim().isEmpty()
                || newPasswordField.getText().trim().isEmpty()
                || confirmPasswordField.getText().trim().isEmpty();
    }

    private void getNote() {
        note.setText(Main.getDatabaseConnection().getNoteFromDB(this));
    }

    private boolean isNewPasswordValid() {
        return new Validation().isValid("password", newPasswordField, warningText_changePass);
    }

    private boolean doesCurrentPasswordMatch() {

        String tempPSD = user.getPassword().substring(0, user.getPassword().indexOf(":"));

        String passwordToCheck = new Security().getFinalHashedPassword(
                tempPSD + currentPasswordField.getText(), false);

        return passwordToCheck.equals(
                user.getPassword().substring(
                        user.getPassword().indexOf(":") + 1));
    }

    private void getNews() {

        try {
            Main.getDatabaseConnection().getNewsFromDB(this);
        } catch (SQLException e) {
            System.out.println("Could not get news from DB!");
        }

        newsTitle.setText(newsTitleString);
        newsText.setText(newsTextString);

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        sex.getItems().addAll('M', 'F');
        panes.add(homePane);
        panes.add(changeInfoPane);
        panes.add(changePassPane);
        user = UserController.getUserSignedIn();
        firstNameToShowInProfile.setText(user.getFirstName());
        getNote();
        getNews();
    }

}
