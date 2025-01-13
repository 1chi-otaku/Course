package itstep.learning.kdf;

public interface KdfService {
    String dk(String password, String salt);
}
