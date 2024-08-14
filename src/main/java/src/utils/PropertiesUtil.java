package src.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesUtil {
    private static final Properties properties;

    static {
        try {
            properties = new Properties();
            properties.load(new FileInputStream((new File(PropertiesUtil.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParent() + "/application.properties" )));
        } catch (IOException e) {
           throw new RuntimeException(e);
        }
    }

    public static String getProperty(String key) {
        String property = properties.getProperty(key);
        if (property == null)
            throw new RuntimeException(String.format("Property %s not found", key));
        return property;
    }

    public static int getPropertyAsInteger(String key) {
        return Integer.parseInt(getProperty(key));
    }

    public static int[] getPropertyAsIntegerArray(String key) {
        String[] values = getProperty(key).split(",");
        int[] result = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = Integer.parseInt(values[i]);
        }
        return result;
    }
    public static float getPropertyAsFloat(String key) {
        return Float.parseFloat(getProperty(key));
    }

    public static void main(String[] args) {
        System.out.println(getPropertyAsInteger("g"));
    }
}
