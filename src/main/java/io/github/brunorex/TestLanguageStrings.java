package io.github.brunorex;

import java.util.List;

public class TestLanguageStrings {
    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        System.out.println("=== Testing MkvStrings for Default Language ===");
        MkvStrings strings = new MkvStrings();
        List<String> codes = strings.getLangCodeList();
        int undIndex = codes.indexOf("und");
        System.out.println("'und' index: " + undIndex);
        if (undIndex != -1) {
            System.out.println("  Name at 'und' index: " + strings.getLangNameList().get(undIndex));
            System.out.println("  [PASS] 'und' code found.");
        } else {
            System.err.println("  [FAIL] 'und' code NOT found!");
        }

        System.out.println("\n=== Testing LanguageManager ===");
        // Test Default (English)
        LanguageManager.setLocale(java.util.Locale.ENGLISH);
        String titleEn = LanguageManager.getString("tab.general");
        System.out.println("English 'tab.general': " + titleEn);
        if ("General".equals(titleEn)) {
            System.out.println("  [PASS] English Loaded");
        } else {
            System.err.println("  [FAIL] English Incorrect: " + titleEn);
        }

        // Test Spanish
        LanguageManager.setLocale(new java.util.Locale("es"));
        String titleEs = LanguageManager.getString("tab.general");
        System.out.println("Spanish 'tab.general': " + titleEs);
        if ("General".equals(titleEs)) { // "General" is same in both, bad example?
            // Let's check tab.input
            String inputEs = LanguageManager.getString("tab.input");
            System.out.println("Spanish 'tab.input': " + inputEs);
            if ("Entrada".equals(inputEs)) {
                System.out.println("  [PASS] Spanish Loaded");
            } else {
                System.err.println("  [FAIL] Spanish Incorrect: " + inputEs);
            }
        }

    }
}
