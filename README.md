# Smart Home Controller — CS 415 Reference Implementation

## Quick Start (No Build Tool Required)

```bash
chmod +x build-and-run.sh
./build-and-run.sh
```

**Or manually:**
```bash
mkdir -p out
javac -d out/ src/smarthome/*.java
java -cp out/ smarthome.Main
```

## Optional: Gradle Build

```bash
gradle run
```

See `build.gradle` for FlatLaf dependency (optional modern L&F).

---

## Design Patterns Implemented (6 Total)

| # | Pattern      | Where                                | Purpose                                         |
|---|-------------|--------------------------------------|--------------------------------------------------|
| 1 | **Observer** | `SmartDevice` → `DeviceObserver`     | Decoupled event-driven UI updates (push model)   |
| 2 | **Factory**  | `DeviceFactory` + `DeviceRegistry`   | Reflection-based creation; no if/else chains     |
| 3 | **Singleton**| `SmartHomeHub` (Bill Pugh)           | Single coordinator; thread-safe, lazy init       |
| 4 | **Strategy** | `AutomationStrategy` hierarchy       | Swappable automation algorithms                  |
| 5 | **Command**  | `Command` + `CommandManager`         | Undo/redo with memento-style state snapshots     |
| 6 | **Memento**  | State snapshots inside Commands      | Capture & restore device states for rollback     |

---

## Architectural Justification

The **Reflection-Driven Factory** eliminates the fragile switch/case antipattern
that plagues student projects. Adding a new device type (e.g., `SecurityCamera`)
requires creating one new class file and adding a single registration line — zero
modifications to the Factory, GUI, or Hub. This directly satisfies the Open/Closed
Principle and mirrors how production IoT platforms handle extensibility.

The **Command + Memento** undo system transforms throwaway toggle buttons into
a fully reversible operation history. Each Command captures a state snapshot
(memento) before execution, enabling precise rollback even for bulk operations
like room master switches and automation modes. This pattern combination is the
same architecture used by professional applications (IDEs, Photoshop, databases)
for transactional undo, and demonstrates that the student understands the
difference between toy code and production-grade architecture.

Together, these patterns create a system where **every axis of change is
isolated**: new devices extend without modifying existing code (Factory),
new automation modes plug in without touching the Hub (Strategy), the GUI
reacts without polling or coupling (Observer), and every user action is
safely reversible (Command/Memento).

---

## File Inventory

```
src/smarthome/
├── Main.java                 Entry point, Nimbus L&F setup
├── SmartHomeGUI.java         JFrame, Observer implementation, full UI
├── SmartHomeHub.java         Bill Pugh Singleton, central coordinator
├── SmartDevice.java          Abstract Subject (Observer pattern)
├── Light.java                Concrete device
├── Thermostat.java           Concrete device
├── DoorLock.java             Concrete device
├── DeviceEvent.java          Push-model event payload
├── DeviceObserver.java       Observer interface
├── Room.java                 Device grouping with master switch
├── DeviceFactory.java        Factory facade
├── DeviceRegistry.java       Reflection-based type registry
├── AutomationStrategy.java   Strategy interface
├── NightModeStrategy.java    Concrete strategy
├── VacationModeStrategy.java Concrete strategy
├── Command.java              Command interface
├── ToggleDeviceCommand.java  Single-device command
├── RoomToggleCommand.java    Room bulk command
├── AutomationCommand.java    Automation command
└── CommandManager.java       Undo/redo stack manager

class-diagram.mermaid          UML class diagram (view at mermaid.live)
build-and-run.sh               One-click compile & launch
build.gradle                   Optional Gradle build file
```
