package uk.ac.dundee.ac41004.team9;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/** App configuration. */
@Slf4j
@UtilityClass
public class Config {

    // Database
    @Getter private static String dbHost = "localhost";
    @Getter private static int dbPort = 5432;
    @Getter private static String dbUser = "postgres";
    @Getter private static String dbPass = "banana";
    @Getter private static String dbName = "dashoboards";

    // Security
    @Getter private static boolean secEnable = true;
    @Getter private static boolean secUseRealDatabase = false;
    @Getter private static boolean secLimitUploadSize = true;

    // Dev utils
    @Getter private static File devResourcesPath = null;
    @Getter private static boolean devRouteOverview = true;

    // Productions
    @Getter private static boolean prodUseGzip = false;

    // Internal bookkeeping (DO NOT EDIT THIS BIT)
    private static Properties props = new Properties();

    /**
     * Loads all configs from env/disk/classpath
     */
    public static void init() {
        props = new Properties();

        // Compiled into jar
        try (InputStream strm = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (strm != null) {
                log.info("Loading config properties file from classpath.");
                props.load(strm);
            }
        } catch (IOException ex) {
            log.info("Unable to load config from classpath; skipping.");
        }

        // Working dir
        Path workingDir = Paths.get(".", "config.properties");
        if (Files.exists(workingDir)) {
            try (InputStream strm = new FileInputStream(workingDir.toFile())) {
                log.info("Loading config properties file from working directory.");
                props.load(strm);
            } catch (IOException ex) {
                log.warn("Error while loading config file in working directory; skipping.", ex);
            }
        }

        // Process properties, check env vars, etc.
        // ADD NEW SETTING VARIABLES HERE!

        // Database
        log.debug("Loading database configuration.");
        dbHost = configString("dbHost", dbHost);
        dbPort = configInt("dbPort", dbPort);
        dbUser = configString("dbUser", dbUser);
        dbPass = configString("dbPass", dbPass);
        dbName = configString("dbName", dbName);

        // Security
        log.debug("Loading security configuration.");
        secEnable = configBool("secEnable", secEnable);
        secUseRealDatabase = configBool("secUseRealDatabase", secUseRealDatabase);
        secLimitUploadSize = configBool("secLimitUploadSize", secLimitUploadSize);

        // Dev utils
        log.debug("Loading dev utils configuration.");
        devResourcesPath = configDir("devResourcesPath", devResourcesPath);
        devRouteOverview = configBool("devRouteOverview", devRouteOverview);

        // Production
        log.debug("Loading production configuration.");
        prodUseGzip = configBool("prodUseGzip", prodUseGzip);

        // Fin.
        log.info("Configuration loading complete.");
    }

    private static String configString(String configName, Object defaultValue) {
        String envName = "DASHCFG_" + environmentiseConfString(configName);
        String env = System.getenv(envName);
        if (env != null) {
            log.info("Using {} ({}) value from environment.", configName, envName);
            return env;
        }
        String defaultStr = defaultValue != null ? defaultValue.toString() : null;
        return props.getProperty(configName, defaultStr);
    }

    /**
     * Converts a config name in camelCase to upper snake_case for env vars.
     *
     * @param configName The config name to convert.
     * @return An env-var style string for this config name.
     */
    private static String environmentiseConfString(String configName) {
        StringBuffer buf = new StringBuffer();
        StringBuilder out = new StringBuilder();
        boolean firstBlock = true;
        for (char c : configName.toCharArray()) {
            if (Character.isUpperCase(c)) {
                 if (!firstBlock) out.append('_');
                 firstBlock = false;
                out.append(buf.toString());
                buf = new StringBuffer();
            }
            buf.append(c);
        }
        if (!firstBlock) out.append('_');
        return out.append(buf.toString()).toString().toUpperCase();
    }

    @SuppressWarnings("SameParameterValue")
    private static int configInt(String configName, int defaultValue) {
        String str = configString(configName, defaultValue);
        try {
            return Integer.decode(str);
        } catch (NumberFormatException ex) {
            log.warn("Unable to parse '{}' as a positive integer. Check value of the config '{}'!" +
                            " Using default or config file value..", str, configName);
            return defaultValue;
        }
    }

    private static boolean configBool(String configName, boolean defaultValue) {
        String str = configString(configName, defaultValue).toLowerCase();
        return str.equals("true") || str.equals("yes");
    }

    private static File configDir(String configName, File defaultValue) {
        String str = configString(configName, null);
        if (str == null) return defaultValue;
        File f = new File(str);
        if (f.exists() && f.isDirectory()) return f;
        log.warn("{} was defined as '{}' but was not an existing directory; skipping.", configName, str);
        return defaultValue;
    }

}
