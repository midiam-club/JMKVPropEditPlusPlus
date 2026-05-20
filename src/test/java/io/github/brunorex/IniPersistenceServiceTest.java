package io.github.brunorex;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link IniPersistenceService}.
 */
class IniPersistenceServiceTest {

    private IniPersistenceService createService(@TempDir Path tempDir) {
        File iniFile = tempDir.resolve("test.ini").toFile();
        return new IniPersistenceService(iniFile);
    }

    @Test
    void readOrCreateIni_createsDefaultWhenMissing(@TempDir Path tempDir) {
        IniPersistenceService service = createService(tempDir);
        var ini = service.readOrCreateIni();
        assertNotNull(ini);
        assertNotNull(ini.get("General", "mkvpropedit"));
    }

    @Test
    void readOrCreateIni_readsExistingFile(@TempDir Path tempDir) throws IOException {
        File iniFile = tempDir.resolve("existing.ini").toFile();
        Files.writeString(iniFile.toPath(), "[General]\nmkvpropedit=/usr/bin/mkvpropedit\ntheme=dark\n");

        IniPersistenceService service = new IniPersistenceService(iniFile);
        var ini = service.readOrCreateIni();
        assertNotNull(ini);
        assertEquals("/usr/bin/mkvpropedit", ini.get("General", "mkvpropedit"));
        assertEquals("dark", ini.get("General", "theme"));
    }

    @Test
    void readOrCreateIni_returnsNullForInvalidFile() throws IOException {
        // Use system temp file (not @TempDir) to avoid Windows file-lock cleanup issues with ini4j
        File iniFile = File.createTempFile("invalid", ".ini");
        iniFile.deleteOnExit();
        Files.writeString(iniFile.toPath(), "this is not valid ini {{{");

        IniPersistenceService service = new IniPersistenceService(iniFile);
        assertNull(service.readOrCreateIni());
    }

    @Test
    void getExecutablePath_returnsPersistedValue(@TempDir Path tempDir) throws IOException {
        File iniFile = tempDir.resolve("path.ini").toFile();
        // ini4j interprets backslash as escape; use forward slashes for test portability
        Files.writeString(iniFile.toPath(), "[General]\nmkvpropedit=C:/Tools/mkvpropedit.exe\n");

        IniPersistenceService service = new IniPersistenceService(iniFile);
        var ini = service.readOrCreateIni();
        assertEquals("C:/Tools/mkvpropedit.exe", service.getExecutablePath(ini));
    }

    @Test
    void getExecutablePath_returnsDefaultWhenNull(@TempDir Path tempDir) {
        assertEquals("mkvpropedit", createService(tempDir).getExecutablePath(null));
    }

    @Test
    void getExecutablePath_returnsDefaultWhenMissing(@TempDir Path tempDir) {
        var service = createService(tempDir);
        var ini = service.readOrCreateIni();
        // Remove the key
        ini.remove("General", "mkvpropedit");
        assertEquals("mkvpropedit", service.getExecutablePath(ini));
    }

    @Test
    void getTheme_returnsPersistedValue(@TempDir Path tempDir) throws IOException {
        File iniFile = tempDir.resolve("theme.ini").toFile();
        Files.writeString(iniFile.toPath(), "[General]\ntheme=light\n");

        IniPersistenceService service = new IniPersistenceService(iniFile);
        var ini = service.readOrCreateIni();
        assertEquals("light", service.getTheme(ini));
    }

    @Test
    void getTheme_returnsNullForNullIni(@TempDir Path tempDir) {
        assertNull(createService(tempDir).getTheme(null));
    }

    @Test
    void saveExecutablePath_persistsValue(@TempDir Path tempDir) {
        IniPersistenceService service = createService(tempDir);
        service.saveExecutablePath("/opt/mkvpropedit");

        var ini = service.readOrCreateIni();
        assertEquals("/opt/mkvpropedit", service.getExecutablePath(ini));
    }

    @Test
    void saveTheme_persistsValue(@TempDir Path tempDir) {
        IniPersistenceService service = createService(tempDir);
        service.saveTheme("darcula");

        var ini = service.readOrCreateIni();
        assertEquals("darcula", service.getTheme(ini));
    }

    @Test
    void loadProfiles_returnsProfileManager(@TempDir Path tempDir) {
        IniPersistenceService service = createService(tempDir);
        var pm = service.loadProfiles();
        assertNotNull(pm);
    }

    @Test
    void loadProfiles_returnsNullWhenIniUnreadable() throws IOException {
        File iniFile = File.createTempFile("bad", ".ini");
        iniFile.deleteOnExit();
        Files.writeString(iniFile.toPath(), "{{{");
        IniPersistenceService service = new IniPersistenceService(iniFile);
        assertNull(service.loadProfiles());
    }

    @Test
    void discoverMkvPropExe_returnsNullOnNonWindowsOrMissing() {
        String result = IniPersistenceService.discoverMkvPropExe();
        if (!Utils.isWindows()) {
            assertNull(result);
        }
        // On Windows, result may be null or a real path; we just ensure no exception.
    }
}
