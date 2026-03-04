package io.github.brunorex;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {
    private static ResourceBundle messages;
    private static Locale currentLocale;

    static {
        // Attempt to load saved locale or default
        // For now, default to system or English.
        // In a real app, we might load from prefs here, but the main app loads prefs
        // later.
        // unique instance initialization
        setLocale(Locale.getDefault());
    }

    public static void setLocale(Locale locale) {
        currentLocale = locale;
        try {
            messages = ResourceBundle.getBundle("io.github.brunorex.resources.messages", currentLocale,
                    new UTF8Control());
        } catch (Exception e) {
            // Fallback to English if default fails or properties missing
            messages = ResourceBundle.getBundle("io.github.brunorex.resources.messages", Locale.ENGLISH,
                    new UTF8Control());
        }
    }

    private static class UTF8Control extends ResourceBundle.Control {
        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader,
                boolean reload)
                throws IllegalAccessException, InstantiationException, java.io.IOException {
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            ResourceBundle bundle = null;
            java.io.InputStream stream = null;
            if (reload) {
                java.net.URL url = loader.getResource(resourceName);
                if (url != null) {
                    java.net.URLConnection connection = url.openConnection();
                    if (connection != null) {
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
            } else {
                stream = loader.getResourceAsStream(resourceName);
            }
            if (stream != null) {
                try {
                    bundle = new java.util.PropertyResourceBundle(new java.io.InputStreamReader(stream, "UTF-8"));
                } finally {
                    stream.close();
                }
            }
            return bundle;
        }
    }

    public static Locale getLocale() {
        return currentLocale;
    }

    public static String getString(String key) {
        try {
            return messages.getString(key);
        } catch (Exception e) {
            return "!" + key + "!";
        }
    }
}
