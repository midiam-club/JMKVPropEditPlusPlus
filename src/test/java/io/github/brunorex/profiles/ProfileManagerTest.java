package io.github.brunorex.profiles;

import org.ini4j.Ini;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.DefaultListModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ProfileManagerTest {

    private Ini createIniWithProfiles() {
        Ini ini = new Ini();
        ini.put("AudioProfile_0", "name", "Audio1");
        ini.put("AudioProfile_0", "defaultTrack", true);
        ini.put("AudioProfile_0", "forcedTrack", false);
        ini.put("AudioProfile_0", "enableTrack", true);
        ini.put("AudioProfile_0", "trackName", "Audio Track");
        ini.put("AudioProfile_0", "language", "eng");
        ini.put("AudioProfile_0", "useDefaultTrack", true);
        ini.put("AudioProfile_0", "useForcedTrack", false);
        ini.put("AudioProfile_0", "useEnableTrack", true);
        ini.put("AudioProfile_0", "useName", true);
        ini.put("AudioProfile_0", "useLanguage", true);

        ini.put("VideoProfile_0", "name", "Video1");
        ini.put("VideoProfile_0", "defaultTrack", false);
        ini.put("VideoProfile_0", "forcedTrack", true);
        ini.put("VideoProfile_0", "enableTrack", false);
        ini.put("VideoProfile_0", "trackName", "Video Track");
        ini.put("VideoProfile_0", "language", "spa");
        ini.put("VideoProfile_0", "useDefaultTrack", false);
        ini.put("VideoProfile_0", "useForcedTrack", true);
        ini.put("VideoProfile_0", "useEnableTrack", false);
        ini.put("VideoProfile_0", "useName", false);
        ini.put("VideoProfile_0", "useLanguage", false);

        ini.put("SubtitleProfile_0", "name", "Sub1");
        ini.put("SubtitleProfile_0", "defaultTrack", true);
        ini.put("SubtitleProfile_0", "forcedTrack", true);
        ini.put("SubtitleProfile_0", "enableTrack", true);
        ini.put("SubtitleProfile_0", "trackName", "Sub Track");
        ini.put("SubtitleProfile_0", "language", "fre");
        ini.put("SubtitleProfile_0", "useDefaultTrack", true);
        ini.put("SubtitleProfile_0", "useForcedTrack", true);
        ini.put("SubtitleProfile_0", "useEnableTrack", true);
        ini.put("SubtitleProfile_0", "useName", true);
        ini.put("SubtitleProfile_0", "useLanguage", true);

        return ini;
    }

    @Test
    void constructorLoadsProfilesFromIni() {
        Ini ini = createIniWithProfiles();
        ProfileManager manager = new ProfileManager(ini);

        assertEquals(1, manager.getProfiles(ProfileManager.ProfileType.AUDIO).size());
        assertEquals(1, manager.getProfiles(ProfileManager.ProfileType.VIDEO).size());
        assertEquals(1, manager.getProfiles(ProfileManager.ProfileType.SUBTITLE).size());

        TrackProfile audio = manager.getProfiles(ProfileManager.ProfileType.AUDIO).get(0);
        assertEquals("Audio1", audio.getName());
        assertTrue(audio.isDefaultTrack());
        assertFalse(audio.isForcedTrack());
        assertTrue(audio.isEnableTrack());
        assertEquals("Audio Track", audio.getTrackName());
        assertEquals("eng", audio.getLanguage());
        assertTrue(audio.isUseDefaultTrack());
        assertFalse(audio.isUseForcedTrack());
        assertTrue(audio.isUseEnableTrack());
        assertTrue(audio.isUseName());
        assertTrue(audio.isUseLanguage());

        TrackProfile video = manager.getProfiles(ProfileManager.ProfileType.VIDEO).get(0);
        assertEquals("Video1", video.getName());
        assertEquals("spa", video.getLanguage());

        TrackProfile sub = manager.getProfiles(ProfileManager.ProfileType.SUBTITLE).get(0);
        assertEquals("Sub1", sub.getName());
        assertEquals("fre", sub.getLanguage());
    }

    @Test
    void constructorWithEmptyIniCreatesEmptyLists() {
        Ini ini = new Ini();
        ProfileManager manager = new ProfileManager(ini);

        assertTrue(manager.getProfiles(ProfileManager.ProfileType.AUDIO).isEmpty());
        assertTrue(manager.getProfiles(ProfileManager.ProfileType.VIDEO).isEmpty());
        assertTrue(manager.getProfiles(ProfileManager.ProfileType.SUBTITLE).isEmpty());
    }

    @Test
    void addProfileAppendsAndSaves(@TempDir Path tempDir) throws IOException {
        File iniFile = tempDir.resolve("profiles.ini").toFile();
        iniFile.createNewFile();
        Ini ini = new Ini(iniFile);
        ProfileManager manager = new ProfileManager(ini);

        TrackProfile profile = new TrackProfile("NewAudio", true, true, false, "Name", "ger");
        manager.addProfile(ProfileManager.ProfileType.AUDIO, profile);

        assertEquals(1, manager.getProfiles(ProfileManager.ProfileType.AUDIO).size());
        assertEquals("NewAudio", manager.getProfiles(ProfileManager.ProfileType.AUDIO).get(0).getName());

        Ini reloaded = new Ini(iniFile);
        assertEquals("NewAudio", reloaded.get("AudioProfile_0", "name"));
    }

    @Test
    void removeProfileDeletesAndSaves(@TempDir Path tempDir) throws IOException {
        File iniFile = tempDir.resolve("profiles.ini").toFile();
        iniFile.createNewFile();
        Ini ini = new Ini(iniFile);
        ini.put("AudioProfile_0", "name", "ToDelete");
        ini.store();

        ProfileManager manager = new ProfileManager(ini);
        assertEquals(1, manager.getProfiles(ProfileManager.ProfileType.AUDIO).size());

        manager.removeProfile(ProfileManager.ProfileType.AUDIO, 0);
        assertTrue(manager.getProfiles(ProfileManager.ProfileType.AUDIO).isEmpty());

        Ini reloaded = new Ini(iniFile);
        assertNull(reloaded.get("AudioProfile_0"));
    }

    @Test
    void removeProfileWithInvalidIndexDoesNothing() {
        Ini ini = createIniWithProfiles();
        ProfileManager manager = new ProfileManager(ini);

        manager.removeProfile(ProfileManager.ProfileType.AUDIO, -1);
        manager.removeProfile(ProfileManager.ProfileType.AUDIO, 999);

        assertEquals(1, manager.getProfiles(ProfileManager.ProfileType.AUDIO).size());
    }

    @Test
    void reorderProfilesUpdatesOrderAndSaves(@TempDir Path tempDir) throws IOException {
        File iniFile = tempDir.resolve("profiles.ini").toFile();
        iniFile.createNewFile();
        Ini ini = new Ini(iniFile);
        ini.put("AudioProfile_0", "name", "First");
        ini.put("AudioProfile_1", "name", "Second");
        ini.store();

        ProfileManager manager = new ProfileManager(ini);
        assertEquals("First", manager.getProfiles(ProfileManager.ProfileType.AUDIO).get(0).getName());
        assertEquals("Second", manager.getProfiles(ProfileManager.ProfileType.AUDIO).get(1).getName());

        DefaultListModel<TrackProfile> model = new DefaultListModel<>();
        model.addElement(manager.getProfiles(ProfileManager.ProfileType.AUDIO).get(1));
        model.addElement(manager.getProfiles(ProfileManager.ProfileType.AUDIO).get(0));

        manager.reorderProfiles(ProfileManager.ProfileType.AUDIO, model);

        assertEquals("Second", manager.getProfiles(ProfileManager.ProfileType.AUDIO).get(0).getName());
        assertEquals("First", manager.getProfiles(ProfileManager.ProfileType.AUDIO).get(1).getName());

        Ini reloaded = new Ini(iniFile);
        assertEquals("Second", reloaded.get("AudioProfile_0", "name"));
        assertEquals("First", reloaded.get("AudioProfile_1", "name"));
    }

    @Test
    void saveProfilesHandlesMultipleTypes(@TempDir Path tempDir) throws IOException {
        File iniFile = tempDir.resolve("profiles.ini").toFile();
        iniFile.createNewFile();
        Ini ini = new Ini(iniFile);
        ProfileManager manager = new ProfileManager(ini);

        manager.addProfile(ProfileManager.ProfileType.AUDIO, new TrackProfile("A1", true, false, true, "A", "eng"));
        manager.addProfile(ProfileManager.ProfileType.VIDEO, new TrackProfile("V1", false, true, false, "V", "spa"));
        manager.addProfile(ProfileManager.ProfileType.SUBTITLE, new TrackProfile("S1", true, true, true, "S", "fre"));

        Ini reloaded = new Ini(iniFile);
        assertEquals("A1", reloaded.get("AudioProfile_0", "name"));
        assertEquals("V1", reloaded.get("VideoProfile_0", "name"));
        assertEquals("S1", reloaded.get("SubtitleProfile_0", "name"));
    }

    @Test
    void loadProfilesHandlesMissingFields() {
        Ini ini = new Ini();
        ini.put("AudioProfile_0", "name", "NullTest");
        ProfileManager manager = new ProfileManager(ini);

        TrackProfile profile = manager.getProfiles(ProfileManager.ProfileType.AUDIO).get(0);
        assertEquals("NullTest", profile.getName());
        assertFalse(profile.isDefaultTrack());
        assertNull(profile.getTrackName());
    }
}
