package authentication.user;

public class UserController {

    private static User userSignedIn;
    private static User tempUser;

    public static void signInUser(User user) {
        UserController.userSignedIn = user;
    }

    public static void signOut() {
        UserController.userSignedIn = null;
    }

    public static User getUserSignedIn() {
        return userSignedIn;
    }

    public static User getTempUser() {
        return tempUser;
    }

    public static void setTempUser(User tempUser) {
        UserController.tempUser = tempUser;
    }

}
