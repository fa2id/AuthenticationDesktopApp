package authentication.user;

import authentication.controllers.RegistrationController;
import authentication.database.DatabaseConnection;

/**
 * Created by Farid on 2017-08-03.
 */
public class User {

    private final String username;
    private final String password;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String birthDate;
    private final Character sex;

    public User(String username, String password, String firstName, String lastName, String email, String birthDate, Character sex) {
        this.username = username.toLowerCase();
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthDate = birthDate;
        this.sex = sex;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public Character getSex() {
        return sex;
    }

    public void addUserToDB(RegistrationController registrationController) throws ClassNotFoundException {

        DatabaseConnection databaseConnection = new DatabaseConnection();
        databaseConnection.addUserInfoToDB(this, registrationController);
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                '}';
    }
}
