package uk.ac.dundee.ac41004.team9;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import io.drakon.spark.syn.Syn;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import spark.Redirect;
import uk.ac.dundee.ac41004.team9.db.UserManager;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
@UtilityClass
@Slf4j
public class SecurityManager {

    private static Syn SYN_INST = null;

    public static void init() {
        Syn syn = getSyn();
        if (Config.isSecEnable()) syn.route();
    }

    public static Syn getSyn() {
        if (SYN_INST == null) {
            Syn.AuthProvider provider =
                    Config.isSecUseRealDatabase() ? new DatabaseAuthProvider() : new MemeticAuthProvider();

            SYN_INST = new Syn("/login", provider, false,
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

            if (!Config.isSecUseRealDatabase()) SYN_INST.createUser("test", "test");
        }
        return SYN_INST;
    }

    @ParametersAreNonnullByDefault
    private static class DatabaseAuthProvider implements Syn.AuthProvider {

        @Override
        public String getUser(String username) {
            UserManager.User user = UserManager.getUser(username);
            if (user == null) return null;
            return user.getPassword();
        }

        @Override
        public void writeUser(String user, String hashSaltField, @Nullable Object metadata) {
            if (metadata instanceof UserManager.User) {
                UserManager.writeUser(((UserManager.User) metadata).withPassword(hashSaltField));
            } else {
                throw new IllegalArgumentException("Metadata must be a User object.");
            }
        }

    }

    @ParametersAreNonnullByDefault
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
