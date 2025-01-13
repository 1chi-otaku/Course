package itstep.learning.services.filename;

import com.google.inject.Singleton;

import javax.servlet.*;
import java.io.IOException;
import java.security.SecureRandom;

@Singleton
public class FileNameService implements RandomLength {
    private static final String ALLOWED_CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int DEFAULT_LENGTH = 8; // приблизно 64 біти ентропії
    private SecureRandom random = new SecureRandom();



    @Override
    public String generateRandomFileName() {
        return generateRandomFileName(DEFAULT_LENGTH);
    }

    @Override
    public String generateRandomFileName(int length) {
        if (length <= 0) {
            length = DEFAULT_LENGTH;
        }

        StringBuilder fileName = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(ALLOWED_CHARACTERS.length());
            fileName.append(ALLOWED_CHARACTERS.charAt(index));
        }

        return fileName.toString();
    }

}
