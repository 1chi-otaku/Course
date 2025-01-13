package itstep.learning.services.file;

import java.security.SecureRandom;

public class RandomFileNameService {
    private static final String ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int DEFAULT_ENTROPY = 64;  // 64-битовая энтропия (~11 символов)

    private final SecureRandom random = new SecureRandom();

    public String generate(int length) {
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(ALLOWED_CHARS.length());
            result.append(ALLOWED_CHARS.charAt(index));
        }
        return result.toString();
    }

    public String generateDefault() {
        int defaultLength = (int) Math.ceil(DEFAULT_ENTROPY / (Math.log(ALLOWED_CHARS.length()) / Math.log(2)));
        return generate(defaultLength);
    }
}
