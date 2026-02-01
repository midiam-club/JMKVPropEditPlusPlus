package io.github.brunorex;

/**
 * Enum defining file extensions used in the application.
 */
public enum FileExtension {
    // Matroska formats
    MKV(".mkv", "Matroska Video"),
    MKA(".mka", "Matroska Audio"),
    MKS(".mks", "Matroska Subtitle"),
    MK3D(".mk3d", "Matroska 3D Video"),
    WEBM(".webm", "WebM Video"),

    // Chapter/Tag file formats
    CHAPTERS_XML(".xml", "XML Chapters"),
    CHAPTERS_TXT(".txt", "Text Chapters"),

    // Other
    EXE(".exe", "Executable");

    private final String extension;
    private final String description;

    FileExtension(String extension, String description) {
        this.extension = extension;
        this.description = description;
    }

    /**
     * Gets the file extension including the dot.
     * 
     * @return The extension (e.g., ".mkv")
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Gets the human-readable description.
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if a filename has this extension.
     * 
     * @param filename The filename to check
     * @return true if the filename ends with this extension
     */
    public boolean matches(String filename) {
        return filename != null && filename.toLowerCase().endsWith(extension);
    }

    /**
     * Gets all Matroska-compatible extensions.
     * 
     * @return Array of Matroska extensions
     */
    public static FileExtension[] getMatroskaExtensions() {
        return new FileExtension[] { MKV, MKA, MKS, MK3D, WEBM };
    }

    /**
     * Checks if a filename is a valid Matroska file.
     * 
     * @param filename The filename to check
     * @return true if it's a Matroska file
     */
    public static boolean isMatroskaFile(String filename) {
        for (FileExtension ext : getMatroskaExtensions()) {
            if (ext.matches(filename)) {
                return true;
            }
        }
        return false;
    }
}
