package io.github.brunorex;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.*;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link JMkvpropedit}.
 *
 * <p>These tests exercise the monolithic Swing UI together with the extracted
 * service classes ({@link IniPersistenceService}, {@link BatchExecutorService},
 * {@link LanguageManager}, etc.). They require a graphical environment and are
 * skipped when running in headless mode.</p>
 */
@EnabledIfSystemProperty(named = "java.awt.headless", matches = "false")
class JMkvpropeditIntegrationTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void resetArgsArray() throws Exception {
        Field argsField = JMkvpropedit.class.getDeclaredField("argsArray");
        argsField.setAccessible(true);
        argsField.set(null, new String[0]);
    }

    /**
     * Helper to invoke a private method by name on a specific class.
     */
    private Object invokePrivate(Class<?> clazz, Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method method = clazz.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    /**
     * Helper to invoke a private method by name on JMkvpropedit.
     */
    private Object invokePrivate(Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        return invokePrivate(JMkvpropedit.class, target, methodName, paramTypes, args);
    }

    /**
     * Helper to read a private field.
     */
    private Object readField(Object target, String fieldName) throws Exception {
        Field field = JMkvpropedit.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    /**
     * Helper to write a private field.
     */
    private void writeField(Object target, String fieldName, Object value) throws Exception {
        Field field = JMkvpropedit.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    /**
     * Helper to get the InputTabPanel from a JMkvpropedit instance.
     */
    private InputTabPanel getInputTabPanel(JMkvpropedit app) throws Exception {
        return (InputTabPanel) readField(app, "inputTabPanel");
    }

    @Test
    void constructor_initializesFrameAndServices() throws Exception {
        final JMkvpropedit[] appHolder = new JMkvpropedit[1];
        SwingUtilities.invokeAndWait(() -> {
            try {
                appHolder[0] = new JMkvpropedit();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        JMkvpropedit app = appHolder[0];
        assertNotNull(app);

        JFrame frame = (JFrame) readField(app, "frmJMkvpropedit");
        assertNotNull(frame);
        assertTrue(frame.getTitle().contains("JMKVPropedit++"));
        assertTrue(frame.getTitle().contains("v2.5.1"));

        IniPersistenceService iniService = (IniPersistenceService) readField(app, "iniService");
        assertNotNull(iniService);
    }

    @Test
    void addFile_integration_addsMkvToModel() throws Exception {
        File mkvFile = tempDir.resolve("test.mkv").toFile();
        Files.createFile(mkvFile.toPath());

        final JMkvpropedit[] appHolder = new JMkvpropedit[1];
        SwingUtilities.invokeAndWait(() -> {
            try {
                appHolder[0] = new JMkvpropedit();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        JMkvpropedit app = appHolder[0];
        InputTabPanel inputPanel = getInputTabPanel(app);
        invokePrivate(InputTabPanel.class, inputPanel, "addFile", new Class[]{File.class, boolean.class}, mkvFile, true);

        DefaultListModel<String> modelFiles = inputPanel.getModel();
        assertNotNull(modelFiles);
        assertEquals(1, modelFiles.getSize());
        assertEquals(mkvFile.getCanonicalPath(), modelFiles.get(0));
    }

    @Test
    void parseFiles_integration_loadsFromArgs() throws Exception {
        File mkvFile = tempDir.resolve("arg_test.mkv").toFile();
        Files.createFile(mkvFile.toPath());

        // Reset static argsArray and set it to our temp file
        Field argsField = JMkvpropedit.class.getDeclaredField("argsArray");
        argsField.setAccessible(true);
        argsField.set(null, new String[]{mkvFile.getAbsolutePath()});

        final JMkvpropedit[] appHolder = new JMkvpropedit[1];
        SwingUtilities.invokeAndWait(() -> {
            try {
                appHolder[0] = new JMkvpropedit();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        JMkvpropedit app = appHolder[0];
        InputTabPanel inputPanel = getInputTabPanel(app);
        DefaultListModel<String> modelFiles = inputPanel.getModel();
        assertNotNull(modelFiles);
        assertTrue(modelFiles.getSize() >= 1, "Expected at least one file in modelFiles");
        assertEquals(mkvFile.getCanonicalPath(), modelFiles.get(0));
    }

    @Test
    void themeSwitch_integration_persistsTheme() throws Exception {
        final JMkvpropedit[] appHolder = new JMkvpropedit[1];
        SwingUtilities.invokeAndWait(() -> {
            try {
                appHolder[0] = new JMkvpropedit();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        JMkvpropedit app = appHolder[0];

        // Swap in a temporary INI so we don't pollute the working directory
        File tempIni = tempDir.resolve("theme_test.ini").toFile();
        IniPersistenceService tempService = new IniPersistenceService(tempIni);
        writeField(app, "iniService", tempService);
        writeField(app, "iniFile", tempIni);

        // Switch to dark theme
        invokePrivate(app, "switchTheme", new Class[]{String.class}, "dark");

        // Verify it was persisted
        var ini = tempService.readOrCreateIni();
        assertEquals("dark", tempService.getTheme(ini));

        // Switch back to light
        invokePrivate(app, "switchTheme", new Class[]{String.class}, "light");
        ini = tempService.readOrCreateIni();
        assertEquals("light", tempService.getTheme(ini));
    }

    @Test
    void batchExecutorService_integration_isAvailableForJava() {
        assertTrue(BatchExecutorService.isExecutableAvailable("java"));
    }
}
