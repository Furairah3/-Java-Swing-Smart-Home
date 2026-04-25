package smarthome;

import javax.swing.*;

// I made this class because every Java app needs a main method to start.
// I set up a dark look using Nimbus, then I open my GUI window.
public class Main {

    public static void main(String[] args) {

        // I try to set the Nimbus look and feel because it looks nicer than the default.
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());

                    // I changed the colors here to make it look more like dark mode.
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
            // If Nimbus is not there I just print a message and the app uses the default.
            System.err.println("Nimbus L&F not available, using system default.");
        }

        // I start the GUI on the special Swing thread because that's the safe way to do it.
        SwingUtilities.invokeLater(() -> {
            SmartHomeGUI gui = new SmartHomeGUI();
            gui.setVisible(true);
        });
    }
}
