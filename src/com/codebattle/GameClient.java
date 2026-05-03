package com.codebattle;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.net.*;

/**
 * CodeBattle Royale — Game Client with Swing GUI
 * SPPU BE IT | Lab Practice V | Distributed Systems Mini Project
 *
 * Features:
 *  - Real-time timer display
 *  - Live leaderboard (updates on every answer)
 *  - Clean card-based UI with 4 screens
 */
public class GameClient extends JFrame {

    // Network
    ObjectOutputStream out;
    ObjectInputStream in;

    // Colors
    static final Color BG     = new Color(15, 20, 30);
    static final Color CARD   = new Color(30, 40, 55);
    static final Color PURPLE = new Color(130, 90, 240);
    static final Color CYAN   = new Color(0, 200, 220);
    static final Color WHITE  = new Color(230, 235, 245);
    static final Color RED    = new Color(240, 70, 70);
    static final Color GREEN  = new Color(40, 200, 100);

    // Screens
    CardLayout cards = new CardLayout();
    JPanel root = new JPanel(cards);

    // Join screen
    JTextField nameField = new JTextField("Player" + (int)(Math.random()*900+100));

    // Lobby screen
    JTextArea lobbyList = new JTextArea();
    JButton startBtn;

    // Game screen
    JLabel timerLabel   = new JLabel("15", SwingConstants.CENTER);
    JLabel questionText = new JLabel("", SwingConstants.CENTER);
    JLabel roundLabel   = new JLabel("Round 1/5", SwingConstants.CENTER);
    JButton[] optBtns   = new JButton[4];
    JTextArea scoreboard = new JTextArea();
    int correctAnswer = -1;

    // Result screen
    JTextArea resultArea = new JTextArea();

    public GameClient() {
        super("CodeBattle Royale");
        setSize(900, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        root.setBackground(BG);
        add(root);

        root.add(makeJoinScreen(),   "JOIN");
        root.add(makeLobbyScreen(),  "LOBBY");
        root.add(makeGameScreen(),   "GAME");
        root.add(makeResultScreen(), "RESULT");
        cards.show(root, "JOIN");
    }

    // ═══════════════════════════════════════════ JOIN SCREEN
    JPanel makeJoinScreen() {
        JPanel p = darkPanel(new GridBagLayout());
        GridBagConstraints g = gbc();

        g.gridy = 0;
        JLabel title = label("CODEBATTLE ROYALE", 42, Font.BOLD, PURPLE);
        p.add(title, g);

        g.gridy = 1;
        p.add(label("Distributed Quiz Game", 16, Font.PLAIN, CYAN), g);

        g.gridy = 2; g.insets = new Insets(30,10,10,10);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        nameField.setPreferredSize(new Dimension(350, 45));
        nameField.setBackground(CARD); nameField.setForeground(WHITE);
        nameField.setCaretColor(CYAN);
        nameField.setBorder(BorderFactory.createTitledBorder(
            new LineBorder(PURPLE), "YOUR NAME", 0, 0, null, Color.GRAY));
        p.add(nameField, g);

        g.gridy = 3; g.insets = new Insets(15,10,10,10);
        JButton joinBtn = button("JOIN SERVER", PURPLE);
        joinBtn.addActionListener(e -> connectAndJoin());
        p.add(joinBtn, g);

        return p;
    }

    // ═══════════════════════════════════════════ LOBBY SCREEN
    JPanel makeLobbyScreen() {
        JPanel p = darkPanel(new BorderLayout(20, 20));
        p.setBorder(new EmptyBorder(30, 30, 30, 30));

        p.add(label("WAITING ROOM", 28, Font.BOLD, CYAN), BorderLayout.NORTH);

        lobbyList.setEditable(false);
        lobbyList.setFont(new Font("Consolas", Font.PLAIN, 18));
        lobbyList.setBackground(CARD); lobbyList.setForeground(WHITE);
        lobbyList.setBorder(new EmptyBorder(15, 15, 15, 15));
        p.add(new JScrollPane(lobbyList), BorderLayout.CENTER);

        startBtn = button("START GAME (Host Only)", GREEN);
        startBtn.addActionListener(e -> {
            GameMessage msg = new GameMessage(GameMessage.Type.START);
            sendMsg(msg);
        });
        p.add(startBtn, BorderLayout.SOUTH);

        return p;
    }

    // ═══════════════════════════════════════════ GAME SCREEN
    JPanel makeGameScreen() {
        JPanel p = darkPanel(new BorderLayout(15, 15));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top bar: round + timer
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        roundLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        roundLabel.setForeground(CYAN);
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 52));
        timerLabel.setForeground(PURPLE);
        top.add(roundLabel, BorderLayout.WEST);
        top.add(timerLabel, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        // Center: question + 4 options
        JPanel center = new JPanel(new BorderLayout(10, 20));
        center.setOpaque(false);
        questionText.setFont(new Font("Segoe UI", Font.BOLD, 22));
        questionText.setForeground(WHITE);
        center.add(questionText, BorderLayout.NORTH);

        JPanel opts = new JPanel(new GridLayout(2, 2, 12, 12));
        opts.setOpaque(false);
        Color[] optColors = {new Color(30,60,140), new Color(10,50,30), new Color(80,15,15), new Color(55,20,80)};
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            optBtns[i] = button("...", optColors[i]);
            optBtns[i].setFont(new Font("Segoe UI", Font.BOLD, 15));
            optBtns[i].addActionListener(e -> submitAnswer(idx));
            opts.add(optBtns[i]);
        }
        center.add(opts, BorderLayout.CENTER);
        p.add(center, BorderLayout.CENTER);

        // Right side: live scoreboard
        scoreboard.setEditable(false);
        scoreboard.setFont(new Font("Consolas", Font.BOLD, 14));
        scoreboard.setBackground(CARD); scoreboard.setForeground(CYAN);
        scoreboard.setBorder(BorderFactory.createTitledBorder(
            new LineBorder(CYAN), "LIVE SCORES", 0, 0, null, CYAN));
        scoreboard.setPreferredSize(new Dimension(200, 0));
        p.add(scoreboard, BorderLayout.EAST);

        return p;
    }

    // ═══════════════════════════════════════════ RESULT SCREEN
    JPanel makeResultScreen() {
        JPanel p = darkPanel(new BorderLayout(20, 20));
        p.setBorder(new EmptyBorder(40, 40, 40, 40));

        p.add(label("GAME OVER", 48, Font.BOLD, PURPLE), BorderLayout.NORTH);

        resultArea.setEditable(false);
        resultArea.setFont(new Font("Consolas", Font.BOLD, 20));
        resultArea.setBackground(CARD); resultArea.setForeground(WHITE);
        resultArea.setBorder(new EmptyBorder(20, 20, 20, 20));
        p.add(resultArea, BorderLayout.CENTER);

        JButton again = button("BACK TO MENU", CYAN);
        again.addActionListener(e -> cards.show(root, "JOIN"));
        p.add(again, BorderLayout.SOUTH);

        return p;
    }

    // ═══════════════════════════════════════════ NETWORK
    void connectAndJoin() {
        new Thread(() -> {
            try {
                Socket sock = new Socket("localhost", 5000);
                out = new ObjectOutputStream(sock.getOutputStream());
                out.flush();
                in = new ObjectInputStream(sock.getInputStream());

                // Send join
                GameMessage jm = new GameMessage(GameMessage.Type.JOIN);
                jm.text = nameField.getText().trim();
                sendMsg(jm);

                SwingUtilities.invokeLater(() -> cards.show(root, "LOBBY"));

                // Listen loop
                while (true) {
                    GameMessage msg = (GameMessage) in.readObject();
                    SwingUtilities.invokeLater(() -> handleMessage(msg));
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this,
                        "Cannot connect to server!\nMake sure server is running first.",
                        "Connection Error", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    void handleMessage(GameMessage msg) {
        switch (msg.type) {
            case WELCOME:
                break;

            case PLAYER_LIST:
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < msg.names.length; i++) {
                    String medal = i == 0 ? "  [HOST]" : "";
                    sb.append("  ").append(msg.names[i])
                      .append(" — ").append(msg.scores[i]).append(" pts")
                      .append(medal).append("\n");
                }
                lobbyList.setText(sb.toString());
                // Also update game scoreboard
                StringBuilder sb2 = new StringBuilder();
                // Sort by score
                Integer[] idx = new Integer[msg.names.length];
                for (int i = 0; i < idx.length; i++) idx[i] = i;
                java.util.Arrays.sort(idx, (a, b) -> msg.scores[b] - msg.scores[a]);
                for (int i = 0; i < idx.length; i++) {
                    String medal = i == 0 ? "🥇" : i == 1 ? "🥈" : i == 2 ? "🥉" : "  ";
                    sb2.append(medal).append(" ").append(msg.names[idx[i]])
                       .append("\n   ").append(msg.scores[idx[i]]).append(" pts\n\n");
                }
                scoreboard.setText(sb2.toString());
                break;

            case QUESTION:
                cards.show(root, "GAME");
                questionText.setText("<html><center>" + msg.text + "</center></html>");
                roundLabel.setText("Round " + msg.number + " / 5");
                correctAnswer = -1;
                for (int i = 0; i < 4; i++) {
                    optBtns[i].setText((char)('A'+i) + ". " + msg.options[i]);
                    optBtns[i].setEnabled(true);
                }
                break;

            case TIMER:
                timerLabel.setText(String.valueOf(msg.number));
                timerLabel.setForeground(msg.number <= 5 ? RED : PURPLE);
                break;

            case RESULT:
                correctAnswer = msg.number;
                for (int i = 0; i < 4; i++) {
                    optBtns[i].setEnabled(false);
                    if (i == msg.number) optBtns[i].setBackground(GREEN);
                }
                break;

            case GAMEOVER:
                resultArea.setText("\n  " + msg.text + "\n\n" + scoreboard.getText());
                cards.show(root, "RESULT");
                break;

            case ERROR:
                JOptionPane.showMessageDialog(this, msg.text);
                break;

            default: break;
        }
    }

    void submitAnswer(int choice) {
        for (JButton b : optBtns) b.setEnabled(false);
        GameMessage msg = new GameMessage(GameMessage.Type.ANSWER);
        msg.number = choice;  // Send selected option index to server for validation
        sendMsg(msg);
    }

    void sendMsg(GameMessage msg) {
        try {
            if (out != null) {
                out.reset();
                out.writeObject(msg);
                out.flush();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ═══════════════════════════════════════════ UI HELPERS
    static JPanel darkPanel(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(BG);
        return p;
    }
    static JLabel label(String text, int size, int style, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", style, size));
        l.setForeground(color);
        return l;
    }
    static JButton button(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b.setBackground(bg); b.setForeground(WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(300, 50));
        return b;
    }
    static GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);
        g.gridx = 0;
        return g;
    }

    // ═══════════════════════════════════════════ MAIN
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameClient().setVisible(true));
    }
}
