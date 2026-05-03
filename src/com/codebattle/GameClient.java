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
    JPanel lobbyListPanel;
    JButton startBtn;
    
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
        card.setPreferredSize(new Dimension(500, 400));
        
        GridBagConstraints cg = new GridBagConstraints();
        cg.insets = new Insets(10, 10, 10, 10);
        cg.gridy = 0;
        
        JLabel logo = new JLabel("🎮 CodeBattle Royale", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 36));
        logo.setForeground(Color.WHITE);
        card.add(logo, cg);

        cg.gridy = 1;
        JLabel sub = new JLabel("Esports Trivia Engine", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        sub.setForeground(ACCENT_PURPLE);
        card.add(sub, cg);

        cg.gridy = 2; cg.insets = new Insets(40, 20, 10, 20);
        nameField = new JTextField("Player" + (int)(Math.random()*9000));
        nameField.setFont(new Font("Segoe UI", Font.BOLD, 20));
        nameField.setPreferredSize(new Dimension(300, 50));
        nameField.setBackground(BG_DARK);
        nameField.setForeground(Color.WHITE);
        nameField.setCaretColor(ACCENT_BLUE);
        nameField.setHorizontalAlignment(JTextField.CENTER);
        nameField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ACCENT_PURPLE, 2, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        card.add(nameField, cg);

        cg.gridy = 3; cg.insets = new Insets(10, 20, 20, 20);
        JButton joinBtn = new StyledButton("JOIN MATCH", ACCENT_PURPLE);
        joinBtn.setPreferredSize(new Dimension(300, 50));
        joinBtn.addActionListener(e -> connectAndJoin());
        card.add(joinBtn, cg);

        p.add(card, g);
        return p;
    }

    // ─── LOBBY SCREEN ────────────────────────────────────────────────────────
    JPanel makeLobbyScreen() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(40, 60, 40, 60));

        JLabel title = new JLabel("WAITING LOBBY", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        p.add(title, BorderLayout.NORTH);

        lobbyListPanel = new GlassPanel(new FlowLayout(FlowLayout.CENTER, 20, 20), 15);
        JScrollPane scroll = new JScrollPane(lobbyListPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        p.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        
        startBtn = new StyledButton("START GAME (HOST)", ACCENT_GREEN);
        startBtn.setPreferredSize(new Dimension(0, 60));
        startBtn.addActionListener(e -> sendMsg(new GameMessage(GameMessage.Type.START)));
        bottom.add(startBtn, BorderLayout.CENTER);
        
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
        roundLabel = new JLabel("Round 1/5");
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

        JLabel title = new JLabel("🏆 MATCH COMPLETE 🏆", SwingConstants.CENTER);
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
    void connectAndJoin() {
        myName = nameField.getText().trim();
        new Thread(() -> {
            try {
                sock = new Socket("localhost", 5000);
                out = new ObjectOutputStream(sock.getOutputStream());
                out.flush();
                in = new ObjectInputStream(sock.getInputStream());

                GameMessage jm = new GameMessage(GameMessage.Type.JOIN);
                jm.text = myName;
                sendMsg(jm);

                SwingUtilities.invokeLater(() -> cards.show(root, "LOBBY"));

                while (true) {
                    GameMessage msg = (GameMessage) in.readObject();
                    SwingUtilities.invokeLater(() -> handleMessage(msg));
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this, "Could not connect to server.", "Error", JOptionPane.ERROR_MESSAGE));
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
                roundLabel.setText("Round " + msg.number + " / 5");
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
            JLabel p = new JLabel("👤 " + names[i]);
            p.setFont(new Font("Segoe UI", Font.BOLD, 18));
            p.setForeground(Color.WHITE);
            p.setBorder(new EmptyBorder(10, 20, 10, 20));
            lobbyListPanel.add(p);
        }
        lobbyListPanel.revalidate(); lobbyListPanel.repaint();
    }

    void updateScoreboard(String[] names, int[] scores) {
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
            
            String medal = i == 0 ? "🥇 " : i == 1 ? "🥈 " : i == 2 ? "🥉 " : "  ";
            JLabel n = new JLabel(medal + names[idx[i]]);
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

    public static void main(String[] args) {
        // Set Look and Feel for smoother rendering
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception e){}
        SwingUtilities.invokeLater(() -> new GameClient().setVisible(true));
    }
}
