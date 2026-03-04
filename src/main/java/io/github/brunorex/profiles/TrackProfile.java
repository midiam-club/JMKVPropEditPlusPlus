package io.github.brunorex.profiles;

import java.io.Serializable;

public class TrackProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private boolean defaultTrack;
    private boolean forcedTrack;
    private boolean enableTrack;
    private String trackName;
    private String language;

    // Fields to store if the property was checked (enabled) in the UI
    private boolean useDefaultTrack;
    private boolean useForcedTrack;
    private boolean useEnableTrack;
    private boolean useName;
    private boolean useLanguage;

    public TrackProfile() {
    }

    public TrackProfile(String name, boolean defaultTrack, boolean forcedTrack, boolean enableTrack, String trackName,
            String language) {
        this.name = name;
        this.defaultTrack = defaultTrack;
        this.forcedTrack = forcedTrack;
        this.enableTrack = enableTrack;
        this.trackName = trackName;
        this.language = language;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDefaultTrack() {
        return defaultTrack;
    }

    public void setDefaultTrack(boolean defaultTrack) {
        this.defaultTrack = defaultTrack;
    }

    public boolean isForcedTrack() {
        return forcedTrack;
    }

    public void setForcedTrack(boolean forcedTrack) {
        this.forcedTrack = forcedTrack;
    }

    public boolean isEnableTrack() {
        return enableTrack;
    }

    public void setEnableTrack(boolean enableTrack) {
        this.enableTrack = enableTrack;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isUseDefaultTrack() {
        return useDefaultTrack;
    }

    public void setUseDefaultTrack(boolean useDefaultTrack) {
        this.useDefaultTrack = useDefaultTrack;
    }

    public boolean isUseForcedTrack() {
        return useForcedTrack;
    }

    public void setUseForcedTrack(boolean useForcedTrack) {
        this.useForcedTrack = useForcedTrack;
    }

    public boolean isUseEnableTrack() {
        return useEnableTrack;
    }

    public void setUseEnableTrack(boolean useEnableTrack) {
        this.useEnableTrack = useEnableTrack;
    }

    public boolean isUseName() {
        return useName;
    }

    public void setUseName(boolean useName) {
        this.useName = useName;
    }

    public boolean isUseLanguage() {
        return useLanguage;
    }

    public void setUseLanguage(boolean useLanguage) {
        this.useLanguage = useLanguage;
    }

    @Override
    public String toString() {
        return name;
    }
}
