package itstep.learning.services.kdf;
/*
Key derivation service.
RFC 2898 Password-Based Cryptography
*/
public interface KdfService {
    String dk(String password, String salt);
}
