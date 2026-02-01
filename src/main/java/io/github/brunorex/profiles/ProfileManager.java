package io.github.brunorex.profiles;

import java.util.ArrayList;
import java.util.List;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

public class ProfileManager {
    private static final String AUDIO_PREFIX = "AudioProfile_";
    private static final String VIDEO_PREFIX = "VideoProfile_";
    private static final String SUBTITLE_PREFIX = "SubtitleProfile_";

    public enum ProfileType {
        AUDIO, VIDEO, SUBTITLE
    }

    private List<TrackProfile> audioProfiles;
    private List<TrackProfile> videoProfiles;
    private List<TrackProfile> subtitleProfiles;
    private Ini ini;

    public ProfileManager(Ini ini) {
        this.ini = ini;
        this.audioProfiles = new ArrayList<>();
        this.videoProfiles = new ArrayList<>();
        this.subtitleProfiles = new ArrayList<>();
        loadProfiles();
    }

    private void loadProfiles() {
        loadProfileList(audioProfiles, AUDIO_PREFIX);
        loadProfileList(videoProfiles, VIDEO_PREFIX);
        loadProfileList(subtitleProfiles, SUBTITLE_PREFIX);
    }

    private void loadProfileList(List<TrackProfile> list, String prefix) {
        list.clear();
        int i = 0;
        while (true) {
            Section section = ini.get(prefix + i);
            if (section == null)
                break;

            TrackProfile profile = new TrackProfile();
            profile.setName(section.get("name"));
            profile.setDefaultTrack(section.get("defaultTrack", boolean.class));
            profile.setForcedTrack(section.get("forcedTrack", boolean.class));
            profile.setEnableTrack(section.get("enableTrack", boolean.class));
            profile.setTrackName(section.get("trackName"));
            profile.setTrackName(section.get("trackName"));
            profile.setLanguage(section.get("language"));

            // Load saved state checkboxes
            profile.setUseDefaultTrack(section.get("useDefaultTrack", boolean.class));
            profile.setUseForcedTrack(section.get("useForcedTrack", boolean.class));
            profile.setUseEnableTrack(section.get("useEnableTrack", boolean.class));
            profile.setUseName(section.get("useName", boolean.class));
            profile.setUseLanguage(section.get("useLanguage", boolean.class));

            list.add(profile);
            i++;
        }
    }

    public void saveProfiles() {
        saveProfileList(audioProfiles, AUDIO_PREFIX);
        saveProfileList(videoProfiles, VIDEO_PREFIX);
        saveProfileList(subtitleProfiles, SUBTITLE_PREFIX);

        try {
            ini.store();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveProfileList(List<TrackProfile> list, String prefix) {
        // Remove existing profile sections first
        int i = 0;
        while (true) {
            if (ini.remove(prefix + i) == null)
                break;
            i++;
        }

        for (int j = 0; j < list.size(); j++) {
            TrackProfile p = list.get(j);
            String sectionName = prefix + j;
            ini.put(sectionName, "name", p.getName());
            ini.put(sectionName, "defaultTrack", p.isDefaultTrack());
            ini.put(sectionName, "forcedTrack", p.isForcedTrack());
            ini.put(sectionName, "enableTrack", p.isEnableTrack());
            ini.put(sectionName, "trackName", p.getTrackName() == null ? "" : p.getTrackName());
            ini.put(sectionName, "trackName", p.getTrackName() == null ? "" : p.getTrackName());
            ini.put(sectionName, "language", p.getLanguage() == null ? "" : p.getLanguage());

            // Save state checkboxes
            ini.put(sectionName, "useDefaultTrack", p.isUseDefaultTrack());
            ini.put(sectionName, "useForcedTrack", p.isUseForcedTrack());
            ini.put(sectionName, "useEnableTrack", p.isUseEnableTrack());
            ini.put(sectionName, "useName", p.isUseName());
            ini.put(sectionName, "useLanguage", p.isUseLanguage());
        }
    }

    public List<TrackProfile> getProfiles(ProfileType type) {
        switch (type) {
            case VIDEO:
                return videoProfiles;
            case SUBTITLE:
                return subtitleProfiles;
            case AUDIO:
            default:
                return audioProfiles;
        }
    }

    public void addProfile(ProfileType type, TrackProfile profile) {
        getProfiles(type).add(profile);
        saveProfiles();
    }

    public void removeProfile(ProfileType type, int index) {
        List<TrackProfile> list = getProfiles(type);
        if (index >= 0 && index < list.size()) {
            list.remove(index);
            saveProfiles();
        }
    }

    public void reorderProfiles(ProfileType type, javax.swing.DefaultListModel<TrackProfile> model) {
        List<TrackProfile> list = getProfiles(type);
        list.clear();
        for (int i = 0; i < model.getSize(); i++) {
            list.add(model.get(i));
        }
        saveProfiles();
    }
}
