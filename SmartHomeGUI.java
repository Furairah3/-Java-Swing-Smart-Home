package smarthome;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * SmartHomeGUI — Main Application Window
 *
 * DESIGN PATTERN: Observer (Concrete Observer)
 *   Implements DeviceObserver so it receives push-model DeviceEvent objects
 *   from every SmartDevice in the system.  The log area and device buttons
 *   update automatically — no device ever calls a GUI method directly.
 *
 * Layout overview:
 * ┌──────────────────────────────────────────────────┐
 * │  Toolbar: [Undo] [Redo] │ [Night Mode] [Vacation]│  ← top
 * ├──────────────────────────┬───────────────────────┤
 * │  Room Panels (scrollable)│  Event Log (JTextArea)│  ← center
 * │  - TitledBorder per room │  - auto-scroll        │
 * │  - Master Switch toggle  │  - emoji indicators   │
 * │  - Device toggle buttons │                       │
 * ├──────────────────────────┴───────────────────────┤
 * │  Add Device Panel: [Type ▾] [Name] [Room] [Add]  │  ← bottom
 * └──────────────────────────────────────────────────┘
 */
public class SmartHomeGUI extends JFrame implements DeviceObserver {

    /* ── Colour palette (used throughout for consistency) ───────────── */
    private static final Color CLR_BG        = new Color(30, 33, 40);
    private static final Color CLR_PANEL     = new Color(40, 44, 52);
    private static final Color CLR_CARD      = new Color(50, 55, 65);
    private static final Color CLR_ACCENT    = new Color(80, 160, 255);
    private static final Color CLR_ON        = new Color(72, 199, 142);
    private static final Color CLR_OFF       = new Color(180, 80, 80);
    private static final Color CLR_TEXT      = new Color(220, 225, 235);
    private static final Color CLR_TEXT_DIM  = new Color(140, 148, 165);
    private static final Color CLR_TOOLBAR   = new Color(35, 38, 46);

    /* ── Core references ───────────────────────────────────────────── */
    private final SmartHomeHub hub = SmartHomeHub.getInstance();

    /* ── GUI components ────────────────────────────────────────────── */
    private JTextArea   logArea;
    private JPanel      roomsContainer;   // holds all room panels
    private JButton     undoBtn, redoBtn;

    /** Maps a SmartDevice → its toggle button in the GUI for live updates. */
    private final Map<SmartDevice, JToggleButton> deviceButtons = new LinkedHashMap<>();

    /** Maps a Room name → its master-switch toggle. */
    private final Map<String, JToggleButton> masterSwitches = new LinkedHashMap<>();

    /* ── Constructor ───────────────────────────────────────────────── */

    public SmartHomeGUI() {
        super("Smart Home Controller");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 680);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);

        // Save preferences on close
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                hub.savePreferences();
            }
        });

        initComponents();
        populateDemoDevices();
        hub.loadPreferences();
        refreshAllButtons();   // sync button labels with restored state
    }

    /* ================================================================
     *  GUI CONSTRUCTION
     * ================================================================ */

    private void initComponents() {
        // Root content pane
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(CLR_BG);
        setContentPane(root);

        root.add(buildToolbar(),     BorderLayout.NORTH);
        root.add(buildCenterSplit(), BorderLayout.CENTER);
        root.add(buildAddPanel(),    BorderLayout.SOUTH);
    }

    /* ── Toolbar ───────────────────────────────────────────────────── */

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        bar.setBackground(CLR_TOOLBAR);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CLR_CARD));

        // ── Undo / Redo ──
        undoBtn = toolbarButton("↩ Undo");
        redoBtn = toolbarButton("↪ Redo");
        undoBtn.addActionListener(e -> { hub.getCommandManager().undo(); refreshAfterCommand(); });
        redoBtn.addActionListener(e -> { hub.getCommandManager().redo(); refreshAfterCommand(); });
        bar.add(undoBtn);
        bar.add(redoBtn);

        bar.add(toolbarSeparator());

        // ── Automation strategy buttons ──
        hub.registerStrategy(new NightModeStrategy());
        hub.registerStrategy(new VacationModeStrategy());

        for (AutomationStrategy s : hub.getStrategies()) {
            JButton btn = toolbarButton("\uD83C\uDF19 " + s.getName());
            if (s.getName().contains("Vacation")) {
                btn.setText("✈ " + s.getName());
            }
            btn.addActionListener(e -> {
                hub.runAutomation(s);
                refreshAfterCommand();
            });
            bar.add(btn);
        }

        // ── Clear Log ──
        bar.add(toolbarSeparator());
        JButton clearBtn = toolbarButton("Clear Log");
        clearBtn.addActionListener(e -> logArea.setText(""));
        bar.add(clearBtn);

        return bar;
    }

    /** Factory helper for consistent toolbar buttons. */
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

    private JSeparator toolbarSeparator() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 28));
        sep.setForeground(CLR_TEXT_DIM);
        return sep;
    }

    /* ── Center: rooms + log split ─────────────────────────────────── */

    private JSplitPane buildCenterSplit() {
        // Left: scrollable room panels
        roomsContainer = new JPanel();
        roomsContainer.setLayout(new BoxLayout(roomsContainer, BoxLayout.Y_AXIS));
        roomsContainer.setBackground(CLR_BG);

        JScrollPane roomScroll = new JScrollPane(roomsContainer);
        roomScroll.setBorder(BorderFactory.createEmptyBorder());
        roomScroll.getViewport().setBackground(CLR_BG);
        roomScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Right: event log
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logArea.setBackground(CLR_PANEL);
        logArea.setForeground(CLR_TEXT);
        logArea.setCaretColor(CLR_ACCENT);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setMargin(new Insets(8, 10, 8, 10));

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CLR_TEXT_DIM),
                " Event Log ", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13), CLR_ACCENT));
        logScroll.getViewport().setBackground(CLR_PANEL);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, roomScroll, logScroll);
        split.setDividerLocation(580);
        split.setResizeWeight(0.6);
        split.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        split.setBackground(CLR_BG);
        return split;
    }

    /* ── Bottom: Add Device panel ──────────────────────────────────── */

    private JPanel buildAddPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panel.setBackground(CLR_TOOLBAR);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, CLR_CARD));

        // Type combo — populated dynamically from DeviceFactory
        JComboBox<String> typeCombo = new JComboBox<>(
                DeviceFactory.getAvailableTypes().toArray(new String[0]));
        styleCombo(typeCombo);

        // Name field
        JTextField nameField = new JTextField(14);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nameField.setBackground(CLR_CARD);
        nameField.setForeground(CLR_TEXT);
        nameField.setCaretColor(CLR_TEXT);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CLR_TEXT_DIM),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));

        // Room combo — editable so user can type a new room
        JComboBox<String> roomCombo = new JComboBox<>();
        roomCombo.setEditable(true);
        styleCombo(roomCombo);

        // Add button
        JButton addBtn = toolbarButton("+ Add Device");
        addBtn.setBackground(CLR_ACCENT);
        addBtn.setForeground(Color.WHITE);

        addBtn.addActionListener(e -> {
            String type = (String) typeCombo.getSelectedItem();
            String name = nameField.getText().trim();
            String room = ((String) roomCombo.getSelectedItem());
            if (room != null) room = room.trim();

            if (name.isEmpty() || room == null || room.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a device name and room.",
                        "Missing Input", JOptionPane.WARNING_MESSAGE);
                return;
            }

            SmartDevice device = hub.addDevice(type, name, room);
            device.addObserver(this);
            rebuildRoomPanels();
            nameField.setText("");
            refreshRoomCombo(roomCombo);
        });

        JLabel lbl1 = new JLabel("Type:");  lbl1.setForeground(CLR_TEXT_DIM);
        JLabel lbl2 = new JLabel("Name:");  lbl2.setForeground(CLR_TEXT_DIM);
        JLabel lbl3 = new JLabel("Room:");  lbl3.setForeground(CLR_TEXT_DIM);

        panel.add(lbl1); panel.add(typeCombo);
        panel.add(lbl2); panel.add(nameField);
        panel.add(lbl3); panel.add(roomCombo);
        panel.add(addBtn);

        // Initial room list population deferred until demo devices exist
        SwingUtilities.invokeLater(() -> refreshRoomCombo(roomCombo));

        return panel;
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(CLR_CARD);
        combo.setForeground(CLR_TEXT);
    }

    private void refreshRoomCombo(JComboBox<String> combo) {
        String current = (String) combo.getSelectedItem();
        combo.removeAllItems();
        for (String rn : hub.getRoomNames()) combo.addItem(rn);
        if (current != null && !current.isEmpty()) combo.setSelectedItem(current);
    }

    /* ================================================================
     *  ROOM PANEL CONSTRUCTION
     * ================================================================ */

    /** Rebuild all room panels from scratch. Called on add/remove. */
    private void rebuildRoomPanels() {
        roomsContainer.removeAll();
        deviceButtons.clear();
        masterSwitches.clear();

        for (Room room : hub.getRooms()) {
            roomsContainer.add(buildRoomPanel(room));
            roomsContainer.add(Box.createVerticalStrut(6));
        }
        roomsContainer.revalidate();
        roomsContainer.repaint();
    }

    /** Builds a single room panel with TitledBorder, master switch, device buttons. */
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

        // ── Master Switch (JToggleButton) ──
        JToggleButton master = new JToggleButton("Master Switch: OFF");
        master.setFont(new Font("Segoe UI", Font.BOLD, 12));
        master.setFocusPainted(false);
        master.setBackground(CLR_OFF);
        master.setForeground(Color.WHITE);
        master.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        master.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));

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

        // ── Device buttons grid ──
        List<SmartDevice> devices = room.getDevices();
        int cols = Math.max(2, Math.min(4, devices.size()));
        JPanel grid = new JPanel(new GridLayout(0, cols, 8, 8));
        grid.setOpaque(false);

        for (SmartDevice device : devices) {
            JToggleButton btn = createDeviceButton(device);
            deviceButtons.put(device, btn);
            grid.add(btn);
        }

        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    /** Creates a styled toggle button for a single device. */
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

        btn.addActionListener(e -> {
            // Revert the toggle button's visual state — the Command/Observer
            // cycle will set it correctly after the state change propagates.
            btn.setSelected(device.isOn());
            ToggleDeviceCommand cmd = new ToggleDeviceCommand(device);
            hub.executeCommand(cmd);
            refreshAfterCommand();
        });

        return btn;
    }

    /* ================================================================
     *  OBSERVER CALLBACK (Push Model)
     * ================================================================ */

    /**
     * Called by every SmartDevice when its state changes.
     * This is the Observer pattern in action — the GUI never polls;
     * it reacts to events pushed from the model layer.
     */
    @Override
    public void onDeviceEvent(DeviceEvent event) {
        // Swing thread safety: all GUI updates on the EDT
        SwingUtilities.invokeLater(() -> {
            // 1) Append to log
            logArea.append(event.toLogString() + "\n");
            // Auto-scroll to bottom
            logArea.setCaretPosition(logArea.getDocument().getLength());

            // 2) Update the specific device button
            SmartDevice source = findDevice(event.getDeviceName(), event.getRoomName());
            if (source != null) {
                JToggleButton btn = deviceButtons.get(source);
                if (btn != null) {
                    syncButtonToDevice(btn, source);
                }
            }

            // 3) Update master switch for the room
            updateMasterSwitch(event.getRoomName());

            // 4) Update undo/redo button states
            updateUndoRedoButtons();
        });
    }

    /* ================================================================
     *  GUI REFRESH HELPERS
     * ================================================================ */

    /** After any command execution, refresh all button states. */
    private void refreshAfterCommand() {
        SwingUtilities.invokeLater(() -> {
            refreshAllButtons();
            updateUndoRedoButtons();
        });
    }

    /** Sync every device button and master switch with current model state. */
    private void refreshAllButtons() {
        for (Map.Entry<SmartDevice, JToggleButton> e : deviceButtons.entrySet()) {
            syncButtonToDevice(e.getValue(), e.getKey());
        }
        for (String roomName : masterSwitches.keySet()) {
            updateMasterSwitch(roomName);
        }
    }

    /** Sync one button's label, selection, and border colour to the device. */
    private void syncButtonToDevice(JToggleButton btn, SmartDevice device) {
        btn.setSelected(device.isOn());
        btn.setText(device.getDisplayLabel());
        Color borderClr = device.isOn() ? CLR_ON : CLR_OFF;
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderClr, 2, true),
                BorderFactory.createEmptyBorder(10, 8, 10, 8)));
    }

    /** Update a room's master switch label and colour. */
    private void updateMasterSwitch(String roomName) {
        JToggleButton master = masterSwitches.get(roomName);
        Room room = hub.getRoom(roomName);
        if (master == null || room == null) return;

        boolean anyOn = room.hasActiveDevice();
        master.setSelected(anyOn);
        master.setText("Master Switch: " + (anyOn ? "ON" : "OFF"));
        master.setBackground(anyOn ? CLR_ON : CLR_OFF);
    }

    /** Update undo/redo button enabled state and tooltips. */
    private void updateUndoRedoButtons() {
        CommandManager cm = hub.getCommandManager();
        undoBtn.setEnabled(cm.canUndo());
        redoBtn.setEnabled(cm.canRedo());
        undoBtn.setToolTipText(cm.canUndo() ? "Undo: " + cm.peekUndoDescription() : "Nothing to undo");
        redoBtn.setToolTipText(cm.canRedo() ? "Redo: " + cm.peekRedoDescription() : "Nothing to redo");
    }

    /** Find a device by name + room (used in observer callback). */
    private SmartDevice findDevice(String name, String roomName) {
        Room room = hub.getRoom(roomName);
        if (room == null) return null;
        for (SmartDevice d : room.getDevices()) {
            if (d.getName().equals(name)) return d;
        }
        return null;
    }

    /* ================================================================
     *  DEMO DATA POPULATION
     * ================================================================ */

    /**
     * Pre-populates the system with sample devices so the user sees a
     * functional interface immediately on launch.
     *
     * All devices are created through {@link DeviceFactory} (Factory Pattern)
     * and registered with this GUI as their observer (Observer Pattern).
     */
    private void populateDemoDevices() {
        String[][] demo = {
            // { type,        name,             room           }
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

        for (String[] spec : demo) {
            SmartDevice device = hub.addDevice(spec[0], spec[1], spec[2]);
            device.addObserver(this);   // GUI listens to every device
        }

        rebuildRoomPanels();
        logArea.append("═══ Smart Home Controller initialized ═══\n");
        logArea.append("   " + hub.getAllDevices().size() + " devices across "
                + hub.getRooms().size() + " rooms\n");
        logArea.append("   Patterns: Observer · Factory · Singleton · "
                + "Strategy · Command\n\n");
    }
}
