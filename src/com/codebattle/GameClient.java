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
 * Truly Responsive Version
 */
public class GameClient extends JFrame {

    // Network
    ObjectOutputStream out;
    ObjectInputStream in;
    Socket sock;
    String myName = "";
    boolean isHost = false;
    volatile boolean intentionalDisconnect = false;

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
        setSize(800, 600);
        setMinimumSize(new Dimension(350, 450)); // Allow very small split-screen
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

        // Global Event Feed
        eventFeed = new JTextArea();
        eventFeed.setEditable(false);
        eventFeed.setOpaque(false);
        eventFeed.setForeground(ACCENT_BLUE);
        eventFeed.setFont(new Font("Segoe UI", Font.BOLD, 12));
        eventFeed.setFocusable(false);

        root.add(makeJoinScreen(),   "JOIN");
        root.add(makeLobbyScreen(),  "LOBBY");
        root.add(makeGameScreen(),   "GAME");
        root.add(makeResultScreen(), "RESULT");
        cards.show(root, "JOIN");
        
        // Dynamic Resizing Logic
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustUIForSize();
            }
        });
    }

    private void adjustUIForSize() {
        boolean isSmall = getWidth() < 600 || getHeight() < 500;
        float scale = isSmall ? 0.8f : 1.0f;
        // Logic to tweak fonts if needed
    }

    // ─── JOIN SCREEN (FLUID LAYOUT) ──────────────────────────────────────────
    JPanel makeJoinScreen() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();

        GlassPanel card = new GlassPanel(new GridBagLayout(), 20);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints cg = new GridBagConstraints();
        cg.insets = new Insets(5, 10, 5, 10);
        cg.fill = GridBagConstraints.HORIZONTAL;
        cg.weightx = 1.0;
        cg.gridx = 0;
        
        // Logo
        cg.gridy = 0;
        JLabel logo = new JLabel("CodeBattle Royale", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        logo.setForeground(Color.WHITE);
        card.add(logo, cg);

        cg.gridy = 1;
        JLabel sub = new JLabel("Esports Trivia Engine", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(ACCENT_PURPLE);
        card.add(sub, cg);

        // Name Field
        cg.gridy = 2; cg.insets = new Insets(15, 10, 5, 10);
        nameField = new JTextField("Player" + (int)(Math.random()*9000));
        nameField.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameField.setPreferredSize(new Dimension(200, 40));
        setupFieldStyle(nameField, "YOUR NAME", ACCENT_PURPLE);
        card.add(nameField, cg);

        // Create Button
        cg.gridy = 3; cg.insets = new Insets(5, 10, 5, 10);
        JButton createBtn = new StyledButton("CREATE ROOM (HOST)", ACCENT_GREEN);
        createBtn.setPreferredSize(new Dimension(200, 40));
        createBtn.addActionListener(e -> {
            isHost = true;
            String randomCode = String.format("%04X", (int)(Math.random()*65535));
            connectAndJoin(randomCode);
        });
        card.add(createBtn, cg);

        // Separator
        cg.gridy = 4;
        JLabel orLabel = new JLabel("— OR JOIN —", SwingConstants.CENTER);
        orLabel.setForeground(TEXT_MUTED);
        orLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        card.add(orLabel, cg);

        // Room Field
        cg.gridy = 5;
        roomField = new JTextField();
        roomField.setFont(new Font("Segoe UI", Font.BOLD, 16));
        roomField.setPreferredSize(new Dimension(200, 40));
        setupFieldStyle(roomField, "ENTER CODE", ACCENT_BLUE);
        card.add(roomField, cg);

        // Join Button
        cg.gridy = 6;
        JButton joinBtn = new StyledButton("JOIN MATCH", ACCENT_BLUE);
        joinBtn.setPreferredSize(new Dimension(200, 40));
        joinBtn.addActionListener(e -> {
            isHost = false;
            String code = roomField.getText().trim();
            if (code.isEmpty()) showProPopup("Error", "Enter a valid code.", true);
            else connectAndJoin(code);
        });
        card.add(joinBtn, cg);

        // Card Constraints in Parent
        g.gridx = 0; g.gridy = 0;
        g.weightx = 1.0; g.weighty = 1.0;
        g.fill = GridBagConstraints.NONE;
        p.add(card, g);
        
        return p;
    }

    private void setupFieldStyle(JTextField f, String title, Color accent) {
        f.setBackground(BG_DARK);
        f.setForeground(Color.WHITE);
        f.setCaretColor(ACCENT_BLUE);
        f.setHorizontalAlignment(JTextField.CENTER);
        f.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(accent, 2, true),
            BorderFactory.createTitledBorder(new EmptyBorder(0,0,0,0), title, 0, 0, new Font("Segoe UI", Font.BOLD, 9), TEXT_MUTED)
        ));
    }

    // ─── LOBBY SCREEN (RESPONSIVE) ──────────────────────────────────────────
    JPanel makeLobbyScreen() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);
        g.fill = GridBagConstraints.BOTH;
        g.weightx = 1.0;

        // Header
        g.gridy = 0; g.weighty = 0.1;
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setOpaque(false);
        JLabel title = new JLabel("LOBBY", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        header.add(title);
        lobbyRoomCodeLabel = new JLabel("CODE: ----", SwingConstants.CENTER);
        lobbyRoomCodeLabel.setFont(new Font("Consolas", Font.BOLD, 18));
        lobbyRoomCodeLabel.setForeground(ACCENT_BLUE);
        header.add(lobbyRoomCodeLabel);
        p.add(header, g);

        // Player List (Scrollable)
        g.gridy = 1; g.weighty = 0.6;
        lobbyListPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        lobbyListPanel.setOpaque(false);
        JScrollPane scroll = new JScrollPane(lobbyListPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(new LineBorder(new Color(255,255,255,20), 1));
        p.add(scroll, g);

        // Settings + Start
        g.gridy = 2; g.weighty = 0.3;
        JPanel bottom = new JPanel(new GridBagLayout());
        bottom.setOpaque(false);
        GridBagConstraints bg = new GridBagConstraints();
        bg.fill = GridBagConstraints.HORIZONTAL;
        bg.weightx = 1.0; bg.gridx = 0; bg.insets = new Insets(2, 0, 2, 0);

        hostSettingsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        hostSettingsPanel.setOpaque(false);
        roundsCombo = new JComboBox<>(new String[]{"3 Rounds", "5 Rounds", "10 Rounds"});
        diffCombo = new JComboBox<>(new String[]{"Mixed", "Easy", "Medium", "Hard"});
        hostSettingsPanel.add(roundsCombo);
        hostSettingsPanel.add(diffCombo);
        
        bg.gridy = 0;
        bottom.add(hostSettingsPanel, bg);
        
        bg.gridy = 1;
        startBtn = new StyledButton("START GAME", ACCENT_GREEN);
        startBtn.setPreferredSize(new Dimension(0, 45));
        startBtn.addActionListener(e -> {
            int rounds = Integer.parseInt(roundsCombo.getSelectedItem().toString().split(" ")[0]);
            String diff = diffCombo.getSelectedItem().toString();
            GameMessage msg = new GameMessage(GameMessage.Type.START);
            msg.number = rounds;
            msg.text = diff;
            sendMsg(msg);
        });
        bottom.add(startBtn, bg);

        bg.gridy = 2;
        waitingLabel = new JLabel("Waiting for host...", SwingConstants.CENTER);
        waitingLabel.setForeground(TEXT_MUTED);
        bottom.add(waitingLabel, bg);

        p.add(bottom, g);
        return p;
    }

    // ─── GAME SCREEN (PRO LAYOUT) ────────────────────────────────────────────
    JPanel makeGameScreen() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(10, 15, 10, 15));

        // Top Bar
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        roundLabel = new JLabel("R1");
        roundLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        roundLabel.setForeground(TEXT_MUTED);
        top.add(roundLabel, BorderLayout.WEST);
        timerLabel = new JLabel("15");
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        timerLabel.setForeground(Color.WHITE);
        top.add(timerLabel, BorderLayout.EAST);
        timerBar = new JProgressBar(0, 15);
        timerBar.setPreferredSize(new Dimension(0, 5));
        timerBar.setForeground(ACCENT_BLUE);
        timerBar.setBackground(BG_DARK);
        top.add(timerBar, BorderLayout.SOUTH);
        p.add(top, BorderLayout.NORTH);

        // Center (Question + Options)
        JPanel main = new JPanel(new GridBagLayout());
        main.setOpaque(false);
        GridBagConstraints mc = new GridBagConstraints();
        mc.fill = GridBagConstraints.BOTH;
        mc.weightx = 1.0; mc.gridx = 0;

        questionText = new JLabel("Question Loading...");
        questionText.setFont(new Font("Segoe UI", Font.BOLD, 18));
        questionText.setForeground(Color.WHITE);
        questionText.setHorizontalAlignment(SwingConstants.CENTER);
        mc.gridy = 0; mc.weighty = 0.4;
        main.add(questionText, mc);

        JPanel opts = new JPanel(new GridLayout(2, 2, 10, 10));
        opts.setOpaque(false);
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            optBtns[i] = new StyledButton("...", BG_LIGHT);
            optBtns[i].setFont(new Font("Segoe UI", Font.BOLD, 14));
            optBtns[i].addActionListener(e -> submitAnswer(idx));
            opts.add(optBtns[i]);
        }
        mc.gridy = 1; mc.weighty = 0.6;
        main.add(opts, mc);
        p.add(main, BorderLayout.CENTER);

        // Sidebar
        GlassPanel side = new GlassPanel(new BorderLayout(), 10);
        side.setPreferredSize(new Dimension(140, 0));
        scoreboardPanel = new JPanel();
        scoreboardPanel.setLayout(new BoxLayout(scoreboardPanel, BoxLayout.Y_AXIS));
        scoreboardPanel.setOpaque(false);
        side.add(new JScrollPane(scoreboardPanel) {{
            setOpaque(false); getViewport().setOpaque(false); setBorder(null);
        }}, BorderLayout.CENTER);
        p.add(side, BorderLayout.EAST);

        return p;
    }

    // ─── RESULT SCREEN ───────────────────────────────────────────────────────
    JPanel makeResultScreen() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("FINISH!", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(ACCENT_PURPLE);
        p.add(title, BorderLayout.NORTH);

        podiumPanel = new JPanel(new GridBagLayout());
        podiumPanel.setOpaque(false);
        p.add(podiumPanel, BorderLayout.CENTER);

        JButton again = new StyledButton("MAIN MENU", ACCENT_BLUE);
        again.setPreferredSize(new Dimension(0, 45));
        again.addActionListener(e -> {
            intentionalDisconnect = true;
            try { if(out != null) sock.close(); } catch(Exception ex){}
            cards.show(root, "JOIN");
        });
        p.add(again, BorderLayout.SOUTH);

        return p;
    }

    // ─── LOGIC & NETWORK ─────────────────────────────────────────────────────
    void connectAndJoin(String room) {
        intentionalDisconnect = false;
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
                if (!intentionalDisconnect) {
                    SwingUtilities.invokeLater(() -> showProPopup("Failed", "Server offline.", true));
                }
            }
        }).start();
    }

    void handleMessage(GameMessage msg) {
        switch (msg.type) {
            case PLAYER_LIST: updateLobby(msg.names, msg.scores); updateScoreboard(msg.names, msg.scores); break;
            case QUESTION:
                cards.show(root, "GAME");
                questionText.setText("<html><center>" + msg.text + "</center></html>");
                roundLabel.setText("R" + msg.number);
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
                break;
            case RESULT:
                correctAnswer = msg.number;
                for (int i = 0; i < 4; i++) {
                    optBtns[i].setEnabled(false);
                    if (i == msg.number) optBtns[i].setBackground(ACCENT_GREEN);
                }
                break;
            case EVENT: break;
            case GAMEOVER: buildPodium(msg.names, msg.scores); cards.show(root, "RESULT"); break;
            case ERROR: showProPopup("Error", msg.text, true); break;
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
        try { if (out != null) { out.reset(); out.writeObject(msg); out.flush(); } } catch (Exception e) {}
    }

    void updateLobby(String[] names, int[] scores) {
        lobbyListPanel.removeAll();
        for (String n : names) {
            JLabel lbl = new JLabel(n);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lbl.setForeground(Color.WHITE);
            lbl.setBorder(new EmptyBorder(5, 10, 5, 10));
            lobbyListPanel.add(lbl);
        }
        lobbyListPanel.revalidate(); lobbyListPanel.repaint();
    }

    void updateScoreboard(String[] names, int[] scores) {
        if (names == null) return;
        scoreboardPanel.removeAll();
        Integer[] idx = new Integer[names.length];
        for (int i = 0; i < idx.length; i++) idx[i] = i;
        java.util.Arrays.sort(idx, (a, b) -> scores[b] - scores[a]);
        for (int i = 0; i < idx.length; i++) {
            JLabel l = new JLabel((i+1) + ". " + names[idx[i]] + " (" + scores[idx[i]] + ")");
            l.setFont(new Font("Segoe UI", Font.BOLD, 11));
            l.setForeground(ACCENT_GREEN);
            scoreboardPanel.add(l);
        }
        scoreboardPanel.revalidate(); scoreboardPanel.repaint();
    }

    void buildPodium(String[] names, int[] scores) {
        if (names == null || names.length == 0) return;
        podiumPanel.removeAll();
        Integer[] idx = new Integer[names.length];
        for (int i = 0; i < idx.length; i++) idx[i] = i;
        java.util.Arrays.sort(idx, (a, b) -> scores[b] - scores[a]);
        int[] order = {1, 0, 2}, heights = {60, 100, 40};
        Color[] colors = {Color.LIGHT_GRAY, Color.YELLOW, new Color(205, 127, 50)};
        for (int i = 0; i < 3; i++) {
            int rank = order[i];
            if (rank < names.length) {
                JPanel col = new JPanel(new BorderLayout()); col.setOpaque(false);
                JLabel n = new JLabel(names[idx[rank]], SwingConstants.CENTER);
                n.setFont(new Font("Segoe UI", Font.BOLD, 12)); n.setForeground(Color.WHITE);
                col.add(n, BorderLayout.NORTH);
                JPanel b = new JPanel(); b.setBackground(colors[rank]); b.setPreferredSize(new Dimension(60, heights[i]));
                col.add(b, BorderLayout.SOUTH);
                GridBagConstraints g = new GridBagConstraints(); g.gridx = i; g.anchor = GridBagConstraints.SOUTH;
                g.insets = new Insets(0, 5, 0, 5);
                podiumPanel.add(col, g);
            }
        }
    }

    // ─── CUSTOM COMPONENTS ───────────────────────────────────────────────────
    void showProPopup(String titleStr, String message, boolean isError) {
        JDialog dialog = new JDialog(this, titleStr, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        JPanel panel = new JPanel(new BorderLayout(10, 10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(25, 25, 35, 250)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(new Color(255, 255, 255, 30)); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
            }
        };
        panel.setOpaque(false); panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel title = new JLabel(titleStr.toUpperCase(), SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(isError ? ACCENT_RED : ACCENT_BLUE);
        panel.add(title, BorderLayout.NORTH);
        JLabel msg = new JLabel("<html><center>" + message + "</center></html>", SwingConstants.CENTER);
        msg.setForeground(Color.WHITE); panel.add(msg, BorderLayout.CENTER);
        JButton ok = new StyledButton("OK", ACCENT_BLUE); ok.addActionListener(e -> dialog.dispose());
        panel.add(ok, BorderLayout.SOUTH);
        dialog.add(panel); dialog.pack(); dialog.setLocationRelativeTo(this); dialog.setVisible(true);
    }

    class GlassPanel extends JPanel {
        int r; public GlassPanel(LayoutManager lm, int r) { super(lm); this.r = r; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 15)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), r, r);
            g2.setColor(new Color(255, 255, 255, 40)); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, r, r);
        }
    }

    class StyledButton extends JButton {
        Color b; public StyledButton(String t, Color b) { super(t); this.b = b; setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false); setForeground(Color.WHITE); setFont(new Font("Segoe UI", Font.BOLD, 13)); setCursor(new Cursor(Cursor.HAND_CURSOR)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isPressed() ? b.darker() : getModel().isRollover() ? b.brighter() : b);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception e){}
        SwingUtilities.invokeLater(() -> new GameClient().setVisible(true));
    }
}
