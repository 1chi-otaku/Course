package itstep.learning.services.hash;

import com.google.inject.Singleton;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Singleton
public class Md5HasService implements HashService {


    @Override
    public String hash(String text) {
        try{
            MessageDigest md = MessageDigest.getInstance(("MD5"));
            md.update(text.getBytes());
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(
                        Integer.toString((b & 0xff) + 0x100, 16).substring(1)
                );
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            System.err.println(ex.getMessage());
            return null;
        }
    }
}
