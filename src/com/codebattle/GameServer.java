package com.codebattle;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * CodeBattle Royale — Distributed Game Server
 * SPPU BE IT | Lab Practice V | Distributed Systems Mini Project
 *
 * Concepts demonstrated:
 *  - Message Passing (TCP Object Serialization)
 *  - Mutual Exclusion (first-correct-answer bonus)
 *  - Clock Synchronization (server-authoritative timer)
 *  - Distributed State Management (centralized game room)
 */
public class GameServer {
    static final int PORT = 5000;
    static List<Handler> players = new CopyOnWriteArrayList<>();
    static String[] names;
    static int[] scores;
    static boolean gameRunning = false;

    // ── Question Bank ────────────────────────────────────────────────────────
    static String[][] QUESTIONS = {
        {"Which algorithm is used for leader election?", "Dijkstra", "Bully", "Prim", "Kruskal", "1"},
        {"RPC stands for?", "Remote Procedure Call", "Rapid Protocol", "Resource Process", "Remote Process", "0"},
        {"CAP theorem covers?", "Consistency,Availability,Partition Tolerance", "CPU,Async,Performance", "Cache,API,Protocol", "Concurrency,Atomicity,Persistence", "0"},
        {"Lamport timestamps are used to?", "Sync physical clocks", "Order events logically", "Detect partitions", "Balance load", "1"},
        {"Mutual exclusion ensures?", "All processes run together", "Only one enters critical section", "Equal CPU time", "No deadlocks ever", "1"},
        {"Berkeley algorithm is for?", "Routing", "Clock synchronization", "Encryption", "Load balancing", "1"},
        {"Which OSI layer routes packets?", "Data Link", "Transport", "Network", "Session", "2"},
        {"TCP guarantees delivery using?", "Checksums only", "ACK and retransmit", "Broadcast", "UDP fallback", "1"},
        {"Token ring mutex works by?", "Broadcasting", "Only token holder enters CS", "Priority queue", "Random selection", "1"},
        {"What does typeof null return in JS?", "null", "undefined", "object", "boolean", "2"},
    };

    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(PORT);
        System.out.println("=== CodeBattle Royale Server on port " + PORT + " ===");
        System.out.println("Waiting for players to join...");

        while (true) {
            Socket sock = server.accept();
            Handler h = new Handler(sock);
            new Thread(h).start();
        }
    }

    // ── Broadcast to all players ─────────────────────────────────────────────
    static void broadcast(GameMessage msg) {
        for (Handler h : players) h.send(msg);
    }

    static void sendPlayerList() {
        names  = new String[players.size()];
        scores = new int[players.size()];
        for (int i = 0; i < players.size(); i++) {
            names[i]  = players.get(i).name;
            scores[i] = players.get(i).score;
        }
        GameMessage msg = new GameMessage(GameMessage.Type.PLAYER_LIST);
        msg.names  = names;
        msg.scores = scores;
        broadcast(msg);
    }

    static volatile int currentCorrect = -1;  // server knows the correct answer
    static volatile boolean firstCorrectTaken = false;

    // ── Game Loop (runs on server thread — authoritative timer) ──────────────
    static void startGame() {
        if (gameRunning) return;
        gameRunning = true;

        new Thread(() -> {
            try {
                // Shuffle and pick 5 questions
                List<String[]> qs = new ArrayList<>(Arrays.asList(QUESTIONS));
                Collections.shuffle(qs);
                int total = Math.min(5, qs.size());

                for (int q = 0; q < total; q++) {
                    String[] data = qs.get(q);

                    // Reset per-round state
                    for (Handler h : players) h.answered = false;
                    currentCorrect = Integer.parseInt(data[5]);
                    firstCorrectTaken = false;

                    // Send question to all
                    GameMessage qm = new GameMessage(GameMessage.Type.QUESTION);
                    qm.text    = data[0];
                    qm.options = new String[]{data[1], data[2], data[3], data[4]};
                    qm.number  = q + 1;
                    broadcast(qm);

                    // Timer countdown: 15 seconds
                    for (int t = 15; t >= 0; t--) {
                        GameMessage tm = new GameMessage(GameMessage.Type.TIMER);
                        tm.number = t;
                        broadcast(tm);

                        if (t > 0) {
                            Thread.sleep(1000);
                            // Smart skip: if all answered, break early
                            boolean allDone = true;
                            for (Handler h : players) if (!h.answered) allDone = false;
                            if (allDone) {
                                GameMessage skip = new GameMessage(GameMessage.Type.TIMER);
                                skip.number = 0;
                                broadcast(skip);
                                break;
                            }
                        }
                    }

                    // Send result with correct answer
                    GameMessage rm = new GameMessage(GameMessage.Type.RESULT);
                    rm.number = currentCorrect;
                    rm.text = "Correct answer: " + data[currentCorrect + 1];
                    sendPlayerList();  // Update leaderboard
                    broadcast(rm);

                    Thread.sleep(3000); // Pause between questions
                }

                // Game over!
                sendPlayerList();
                GameMessage gm = new GameMessage(GameMessage.Type.GAMEOVER);
                int best = 0; String winner = "";
                for (Handler h : players) {
                    if (h.score > best) { best = h.score; winner = h.name; }
                }
                gm.text = winner + " wins with " + best + " points!";
                broadcast(gm);
                gameRunning = false;

            } catch (Exception e) { e.printStackTrace(); gameRunning = false; }
        }).start();
    }

    static void broadcastEvent(String text) {
        GameMessage m = new GameMessage(GameMessage.Type.EVENT);
        m.text = text;
        broadcast(m);
    }

    // ── Per-Client Handler ───────────────────────────────────────────────────
    static class Handler implements Runnable {
        Socket sock;
        ObjectOutputStream out;
        ObjectInputStream in;
        String name = "";
        int score = 0;
        boolean answered = false;

        Handler(Socket s) { this.sock = s; }

        public void run() {
            try {
                out = new ObjectOutputStream(sock.getOutputStream());
                out.flush();
                in  = new ObjectInputStream(sock.getInputStream());

                // Send welcome
                GameMessage wm = new GameMessage(GameMessage.Type.WELCOME);
                wm.text = "Connected! Enter your name and wait for host to start.";
                send(wm);

                while (true) {
                    GameMessage msg = (GameMessage) in.readObject();

                    switch (msg.type) {
                        case JOIN:
                            name = msg.text;
                            players.add(this);
                            System.out.println("[+] " + name + " joined (" + players.size() + " players)");
                            broadcastEvent("🔥 " + name + " dropped into the lobby!");
                            sendPlayerList();
                            break;

                        case START:
                            if (players.size() >= 1) {
                                broadcastEvent("🎮 Match started by " + name + "!");
                                startGame();
                            }
                            break;

                        case ANSWER:
                            if (!answered && gameRunning) {
                                answered = true;
                                int choice = msg.number;  // client sends selected index
                                // Server validates answer (server-authoritative)
                                if (choice == currentCorrect) {
                                    // Mutual Exclusion: first correct gets bonus
                                    if (!firstCorrectTaken) {
                                        firstCorrectTaken = true;
                                        score += 150;  // First correct: 150 pts
                                        broadcastEvent("⚡ FIRST STRIKE! " + name + " answered perfectly! (+150)");
                                    } else {
                                        score += 100;  // Late correct: 100 pts
                                        broadcastEvent("✔️ " + name + " got it right! (+100)");
                                    }
                                } else {
                                    broadcastEvent("💀 " + name + " missed the shot...");
                                }
                                sendPlayerList(); // Real-time leaderboard update
                            }
                            break;

                        default: break;
                    }
                }
            } catch (Exception e) {
                players.remove(this);
                broadcastEvent("🚪 " + name + " disconnected.");
                if (!players.isEmpty()) sendPlayerList();
            }
        }

        synchronized void send(GameMessage msg) {
            try {
                out.reset();  // CRITICAL: prevents Java from caching old objects
                out.writeObject(msg);
                out.flush();
            } catch (Exception e) {}
        }
    }
}
