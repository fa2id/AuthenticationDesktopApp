package authentication.security;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public class Security {

    private String randomKey = null;

    private String generateRandomKey() {

        String stringRange = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        int length = stringRange.length();
        Random random = new Random();
        String result;
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < length; i++) {

            stringBuilder.append(stringRange.charAt(random.nextInt(length)));

        }
        result = stringBuilder.toString();
        randomKey = result;
        return result;
    }

    public String generateVerificationKey() {
        SecureRandom secureRandom = new SecureRandom();
        int num = secureRandom.nextInt(100000000) + 1;
        return String.valueOf(num);
    }

    private String hashedValueWithHighSecurity(String password, boolean isRegistration) {
        String value;
        if (isRegistration)
            value = generateRandomKey() + password;
        else value = password;
        byte[] valueBytes = value.getBytes();
        String hashValue = null;

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
            messageDigest.update(valueBytes);
            byte[] digestedMSG = messageDigest.digest();
            hashValue = DatatypeConverter.printHexBinary(digestedMSG).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return hashValue;
    }

    public String getFinalHashedPassword(String password, boolean isRegistration) {
        String hashedValue = hashedValueWithHighSecurity(password, isRegistration);
        if (isRegistration)
            return randomKey + ":" + hashedValue;
        return hashedValue;
    }
}