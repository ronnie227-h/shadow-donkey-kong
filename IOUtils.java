import java.io.*;
import java.util.Properties;

/**
 * A utility class that provides methods to read and write files.
 */
public class IOUtils {

    private static final Properties GAME_PROPS = readPropertiesFile("res/app.properties");
    /***
     * Read a properties file and return a Properties object
     * @param configFile: the path to the properties file
     * @return: Properties object
     */
    public static Properties readPropertiesFile(String configFile) {
        Properties appProps = new Properties();
        try {
            appProps.load(new FileInputStream(configFile));
        } catch(IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }

        return appProps;
    }

    /**
     * Retrieves a required property by key from the default configuration.
     * Throws an error if the key is missing.
     *
     * @param key The property key
     * @return The value associated with the key
     */
    public static String getProperty(String key) {
        String value = GAME_PROPS.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing property for key: " + key);
        }
        return value;
    }

    /**
     * Retrieves a property by key from the default configuration.
     * If the key is missing, returns the provided default value instead.
     *
     * @param key The property key
     * @param defaultValue Fallback value if key is missing
     * @return Property value or default if not found
     */
    public static String getPropertyOrDefault(String key, String defaultValue) {
        String value = GAME_PROPS.getProperty(key);
        return value != null ? value : defaultValue;
    }

}
