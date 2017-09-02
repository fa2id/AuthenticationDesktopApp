package authentication.database;

import authentication.Init;
import authentication.controllers.ProfileController;
import authentication.controllers.RegistrationController;
import authentication.email.EmailService;
import authentication.security.Security;
import authentication.user.User;
import authentication.user.UserController;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import java.sql.*;

/**
 * Created by Farid on 2017-08-03.
 */
public class DatabaseConnection {

    private static Connection connection;
    private static Statement statement;

    public DatabaseConnection() throws ClassNotFoundException {

        if (connection == null) {
            try {
                Class.forName("com.mysql.jdbc.Driver");

                Init init = new Init();
                connection = DriverManager.getConnection(init.getHost(), init.getDbUser(), init.getDbPass());
                statement = connection.createStatement();

            } catch (SQLException e) {
                System.out.println(" ==!! Connection to SQL error.");
            }
        }
    }


    public void getUserInfoFromDB(String userName, boolean isForSigningIn) {
        try {

            ResultSet resultSet = statement.executeQuery("SELECT * FROM faridsen_javaApp.user WHERE username= '" + userName + "'");

            while (resultSet.next()) {
                String username = resultSet.getString(1);
                String password = resultSet.getString(2);
                String firstName = resultSet.getString(3);
                String lastName = resultSet.getString(4);
                Character sex = resultSet.getString(5).charAt(0);
                String age = resultSet.getString(6);
                String email = resultSet.getString(7);

                User user = new User(username, password, firstName, lastName, email, age, sex);
                if (isForSigningIn)
                    UserController.signInUser(user);
                else
                    UserController.setTempUser(user);
            }
        } catch (SQLException e) {
            System.out.println("==!! Could not execute the query.");
        }
    }

    public void addUserInfoToDB(User userToAdd, RegistrationController registrationController) {

        try {
            Security security = new Security();

            String query = "INSERT INTO faridsen_javaApp.user(username, password, firstname, lastname, sex, birthdate, email) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, userToAdd.getUsername());
            preparedStatement.setString(2, security.getFinalHashedPassword(userToAdd.getPassword(), true));
            preparedStatement.setString(3, userToAdd.getFirstName());
            preparedStatement.setString(4, userToAdd.getLastName());
            preparedStatement.setString(5, userToAdd.getSex().toString());
            preparedStatement.setString(6, userToAdd.getBirthDate());
            preparedStatement.setString(7, userToAdd.getEmail());
            preparedStatement.execute();

            sendVerificationDetailsToUser(userToAdd.getEmail(), false);

        } catch (MySQLIntegrityConstraintViolationException e) {
            System.out.println("Duplicate user!");
            registrationController.setDuplicateUserError(true);

        } catch (SQLException e) {
            System.out.println("==!! Could not execute the query.");
        }
    }

    public void sendVerificationDetailsToUser(String userEmail, boolean isResendCode) {

        Security security = new Security();
        String verificationKey = security.generateVerificationKey();

        addVerificationCodeToDB(userEmail, verificationKey, isResendCode);

        EmailService emailService = new EmailService();
        emailService.sendMail(userEmail,
                "Verify your email address!",
                "Hi,\n\nPlease enter this verification code to verify your email address:\n" + verificationKey,
                null,
                " ", " ");
    }

    private void addVerificationCodeToDB(String userEmail, String verificationKey, boolean isResendCode) {

        Security security = new Security();

        String query = isResendCode ?
                "UPDATE faridsen_javaApp.verification " +
                        "SET email=(?),code=(?) WHERE email='" + userEmail + "'"
                :
                "INSERT INTO faridsen_javaApp.verification(email, code) " +
                        "VALUES (?, ?)";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, userEmail);
            preparedStatement.setString(2, security.getFinalHashedPassword(verificationKey, true));
            preparedStatement.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public String getInfoForThisEmail(String email) {

        String info = null;

        try {
            String user;
            String password;

            ResultSet resultSet = statement.executeQuery("SELECT username,password FROM faridsen_javaApp.user WHERE email ='" + email + "'");
            if (resultSet.next()) {
                user = resultSet.getString(1);
                password = resultSet.getString(2);
                info = user + " " + password;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("==! Could not execute query for checking email existence !==");
        }

        return info;
    }

    public void addNoteToDB(User user, String note) {

        try {
            ResultSet resultSet = statement.executeQuery("SELECT username FROM faridsen_javaApp.note WHERE username ='" + user.getUsername() + "'");
            if (resultSet.next()) {

                updateQuery("faridsen_javaApp.note", "username", user.getUsername(), "note", note);

            } else {

                String query = "INSERT INTO faridsen_javaApp.note(username, note) " +
                        "VALUES (?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, user.getUsername());
                preparedStatement.setString(2, note);

                preparedStatement.execute();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("==!! Could not execute the query.");
        }

    }

    public void getNewsFromDB(ProfileController profileController) throws SQLException {

        String newsTitle;
        String newsText;

        ResultSet resultSet = statement.executeQuery("SELECT * FROM faridsen_javaApp.news");

        if (resultSet.last()) {

            newsTitle = resultSet.getString(1);
            newsText = resultSet.getString(2);

            profileController.setNewsComponents(newsTitle, newsText);
        }
    }

    public String getNoteFromDB(ProfileController profileController) {

        String note = null;

        User user = profileController.getUser();

        try {

            ResultSet resultSet = statement.executeQuery(
                    "SELECT * " +
                            "FROM faridsen_javaApp.note " +
                            "WHERE username= '" + user.getUsername() + "'");

            if (resultSet.next()) {
                note = resultSet.getString(2);
            }
        } catch (SQLException e) {
            System.out.println("==!! Could not execute the query.");
        }

        return note;
    }

    public void updateUserPasswordInDB(User user, String password) {

        updateQuery("faridsen_javaApp.user", "username", user.getUsername(), "password", password);
    }

    public void changeInfoOfUserInDB(String username, String firstName, String lastName, String email, String sex, String birthDate) {

        try {

            String query = "UPDATE faridsen_javaApp.user " +
                    "SET firstname=(?),lastname=(?),email=(?),sex=(?),birthdate=(?) WHERE username='" + username + "'";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, email);
            preparedStatement.setString(4, sex);
            preparedStatement.setString(5, birthDate);
            preparedStatement.execute();

        } catch (SQLException e) {
            System.out.println("==!! Could not execute the query!!==");
        }
    }

    public boolean isUserEmailVerified(String userName) {

        String result = null;

        try {

            ResultSet resultSet = statement.executeQuery(
                    "SELECT * " +
                            "FROM faridsen_javaApp.user " +
                            "WHERE username= '" + userName + "'");

            if (resultSet.next()) {
                result = resultSet.getString(8);
            }
        } catch (SQLException e) {
            System.out.println("==!! Could not execute the query.");
            e.printStackTrace();
        }

        assert result != null;
        return result.equals("1");
    }

    public String getVerificationCode(String email) {

        String code = null;

        try {

            ResultSet resultSet = statement.executeQuery(
                    "SELECT * " +
                            "FROM faridsen_javaApp.verification " +
                            "WHERE email= '" + email + "'");

            if (resultSet.next()) {
                code = resultSet.getString(2);
            }
        } catch (SQLException e) {
            System.out.println("==!! Could not execute the query.");
            e.printStackTrace();
        }

        return code;
    }

    public void verifyEmailOfUser(String username, boolean isVerified) {

        try {

            updateQuery("faridsen_javaApp.user", "username", username, "isVerified", isVerified ? 1 : 0);
            if (isVerified)
                deleteQuery("faridsen_javaApp.verification", "email", UserController.getUserSignedIn().getEmail());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isDuplicateEmail(User userToCheck, String emailAddress) {
        try {

            ResultSet resultSet = statement.executeQuery(
                    "SELECT * " +
                            "FROM faridsen_javaApp.user " +
                            "WHERE email= '" + emailAddress + "' AND username!= '" + userToCheck.getUsername() + "'");

            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println("==!! Could not execute the query.");
            e.printStackTrace();
        }
        return false;
    }

    public void updateQuery(String table, String whereField, String whereValue, String field, String toValue) {

        try {

            String query = "UPDATE " + table +
                    " SET " + field + "=(?) WHERE " + whereField + "= '" + whereValue + "'";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, toValue);

            preparedStatement.execute();

        } catch (SQLException e) {
            System.out.println("==!! Could not execute the query!!==");
        }
    }

    private void updateQuery(String table, String whereField, String whereValue, String field, int toValue) {

        try {

            String query = "UPDATE " + table +
                    " SET " + field + "=(?) WHERE " + whereField + "= '" + whereValue + "'";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setInt(1, toValue);

            preparedStatement.execute();

        } catch (SQLException e) {
            System.out.println("==!! Could not execute the query!!==");
        }
    }

    public void deleteQuery(String table, String whereField, String whereValue) {

        try {

            String query = "DELETE FROM " + table +
                    " WHERE " + whereField + "= '" + whereValue + "'";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.execute();

        } catch (SQLException e) {
            System.out.println("==!! Could not execute the query!!==");
        }
    }

}