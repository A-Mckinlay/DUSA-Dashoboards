package uk.ac.dundee.ac41004.team9.security;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class SecUtils {

    private static final String SEPERATOR = ":/:";

    private SecUtils() {} // Static

    public static boolean checkPassword(String userPasswd, String dbPasswd) {
        SaltAndPasswd server = new SaltAndPasswd(dbPasswd);
        //SaltAndPasswd client = new SaltAndPasswd()
        return false; // TODO
    }

    /**
     * Hashes a password in accordance with OWASP recommendations. Code/docs from
     * https://www.owasp.org/index.php/Hashing_Java
     *
     * @param password Plaintext password.
     * @param salt Salt bytes.
     * @param iterations Iterations of PBKDF2 (30,000 is a safe lower bound).
     * @param keyLength Key length (> 256 recommended)
     * @return Hashed bytes.
     */
    private static byte[] hashPassword(final char[] password, final byte[] salt, final int iterations,
                                       final int keyLength) {
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKey key = skf.generateSecret(spec);
            return key.getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    private static class SaltAndPasswd {
        public final String salt;
        public final String passwd;

        public SaltAndPasswd(String dbPasswd) {
            String[] parts = dbPasswd.split(SEPERATOR);
            if (parts.length != 2) throw new IllegalArgumentException("Invalid database format.");
            salt = parts[0];
            passwd = parts[1];
        }

        public SaltAndPasswd(String hashedPasswd, String salt) {
            this.passwd = hashedPasswd;
            this.salt = salt;
        }

        public String toDBFormat() {
            return salt + SEPERATOR + passwd;
        }
    }

}
