package other;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;

@Stateless
public class EncryptValue {

    public String encode(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(value.getBytes());
            byte[] digest = md.digest();
            
            StringBuilder buff = new StringBuilder();
            
            for (byte b : digest) {
                String hexStr = Integer.toHexString(b & 0xff);
                if (hexStr.length() == 1) {
                    continue;
                }
                buff.append(hexStr);
            }
            return buff.toString();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(EncryptValue.class.getName()).log(Level.SEVERE, null, ex);
            return  "";
        }
    }

    public String decode(String value) {
        return "";
    }
}
