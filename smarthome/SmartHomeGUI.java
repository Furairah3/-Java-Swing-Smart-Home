package smarthome;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

// I made this class as the main window of my smart home app.
// I extend JFrame so I get a window for free, and I implement DeviceObserver
// so I can react to changes coming from any device.
// The window has a toolbar at the top, rooms on the left, log on the right,
// and an "add device" panel at the bottom.
public class SmartHomeGUI extends JFrame implements DeviceObserver {

    // I made these color constants so all the panels and buttons look the same.
    private static final Color CLR_BG        = new Color(30, 33, 40);
    private static final Color CLR_PANEL     = new Color(40, 44, 52);
    private static final Color CLR_CARD      = new Color(50, 55, 65);
    private static final Color CLR_ACCENT    = new Color(80, 160, 255);
    private static final Color CLR_ON        = new Color(72, 199, 142);
    private static final Color CLR_OFF       = new Color(180, 80, 80);
    private static final Color CLR_TEXT      = new Color(220, 225, 235);
    private static final Color CLR_TEXT_DIM  = new Color(140, 148, 165);
    private static final Color CLR_TOOLBAR   = new Color(35, 38, 46);

    // I grab the singleton hub so I can talk to the rest of the app.
    private final SmartHomeHub hub = SmartHomeHub.getInstance();

    // I keep references to the GUI parts I need to update later.
    private JTextArea   logArea;
    private JPanel      roomsContainer;   // I put all my room panels in here.
    private JButton     undoBtn, redoBtn;

    // I keep a map of each device to its button so I can update the button when the device changes.
    private final Map<SmartDevice, JToggleButton> deviceButtons = new LinkedHashMap<>();

    // I keep a map of each room name to its master switch button.
    private final Map<String, JToggleButton> masterSwitches = new LinkedHashMap<>();

    // I set up the window when someone makes a new SmartHomeGUI.
    public SmartHomeGUI() {
        super("Smart Home Controller");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 680);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);

        // I added this so when the user closes the window, I save their device states.
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                hub.savePreferences();
            }
        });

        initComponents();
        populateDemoDevices();
        hub.loadPreferences();
        refreshAllButtons();   // I refresh the buttons so they match the loaded states.
    }

    // I built the main layout in this method.
    private void initComponents() {
        // I made the root panel that holds everything else.
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(CLR_BG);
        setContentPane(root);

        // I added the toolbar on top, the rooms+log in the middle, and the add panel on bottom.
        root.add(buildToolbar(),     BorderLayout.NORTH);
        root.add(buildCenterSplit(), BorderLayout.CENTER);
        root.add(buildAddPanel(),    BorderLayout.SOUTH);
    }

    // I built the toolbar with undo, redo, automation buttons, and clear log here.
    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        bar.setBackground(CLR_TOOLBAR);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CLR_CARD));

        // I made the undo and redo buttons here and hooked them up to the command manager.
        undoBtn = toolbarButton("↩ Undo");
        redoBtn = toolbarButton("↪ Redo");
        undoBtn.addActionListener(e -> { hub.getCommandManager().undo(); refreshAfterCommand(); });
        redoBtn.addActionListener(e -> { hub.getCommandManager().redo(); refreshAfterCommand(); });
        bar.add(undoBtn);
        bar.add(redoBtn);

        bar.add(toolbarSeparator());

        // I register my Night Mode and Vacation Mode strategies so they show up as buttons.
        hub.registerStrategy(new NightModeStrategy());
        hub.registerStrategy(new VacationModeStrategy());

        // I loop through all the strategies and make a button for each one.
        for (AutomationStrategy s : hub.getStrategies()) {
            JButton btn = toolbarButton("🌙 " + s.getName());
            if (s.getName().contains("Vacation")) {
                btn.setText("✈ " + s.getName());
            }
            btn.addActionListener(e -> {
                hub.runAutomation(s);
                refreshAfterCommand();
            });
            bar.add(btn);
        }

        // I made a Clear Log button so the user can wipe the log when it gets too full.
        bar.add(toolbarSeparator());
        JButton clearBtn = toolbarButton("Clear Log");
        clearBtn.addActionListener(e -> logArea.setText(""));
        bar.add(clearBtn);

        return bar;
    }

    // I made this helper so all my toolbar buttons look the same without me copying code.
    private JButton toolbarButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setFocusPainted(false);
        b.setBackground(CLR_CARD);
        b.setForeground(CLR_TEXT);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CLR_TEXT_DIM, 1, true),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // I made this little vertical line to separate sections of the toolbar.
    private JSeparator toolbarSeparator() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 28));
        sep.setForeground(CLR_TEXT_DIM);
        return sep;
    }

    // I built the middle section here with rooms on the left and the log on the right.
    private JSplitPane buildCenterSplit() {
        // I made the rooms panel scrollable in case the user has lots of rooms.
        roomsContainer = new JPanel();
        roomsContainer.setLayout(new BoxLayout(roomsContainer, BoxLayout.Y_AXIS));
        roomsContainer.setBackground(CLR_BG);

        JScrollPane roomScroll = new JScrollPane(roomsContainer);
        roomScroll.setBorder(BorderFactory.createEmptyBorder());
        roomScroll.getViewport().setBackground(CLR_BG);
        roomScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // I made the log a JTextArea that the user can't edit directly.
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logArea.setBackground(CLR_PANEL);
        logArea.setForeground(CLR_TEXT);
        logArea.setCaretColor(CLR_ACCENT);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setMargin(new Insets(8, 10, 8, 10));

        // I wrapped the log in a scroll pane and gave it a "Event Log" title.
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CLR_TEXT_DIM),
                " Event Log ", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13), CLR_ACCENT));
        logScroll.getViewport().setBackground(CLR_PANEL);

        // I split the rooms and log so the user can drag the divider to resize them.
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, roomScroll, logScroll);
        split.setDividerLocation(580);
        split.setResizeWeight(0.6);
        split.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        split.setBackground(CLR_BG);
        return split;
    }

    // I built the bottom panel here for adding new devices.
    private JPanel buildAddPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panel.setBackground(CLR_TOOLBAR);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, CLR_CARD));

        // I fill the type dropdown using whatever device types the factory knows about.
        JComboBox<String> typeCombo = new JComboBox<>(
                DeviceFactory.getAvailableTypes().toArray(new String[0]));
        styleCombo(typeCombo);

        // I made a text field for the user to type in the device name.
        JTextField nameField = new JTextField(14);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nameField.setBackground(CLR_CARD);
        nameField.setForeground(CLR_TEXT);
        nameField.setCaretColor(CLR_TEXT);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CLR_TEXT_DIM),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));

        // I made the room dropdown editable so the user can also type a brand new room name.
        JComboBox<String> roomCombo = new JComboBox<>();
        roomCombo.setEditable(true);
        styleCombo(roomCombo);

        // I made the Add button stand out with the accent color.
        JButton addBtn = toolbarButton("+ Add Device");
        addBtn.setBackground(CLR_ACCENT);
        addBtn.setForeground(Color.WHITE);

        // I hooked up the Add button so when it's clicked, I make a new device.
        addBtn.addActionListener(e -> {
            String type = (String) typeCombo.getSelectedItem();
            String name = nameField.getText().trim();
            String room = ((String) roomCombo.getSelectedItem());
            if (room != null) room = room.trim();

            // I check that the user actually filled in a name and room.
            if (name.isEmpty() || room == null || room.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a device name and room.",
                        "Missing Input", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // I ask the hub to add the device, then I sign myself up as its observer.
            SmartDevice device = hub.addDevice(type, name, room);
            device.addObserver(this);
            rebuildRoomPanels();
            nameField.setText("");
            refreshRoomCombo(roomCombo);
        });

        // I made simple labels for each input so the user knows what to type where.
        JLabel lbl1 = new JLabel("Type:");  lbl1.setForeground(CLR_TEXT_DIM);
        JLabel lbl2 = new JLabel("Name:");  lbl2.setForeground(CLR_TEXT_DIM);
        JLabel lbl3 = new JLabel("Room:");  lbl3.setForeground(CLR_TEXT_DIM);

        panel.add(lbl1); panel.add(typeCombo);
        panel.add(lbl2); panel.add(nameField);
        panel.add(lbl3); panel.add(roomCombo);
        panel.add(addBtn);

        // I wait until the demo devices are added before I fill the room dropdown.
        SwingUtilities.invokeLater(() -> refreshRoomCombo(roomCombo));

        return panel;
    }

    // I made this helper so all my dropdowns look the same.
    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(CLR_CARD);
        combo.setForeground(CLR_TEXT);
    }

    // I refresh the room dropdown by clearing it and adding all current room names back.
    private void refreshRoomCombo(JComboBox<String> combo) {
        String current = (String) combo.getSelectedItem();
        combo.removeAllItems();
        for (String rn : hub.getRoomNames()) combo.addItem(rn);
        if (current != null && !current.isEmpty()) combo.setSelectedItem(current);
    }

    // I made this method to wipe out the rooms panel and rebuild it from scratch.
    // I call it whenever a device is added or removed.
    private void rebuildRoomPanels() {
        roomsContainer.removeAll();
        deviceButtons.clear();
        masterSwitches.clear();

        // I loop through every room and add a panel for it.
        for (Room room : hub.getRooms()) {
            roomsContainer.add(buildRoomPanel(room));
            roomsContainer.add(Box.createVerticalStrut(6));
        }
        roomsContainer.revalidate();
        roomsContainer.repaint();
    }

    // I made this to build the panel for one single room.
    private JPanel buildRoomPanel(Room room) {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBackground(CLR_PANEL);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 999));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(CLR_ACCENT, 1, true),
                        " " + room.getName() + " (" + room.size() + " devices) ",
                        TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 14), CLR_ACCENT),
                BorderFactory.createEmptyBorder(8, 10, 10, 10)));

        // I made the master switch button that turns everything in the room ON or OFF.
        JToggleButton master = new JToggleButton("Master Switch: OFF");
        master.setFont(new Font("Segoe UI", Font.BOLD, 12));
        master.setFocusPainted(false);
        master.setBackground(CLR_OFF);
        master.setForeground(Color.WHITE);
        master.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        master.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));

        // When the master switch is clicked, I make a RoomToggleCommand and run it.
        master.addActionListener(e -> {
            boolean turnOn = master.isSelected();
            RoomToggleCommand cmd = new RoomToggleCommand(room, turnOn);
            hub.executeCommand(cmd);
            refreshAfterCommand();
        });
        masterSwitches.put(room.getName(), master);

        JPanel masterWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        masterWrap.setOpaque(false);
        masterWrap.add(master);
        panel.add(masterWrap, BorderLayout.NORTH);

        // I made a grid layout for the device buttons so they line up nicely.
        List<SmartDevice> devices = room.getDevices();
        int cols = Math.max(2, Math.min(4, devices.size()));
        JPanel grid = new JPanel(new GridLayout(0, cols, 8, 8));
        grid.setOpaque(false);

        // I make a button for each device and add it to the grid.
        for (SmartDevice device : devices) {
            JToggleButton btn = createDeviceButton(device);
            deviceButtons.put(device, btn);
            grid.add(btn);
        }

        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    // I made this method to build the toggle button for one single device.
    private JToggleButton createDeviceButton(SmartDevice device) {
        JToggleButton btn = new JToggleButton(device.getDisplayLabel());
        btn.setSelected(device.isOn());
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(device.isOn() ? CLR_ON : CLR_OFF, 2, true),
                BorderFactory.createEmptyBorder(10, 8, 10, 8)));
        btn.setBackground(CLR_CARD);
        btn.setForeground(CLR_TEXT);

        // When the user clicks the button, I make a ToggleDeviceCommand and run it.
        btn.addActionListener(e -> {
            // I reset the visual state because the command will set it correctly through the observer.
            btn.setSelected(device.isOn());
            ToggleDeviceCommand cmd = new ToggleDeviceCommand(device);
            hub.executeCommand(cmd);
            refreshAfterCommand();
        });

        return btn;
    }

    // This is the method that gets called whenever any device changes (because I'm an observer).
    @Override
    public void onDeviceEvent(DeviceEvent event) {
        // I always update the GUI on the special Swing thread so it doesn't crash.
        SwingUtilities.invokeLater(() -> {
            // First I add the event to the log.
            logArea.append(event.toLogString() + "\n");
            // I scroll down so the user always sees the newest entry.
            logArea.setCaretPosition(logArea.getDocument().getLength());

            // Then I find the device that changed and update its button.
            SmartDevice source = findDevice(event.getDeviceName(), event.getRoomName());
            if (source != null) {
                JToggleButton btn = deviceButtons.get(source);
                if (btn != null) {
                    syncButtonToDevice(btn, source);
                }
            }

            // I also refresh the room's master switch in case it needs to flip too.
            updateMasterSwitch(event.getRoomName());

            // Finally I update the undo and redo buttons.
            updateUndoRedoButtons();
        });
    }

    // I call this after running any command to make sure the GUI shows the right thing.
    private void refreshAfterCommand() {
        SwingUtilities.invokeLater(() -> {
            refreshAllButtons();
            updateUndoRedoButtons();
        });
    }

    // I made this to refresh every device button and master switch in one go.
    private void refreshAllButtons() {
        for (Map.Entry<SmartDevice, JToggleButton> e : deviceButtons.entrySet()) {
            syncButtonToDevice(e.getValue(), e.getKey());
        }
        for (String roomName : masterSwitches.keySet()) {
            updateMasterSwitch(roomName);
        }
    }

    // I made this to update one button so it matches whatever the device's actual state is.
    private void syncButtonToDevice(JToggleButton btn, SmartDevice device) {
        btn.setSelected(device.isOn());
        btn.setText(device.getDisplayLabel());
        // I change the border color to green when the device is on and red when it's off.
        Color borderClr = device.isOn() ? CLR_ON : CLR_OFF;
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderClr, 2, true),
                BorderFactory.createEmptyBorder(10, 8, 10, 8)));
    }

    // I made this to update a single room's master switch label and color.
    private void updateMasterSwitch(String roomName) {
        JToggleButton master = masterSwitches.get(roomName);
        Room room = hub.getRoom(roomName);
        if (master == null || room == null) return;

        // If at least one device in the room is on, I show the master switch as ON.
        boolean anyOn = room.hasActiveDevice();
        master.setSelected(anyOn);
        master.setText("Master Switch: " + (anyOn ? "ON" : "OFF"));
        master.setBackground(anyOn ? CLR_ON : CLR_OFF);
    }

    // I made this to enable/disable undo and redo and to set their tooltips.
    private void updateUndoRedoButtons() {
        CommandManager cm = hub.getCommandManager();
        undoBtn.setEnabled(cm.canUndo());
        redoBtn.setEnabled(cm.canRedo());
        undoBtn.setToolTipText(cm.canUndo() ? "Undo: " + cm.peekUndoDescription() : "Nothing to undo");
        redoBtn.setToolTipText(cm.canRedo() ? "Redo: " + cm.peekRedoDescription() : "Nothing to redo");
    }

    // I made this little helper to find a device by its name and room.
    private SmartDevice findDevice(String name, String roomName) {
        Room room = hub.getRoom(roomName);
        if (room == null) return null;
        for (SmartDevice d : room.getDevices()) {
            if (d.getName().equals(name)) return d;
        }
        return null;
    }

    // I added some demo devices so the user sees something the moment the app opens.
    // Otherwise it would just be an empty window which is boring.
    private void populateDemoDevices() {
        String[][] demo = {
            // I listed the type, the name, and the room for each demo device.
            { "Light",      "Ceiling Light",    "Living Room"  },
            { "Light",      "Floor Lamp",       "Living Room"  },
            { "Thermostat", "AC Unit",          "Living Room"  },
            { "DoorLock",   "Front Door Lock",  "Living Room"  },
            { "Light",      "Bedside Lamp",     "Bedroom"      },
            { "Thermostat", "Bedroom AC",       "Bedroom"      },
            { "DoorLock",   "Bedroom Lock",     "Bedroom"      },
            { "Light",      "Kitchen Light",    "Kitchen"      },
            { "Thermostat", "Kitchen AC",       "Kitchen"      },
        };

        // I loop through my demo list and add each device, then I observe each one.
        for (String[] spec : demo) {
            SmartDevice device = hub.addDevice(spec[0], spec[1], spec[2]);
            device.addObserver(this);   // I sign myself up to listen to every device.
        }

        rebuildRoomPanels();
        // I print a friendly intro message in the log so the user knows the app is ready.
        logArea.append("═══ Smart Home Controller initialized ═══\n");
        logArea.append("   " + hub.getAllDevices().size() + " devices across "
                + hub.getRooms().size() + " rooms\n");
        logArea.append("   Patterns: Observer · Factory · Singleton · "
                + "Strategy · Command\n\n");
    }
}
