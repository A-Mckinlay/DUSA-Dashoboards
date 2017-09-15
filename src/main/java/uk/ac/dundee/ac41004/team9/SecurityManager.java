package uk.ac.dundee.ac41004.team9;

import javax.annotation.Nullable;

import io.drakon.spark.syn.Syn;
import lombok.experimental.UtilityClass;
import spark.Redirect;

@UtilityClass
public class SecurityManager {

    private static Syn SYN_INST = null;

    public static void init() {
        getSyn().route();
    }

    public static Syn getSyn() {
        if (SYN_INST == null) {
            SYN_INST = new Syn("/login", new MemeticAuthProvider(), false,
                    (req, res) -> Render.mustache("login"),
                    (req, res) -> { res.redirect("/", Redirect.Status.FOUND.intValue()); return null; });
            SYN_INST.createUser("test", "test");
        }
        return SYN_INST;
    }

    private static class MemeticAuthProvider implements Syn.AuthProvider {

        String username = null;
        String hashSaltField = null;

        @Override
        public String getUser(String s) {
            if (s.equals(username)) return hashSaltField;
            return null;
        }

        @Override
        public void writeUser(String user, String hashSaltField, @Nullable Object metadata) {
            this.username = user;
            this.hashSaltField = hashSaltField;
        }

    }

}
