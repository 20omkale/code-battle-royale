# CodeBattle Royale
### Distributed Multiplayer Quiz Game
**SPPU BE IT | 414454 Lab Practice V | Distributed Systems Mini Project**

---

## About
A real-time multiplayer quiz game where players compete to answer distributed systems questions. Built using Java TCP Sockets demonstrating core distributed computing concepts.

## Distributed Systems Concepts Demonstrated

| Concept | How It's Implemented |
|---|---|
| **Message Passing** | TCP ObjectStreams with custom `GameMessage` protocol |
| **Mutual Exclusion** | First correct answer gets +150 pts, late correct gets +100 pts |
| **Clock Synchronization** | Server-authoritative 15-second countdown timer |
| **Distributed State** | Centralized game room with broadcast score updates |
| **Smart Timer Skip** | Server detects when all clients answered, skips remaining time |

## Project Structure
```
codebattle-royale/
├── src/com/codebattle/
│   ├── GameMessage.java    ← Shared message protocol (30 lines)
│   ├── GameServer.java     ← Server + game logic + questions (220 lines)
│   └── GameClient.java     ← Client + Swing GUI (290 lines)
├── build/                  ← Compiled classes
└── START_GAME.bat          ← One-click launcher
```

## How to Run

### Quick Start
1. Double-click `START_GAME.bat`
2. Select `[4]` for Quick Play (compiles + launches server + 2 clients)
3. Enter your name in each client and click "JOIN SERVER"
4. Click "START GAME" in either client
5. Answer questions — first correct answer gets bonus points!

### Manual
```bash
# Compile
javac -encoding UTF-8 -d build src/com/codebattle/GameMessage.java src/com/codebattle/GameServer.java src/com/codebattle/GameClient.java

# Terminal 1: Start Server
java -cp build com.codebattle.GameServer

# Terminal 2+3: Start Clients
java -cp build com.codebattle.GameClient
```

## Game Features
- **10 questions** covering DS, Networking, OS, and Coding
- **15-second timer** per question with live countdown
- **Live scoreboard** updates in real-time after every answer
- **Speed bonus** — first correct answer gets 150pts, later correct gets 100pts
- **Smart skip** — timer skips when everyone has answered
- **Game Over** screen with final scores and winner

---
*Built by Om Kale — SPPU BE IT 2019 Pattern*
