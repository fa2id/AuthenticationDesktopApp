package authentication.validation;

import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validation {

    public boolean isValid(String type, TextField mainTextField, Text warningText) {

        Pattern pattern;

        switch (type) {
            case "email":
                pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
                break;
            case "username":
                pattern = Pattern.compile("^[a-zA-Z0-9._-]{3,}$");
                break;
            case "password":
                pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{8,}$");
                break;
            case "firstName":
                pattern = Pattern.compile("[a-zA-Z]{2,}");
                break;
            case "lastName":
                pattern = Pattern.compile("[a-zA-Z]{2,}");
                break;
            default:
                pattern = Pattern.compile("");
        }

        Matcher matcher = pattern.matcher(mainTextField.getText());

        if (matcher.find() && matcher.group().equals(mainTextField.getText())) {
            return true;
        } else {
            warningText.setText("Enter a valid " + type + "!");
        }
        return false;
    }

    public boolean isValidAge(DatePicker datePicker, Text warningText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        if (year - 4 < datePicker.getValue().getYear())
            warningText.setText("Not a valid age!");
        return year - 4 > datePicker.getValue().getYear();
    }
}
