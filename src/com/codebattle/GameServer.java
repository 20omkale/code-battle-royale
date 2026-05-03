package com.codebattle;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * CodeBattle Royale — Distributed Game Server
 * SPPU BE IT | Distributed Systems Mini Project
 */
public class GameServer {
    static final int PORT = 5000;
    
    // Maps room codes to actual Room objects (Multi-room support)
    static ConcurrentHashMap<String, Room> activeRooms = new ConcurrentHashMap<>();

    // ── Question Bank [Question, OptA, OptB, OptC, OptD, CorrectIdx, Difficulty]
    static String[][] QUESTIONS = {
        // Easy
        {"What does HTML stand for?", "Hyper Text Markup Language", "High Tech Main Loop", "Home Tool Markup", "Hyperlink Text Module", "0", "Easy"},
        {"Which symbol is used for comments in Python?", "//", "/*", "#", "<!--", "2", "Easy"},
        {"What does CSS do?", "Adds logic", "Styles web pages", "Queries databases", "Compiles code", "1", "Easy"},
        {"Which operator is used for assignment in Java?", "==", "=", "===", "=>", "1", "Easy"},
        {"What is the boolean opposite of true?", "null", "undefined", "false", "0", "2", "Easy"},
        
        // Medium
        {"Which algorithm is used for leader election?", "Dijkstra", "Bully", "Prim", "Kruskal", "1", "Medium"},
        {"RPC stands for?", "Remote Procedure Call", "Rapid Protocol", "Resource Process", "Remote Process", "0", "Medium"},
        {"What does typeof null return in JS?", "null", "undefined", "object", "boolean", "2", "Medium"},
        {"Which OSI layer routes packets?", "Data Link", "Transport", "Network", "Session", "2", "Medium"},
        {"TCP guarantees delivery using?", "Checksums only", "ACK and retransmit", "Broadcast", "UDP fallback", "1", "Medium"},
        
        // Hard
        {"CAP theorem covers?", "Consistency,Availability,Partition Tolerance", "CPU,Async,Performance", "Cache,API,Protocol", "Concurrency,Atomicity,Persistence", "0", "Hard"},
        {"Lamport timestamps are used to?", "Sync physical clocks", "Order events logically", "Detect partitions", "Balance load", "1", "Hard"},
        {"Berkeley algorithm is for?", "Routing", "Clock synchronization", "Encryption", "Load balancing", "1", "Hard"},
        {"Token ring mutex works by?", "Broadcasting", "Only token holder enters CS", "Priority queue", "Random selection", "1", "Hard"},
        {"Mutual exclusion ensures?", "All processes run together", "Only one enters critical section", "Equal CPU time", "No deadlocks ever", "1", "Hard"}
    };

    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(PORT);
        System.out.println("=== CodeBattle Royale Server on port " + PORT + " ===");
        System.out.println("Waiting for players...");

        while (true) {
            Socket sock = server.accept();
            new Thread(new Handler(sock)).start();
        }
    }

    // ── Game Room Logic ──────────────────────────────────────────────────────
    static class Room {
        String code;
        List<Handler> players = new CopyOnWriteArrayList<>();
        boolean gameRunning = false;
        volatile int currentCorrect = -1;
        volatile boolean firstCorrectTaken = false;

        Room(String code) { this.code = code; }

        void broadcast(GameMessage msg) {
            for (Handler h : players) h.send(msg);
        }

        void broadcastEvent(String text) {
            GameMessage m = new GameMessage(GameMessage.Type.EVENT);
            m.text = text;
            broadcast(m);
        }

        void sendPlayerList() {
            String[] names = new String[players.size()];
            int[] scores = new int[players.size()];
            for (int i = 0; i < players.size(); i++) {
                names[i] = players.get(i).name;
                scores[i] = players.get(i).score;
            }
            GameMessage msg = new GameMessage(GameMessage.Type.PLAYER_LIST);
            msg.names = names;
            msg.scores = scores;
            broadcast(msg);
        }

        void startGame(int maxRounds, String difficulty) {
            if (gameRunning) return;
            gameRunning = true;

            new Thread(() -> {
                try {
                    List<String[]> qs = new ArrayList<>();
                    for (String[] q : QUESTIONS) {
                        if (difficulty.equals("Mixed") || q[6].equalsIgnoreCase(difficulty)) {
                            qs.add(q);
                        }
                    }
                    Collections.shuffle(qs);
                    int total = Math.min(maxRounds, qs.size());

                    for (int q = 0; q < total; q++) {
                        String[] data = qs.get(q);

                        for (Handler h : players) h.answered = false;
                        currentCorrect = Integer.parseInt(data[5]);
                        firstCorrectTaken = false;

                        GameMessage qm = new GameMessage(GameMessage.Type.QUESTION);
                        qm.text = data[0];
                        qm.options = new String[]{data[1], data[2], data[3], data[4]};
                        qm.number = q + 1;
                        broadcast(qm);

                        for (int t = 15; t >= 0; t--) {
                            GameMessage tm = new GameMessage(GameMessage.Type.TIMER);
                            tm.number = t;
                            broadcast(tm);

                            if (t > 0) {
                                boolean allDone = false;
                                for (int wait = 0; wait < 10; wait++) {
                                    Thread.sleep(100);
                                    allDone = true;
                                    for (Handler h : players) {
                                        if (!h.answered) allDone = false;
                                    }
                                    if (allDone) break;
                                }
                                
                                if (allDone) {
                                    GameMessage skip = new GameMessage(GameMessage.Type.TIMER);
                                    skip.number = 0;
                                    broadcast(skip);
                                    break;
                                }
                            }
                        }

                        GameMessage rm = new GameMessage(GameMessage.Type.RESULT);
                        rm.number = currentCorrect;
                        rm.text = "Correct answer: " + data[currentCorrect + 1];
                        sendPlayerList();
                        broadcast(rm);

                        Thread.sleep(2500);
                    }

                    sendPlayerList();
                    GameMessage gm = new GameMessage(GameMessage.Type.GAMEOVER);
                    
                    // Attach names and scores to fix NPE on client side
                    String[] finalNames = new String[players.size()];
                    int[] finalScores = new int[players.size()];
                    for (int i = 0; i < players.size(); i++) {
                        finalNames[i] = players.get(i).name;
                        finalScores[i] = players.get(i).score;
                    }
                    gm.names = finalNames;
                    gm.scores = finalScores;
                    
                    broadcast(gm);
                    gameRunning = false;
                    activeRooms.remove(code); // Clean up room after game ends

                } catch (Exception e) { e.printStackTrace(); gameRunning = false; activeRooms.remove(code); }
            }).start();
        }
    }

    // ── Per-Client Handler ───────────────────────────────────────────────────
    static class Handler implements Runnable {
        Socket sock;
        ObjectOutputStream out;
        ObjectInputStream in;
        String name = "";
        int score = 0;
        boolean answered = false;
        Room myRoom = null;

        Handler(Socket s) { this.sock = s; }

        public void run() {
            try {
                out = new ObjectOutputStream(sock.getOutputStream());
                out.flush();
                in  = new ObjectInputStream(sock.getInputStream());

                while (true) {
                    GameMessage msg = (GameMessage) in.readObject();

                    switch (msg.type) {
                        case JOIN:
                            name = msg.text;
                            String code = msg.roomCode;
                            if (code == null || code.trim().isEmpty()) code = "DEFAULT";
                            code = code.toUpperCase();

                            // Create or join room
                            activeRooms.putIfAbsent(code, new Room(code));
                            myRoom = activeRooms.get(code);

                            if (myRoom.gameRunning) {
                                GameMessage err = new GameMessage(GameMessage.Type.ERROR);
                                err.text = "Game already running in room " + code;
                                send(err);
                                return; // End thread
                            }

                            myRoom.players.add(this);
                            System.out.println("[+] " + name + " joined room " + code);
                            myRoom.broadcastEvent(">>> " + name + " joined Room " + code + "!");
                            myRoom.sendPlayerList();
                            break;

                        case START:
                            if (myRoom != null) {
                                if (myRoom.players.size() < 2) {
                                    GameMessage err = new GameMessage(GameMessage.Type.ERROR);
                                    err.text = "You need at least 2 players to start a multiplayer match!";
                                    send(err);
                                } else {
                                    int rounds = msg.number > 0 ? msg.number : 5;
                                    String diff = (msg.text != null) ? msg.text : "Mixed";
                                    myRoom.broadcastEvent("*** Match started by " + name + "! (" + rounds + " Rounds | " + diff + " Mode)");
                                    myRoom.startGame(rounds, diff);
                                }
                            }
                            break;

                        case ANSWER:
                            if (myRoom != null && !answered && myRoom.gameRunning) {
                                answered = true;
                                int choice = msg.number;
                                if (choice == myRoom.currentCorrect) {
                                    if (!myRoom.firstCorrectTaken) {
                                        myRoom.firstCorrectTaken = true;
                                        score += 150;
                                        myRoom.broadcastEvent("FIRST STRIKE! " + name + " answered perfectly! (+150)");
                                    } else {
                                        score += 100;
                                        myRoom.broadcastEvent("CORRECT! " + name + " got it right! (+100)");
                                    }
                                } else {
                                    myRoom.broadcastEvent("INCORRECT! " + name + " missed the shot...");
                                }
                                myRoom.sendPlayerList();
                            }
                            break;

                        default: break;
                    }
                }
            } catch (Exception e) {
                if (myRoom != null) {
                    myRoom.players.remove(this);
                    myRoom.broadcastEvent("<<< " + name + " disconnected.");
                    if (!myRoom.players.isEmpty()) myRoom.sendPlayerList();
                    else activeRooms.remove(myRoom.code); // destroy empty room
                }
            }
        }

        synchronized void send(GameMessage msg) {
            try {
                out.reset();
                out.writeObject(msg);
                out.flush();
            } catch (Exception e) {}
        }
    }
}
