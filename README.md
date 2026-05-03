<div align="center">

# 🎮 CodeBattle Royale

**A High-Performance Distributed Multiplayer Quiz Engine**

[![Java](https://img.shields.io/badge/Java-11%2B-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/)
[![Swing](https://img.shields.io/badge/GUI-Swing-2C2255?style=for-the-badge)](https://docs.oracle.com/javase/tutorial/uiswing/)
[![TCP Sockets](https://img.shields.io/badge/Networking-TCP_Sockets-007396?style=for-the-badge)](https://docs.oracle.com/javase/tutorial/networking/sockets/index.html)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

CodeBattle Royale is a lightning-fast, real-time multiplayer trivia game built from the ground up in pure Java. Designed to demonstrate advanced distributed system concepts, it features a completely centralized authoritative server, real-time state broadcasting, and a sleek, responsive Swing-based client.

[Features](#-features) • [Architecture](#-architecture) • [Quick Start](#-quick-start) • [How to Play](#-how-to-play) 

</div>

---

## ✨ Features

- **Real-Time Multiplayer** — Zero-lag socket communication ensures all players stay in perfect sync.
- **Authoritative Server Architecture** — Game logic, timers, and scoring are handled server-side to prevent client spoofing or cheating.
- **Dynamic Mutual Exclusion Scoring** — Speed matters! The first player to answer correctly receives a massive `+150` point bonus, while subsequent correct answers receive `+100` points.
- **Smart Timer Synchronization** — A server-authoritative 15-second countdown timer that automatically skips ahead the exact moment all active players lock in their answers.
- **Glassmorphic UI** — A modern, dark-mode Swing interface featuring custom rendering, gradient color schemes, and real-time interactive components.
- **Live Leaderboards** — The game room scoreboard updates instantly across all connected clients the moment an event occurs.

---

## 🏗️ Architecture Under The Hood

CodeBattle Royale isn't just a game; it's a practical implementation of core distributed systems patterns:

* **Message Passing Protocol**: Custom `GameMessage` serialized objects flow over TCP streams, cleanly defining requests (JOIN, START, ANSWER) and broadcasts (QUESTION, TIMER, RESULT).
* **Concurrency Management**: The server handles an arbitrary number of clients using a `CopyOnWriteArrayList` and dedicated thread-per-client `Runnable` handlers.
* **State Consistency**: By enforcing `out.reset()` on the `ObjectOutputStream`, the engine bypasses Java's aggressive object caching, ensuring clients always render the absolute latest state without stale references.
* **Event Loop Synchronization**: All UI updates on the client side are strictly dispatched to the Event Dispatch Thread (EDT) via `SwingUtilities.invokeLater()`, guaranteeing thread-safe GUI rendering.

---

## 🚀 Quick Start

### Prerequisites
- **Java Development Kit (JDK) 11** or higher.
- A terminal or command prompt.

### Windows (One-Click Launch)
We've included a comprehensive Command Center script for Windows users.
1. Clone the repository and navigate to the directory.
2. Double-click `START_GAME.bat`.
3. Press `[4]` to launch the **Quick Play** environment (automatically compiles the project, starts the Server, and boots up 2 test Clients).

### MacOS / Linux (Manual Build)
Compile the source and boot up the distributed nodes manually:

```bash
# 1. Compile the Project
javac -encoding UTF-8 -d build src/com/codebattle/GameMessage.java src/com/codebattle/GameServer.java src/com/codebattle/GameClient.java

# 2. Start the Authoritative Server (Terminal 1)
java -cp build com.codebattle.GameServer

# 3. Start the Game Clients (Terminal 2, 3, etc.)
java -cp build com.codebattle.GameClient
```

---

## 🚀 Quick Demo Commands

For university presentations or quick tests, you do **not** need to memorize long `javac` commands. You can simply use these 1-click batch files from the terminal:

- `build.bat` — Instantly compiles the entire project into the `build` folder.
- `server.bat` — Starts the Game Server.
- `client.bat` — Starts the Game Client.

Alternatively, if you want to type the shortest possible manual commands:
- **Compile**: `javac -d build src/com/codebattle/*.java`
- **Run Server**: `java -cp build com.codebattle.GameServer`
- **Run Client**: `java -cp build com.codebattle.GameClient`

You can also just double-click `START_GAME.bat` and use the built-in Command Center!

---

## 🕹️ How to Play

1. **Host a Server**: One person needs to run the `GameServer` on their machine (or a cloud VPS).
2. **Join a Room**: Open the `GameClient`. A random 4-character **ROOM CODE** will be generated for you. Share this code with your friends so they can join your specific game room!
3. **Connect**: Enter your username, ensure the Room Code matches your friends', and hit **JOIN MATCH**.
4. **Start**: Once everyone is in the lobby, the host can click **START GAME (HOST)** to initiate the match.
5. **Battle**: You have 15 seconds per round. Read the question and click the correct option. Remember, the *first* person to get it right gets a massive speed bonus!
6. **Victory**: At the end of the 5-round battle, the player with the highest score is crowned the champion on the 3D Victory Podium.

---

<div align="center">
<i>Built with passion for high-performance distributed architecture.</i>
</div>
