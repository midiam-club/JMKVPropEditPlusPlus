package io.github.brunorex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/*
 * Original code by Michael C. Daconta
 * Source: http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4
 *
 */

public class StreamGobbler extends Thread {
    private final InputStream is;
    private final JTextArea text;

    public StreamGobbler(InputStream is, JTextArea text) {
        this.is = is;
        this.text = text;
    }

    public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                final String lineToAppend = line + "\n";
                // JTextArea.append is thread-safe
                text.append(lineToAppend);

                // Use invokeLater for scrolling to ensure EDT safety
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        text.setCaretPosition(text.getDocument().getLength());
                    }
                });
            }
        } catch (IOException e) {
            text.append(e.toString() + "\n");
        }
    }
}
