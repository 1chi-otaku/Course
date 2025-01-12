package itstep.learning.services.kdf;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.services.hash.HashService;

/*
   KDF implementation by sec. t.1 RFC  2898
    <a href=https://datatracker.ietf.org/doc/html/rfc2898>RFC 2898 Password-Based
 */
@Singleton
public class PbKdf1Service implements KdfService {
    private  int dkLen =20;
    private final HashService hashService;

    @Inject
    public PbKdf1Service(HashService hashService) {
        this.hashService = hashService;
    }

    @Override
    public String dk(String password, String salt) {
        //доп параметр iterationCont будем добавлять в соль
        // как суффикс соли после символ '.'
        // salt = 202c962c5907b.3
        int iterationCount=0;
        int dotPos = password.lastIndexOf('.');
        if (dotPos > 0) {
            try{
                iterationCount = Integer.parseInt(salt.substring( dotPos+1));
            }
            catch (NumberFormatException ignored){}
        }
        if(iterationCount <1 || iterationCount  >10){
            iterationCount=3;
        }
        String t =hashService.hash(password +salt);
        for (int i = 1; i < iterationCount; i++) {
            t= hashService.hash(t);
        }
        while (t.length() <dkLen){
            //Если hash даешь меньше символов чем требуется - дублируем его
            t+=t;
        }
        return t.substring(0,dkLen);
    }
}
