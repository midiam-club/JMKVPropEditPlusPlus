package io.github.brunorex;

import io.github.brunorex.profiles.ProfileManager;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

/**
 * Encapsulates reading and writing the JMkvpropedit.ini configuration file.
 * <p>
 * This service is UI-agnostic: it does not touch Swing components. Callers
 * receive plain data (strings, booleans, {@link ProfileManager}) and are
 * responsible for updating the view.
 * </p>
 */
public class IniPersistenceService {

    private static final Logger LOGGER = Logger.getLogger(IniPersistenceService.class.getName());

    private final File iniFile;

    public IniPersistenceService(File iniFile) {
        this.iniFile = iniFile;
    }

    /**
     * Reads the INI file and returns the parsed {@link Ini} instance.
     * If the file does not exist, it is created and pre-populated with
     * sensible defaults (auto-discover mkvpropedit on Windows, or
     * "mkvpropedit" on other platforms).
     *
     * @return the Ini instance, or null if the file could not be read
     */
    public Ini readOrCreateIni() {
        if (!iniFile.exists()) {
            createDefaultIni();
        }

        try {
            return new Ini(iniFile);
        } catch (InvalidFileFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid INI file format: " + iniFile.getPath(), e);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error reading INI file: " + iniFile.getPath(), e);
        }
        return null;
    }

    private void createDefaultIni() {
        try {
            iniFile.createNewFile();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not create INI file: " + iniFile.getPath(), e);
            return;
        }

        if (Utils.isWindows()) {
            String exePath = discoverMkvPropExe();
            if (exePath != null) {
                saveExecutablePath(exePath);
            } else {
                saveExecutablePath("mkvpropedit");
            }
        } else {
            saveExecutablePath("mkvpropedit");
        }
    }

    /**
     * Returns the persisted mkvpropedit executable path.
     *
     * @param ini the parsed Ini
     * @return the path string, or "mkvpropedit" if not set
     */
    public String getExecutablePath(Ini ini) {
        if (ini == null) {
            return "mkvpropedit";
        }
        String path = ini.get("General", "mkvpropedit");
        return path != null ? path : "mkvpropedit";
    }

    /**
     * Returns the persisted theme preference.
     *
     * @param ini the parsed Ini
     * @return the theme string, or null if not set
     */
    public String getTheme(Ini ini) {
        if (ini == null) {
            return null;
        }
        return ini.get("General", "theme");
    }

    /**
     * Saves the executable path to the INI file.
     *
     * @param path the path to persist
     */
    public void saveExecutablePath(String path) {
        try {
            ensureFileExists();
            Ini ini = new Ini(iniFile);
            ini.put("General", "mkvpropedit", path);
            ini.store();
        } catch (InvalidFileFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid INI file format while saving: " + iniFile.getPath(), e);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error saving INI file: " + iniFile.getPath(), e);
        }
    }

    /**
     * Saves the theme preference to the INI file.
     *
     * @param theme the theme to persist
     */
    public void saveTheme(String theme) {
        try {
            ensureFileExists();
            Ini ini = new Ini(iniFile);
            ini.put("General", "theme", theme);
            ini.store();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error saving theme preference", e);
        }
    }

    private void ensureFileExists() throws IOException {
        if (!iniFile.exists()) {
            iniFile.createNewFile();
        }
    }

    /**
     * Creates a {@link ProfileManager} from the current INI file.
     *
     * @return a ProfileManager, or null if the INI could not be read
     */
    public ProfileManager loadProfiles() {
        Ini ini = readOrCreateIni();
        if (ini == null) {
            return null;
        }
        return new ProfileManager(ini);
    }

    /**
     * Discovers the mkvpropedit executable on Windows by checking common
     * installation directories.
     *
     * @return the absolute path, or null if not found
     */
    public static String discoverMkvPropExe() {
        String sysDrive = System.getenv("SystemDrive");
        if (sysDrive == null) {
            sysDrive = "C:";
        }
        String[] exePaths = new String[] {
                sysDrive + "\\Program Files (x86)\\MKVToolNix",
                sysDrive + "\\Program Files\\MKVToolNix"
        };

        for (String exePath : exePaths) {
            File tmpExe = new File(exePath + "\\mkvpropedit.exe");
            if (tmpExe.exists()) {
                return tmpExe.toString();
            }
        }
        return null;
    }
}
