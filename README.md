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

I used six classic design patterns in this project. Here is what each one does
and where I put it in the code.

**1. Observer** — I implemented this between `SmartDevice` and `DeviceObserver`.
Devices push events to anything that's listening, so the GUI updates by itself
without ever polling.

**2. Factory** — I built this with `DeviceFactory` and `DeviceRegistry`.
It creates devices using reflection, so I never had to write big if/else chains
to pick which class to instantiate.

**3. Singleton** — I used the Bill Pugh style in `SmartHomeHub`. There is only
one hub for the whole app and it's both thread-safe and lazy-loaded.

**4. Strategy** — I made this with the `AutomationStrategy` interface and its
subclasses. Each automation mode (Night Mode, Vacation Mode) is its own swappable
algorithm.

**5. Command** — I built this with the `Command` interface and `CommandManager`.
Every user action becomes a command object so I can undo and redo it later.

**6. Memento** — I tucked this inside the Command classes. Each command takes a
snapshot of the device states before changing anything, so undo can put things
back exactly how they were.

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

All my Java source files live in `src/smarthome/`.

- `Main.java` — the entry point. I set up the Nimbus look and feel here and launch the GUI.
- `SmartHomeGUI.java` — the main JFrame window. It also implements the Observer interface so it can react to device changes.
- `SmartHomeHub.java` — the Bill Pugh Singleton that coordinates the whole app.
- `SmartDevice.java` — the abstract base class that plays the Subject role in the Observer pattern.
- `Light.java` — a concrete device for light bulbs.
- `Thermostat.java` — a concrete device for AC units.
- `DoorLock.java` — a concrete device for door locks.
- `DeviceEvent.java` — the immutable event object I push through the observer chain.
- `DeviceObserver.java` — the Observer interface anything can implement to listen to device changes.
- `Room.java` — groups devices together and provides a master switch.
- `DeviceFactory.java` — the simple facade I call to make new devices.
- `DeviceRegistry.java` — the reflection-based registry that holds the device type map.
- `AutomationStrategy.java` — the Strategy interface for automation modes.
- `NightModeStrategy.java` — a concrete strategy that turns off lights and locks doors.
- `VacationModeStrategy.java` — a concrete strategy that turns off thermostats and locks doors.
- `Command.java` — the Command interface that every undoable action implements.
- `ToggleDeviceCommand.java` — the command for flipping one single device.
- `RoomToggleCommand.java` — the command for flipping a whole room at once.
- `AutomationCommand.java` — the command that wraps an automation strategy so it can be undone.
- `CommandManager.java` — manages the undo and redo stacks.

I also have a few extra files in the project root:

- `class-diagram.mermaid` — the UML class diagram, viewable at mermaid.live.
- `build-and-run.sh` — a one-click script that compiles and launches the app.
- `build.gradle` — the optional Gradle build file.
