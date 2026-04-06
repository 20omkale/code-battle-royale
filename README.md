# 🎮 CodeBattle Royale
### Distributed Multiplayer Programming Quiz Game
**SPPU BE IT | 414454 Lab Practice V | Distributed Systems Mini Project**

---

![Game Preview](https://img.shields.io/badge/Status-Ready_to_Play-00ff9d?style=for-the-badge)
![SPPU](https://img.shields.io/badge/SPPU-BE_IT_2019_Pattern-7b5ea7?style=for-the-badge)
![Course](https://img.shields.io/badge/Course-414450_Distributed_Systems-ff3d6b?style=for-the-badge)

---

## 📌 About

**CodeBattle Royale** is a real-time multiplayer competitive quiz game where players race to answer programming and distributed systems questions. Built as a distributed application demonstrating core concepts from the SPPU BE IT Distributed Systems syllabus (414450).

Players create/join rooms using a unique code, then compete in real-time — points are awarded based on correctness and speed.

---

## 🧠 Distributed Systems Concepts Implemented

| DS Concept | Implementation |
|---|---|
| **Message Passing** | Socket.io WebSocket events (`emit`/`on`) replace traditional send/receive primitives |
| **Clock Synchronization** | Server-authoritative countdown — server broadcasts timer ticks to all clients (Berkeley-style central server approach) |
| **Mutual Exclusion** | First correct answer locks the bonus points for that question — only one player can win the full points (Token-based exclusion) |
| **Distributed State Management** | Game rooms maintain isolated shared state; each room is a distributed process group |
| **Event-Driven RPC** | Socket.io events function as Remote Procedure Calls — `start_game`, `submit_answer`, `next_question` |
| **Distributed Transactions** | Score updates are atomic: server validates, updates state, and broadcasts in one transaction |
| **Fault Tolerance** | Host migration — if host disconnects, host role transfers to next player |
| **Room-based Process Groups** | Each game room = independent distributed process group with its own state |

---

## 🚀 Tech Stack

```
Frontend   → HTML5 + CSS3 + Vanilla JavaScript
Backend    → Node.js + Express.js
Real-time  → Socket.io (WebSockets)
Database   → In-memory (extendable to MongoDB)
Protocol   → WebSocket over HTTP/HTTPS
```

---

## 📁 Project Structure

```
codebattle-royale/
├── server.js              ← Node.js + Socket.io server (main backend)
├── package.json
├── public/
│   └── index.html         ← Frontend (single-page game)
├── codebattle-royale.html ← Standalone demo (no server needed)
└── README.md
```

---

## ⚙️ Setup & Run

### Prerequisites
- Node.js v18+
- npm

### Installation

```bash
# 1. Clone the repo
git clone https://github.com/YOUR_USERNAME/codebattle-royale.git
cd codebattle-royale

# 2. Install dependencies
npm install

# 3. Start server
node server.js

# 4. Open browser
# Go to http://localhost:3000
```

### Dependencies

```bash
npm install express socket.io cors
```

---

## 🎮 How to Play

1. **Player 1** opens the game → enters name → clicks **"Create New Room"**
2. A **Room Code** is generated (e.g., `ALPHA42`)
3. **Player 1** shares the code with friends
4. **Other Players** enter the room code → **Join Room**
5. Host clicks **Start Game** when all players are ready
6. Answer programming/DS questions as fast as possible!
7. **Scoring:**
   - ✅ Correct + First = Full points + Speed bonus
   - ✅ Correct + Late = 50% points
   - ❌ Wrong = 0 points
   - ⏰ No answer = 0 points (20s timer)

---

## 🏗️ Architecture Diagram

```
┌─────────────────────────────────────────────────────────┐
│                    CLIENTS (Players)                     │
│  [Browser 1]   [Browser 2]   [Browser 3]   [Browser 4]  │
│      │              │              │              │       │
└──────┼──────────────┼──────────────┼──────────────┼──────┘
       │              │    WebSocket │ (Socket.io)  │
       └──────────────┴──────┬───────┴──────────────┘
                             │
              ┌──────────────▼──────────────┐
              │    Node.js + Socket.io       │
              │         SERVER               │
              │  ┌────────────────────────┐ │
              │  │  Room Manager          │ │
              │  │  ┌──────┐ ┌──────┐    │ │
              │  │  │Room 1│ │Room 2│... │ │
              │  │  └──────┘ └──────┘    │ │
              │  └────────────────────────┘ │
              │  Clock Sync │ Mutex │ State  │
              └─────────────────────────────┘
```

---

## 📡 Socket.io Events (Message Passing API)

### Client → Server
| Event | Payload | Description |
|---|---|---|
| `create_room` | `{ name, avatar }` | Create a new game room |
| `join_room` | `{ code, name, avatar }` | Join existing room |
| `start_game` | `{ qCount }` | Host starts the game |
| `submit_answer` | `{ answerIdx, qIndex }` | Player submits answer |

### Server → Client
| Event | Payload | Description |
|---|---|---|
| `room_created` | `{ code, player }` | Room creation confirmed |
| `room_joined` | `{ code, players }` | Join confirmed + player list |
| `player_joined` | `{ players }` | New player joined broadcast |
| `game_started` | `{ questions, totalQ }` | Game begins with questions |
| `timer_tick` | `{ timeLeft, total }` | Clock sync tick (every 1s) |
| `answer_result` | `{ playerId, correct, points, scores }` | Answer processed + scores |
| `question_timeout` | `{ correctAnswer, scores }` | Timer ran out |
| `next_question` | `{ qIndex }` | Move to next question |
| `game_over` | `{ leaderboard, winner }` | Game ended + final scores |
| `player_left` | `{ players }` | Player disconnected |
| `host_transfer` | `{}` | You are now the host |

---

## 🧪 Testing Multiplayer

### Option 1: Multiple Browsers/Tabs
- Open `http://localhost:3000` in **Tab 1** → Create Room
- Open `http://localhost:3000` in **Tab 2** → Join with code
- Open `http://localhost:3000` in **Tab 3** → Join with code
- Play!

### Option 2: Standalone Demo (No Server)
- Open `codebattle-royale.html` directly in browser
- Click **"Add Bot Players"** to simulate other players
- Uses BroadcastChannel API for cross-tab communication

### Option 3: Network Multiplayer
```bash
# Find your IP
ipconfig (Windows) / ifconfig (Mac/Linux)

# Share with friends on same network
http://YOUR_IP:3000
```

---

## 👥 Team & Contribution

| Member | Role | Tasks |
|---|---|---|
| Member 1 | Backend Lead | server.js, Socket.io events, room management |
| Member 2 | Frontend Lead | UI/UX, game screens, client socket integration |
| Member 3 | Game Logic | Timer sync, scoring, mutual exclusion logic |
| Member 4 | QA + Docs | Testing, README, viva preparation, deployment |

---


## 📜 License
MIT License — Free to use, modify, and distribute.

---

*Built with ❤️ for SPPU BE IT Distributed Systems Mini Project*
