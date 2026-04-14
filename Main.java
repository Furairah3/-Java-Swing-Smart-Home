package smarthome;

import javax.swing.*;

/**
 * Main — Application Entry Point
 *
 * Sets the Nimbus Look and Feel (built into the JDK — no external
 * dependencies) for a modern, cross-platform appearance, then
 * launches the SmartHomeGUI on the Event Dispatch Thread.
 *
 * To use FlatLaf instead (requires the com.formdev:flatlaf dependency):
 *   1. Add to build.gradle:  implementation 'com.formdev:flatlaf:3.4'
 *   2. Replace the try block below with:
 *        com.formdev.flatlaf.FlatDarkLaf.setup();
 */
public class Main {

    public static void main(String[] args) {

        // ── Set Look and Feel ──────────────────────────────────────
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());

                    // Darken Nimbus defaults for a modern dark-mode feel
                    UIManager.put("control",          new java.awt.Color(45, 48, 56));
                    UIManager.put("nimbusBase",        new java.awt.Color(30, 33, 40));
                    UIManager.put("nimbusFocus",       new java.awt.Color(80, 160, 255));
                    UIManager.put("nimbusLightBackground", new java.awt.Color(40, 44, 52));
                    UIManager.put("text",              new java.awt.Color(220, 225, 235));
                    UIManager.put("nimbusSelectionBackground", new java.awt.Color(80, 160, 255));
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Nimbus L&F not available, using system default.");
        }

        // ── Launch GUI on the Event Dispatch Thread ────────────────
        SwingUtilities.invokeLater(() -> {
            SmartHomeGUI gui = new SmartHomeGUI();
            gui.setVisible(true);
        });
    }
}
