package com.codebattle;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
 * CodeBattle Royale — Premium Game Client
 * SPPU BE IT | Distributed Systems Mini Project
 */
public class GameClient extends JFrame {

    // Network
    ObjectOutputStream out;
    ObjectInputStream in;
    String myName = "";
    boolean isHost = false;

    // Brand Colors
    static final Color BG_DARK = new Color(13, 17, 23);
    static final Color BG_LIGHT = new Color(22, 27, 34);
    static final Color ACCENT_PURPLE = new Color(137, 87, 229);
    static final Color ACCENT_BLUE = new Color(47, 129, 247);
    static final Color ACCENT_GREEN = new Color(63, 185, 80);
    static final Color ACCENT_RED = new Color(248, 81, 73);
    static final Color TEXT_MAIN = new Color(201, 209, 217);
    static final Color TEXT_MUTED = new Color(139, 148, 158);

    // Screens
    CardLayout cards = new CardLayout();
    JPanel root = new JPanel(cards);

    // UI Components
    JTextField nameField;
    JTextField roomField;
    JPanel lobbyListPanel;
    JButton startBtn;
    JLabel lobbyRoomCodeLabel;
    
    // Host Settings
    JComboBox<String> roundsCombo;
    JComboBox<String> diffCombo;
    JPanel hostSettingsPanel;
    JLabel waitingLabel;
    
    // Game Components
    JLabel timerLabel;
    JProgressBar timerBar;
    JLabel questionText;
    JLabel roundLabel;
    JButton[] optBtns = new JButton[4];
    JPanel scoreboardPanel;
    int correctAnswer = -1;
    
    // Event Feed
    JTextArea eventFeed;
    
    // Results
    JPanel podiumPanel;

    public GameClient() {
        super("CodeBattle Royale");
        setSize(1000, 700);
        setMinimumSize(new Dimension(800, 600));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Root Panel with Gradient Background
        root = new JPanel(cards) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, BG_DARK, getWidth(), getHeight(), new Color(20, 20, 35));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        add(root);

        // Global Event Feed (Floating at bottom)
        eventFeed = new JTextArea();
        eventFeed.setEditable(false);
        eventFeed.setOpaque(false);
        eventFeed.setForeground(ACCENT_BLUE);
        eventFeed.setFont(new Font("Segoe UI", Font.BOLD, 14));
        eventFeed.setFocusable(false);

        root.add(makeJoinScreen(),   "JOIN");
        root.add(makeLobbyScreen(),  "LOBBY");
        root.add(makeGameScreen(),   "GAME");
        root.add(makeResultScreen(), "RESULT");
        cards.show(root, "JOIN");
    }

    // ─── JOIN SCREEN ─────────────────────────────────────────────────────────
    JPanel makeJoinScreen() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();

        GlassPanel card = new GlassPanel(new GridBagLayout(), 20);
        card.setPreferredSize(new Dimension(500, 520));
        
        GridBagConstraints cg = new GridBagConstraints();
        cg.insets = new Insets(10, 10, 10, 10);
        cg.gridy = 0;
        
        JLabel logo = new JLabel("CodeBattle Royale", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 36));
        logo.setForeground(Color.WHITE);
        card.add(logo, cg);

        cg.gridy = 1;
        JLabel sub = new JLabel("Esports Trivia Engine", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        sub.setForeground(ACCENT_PURPLE);
        card.add(sub, cg);

        cg.gridy = 2; cg.insets = new Insets(30, 20, 10, 20);
        nameField = new JTextField("Player" + (int)(Math.random()*9000));
        nameField.setFont(new Font("Segoe UI", Font.BOLD, 20));
        nameField.setPreferredSize(new Dimension(300, 50));
        nameField.setBackground(BG_DARK);
        nameField.setForeground(Color.WHITE);
        nameField.setCaretColor(ACCENT_BLUE);
        nameField.setHorizontalAlignment(JTextField.CENTER);
        nameField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ACCENT_PURPLE, 2, true),
            BorderFactory.createTitledBorder(new EmptyBorder(0,0,0,0), "YOUR NAME", 0, 0, new Font("Segoe UI", Font.BOLD, 10), TEXT_MUTED)
        ));
        card.add(nameField, cg);

        cg.gridy = 3; cg.insets = new Insets(10, 20, 5, 20);
        JButton createBtn = new StyledButton("CREATE NEW ROOM (HOST)", ACCENT_GREEN);
        createBtn.setPreferredSize(new Dimension(300, 50));
        createBtn.addActionListener(e -> {
            isHost = true;
            String randomCode = String.format("%04X", (int)(Math.random()*65535));
            connectAndJoin(randomCode);
        });
        card.add(createBtn, cg);

        cg.gridy = 4; cg.insets = new Insets(10, 20, 5, 20);
        JLabel orLabel = new JLabel("— OR JOIN EXISTING —");
        orLabel.setForeground(TEXT_MUTED);
        card.add(orLabel, cg);

        cg.gridy = 5; cg.insets = new Insets(5, 20, 5, 20);
        roomField = new JTextField();
        roomField.setFont(new Font("Segoe UI", Font.BOLD, 20));
        roomField.setPreferredSize(new Dimension(300, 50));
        roomField.setBackground(BG_DARK);
        roomField.setForeground(Color.WHITE);
        roomField.setCaretColor(ACCENT_BLUE);
        roomField.setHorizontalAlignment(JTextField.CENTER);
        roomField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ACCENT_BLUE, 2, true),
            BorderFactory.createTitledBorder(new EmptyBorder(0,0,0,0), "ENTER ROOM CODE", 0, 0, new Font("Segoe UI", Font.BOLD, 10), TEXT_MUTED)
        ));
        card.add(roomField, cg);

        cg.gridy = 6; cg.insets = new Insets(5, 20, 20, 20);
        JButton joinBtn = new StyledButton("JOIN MATCH", ACCENT_BLUE);
        joinBtn.setPreferredSize(new Dimension(300, 50));
        joinBtn.addActionListener(e -> {
            isHost = false;
            String code = roomField.getText().trim();
            if (code.isEmpty()) showProPopup("Error", "Please enter a valid Room Code.", true);
            else connectAndJoin(code);
        });
        card.add(joinBtn, cg);

        p.add(card, g);
        return p;
    }

    // ─── LOBBY SCREEN ────────────────────────────────────────────────────────
    JPanel makeLobbyScreen() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(40, 60, 40, 60));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("WAITING LOBBY", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        top.add(title, BorderLayout.NORTH);

        lobbyRoomCodeLabel = new JLabel("ROOM: ----", SwingConstants.CENTER);
        lobbyRoomCodeLabel.setFont(new Font("Consolas", Font.BOLD, 24));
        lobbyRoomCodeLabel.setForeground(ACCENT_BLUE);
        top.add(lobbyRoomCodeLabel, BorderLayout.SOUTH);
        
        p.add(top, BorderLayout.NORTH);

        lobbyListPanel = new GlassPanel(new FlowLayout(FlowLayout.CENTER, 20, 20), 15);
        JScrollPane scroll = new JScrollPane(lobbyListPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        p.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        
        hostSettingsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        hostSettingsPanel.setOpaque(false);
        
        JLabel rL = new JLabel("Rounds:"); rL.setForeground(Color.WHITE);
        roundsCombo = new JComboBox<>(new String[]{"3 Rounds", "5 Rounds", "10 Rounds"});
        roundsCombo.setSelectedIndex(1);
        
        JLabel dL = new JLabel("Difficulty:"); dL.setForeground(Color.WHITE);
        diffCombo = new JComboBox<>(new String[]{"Mixed", "Easy", "Medium", "Hard"});
        
        hostSettingsPanel.add(rL);
        hostSettingsPanel.add(roundsCombo);
        hostSettingsPanel.add(dL);
        hostSettingsPanel.add(diffCombo);

        startBtn = new StyledButton("START GAME (HOST)", ACCENT_GREEN);
        startBtn.setPreferredSize(new Dimension(300, 60));
        startBtn.addActionListener(e -> {
            int rounds = Integer.parseInt(roundsCombo.getSelectedItem().toString().split(" ")[0]);
            String diff = diffCombo.getSelectedItem().toString();
            GameMessage msg = new GameMessage(GameMessage.Type.START);
            msg.number = rounds;
            msg.text = diff;
            sendMsg(msg);
        });
        
        waitingLabel = new JLabel("Waiting for host to start the match...", SwingConstants.CENTER);
        waitingLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        waitingLabel.setForeground(TEXT_MUTED);

        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.setOpaque(false);
        btnPanel.add(hostSettingsPanel, BorderLayout.NORTH);
        btnPanel.add(startBtn, BorderLayout.CENTER);
        btnPanel.add(waitingLabel, BorderLayout.SOUTH);

        bottom.add(btnPanel, BorderLayout.CENTER);
        
        eventFeed.setPreferredSize(new Dimension(0, 30));
        bottom.add(eventFeed, BorderLayout.SOUTH);
        
        p.add(bottom, BorderLayout.SOUTH);

        return p;
    }

    // ─── GAME SCREEN ─────────────────────────────────────────────────────────
    JPanel makeGameScreen() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 30, 20, 30));

        // TOP: Round + Timer
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        roundLabel = new JLabel("Round 1/X");
        roundLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        roundLabel.setForeground(TEXT_MUTED);
        top.add(roundLabel, BorderLayout.WEST);

        timerLabel = new JLabel("15");
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        timerLabel.setForeground(Color.WHITE);
        top.add(timerLabel, BorderLayout.EAST);

        timerBar = new JProgressBar(0, 15);
        timerBar.setValue(15);
        timerBar.setForeground(ACCENT_BLUE);
        timerBar.setBackground(BG_DARK);
        timerBar.setBorderPainted(false);
        timerBar.setPreferredSize(new Dimension(0, 8));
        top.add(timerBar, BorderLayout.SOUTH);

        p.add(top, BorderLayout.NORTH);

        // CENTER: Question & Options
        JPanel center = new JPanel(new BorderLayout(10, 30));
        center.setOpaque(false);
        
        questionText = new JLabel("Waiting for question...");
        questionText.setFont(new Font("Segoe UI", Font.BOLD, 28));
        questionText.setForeground(Color.WHITE);
        questionText.setHorizontalAlignment(SwingConstants.CENTER);
        center.add(questionText, BorderLayout.NORTH);

        JPanel opts = new JPanel(new GridLayout(2, 2, 20, 20));
        opts.setOpaque(false);
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            optBtns[i] = new StyledButton("...", BG_LIGHT);
            optBtns[i].setFont(new Font("Segoe UI", Font.BOLD, 18));
            optBtns[i].addActionListener(e -> submitAnswer(idx));
            opts.add(optBtns[i]);
        }
        center.add(opts, BorderLayout.CENTER);
        p.add(center, BorderLayout.CENTER);

        // RIGHT: Live Scoreboard
        GlassPanel right = new GlassPanel(new BorderLayout(), 15);
        right.setPreferredSize(new Dimension(250, 0));
        JLabel sTitle = new JLabel("LIVE STANDINGS", SwingConstants.CENTER);
        sTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sTitle.setForeground(ACCENT_PURPLE);
        sTitle.setBorder(new EmptyBorder(10, 0, 10, 0));
        right.add(sTitle, BorderLayout.NORTH);
        
        scoreboardPanel = new JPanel();
        scoreboardPanel.setLayout(new BoxLayout(scoreboardPanel, BoxLayout.Y_AXIS));
        scoreboardPanel.setOpaque(false);
        right.add(new JScrollPane(scoreboardPanel) {{
            setOpaque(false); getViewport().setOpaque(false); setBorder(null);
        }}, BorderLayout.CENTER);
        
        p.add(right, BorderLayout.EAST);

        // BOTTOM: Event Feed
        p.add(eventFeed, BorderLayout.SOUTH);

        return p;
    }

    // ─── RESULT SCREEN ───────────────────────────────────────────────────────
    JPanel makeResultScreen() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("MATCH COMPLETE", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 42));
        title.setForeground(ACCENT_PURPLE);
        p.add(title, BorderLayout.NORTH);

        podiumPanel = new JPanel(new GridBagLayout());
        podiumPanel.setOpaque(false);
        p.add(podiumPanel, BorderLayout.CENTER);

        JButton again = new StyledButton("RETURN TO MAIN MENU", ACCENT_BLUE);
        again.setPreferredSize(new Dimension(0, 60));
        again.addActionListener(e -> {
            try { if(out != null) sock.close(); } catch(Exception ex){}
            cards.show(root, "JOIN");
        });
        p.add(again, BorderLayout.SOUTH);

        return p;
    }

    // ─── NETWORK & LOGIC ─────────────────────────────────────────────────────
    Socket sock;
    void connectAndJoin(String room) {
        myName = nameField.getText().trim();
        new Thread(() -> {
            try {
                sock = new Socket("localhost", 5000);
                out = new ObjectOutputStream(sock.getOutputStream());
                out.flush();
                in = new ObjectInputStream(sock.getInputStream());

                GameMessage jm = new GameMessage(GameMessage.Type.JOIN);
                jm.text = myName;
                jm.roomCode = room;
                sendMsg(jm);

                SwingUtilities.invokeLater(() -> {
                    lobbyRoomCodeLabel.setText("ROOM: " + room.toUpperCase());
                    
                    // Update UI based on Host/Player
                    hostSettingsPanel.setVisible(isHost);
                    startBtn.setVisible(isHost);
                    waitingLabel.setVisible(!isHost);
                    
                    cards.show(root, "LOBBY");
                });

                while (true) {
                    GameMessage msg = (GameMessage) in.readObject();
                    SwingUtilities.invokeLater(() -> handleMessage(msg));
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                    showProPopup("Connection Failed", "Could not connect to the CodeBattle Server.", true)
                );
            }
        }).start();
    }

    void handleMessage(GameMessage msg) {
        switch (msg.type) {
            case PLAYER_LIST:
                updateLobby(msg.names, msg.scores);
                updateScoreboard(msg.names, msg.scores);
                break;
            case QUESTION:
                cards.show(root, "GAME");
                questionText.setText("<html><center>" + msg.text + "</center></html>");
                roundLabel.setText("Round " + msg.number);
                correctAnswer = -1;
                for (int i = 0; i < 4; i++) {
                    optBtns[i].setText((char)('A'+i) + ". " + msg.options[i]);
                    optBtns[i].setBackground(BG_LIGHT);
                    optBtns[i].setEnabled(true);
                }
                break;
            case TIMER:
                timerLabel.setText(String.valueOf(msg.number));
                timerBar.setValue(msg.number);
                if (msg.number <= 5) {
                    timerLabel.setForeground(ACCENT_RED);
                    timerBar.setForeground(ACCENT_RED);
                } else {
                    timerLabel.setForeground(Color.WHITE);
                    timerBar.setForeground(ACCENT_BLUE);
                }
                break;
            case RESULT:
                correctAnswer = msg.number;
                for (int i = 0; i < 4; i++) {
                    optBtns[i].setEnabled(false);
                    if (i == msg.number) optBtns[i].setBackground(ACCENT_GREEN);
                }
                break;
            case EVENT:
                eventFeed.setText(msg.text);
                break;
            case GAMEOVER:
                buildPodium(msg.names, msg.scores);
                cards.show(root, "RESULT");
                break;
            default: break;
        }
    }

    void submitAnswer(int choice) {
        for (JButton b : optBtns) b.setEnabled(false);
        optBtns[choice].setBackground(ACCENT_PURPLE);
        GameMessage msg = new GameMessage(GameMessage.Type.ANSWER);
        msg.number = choice;
        sendMsg(msg);
    }

    void sendMsg(GameMessage msg) {
        try {
            if (out != null) { out.reset(); out.writeObject(msg); out.flush(); }
        } catch (Exception e) {}
    }

    // ─── UI UPDATERS ─────────────────────────────────────────────────────────
    void updateLobby(String[] names, int[] scores) {
        lobbyListPanel.removeAll();
        for (int i = 0; i < names.length; i++) {
            JLabel p = new JLabel("  " + names[i] + "  ");
            p.setFont(new Font("Segoe UI", Font.BOLD, 18));
            p.setForeground(Color.WHITE);
            p.setBorder(new EmptyBorder(10, 20, 10, 20));
            lobbyListPanel.add(p);
        }
        lobbyListPanel.revalidate(); lobbyListPanel.repaint();
    }

    void updateScoreboard(String[] names, int[] scores) {
        if (names == null || scores == null) return;
        scoreboardPanel.removeAll();
        Integer[] idx = new Integer[names.length];
        for (int i = 0; i < idx.length; i++) idx[i] = i;
        java.util.Arrays.sort(idx, (a, b) -> scores[b] - scores[a]);
        
        for (int i = 0; i < idx.length; i++) {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255,255,255,30)),
                new EmptyBorder(10, 10, 10, 10)
            ));
            
            String rankStr = i == 0 ? "[1] " : i == 1 ? "[2] " : i == 2 ? "[3] " : "[-] ";
            JLabel n = new JLabel(rankStr + names[idx[i]]);
            n.setFont(new Font("Segoe UI", Font.BOLD, 14));
            n.setForeground(Color.WHITE);
            
            JLabel s = new JLabel(scores[idx[i]] + " pts");
            s.setFont(new Font("Segoe UI", Font.BOLD, 14));
            s.setForeground(ACCENT_GREEN);
            
            row.add(n, BorderLayout.WEST);
            row.add(s, BorderLayout.EAST);
            scoreboardPanel.add(row);
        }
        scoreboardPanel.revalidate(); scoreboardPanel.repaint();
    }

    void buildPodium(String[] names, int[] scores) {
        if (names == null || scores == null || names.length == 0) return;
        podiumPanel.removeAll();
        Integer[] idx = new Integer[names.length];
        for (int i = 0; i < idx.length; i++) idx[i] = i;
        java.util.Arrays.sort(idx, (a, b) -> scores[b] - scores[a]);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(0, 10, 0, 10);
        g.anchor = GridBagConstraints.SOUTH;

        // Draw top 3 (2nd, 1st, 3rd)
        int[] order = {1, 0, 2}; 
        int[] heights = {150, 220, 100};
        Color[] colors = {new Color(192, 192, 192), new Color(255, 215, 0), new Color(205, 127, 50)};
        
        for (int i = 0; i < 3; i++) {
            int rank = order[i];
            if (rank < names.length) {
                g.gridx = i;
                
                JPanel col = new JPanel(new BorderLayout());
                col.setOpaque(false);
                
                JLabel n = new JLabel(names[idx[rank]], SwingConstants.CENTER);
                n.setFont(new Font("Segoe UI", Font.BOLD, 20));
                n.setForeground(Color.WHITE);
                col.add(n, BorderLayout.NORTH);
                
                JLabel s = new JLabel(scores[idx[rank]] + " pts", SwingConstants.CENTER);
                s.setFont(new Font("Segoe UI", Font.BOLD, 16));
                s.setForeground(ACCENT_GREEN);
                col.add(s, BorderLayout.CENTER);
                
                JPanel block = new JPanel();
                block.setBackground(colors[rank]);
                block.setPreferredSize(new Dimension(150, heights[i]));
                block.setBorder(new LineBorder(Color.WHITE, 2));
                
                JLabel r = new JLabel("#" + (rank+1), SwingConstants.CENTER);
                r.setFont(new Font("Segoe UI", Font.BOLD, 40));
                r.setForeground(new Color(0,0,0,100));
                block.add(r);
                
                col.add(block, BorderLayout.SOUTH);
                podiumPanel.add(col, g);
            }
        }
        podiumPanel.revalidate(); podiumPanel.repaint();
    }

    // ─── CUSTOM COMPONENTS ───────────────────────────────────────────────────
    class GlassPanel extends JPanel {
        int radius;
        public GlassPanel(LayoutManager lm, int radius) {
            super(lm); this.radius = radius; setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 15));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.setColor(new Color(255, 255, 255, 40));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);
        }
    }

    class StyledButton extends JButton {
        Color base;
        public StyledButton(String text, Color base) {
            super(text); this.base = base;
            setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false);
            setForeground(Color.WHITE); setFont(new Font("Segoe UI", Font.BOLD, 16));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isPressed()) g2.setColor(base.darker().darker());
            else if (getModel().isRollover()) g2.setColor(base.brighter());
            else g2.setColor(base);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            super.paintComponent(g);
        }
    }

    // ─── PRO POPUPS ──────────────────────────────────────────────────────────
    void showProPopup(String titleStr, String message, boolean isError) {
        JDialog dialog = new JDialog(this, titleStr, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        
        GlassPanel panel = new GlassPanel(new BorderLayout(20, 20), 20);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));
        
        JLabel title = new JLabel(titleStr.toUpperCase(), SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(isError ? ACCENT_RED : ACCENT_BLUE);
        panel.add(title, BorderLayout.NORTH);
        
        JLabel msg = new JLabel("<html><center>" + message + "</center></html>", SwingConstants.CENTER);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        msg.setForeground(Color.WHITE);
        panel.add(msg, BorderLayout.CENTER);
        
        JButton okBtn = new StyledButton("OK", isError ? ACCENT_RED : ACCENT_BLUE);
        okBtn.setPreferredSize(new Dimension(100, 40));
        okBtn.addActionListener(e -> dialog.dispose());
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setOpaque(false);
        btnPanel.add(okBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.pack();
        dialog.setSize(Math.max(400, dialog.getWidth()), Math.max(200, dialog.getHeight()));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        // Set Look and Feel for smoother rendering
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception e){}
        SwingUtilities.invokeLater(() -> new GameClient().setVisible(true));
    }
}
