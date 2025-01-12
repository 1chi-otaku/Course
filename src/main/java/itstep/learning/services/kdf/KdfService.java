package itstep.learning.services.kdf;

/*
    Key derivation function service
    by RFC 2898 Password-Based Cryptography
    <a href=https://datatracker.ietf.org/doc/html/rfc2898>RFC 2898 Password-Based
 */
public interface KdfService {
    String dk(String password, String salt);
}
