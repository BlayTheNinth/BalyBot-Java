package net.blay09.balybot;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class Config {

    private static final Map<String, String> config = new HashMap<>();

    public static void load(Database database) {
        config.clear();
        try {
            Statement stmt = database.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM config");
            while(rs.next()) {
                config.put(rs.getString("config_name"), rs.getString("config_value"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getValue(String name) {
        if(!config.containsKey(name)) {
            throw new RuntimeException("Required config option " + name + " but it's missing and has no default value.");
        }
        return config.get(name);
    }

    public static String getValue(String name, String defaultVal) {
        String value = config.get(name);
        if(value == null) {
            return defaultVal;
        }
        return value;
    }

    public static boolean hasOption(String name) {
        return config.containsKey(name);
    }
}
