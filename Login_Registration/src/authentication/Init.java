package authentication;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.EncryptableProperties;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

/**
 * Created by Farid on 2017-08-09.
 */

public class Init implements Serializable {

    private String prtSmtp;
    private String pssSmtp;
    private String usrSmtp;
    private String hstSmtp;
    private String host;
    private String dbUser;
    private String dbPass;

    public Init() {
        byte[] bytes = {100, 98, 80, 97, 115, 115, 70, 111, 114, 74, 97, 118, 97, 65, 112, 112, 35, 49};
        String string = new String(bytes);
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(string);
        Properties props = new EncryptableProperties(encryptor);
        try {
            props.load(new FileInputStream("init.properties"));
            host = props.getProperty("hst");
            dbUser = props.getProperty("usr");
            dbPass = props.getProperty("pss");
            hstSmtp = props.getProperty("hstSmtp");
            usrSmtp = props.getProperty("usrSmtp");
            pssSmtp = props.getProperty("pssSmtp");
            prtSmtp = props.getProperty("prtSmtp");
        } catch (IOException e) {
            System.out.println("File not found!");
        }
    }

    public String getHost() {
        return host;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPass() {
        return dbPass;
    }

    public String getPrtSmtp() {
        return prtSmtp;
    }

    public String getPssSmtp() {
        return pssSmtp;
    }

    public String getUsrSmtp() {
        return usrSmtp;
    }

    public String getHstSmtp() {
        return hstSmtp;
    }
}