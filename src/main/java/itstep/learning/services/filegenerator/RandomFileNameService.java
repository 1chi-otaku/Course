package itstep.learning.services.filegenerator;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.security.SecureRandom;

@Singleton
public class RandomFileNameService implements FileGeneratorService{

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int DEFAULT_LENGTH = 32;
    private final SecureRandom random;

    @Inject
    public RandomFileNameService() {
        this.random = new SecureRandom();
    }

    @Override
    public String generateFileName() {
        return generateFileName(DEFAULT_LENGTH);
    }

    @Override
    public String generateFileName(int length) {
        StringBuilder filename = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            filename.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return filename.toString();
    }
}
