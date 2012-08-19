package old.org.bukkit.util.config;

/*
 * Revived classes from a now-ancient version of the bukkit api.
 * 
 * https://github.com/Bukkit/Bukkit/tree/55f405f4a855fcf165e02379cae5bebc76c517d4/src/main/java/org/bukkit/util/config
 */

/**
 * Configuration exception.
 *
 * @author sk89q
 */
public class ConfigurationException extends Exception {
    private static final long serialVersionUID = -2442886939908724203L;

    public ConfigurationException() {
        super();
    }

    public ConfigurationException(String msg) {
        super(msg);
    }
}