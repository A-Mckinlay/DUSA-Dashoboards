package uk.ac.dundee.ac41004.team9;

import javax.annotation.Nullable;

import io.drakon.spark.syn.Syn;
import lombok.experimental.UtilityClass;
import spark.Redirect;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class SecurityManager {

    private static Syn SYN_INST = null;

    public static void init() {
        Syn syn = getSyn();
        syn.route();
    }

    public static Syn getSyn() {
        if (SYN_INST == null) {
            SYN_INST = new Syn("/login", new MemeticAuthProvider(), false,
                    (req, res) -> {
                        Map<String, Object> model = new HashMap<>();
                        if (req.attribute("syn-error") != null) {
                            String s;
                            switch ((Syn.ErrorState)req.attribute("syn-error")) {
                                case NO_SUCH_USER:
                                    s = "No such user.";
                                    break;
                                case MISSING_FIELD:
                                    s = "A required field was empty.";
                                    break;
                                case INVALID_CREDENTIALS:
                                    s = "Check your password and try again.";
                                    break;
                                default:
                                    s = "An unknown error occured.";
                            }
                            model.put("error", s);
                        }
                        return Render.mustache("login", model);
                    },
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
