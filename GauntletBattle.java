import javax.swing.*;
import java.awt.*;


// ============================================================
//  The full Gauntlet (AI) battle screen.
//  OOP Pillars:
//    Encapsulation  — battle state is private, exposed via methods
//    Inheritance    — uses Combatant references (GauntletPlayer + Enemy)
//    Abstraction    — calls combatant.decideAction() without caring who it is
//    Polymorphism   — same method call, different behavior per subclass
// ============================================================
public class GauntletBattle extends JPanel  {

    private final GameGUI mainFrame;
    private Image battlefieldImage = null;

    private void loadBattlefield(String mapName) {
        String[][] mapData = {
            { "Asgard",         "asgardgamebg"        },
            { "Avengers Tower", "avengerstowercover"   },
            { "Avengers HQ",    "avengerstowerinside"  },
            { "City Court",     "citubballcourt"       },
            { "Jollibee Arena", "jollibeeinside"       },
            { "Nyan Realm",     "nyanmap"              },
            { "Random Stage",   "randompicture"        },
            { "Sokovia",        "sokoviagamemap"       },
            { "Titan",          "titangame"            },
            { "Wakanda",        "wakandacover"         },
            { "Wakanda Inside", "wakandainside"        },
        };
        String key = null;
        for (String[] entry : mapData)
            if (entry[0].equals(mapName)) { key = entry[1]; break; }
        if (key == null) { battlefieldImage = null; return; }

        java.io.File png = new java.io.File("background/" + key + ".png");
        if (png.exists()) { battlefieldImage = new ImageIcon("background/" + key + ".png").getImage(); return; }
        java.io.File jpg = new java.io.File("background/" + key + ".jpg");
        if (jpg.exists()) { battlefieldImage = new ImageIcon("background/" + key + ".jpg").getImage(); return; }
        battlefieldImage = null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (battlefieldImage != null)
            g.drawImage(battlefieldImage, 0, 0, getWidth(), getHeight(), this);
    }
    private GauntletPlayer player;
    private Enemy          ai;

    private int     playerScore  = 0;
    private int     aiScore      = 0;
    private int     currentRound = 1;
    private boolean playerTurn   = true;
    private boolean roundOver    = false;
    private boolean matchOver    = false;

    // ---- UI Components -------------------------------------
    private JLabel roundLabel, scoreLabel;

    private JLabel       playerNameLabel, playerHpLabel, playerManaLabel;
    private JProgressBar playerHpBar, playerManaBar;

    private JLabel       aiNameLabel, aiHpLabel, aiManaLabel;
    private JProgressBar aiHpBar, aiManaBar;

    private JTextArea   battleLog;
    private JScrollPane logScroll;

    private JButton btnBasic, btnSkill1, btnSkill2, btnSkill3, btnUlt;
    private JButton btnNext;    // "Next Round" / "Back to Menu"
    private JButton btnForfeit; // Forfeit & Return

    private JLabel turnIndicator;

    // =========================================================
    //  Constructors
    // =========================================================
    public GauntletBattle(GameGUI frame, String heroName) {
        this.mainFrame = frame;
        this.player    = GauntletPlayer.fromName(heroName);
        this.ai        = Enemy.getRandom();

        buildUI();
        initBattleLog();
        syncBarMaximums();
    }

    // Overload — accepts a map name (used when launched from Maps screen)
    public GauntletBattle(GameGUI frame, String heroName, String mapName) {
        this(frame, heroName);
        loadBattlefield(mapName);
        logMessage("Stage: " + mapName + "\n");
    }

    // =========================================================
    //  UI Construction
    // =========================================================
    private void buildUI() {
        setLayout(new BorderLayout(8, 8));
        setOpaque(true); // paintComponent draws the battlefield
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildArena(),     BorderLayout.CENTER);
        add(buildActionBar(), BorderLayout.SOUTH);
    }

    // Top bar: round label + score
    private JPanel buildTopBar() {
        JPanel top = new JPanel(new GridLayout(1, 2));
        top.setOpaque(false);

        roundLabel = makeLabel("ROUND 1 / 3", 22, new Color(255, 215, 0));
        scoreLabel = makeLabel("YOU: 0  |  AI: 0", 20, Color.WHITE);
        roundLabel.setHorizontalAlignment(SwingConstants.LEFT);
        scoreLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        top.add(roundLabel);
        top.add(scoreLabel);
        return top;
    }

    // Arena: player panel | battle log | AI panel
    private JPanel buildArena() {
        JPanel arena = new JPanel(new BorderLayout(8, 8));
        arena.setOpaque(false);
        arena.add(buildFighterPanel(true),  BorderLayout.WEST);
        arena.add(buildLogPanel(),          BorderLayout.CENTER);
        arena.add(buildFighterPanel(false), BorderLayout.EAST);
        return arena;
    }

    // Fighter stat panel — reused for player and AI
    private JPanel buildFighterPanel(boolean isPlayer) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(25, 25, 35));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(
                isPlayer ? new Color(0, 180, 255) : new Color(220, 50, 50), 2),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        p.setPreferredSize(new Dimension(230, 0));

        if (isPlayer) {
            playerNameLabel = makeLabel(player.getName(), 18, new Color(0, 200, 255));
            playerHpLabel   = makeLabel("HP: "   + player.getHp()   + " / " + player.getMaxHp(),   14, Color.WHITE);
            playerManaLabel = makeLabel("MANA: " + player.getMana() + " / " + player.getMaxMana(), 14, new Color(120, 120, 255));
            playerHpBar     = makeBar(player.getHp(),   player.getMaxHp(),   new Color(50, 200, 80));
            playerManaBar   = makeBar(player.getMana(), player.getMaxMana(), new Color(80, 80, 240));

            p.add(makeLabel("[ PLAYER ]", 13, new Color(0, 200, 255)));
            p.add(Box.createVerticalStrut(4));
            p.add(playerNameLabel);
            p.add(Box.createVerticalStrut(8));
            p.add(playerHpLabel);
            p.add(playerHpBar);
            p.add(Box.createVerticalStrut(6));
            p.add(playerManaLabel);
            p.add(playerManaBar);
        } else {
            aiNameLabel   = makeLabel(ai.getName(), 18, new Color(255, 80, 80));
            aiHpLabel     = makeLabel("HP: "   + ai.getHp()   + " / " + ai.getMaxHp(),   14, Color.WHITE);
            aiManaLabel   = makeLabel("MANA: " + ai.getMana() + " / " + ai.getMaxMana(), 14, new Color(120, 120, 255));
            aiHpBar       = makeBar(ai.getHp(),   ai.getMaxHp(),   new Color(200, 50, 50));
            aiManaBar     = makeBar(ai.getMana(), ai.getMaxMana(), new Color(80, 80, 240));

            p.add(makeLabel("[ AI OPPONENT ]", 13, new Color(255, 80, 80)));
            p.add(Box.createVerticalStrut(4));
            p.add(aiNameLabel);
            p.add(Box.createVerticalStrut(8));
            p.add(aiHpLabel);
            p.add(aiHpBar);
            p.add(Box.createVerticalStrut(6));
            p.add(aiManaLabel);
            p.add(aiManaBar);
        }
        return p;
    }

    // Battle log + turn indicator
    private JPanel buildLogPanel() {
        JPanel lp = new JPanel(new BorderLayout(0, 6));
        lp.setOpaque(false);

        turnIndicator = makeLabel("YOUR TURN", 20, new Color(255, 215, 0));
        turnIndicator.setHorizontalAlignment(SwingConstants.CENTER);

        battleLog = new JTextArea();
        battleLog.setEditable(false);
        battleLog.setLineWrap(true);
        battleLog.setWrapStyleWord(true);
        battleLog.setFont(new Font("Consolas", Font.PLAIN, 13));
        battleLog.setBackground(new Color(10, 10, 15));
        battleLog.setForeground(new Color(200, 200, 200));
        battleLog.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        logScroll = new JScrollPane(battleLog);
        logScroll.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 70)));

        lp.add(turnIndicator, BorderLayout.NORTH);
        lp.add(logScroll,     BorderLayout.CENTER);
        return lp;
    }

    // Action bar: skill buttons row + nav row (Next Round hidden + Forfeit)
    private JPanel buildActionBar() {
        JPanel outer = new JPanel(new BorderLayout(6, 6));
        outer.setBackground(new Color(15, 15, 22));
        outer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(50, 50, 80)),
            BorderFactory.createEmptyBorder(10, 10, 12, 10)
        ));

        // Skill buttons
        JPanel btnRow = new JPanel(new GridLayout(1, 5, 8, 0));
        btnRow.setOpaque(false);

        btnBasic  = makeActionBtn("BASIC\nATTACK",
                new Color(60, 60, 80), 0);
        btnSkill1 = makeActionBtn(player.getSkill1Name() + "\n[" + player.getSk1Cost() + " MP]",
                new Color(30, 80, 130), 1);
        btnSkill2 = makeActionBtn(player.getSkill2Name() + "\n[" + player.getSk2Cost() + " MP]",
                new Color(30, 80, 130), 2);
        btnSkill3 = makeActionBtn(player.getSkill3Name() + "\n[" + player.getSk3Cost() + " MP]",
                new Color(30, 80, 130), 3);
        btnUlt    = makeActionBtn(player.getUltimateName() + "\n[" + player.getUltCost() + " MP]",
                new Color(100, 30, 30), 4);

        btnRow.add(btnBasic);
        btnRow.add(btnSkill1);
        btnRow.add(btnSkill2);
        btnRow.add(btnSkill3);
        btnRow.add(btnUlt);
        outer.add(btnRow, BorderLayout.CENTER);

        // Nav row
        JPanel navRow = new JPanel(new BorderLayout(8, 0));
        navRow.setOpaque(false);
        navRow.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        btnNext = new JButton("NEXT ROUND ▶");
        btnNext.setFont(new Font("Impact", Font.PLAIN, 18));
        btnNext.setForeground(Color.WHITE);
        btnNext.setBackground(new Color(40, 120, 40));
        btnNext.setFocusPainted(false);
        btnNext.setVisible(false);
        btnNext.addActionListener(e -> handleNextRound());

        btnForfeit = new JButton("FORFEIT & RETURN");
        btnForfeit.setFont(new Font("Arial", Font.BOLD, 14));
        btnForfeit.setForeground(Color.WHITE);
        btnForfeit.setBackground(new Color(90, 25, 25));
        btnForfeit.setFocusPainted(false);
        btnForfeit.setBorder(BorderFactory.createLineBorder(new Color(150, 40, 40), 1));
        btnForfeit.addActionListener(e -> handleForfeit());

        navRow.add(btnNext,    BorderLayout.CENTER);
        navRow.add(btnForfeit, BorderLayout.EAST);
        outer.add(navRow, BorderLayout.SOUTH);
        return outer;
    }

    private JButton makeActionBtn(String text, Color bg, int actionIndex) {
        JButton b = new JButton("<html><center>" + text.replace("\n", "<br>") + "</center></html>");
        b.setFont(new Font("Arial", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(0, 65));
        b.setBorder(BorderFactory.createLineBorder(bg.brighter(), 1));
        b.addActionListener(e -> handlePlayerAction(actionIndex));
        return b;
    }

    // =========================================================
    //  Initial log — called after UI is built
    // =========================================================
    private void initBattleLog() {
        logMessage("=== GAUNTLET MODE — BEST OF 3 ===");
        logMessage("You: " + player.getName() + "  vs  AI: " + ai.getName());
        logMessage("Round " + currentRound + " — FIGHT!\n");
    }

    // =========================================================
    //  Forfeit
    // =========================================================
    private void handleForfeit() {
        if (matchOver) {
            mainFrame.navigateTo("main");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to forfeit?\nYour progress this match will be lost.",
            "Forfeit Match",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            logMessage("\n⚑ You forfeited the match.");
            mainFrame.navigateTo("main");
        }
    }

    // =========================================================
    //  Core Turn Logic — POLYMORPHISM: same decideAction() call,
    //  GauntletAI returns 0-4, GauntletPlayer returns -1 (never reached here)
    // =========================================================
    private void handlePlayerAction(int actionIndex) {
        if (!playerTurn || roundOver || matchOver) return;

        // --- PLAYER TURN ---
        // FIX: check canUse FIRST for the log message, then call useAction
        // useAction() has its own internal fallback but we want the correct log
        boolean hadEnoughMana = player.canUse(actionIndex);
        int     damage        = player.useAction(actionIndex);   // handles mana deduction + fallback

        if (!hadEnoughMana && actionIndex != 0) {
            logMessage("⚠ Not enough mana! Used Basic Attack instead.");
        } else {
            logMessage("▶ YOU use " + player.getActionName(actionIndex) + "!");
        }

        // FIX: takeDamage() returns int (actual damage dealt) — store it correctly
        int actualDmg = ai.takeDamage(damage);
        logMessage("  → " + ai.getName() + " takes " + actualDmg + " damage!");

        refreshUI();

        if (!ai.isAlive()) {
            endRound(true);
            return;
        }

        // --- AI TURN after delay ---
        playerTurn = false;
        updateTurnIndicator();
        setActionsEnabled(false);

        Timer aiTimer = new Timer(900, e -> {
            // POLYMORPHISM — GauntletAI.decideAction() returns its smart choice
            int aiAction = ai.decideAction();

            // FIX: check canUse before useAction for correct log message
            boolean aiHadMana = ai.canUse(aiAction);
            int     aiDamage  = ai.useAction(aiAction);

            if (!aiHadMana && aiAction != 0) {
                logMessage("⚡ AI couldn't afford skill — used Basic Attack.");
            } else {
                logMessage("⚡ AI uses " + ai.getActionName(aiAction) + "!");
            }

            // FIX: takeDamage() returns int — store correctly
            int aiActualDmg = player.takeDamage(aiDamage);
            logMessage("  → " + player.getName() + " takes " + aiActualDmg + " damage!");

            refreshUI();

            if (!player.isAlive()) {
                endRound(false);
            } else {
                playerTurn = true;
                updateTurnIndicator();
                setActionsEnabled(true);
                logMessage("");
            }
        });
        aiTimer.setRepeats(false);
        aiTimer.start();
    }

    private void endRound(boolean playerWon) {
        roundOver = true;
        setActionsEnabled(false);

        if (playerWon) {
            playerScore++;
            logMessage("\n★ YOU WIN ROUND " + currentRound + "! ★");
        } else {
            aiScore++;
            logMessage("\n✖ AI WINS ROUND " + currentRound + ".");
        }

        scoreLabel.setText("YOU: " + playerScore + "  |  AI: " + aiScore);

        if (playerScore == 2 || aiScore == 2) {
            endMatch(playerScore == 2);
        } else {
            // FIX: increment BEFORE setting the button label so it reads the correct next round
            currentRound++;
            btnNext.setVisible(true);
            btnNext.setText("NEXT ROUND ▶  (Round " + currentRound + " of 3)");
            logMessage("Click NEXT ROUND to continue.");
        }
    }

    private void endMatch(boolean playerWon) {
        matchOver = true;
        turnIndicator.setText(playerWon ? "VICTORY!" : "DEFEATED");
        turnIndicator.setForeground(playerWon ? new Color(255, 215, 0) : new Color(220, 50, 50));

        logMessage("\n══════════════════════════════");
        if (playerWon) {
            logMessage("  MISSION COMPLETE! You win 2-" + aiScore + "!");
        } else {
            logMessage("  MISSION FAILED. AI wins 2-" + playerScore + ".");
        }
        logMessage("══════════════════════════════");

        btnNext.setVisible(true);
        btnNext.setText("BACK TO MENU");
        btnNext.setBackground(new Color(80, 40, 100));
        btnForfeit.setText("RETURN TO MENU");
    }

    private void handleNextRound() {
        if (matchOver) {
            mainFrame.navigateTo("main");
            return;
        }

        // Reset both combatants for the new round (resets HP + mana via Combatant)
        player.resetForNewRound();
        ai.resetForNewRound();

        roundOver  = false;
        playerTurn = true;

        // FIX: currentRound was already incremented in endRound() so use it directly
        roundLabel.setText("ROUND " + currentRound + " / 3");
        btnNext.setVisible(false);
        setActionsEnabled(true);
        updateTurnIndicator();
        refreshUI();

        logMessage("\n--- ROUND " + currentRound + " BEGIN ---\n");
    }

    // =========================================================
    //  UI Refresh Helpers
    // =========================================================
 
    public void refreshUI() {
        // Player
        playerHpLabel.setText("HP: "   + player.getHp()   + " / " + player.getMaxHp());
        playerHpBar.setValue(player.getHp());
        playerManaLabel.setText("MANA: " + player.getMana() + " / " + player.getMaxMana());
        playerManaBar.setValue(player.getMana());

        // AI
        aiHpLabel.setText("HP: "   + ai.getHp()   + " / " + ai.getMaxHp());
        aiHpBar.setValue(ai.getHp());
        aiManaLabel.setText("MANA: " + ai.getMana() + " / " + ai.getMaxMana());
        aiManaBar.setValue(ai.getMana());

        // Grey out skills the player can't afford
        btnSkill1.setEnabled(player.canUse(1));
        btnSkill2.setEnabled(player.canUse(2));
        btnSkill3.setEnabled(player.canUse(3));
        btnUlt.setEnabled(player.canUse(4));
    }

    /** Called once after UI is built to set bar maximums correctly */
    private void syncBarMaximums() {
        playerHpBar.setMaximum(player.getMaxHp());
        playerHpBar.setValue(player.getHp());
        playerManaBar.setMaximum(player.getMaxMana());
        playerManaBar.setValue(player.getMana());

        aiHpBar.setMaximum(ai.getMaxHp());
        aiHpBar.setValue(ai.getHp());
        aiManaBar.setMaximum(ai.getMaxMana());
        aiManaBar.setValue(ai.getMana());
    }

    private void updateTurnIndicator() {
        if (playerTurn) {
            turnIndicator.setText("YOUR TURN");
            turnIndicator.setForeground(new Color(255, 215, 0));
        } else {
            turnIndicator.setText("AI IS THINKING...");
            turnIndicator.setForeground(new Color(255, 100, 100));
        }
    }

    
    public void setActionsEnabled(boolean enabled) {
        btnBasic.setEnabled(enabled);
        // Skills only enabled if player also has enough mana
        btnSkill1.setEnabled(enabled && player.canUse(1));
        btnSkill2.setEnabled(enabled && player.canUse(2));
        btnSkill3.setEnabled(enabled && player.canUse(3));
        btnUlt.setEnabled(enabled && player.canUse(4));
    }

    
    public void logMessage(String msg) {
        battleLog.append(msg + "\n");
        battleLog.setCaretPosition(battleLog.getDocument().getLength());
    }

    // =========================================================
    //  Widget Factories
    // =========================================================
    private JLabel makeLabel(String text, int size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Impact", Font.PLAIN, size));
        l.setForeground(color);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JProgressBar makeBar(int value, int max, Color color) {
        JProgressBar bar = new JProgressBar(0, max);
        bar.setValue(value);
        bar.setForeground(color);
        bar.setBackground(new Color(40, 40, 50));
        bar.setBorderPainted(false);
        bar.setPreferredSize(new Dimension(0, 12));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        return bar;
    }
}