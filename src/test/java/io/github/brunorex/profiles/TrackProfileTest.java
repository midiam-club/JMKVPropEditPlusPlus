package io.github.brunorex.profiles;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrackProfileTest {

    @Test
    void defaultConstructorCreatesEmptyProfile() {
        var profile = new TrackProfile();
        assertNull(profile.getName());
        assertNull(profile.getTrackName());
        assertNull(profile.getLanguage());
        assertFalse(profile.isDefaultTrack());
        assertFalse(profile.isForcedTrack());
        assertFalse(profile.isEnableTrack());
        assertFalse(profile.isUseDefaultTrack());
        assertFalse(profile.isUseForcedTrack());
        assertFalse(profile.isUseEnableTrack());
        assertFalse(profile.isUseName());
        assertFalse(profile.isUseLanguage());
    }

    @Test
    void parameterizedConstructorSetsFields() {
        var profile = new TrackProfile("TestName", true, false, true, "Track1", "eng");
        assertEquals("TestName", profile.getName());
        assertTrue(profile.isDefaultTrack());
        assertFalse(profile.isForcedTrack());
        assertTrue(profile.isEnableTrack());
        assertEquals("Track1", profile.getTrackName());
        assertEquals("eng", profile.getLanguage());
    }

    @Test
    void settersAndGettersWork() {
        var profile = new TrackProfile();
        profile.setName("MyProfile");
        profile.setDefaultTrack(true);
        profile.setForcedTrack(true);
        profile.setEnableTrack(false);
        profile.setTrackName("Video Track");
        profile.setLanguage("spa");
        profile.setUseDefaultTrack(true);
        profile.setUseForcedTrack(true);
        profile.setUseEnableTrack(false);
        profile.setUseName(true);
        profile.setUseLanguage(true);

        assertEquals("MyProfile", profile.getName());
        assertTrue(profile.isDefaultTrack());
        assertTrue(profile.isForcedTrack());
        assertFalse(profile.isEnableTrack());
        assertEquals("Video Track", profile.getTrackName());
        assertEquals("spa", profile.getLanguage());
        assertTrue(profile.isUseDefaultTrack());
        assertTrue(profile.isUseForcedTrack());
        assertFalse(profile.isUseEnableTrack());
        assertTrue(profile.isUseName());
        assertTrue(profile.isUseLanguage());
    }

    @Test
    void toStringReturnsName() {
        var profile = new TrackProfile("DisplayName", false, false, false, null, null);
        assertEquals("DisplayName", profile.toString());
    }
}
