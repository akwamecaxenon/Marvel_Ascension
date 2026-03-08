import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * PvpBattleArena — Local 2-player turn-based combat.
 * Best of 3 rounds — first to win 2 rounds wins the match.
 *
 * Portrait loading priority:
 *   1. gifs/<herokey>.gif   (animated)
 *   2. images/<herokey>.png (still)
 *   3. Coloured initials fallback
 */
public class PvpBattleArena extends JPanel {

    private final GameGUI mainFrame;
    private Image battlefieldImage = null;

    // ── Load battlefield background from background/ folder ────────────────
    private void loadBattlefield(String mapName) {
        // Convert display name back to file key using the same MAP_DATA mapping
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

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (battlefieldImage != null)
            g.drawImage(battlefieldImage, 0, 0, getWidth(), getHeight(), this);
    }

    // ── Fighter state ──────────────────────────────────────────────────────
    private static class Fighter {
        String name, skill1, skill2, skill3, ultimate;
        int hp, maxHp, attack;
        int mana = 50;
        static final int MAX_MANA = 100;
        static final int SK1_COST = 20, SK2_COST = 25, SK3_COST = 30, ULT_COST = 50;

        Fighter(CharacterSelector.CharacterData d) {
            name = d.name; maxHp = d.hp; hp = d.hp; attack = d.attack;
            skill1 = d.skill1; skill2 = d.skill2;
            skill3 = d.skill3; ultimate = d.ultimate;
        }

        void resetForNewRound() { hp = maxHp; mana = 50; }

        boolean isAlive()           { return hp > 0; }
        int     takeDamage(int dmg) { int act = Math.max(1, dmg); hp = Math.max(0, hp - act); return act; }
        void    restoreMana(int v)  { mana = Math.min(MAX_MANA, mana + v); }
        boolean canUse(int idx) {
            switch (idx) {
                case 1: return mana >= SK1_COST;
                case 2: return mana >= SK2_COST;
                case 3: return mana >= SK3_COST;
                case 4: return mana >= ULT_COST;
                default: return true;
            }
        }
        String skillName(int idx) {
            switch (idx) {
                case 0: return "Basic Attack";
                case 1: return skill1; case 2: return skill2;
                case 3: return skill3; case 4: return ultimate;
                default: return "?";
            }
        }
        int calcDamage(int idx) {
            switch (idx) {
                case 0: return attack;
                case 1: return attack + 5;  case 2: return attack + 10;
                case 3: return attack + 15; case 4: return attack * 2;
                default: return attack;
            }
        }
        int useAction(int idx) {
            if (!canUse(idx)) { restoreMana(10); return attack; }
            int cost = new int[]{0, SK1_COST, SK2_COST, SK3_COST, ULT_COST}[idx];
            mana -= cost;
            if (idx == 0) restoreMana(10);
            else          restoreMana(5);
            return calcDamage(idx);
        }
        String imgKey() { return name.replace(" ", "").toLowerCase(); }
    }

    private Fighter p1, p2;
    private boolean p1Turn    = true;
    private boolean gameOver  = false;  // whole match done
    private boolean roundOver = false;  // waiting for Next Round click
    private String  mapName   = "";

    // ── Round / score tracking ─────────────────────────────────────────────
    private static final int MAX_ROUNDS    = 3;
    private static final int ROUNDS_TO_WIN = 2; // first to 2 wins match
    private int currentRound = 1;
    private int p1Wins = 0, p2Wins = 0;

    // ── UI fields ──────────────────────────────────────────────────────────
    private JLabel       turnBanner;
    private JLabel       roundLabel;   // "ROUND 1 / 3"
    private JLabel       scoreLabel;   // "P1: ★☆  |  P2: ☆☆"

    private JLabel       p1Portrait,  p2Portrait;
    private JLabel       p1NameLbl,   p2NameLbl;
    private JLabel       p1HpLbl,     p2HpLbl;
    private JLabel       p1ManaLbl,   p2ManaLbl;
    private JProgressBar p1HpBar,     p2HpBar;
    private JProgressBar p1ManaBar,   p2ManaBar;

    private JTextArea    battleLog;
    private JButton      btnBasic, btnSkill1, btnSkill2, btnSkill3, btnUlt;
    private JButton      btnForfeit;
    private JButton      btnNextRound;
    private JLabel       actionPrompt;

    private CharacterSelector.CharacterData savedD1, savedD2;

    // ── Constructor ────────────────────────────────────────────────────────
    public PvpBattleArena(GameGUI frame) {
        this.mainFrame = frame;
        buildUI();
    }

    // ── Entry point — resets everything for a fresh match ─────────────────
    public void startBattle(CharacterSelector.CharacterData d1,
                            CharacterSelector.CharacterData d2,
                            String map) {
        savedD1 = d1; savedD2 = d2;
        mapName = map;
        loadBattlefield(map);
        p1 = new Fighter(d1);
        p2 = new Fighter(d2);

        currentRound = 1;
        p1Wins = 0;
        p2Wins = 0;

        loadPortrait(p1Portrait, p1.imgKey(), true);
        loadPortrait(p2Portrait, p2.imgKey(), false);

        battleLog.setText("");
        startRound();
    }

    // ── Start a round ──────────────────────────────────────────────────────
    private void startRound() {
        p1Turn    = true;
        gameOver  = false;
        roundOver = false;

        p1.resetForNewRound();
        p2.resetForNewRound();

        btnNextRound.setVisible(false);
        setActionsEnabled(true);

        logMessage("══════════════════════════════════");
        logMessage("  ROUND " + currentRound + " of " + MAX_ROUNDS
            + "   |   " + p1.name + "  vs  " + p2.name);
        logMessage("══════════════════════════════════\n");

        syncAll();
        updateRoundLabel();
        updateScoreLabel();
        updateTurnBanner();
    }

    // ── Portrait loader — GIF → PNG → initials ─────────────────────────────
    private void loadPortrait(JLabel lbl, String key, boolean isP1) {
        File gif = new File("gifs/" + key + ".gif");
        if (gif.exists()) {
            ImageIcon icon = new ImageIcon("gifs/" + key + ".gif");
            lbl.setIcon(new ImageIcon(icon.getImage().getScaledInstance(160, 160, Image.SCALE_DEFAULT)));
            lbl.setText(""); return;
        }
        File png = new File("images/" + key + ".png");
        if (png.exists()) {
            ImageIcon icon = new ImageIcon("images/" + key + ".png");
            lbl.setIcon(new ImageIcon(icon.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH)));
            lbl.setText(""); return;
        }
        lbl.setIcon(null);
        lbl.setText(key.length() >= 2 ? key.substring(0, 2).toUpperCase() : key.toUpperCase());
        lbl.setFont(new Font("Impact", Font.PLAIN, 48));
        lbl.setForeground(Color.WHITE);
        lbl.setBackground(isP1 ? new Color(0, 80, 30) : new Color(100, 20, 20));
        lbl.setOpaque(true);
    }

    // ── UI builder ─────────────────────────────────────────────────────────
    private void buildUI() {
        setLayout(new BorderLayout(8, 8));
        setOpaque(true); // paintComponent draws the battlefield
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildArena(),     BorderLayout.CENTER);
        add(buildActionBar(), BorderLayout.SOUTH);
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout(4, 4));
        top.setOpaque(false);

        roundLabel = new JLabel("ROUND 1 / 3", SwingConstants.LEFT);
        roundLabel.setFont(new Font("Impact", Font.PLAIN, 18));
        roundLabel.setForeground(new Color(180, 180, 255));
        roundLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 0));

        turnBanner = new JLabel("PLAYER 1'S TURN", SwingConstants.CENTER);
        turnBanner.setFont(new Font("Impact", Font.PLAIN, 28));
        turnBanner.setForeground(new Color(0, 220, 80));
        turnBanner.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        scoreLabel = new JLabel("P1: ☆☆  |  P2: ☆☆", SwingConstants.RIGHT);
        scoreLabel.setFont(new Font("Impact", Font.PLAIN, 18));
        scoreLabel.setForeground(new Color(255, 215, 0));
        scoreLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 4));

        top.add(roundLabel,  BorderLayout.WEST);
        top.add(turnBanner,  BorderLayout.CENTER);
        top.add(scoreLabel,  BorderLayout.EAST);
        return top;
    }

    private JPanel buildArena() {
        JPanel arena = new JPanel(new BorderLayout(8, 8));
        arena.setOpaque(false);
        arena.add(buildFighterPanel(true),  BorderLayout.WEST);
        arena.add(buildLogPanel(),          BorderLayout.CENTER);
        arena.add(buildFighterPanel(false), BorderLayout.EAST);
        return arena;
    }

    private JPanel buildFighterPanel(boolean isP1) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(22, 22, 32));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isP1 ? new Color(0, 200, 80) : new Color(220, 50, 50), 2),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        p.setPreferredSize(new Dimension(210, 0));

        JLabel role = lbl(isP1 ? "[ PLAYER 1 ]" : "[ PLAYER 2 ]", 13,
                isP1 ? new Color(0, 220, 80) : new Color(255, 80, 80));
        role.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel portrait = new JLabel("", SwingConstants.CENTER);
        portrait.setPreferredSize(new Dimension(160, 160));
        portrait.setMaximumSize(new Dimension(160, 160));
        portrait.setMinimumSize(new Dimension(160, 160));
        portrait.setAlignmentX(Component.CENTER_ALIGNMENT);
        portrait.setHorizontalAlignment(SwingConstants.CENTER);
        portrait.setVerticalAlignment(SwingConstants.CENTER);
        portrait.setBackground(isP1 ? new Color(0, 60, 20) : new Color(80, 15, 15));
        portrait.setOpaque(true);
        portrait.setBorder(BorderFactory.createLineBorder(
                isP1 ? new Color(0, 180, 60) : new Color(200, 40, 40), 2));

        if (isP1) p1Portrait = portrait;
        else      p2Portrait = portrait;

        if (isP1) {
            p1NameLbl = lbl("—", 16, new Color(0, 220, 80));
            p1HpLbl   = lbl("HP: —",   13, Color.WHITE);
            p1ManaLbl = lbl("MANA: —", 13, new Color(120, 120, 255));
            p1HpBar   = bar(100, 100, new Color(50, 200, 80));
            p1ManaBar = bar(50,  100, new Color(80,  80, 240));
            p1NameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            p1HpLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            p1ManaLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(role);      p.add(Box.createVerticalStrut(6));
            p.add(portrait);  p.add(Box.createVerticalStrut(10));
            p.add(p1NameLbl); p.add(Box.createVerticalStrut(6));
            p.add(p1HpLbl);   p.add(p1HpBar);
            p.add(Box.createVerticalStrut(5));
            p.add(p1ManaLbl); p.add(p1ManaBar);
        } else {
            p2NameLbl = lbl("—", 16, new Color(255, 80, 80));
            p2HpLbl   = lbl("HP: —",   13, Color.WHITE);
            p2ManaLbl = lbl("MANA: —", 13, new Color(120, 120, 255));
            p2HpBar   = bar(100, 100, new Color(200, 50, 50));
            p2ManaBar = bar(50,  100, new Color(80,  80, 240));
            p2NameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            p2HpLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            p2ManaLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(role);      p.add(Box.createVerticalStrut(6));
            p.add(portrait);  p.add(Box.createVerticalStrut(10));
            p.add(p2NameLbl); p.add(Box.createVerticalStrut(6));
            p.add(p2HpLbl);   p.add(p2HpBar);
            p.add(Box.createVerticalStrut(5));
            p.add(p2ManaLbl); p.add(p2ManaBar);
        }
        return p;
    }

    private JPanel buildLogPanel() {
        JPanel lp = new JPanel(new BorderLayout());
        lp.setOpaque(false);
        battleLog = new JTextArea();
        battleLog.setEditable(false);
        battleLog.setLineWrap(true);
        battleLog.setWrapStyleWord(true);
        battleLog.setFont(new Font("Consolas", Font.PLAIN, 13));
        battleLog.setBackground(new Color(10, 10, 15));
        battleLog.setForeground(new Color(200, 200, 200));
        battleLog.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JScrollPane scroll = new JScrollPane(battleLog);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 70)));
        lp.add(scroll, BorderLayout.CENTER);
        return lp;
    }

    private JPanel buildActionBar() {
        JPanel outer = new JPanel(new BorderLayout(6, 6));
        outer.setBackground(new Color(15, 15, 22));
        outer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(50, 50, 80)),
            BorderFactory.createEmptyBorder(8, 10, 10, 10)
        ));

        actionPrompt = new JLabel("Choose your action:", SwingConstants.CENTER);
        actionPrompt.setFont(new Font("Arial", Font.BOLD, 14));
        actionPrompt.setForeground(new Color(255, 215, 0));
        actionPrompt.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        outer.add(actionPrompt, BorderLayout.NORTH);

        JPanel row = new JPanel(new GridLayout(1, 5, 8, 0));
        row.setOpaque(false);
        btnBasic  = actionBtn("✕ BASIC",    new Color(60,  60,  80),  0);
        btnSkill1 = actionBtn("SKILL 1",    new Color(30,  80,  130), 1);
        btnSkill2 = actionBtn("SKILL 2",    new Color(30,  80,  130), 2);
        btnSkill3 = actionBtn("SKILL 3",    new Color(30,  80,  130), 3);
        btnUlt    = actionBtn("★ ULTIMATE", new Color(100, 30,  30),  4);
        row.add(btnBasic); row.add(btnSkill1); row.add(btnSkill2);
        row.add(btnSkill3); row.add(btnUlt);
        outer.add(row, BorderLayout.CENTER);

        // Bottom nav: Next Round button (hidden until round ends) + Forfeit
        JPanel nav = new JPanel(new BorderLayout());
        nav.setOpaque(false);

        btnNextRound = new JButton("▶  NEXT ROUND");
        btnNextRound.setFont(new Font("Impact", Font.PLAIN, 18));
        btnNextRound.setForeground(Color.WHITE);
        btnNextRound.setBackground(new Color(30, 100, 180));
        btnNextRound.setFocusPainted(false);
        btnNextRound.setBorder(BorderFactory.createLineBorder(new Color(80, 160, 220), 2));
        btnNextRound.setVisible(false);
        btnNextRound.addActionListener(e -> {
            currentRound++;
            startRound();
        });

        btnForfeit = new JButton("FORFEIT & RETURN");
        btnForfeit.setFont(new Font("Arial", Font.BOLD, 13));
        btnForfeit.setForeground(Color.WHITE);
        btnForfeit.setBackground(new Color(90, 25, 25));
        btnForfeit.setFocusPainted(false);
        btnForfeit.setBorder(BorderFactory.createLineBorder(new Color(150, 40, 40), 1));
        btnForfeit.addActionListener(e -> handleForfeit());

        JPanel navLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        navLeft.setOpaque(false);
        navLeft.add(btnNextRound);

        JPanel navRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        navRight.setOpaque(false);
        navRight.add(btnForfeit);

        nav.add(navLeft,  BorderLayout.WEST);
        nav.add(navRight, BorderLayout.EAST);
        outer.add(nav, BorderLayout.SOUTH);
        return outer;
    }

    // ── Core turn logic ────────────────────────────────────────────────────
    private void handleAction(int actionIndex) {
        if (gameOver || roundOver) return;

        Fighter attacker = p1Turn ? p1 : p2;
        Fighter defender = p1Turn ? p2 : p1;
        String  pLabel   = p1Turn ? "P1" : "P2";

        boolean usedFallback = !attacker.canUse(actionIndex) && actionIndex != 0;
        int     damage       = attacker.useAction(actionIndex);
        int     actual       = defender.takeDamage(damage);

        if (usedFallback) {
            logMessage("⚠ " + pLabel + " can't afford that — used Basic Attack instead!");
        } else {
            logMessage("[" + pLabel + " — " + attacker.name + "]");
            logMessage("  ⚔ " + attacker.skillName(actionIndex) + " → " + actual + " damage!");
        }
        logMessage("  " + defender.name + " HP: " + defender.hp + "/" + defender.maxHp);

        defender.restoreMana(10);
        syncAll();

        if (!defender.isAlive()) {
            endRound(attacker, pLabel);
            return;
        }

        p1Turn = !p1Turn;
        updateTurnBanner();
        updateSkillButtons();
        logMessage("");
    }

    // ── Round end ──────────────────────────────────────────────────────────
    private void endRound(Fighter roundWinner, String winnerLabel) {
        roundOver = true;
        setActionsEnabled(false);

        boolean isP1 = winnerLabel.equals("P1");
        if (isP1) p1Wins++; else p2Wins++;

        logMessage("\n── Round " + currentRound + " Result ──");
        logMessage("  " + winnerLabel + " (" + roundWinner.name + ") wins Round " + currentRound + "!");
        logMessage("  Score — P1: " + p1Wins + "  |  P2: " + p2Wins + "\n");

        updateScoreLabel();

        // Someone clinched the match (2 wins) OR all rounds played
        if (p1Wins >= ROUNDS_TO_WIN || p2Wins >= ROUNDS_TO_WIN || currentRound >= MAX_ROUNDS) {
            endMatch();
            return;
        }

        // Round over, match continues — show Next Round button
        turnBanner.setText("ROUND " + currentRound + " — " + winnerLabel + " WINS!");
        turnBanner.setForeground(new Color(255, 215, 0));
        actionPrompt.setText("Round " + currentRound + " done!   P1: "
            + p1Wins + " win(s)   |   P2: " + p2Wins + " win(s)   —   Click to continue");
        actionPrompt.setForeground(new Color(200, 200, 200));
        btnNextRound.setText("▶  START ROUND " + (currentRound + 1) + "  ◀");
        btnNextRound.setVisible(true);
    }

    // ── Match end ──────────────────────────────────────────────────────────
    private void endMatch() {
        gameOver = true;
        btnNextRound.setVisible(false);

        String matchWinnerLabel = p1Wins > p2Wins ? "P1" : (p2Wins > p1Wins ? "P2" : "DRAW");
        Fighter matchWinner     = p1Wins >= p2Wins ? p1 : p2;

        logMessage("══════════════════════════════════");
        if (p1Wins == p2Wins) {
            logMessage("  IT'S A DRAW!  Score: " + p1Wins + " — " + p2Wins);
        } else {
            logMessage("  MATCH OVER — " + matchWinnerLabel + " (" + matchWinner.name + ") WINS THE MATCH!");
            logMessage("  Final Score — P1: " + p1Wins + "  |  P2: " + p2Wins);
        }
        logMessage("══════════════════════════════════");

        turnBanner.setText(p1Wins == p2Wins
            ? "IT'S A DRAW!"
            : matchWinnerLabel + " — " + matchWinner.name + " WINS! \uD83C\uDFC6");
        turnBanner.setForeground(new Color(255, 215, 0));
        actionPrompt.setText("Match complete!   Final Score — P1: " + p1Wins + "   P2: " + p2Wins);
        actionPrompt.setForeground(new Color(255, 215, 0));

        String resultMsg = p1Wins == p2Wins
            ? "It's a draw!\n\nFinal Score — P1: " + p1Wins + "  |  P2: " + p2Wins
            : matchWinner.name + " wins the match!\n\nFinal Score — P1: " + p1Wins + "  |  P2: " + p2Wins;

        String[] options = {"REMATCH", "MAIN MENU"};
        int choice = JOptionPane.showOptionDialog(this,
            resultMsg + "\n\nPlay again?",
            "MATCH OVER",
            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
            null, options, options[0]);

        if (choice == 0) {
            startBattle(savedD1, savedD2, mapName);
        } else {
            mainFrame.getSelectorPanel().resetSelections();
            mainFrame.navigateTo("main");
        }
    }

    // ── Forfeit ────────────────────────────────────────────────────────────
    private void handleForfeit() {
        if (gameOver) {
            mainFrame.getSelectorPanel().resetSelections();
            mainFrame.navigateTo("main");
            return;
        }
        String pLabel = p1Turn ? "P1" : "P2";
        int choice = JOptionPane.showConfirmDialog(this,
            pLabel + ", are you sure you want to forfeit?\nThis ends the entire match.",
            "Forfeit Match", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            logMessage("\n⚑ " + pLabel + " forfeited the match.");
            mainFrame.getSelectorPanel().resetSelections();
            mainFrame.navigateTo("main");
        }
    }

    // ── UI sync ────────────────────────────────────────────────────────────
    public void refreshUI() { syncAll(); }

    private void syncAll() {
        if (p1 == null || p2 == null) return;
        p1NameLbl.setText(p1.name);
        p1HpLbl.setText("HP: " + p1.hp + " / " + p1.maxHp);
        p1ManaLbl.setText("MANA: " + p1.mana + " / " + Fighter.MAX_MANA);
        p1HpBar.setMaximum(p1.maxHp); p1HpBar.setValue(p1.hp);
        p1ManaBar.setValue(p1.mana);
        colorHp(p1HpBar, p1.hp, p1.maxHp);

        p2NameLbl.setText(p2.name);
        p2HpLbl.setText("HP: " + p2.hp + " / " + p2.maxHp);
        p2ManaLbl.setText("MANA: " + p2.mana + " / " + Fighter.MAX_MANA);
        p2HpBar.setMaximum(p2.maxHp); p2HpBar.setValue(p2.hp);
        p2ManaBar.setValue(p2.mana);
        colorHp(p2HpBar, p2.hp, p2.maxHp);

        updateSkillButtons();
    }

    private void updateRoundLabel() {
        roundLabel.setText("ROUND  " + currentRound + " / " + MAX_ROUNDS);
    }

    private void updateScoreLabel() {
        String p1Stars = "\u2605".repeat(p1Wins) + "\u2606".repeat(ROUNDS_TO_WIN - Math.min(p1Wins, ROUNDS_TO_WIN));
        String p2Stars = "\u2605".repeat(p2Wins) + "\u2606".repeat(ROUNDS_TO_WIN - Math.min(p2Wins, ROUNDS_TO_WIN));
        scoreLabel.setText("P1: " + p1Stars + "  |  P2: " + p2Stars);
    }

    private void updateTurnBanner() {
        if (p1 == null || p2 == null) return;
        turnBanner.setText(p1Turn ? "\u25B6  PLAYER 1's TURN  \u25C4" : "\u25B6  PLAYER 2's TURN  \u25C4");
        turnBanner.setForeground(p1Turn ? new Color(0, 220, 80) : new Color(255, 80, 80));
        Fighter cur = p1Turn ? p1 : p2;
        actionPrompt.setText("PLAYER " + (p1Turn ? "1" : "2") + " \u2014 " + cur.name + ", choose your action:");
        actionPrompt.setForeground(p1Turn ? new Color(0, 220, 80) : new Color(255, 100, 100));
    }

    private void updateSkillButtons() {
        if (p1 == null || p2 == null) return;
        Fighter cur = p1Turn ? p1 : p2;
        btnSkill1.setText("<html><center>" + cur.skill1   + "<br><font size='2'>" + Fighter.SK1_COST + " mana</font></center></html>");
        btnSkill2.setText("<html><center>" + cur.skill2   + "<br><font size='2'>" + Fighter.SK2_COST + " mana</font></center></html>");
        btnSkill3.setText("<html><center>" + cur.skill3   + "<br><font size='2'>" + Fighter.SK3_COST + " mana</font></center></html>");
        btnUlt.setText   ("<html><center>" + cur.ultimate + "<br><font size='2'>" + Fighter.ULT_COST + " mana</font></center></html>");
        btnSkill1.setEnabled(cur.canUse(1)); btnSkill1.setForeground(cur.canUse(1) ? Color.WHITE : new Color(150,150,150));
        btnSkill2.setEnabled(cur.canUse(2)); btnSkill2.setForeground(cur.canUse(2) ? Color.WHITE : new Color(150,150,150));
        btnSkill3.setEnabled(cur.canUse(3)); btnSkill3.setForeground(cur.canUse(3) ? Color.WHITE : new Color(150,150,150));
        btnUlt.setEnabled(cur.canUse(4));    btnUlt.setForeground(cur.canUse(4) ? new Color(255,180,80) : new Color(150,100,100));
    }

    public void setActionsEnabled(boolean on) {
        btnBasic.setEnabled(on); btnSkill1.setEnabled(on);
        btnSkill2.setEnabled(on); btnSkill3.setEnabled(on); btnUlt.setEnabled(on);
    }

    private void colorHp(JProgressBar bar, int hp, int max) {
        double pct = (double) hp / max;
        if      (pct > 0.5)  bar.setForeground(new Color(50, 200, 80));
        else if (pct > 0.25) bar.setForeground(new Color(220, 180, 0));
        else                 bar.setForeground(new Color(220, 50, 50));
    }

    public void logMessage(String msg) {
        battleLog.append(msg + "\n");
        battleLog.setCaretPosition(battleLog.getDocument().getLength());
    }

    // ── Widget helpers ─────────────────────────────────────────────────────
    private JButton actionBtn(String text, Color bg, int idx) {
        JButton b = new JButton("<html><center>" + text.replace("\n", "<br>") + "</center></html>");
        b.setFont(new Font("Arial", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(0, 65));
        b.setBorder(BorderFactory.createLineBorder(bg.brighter(), 1));
        b.addActionListener(e -> handleAction(idx));
        return b;
    }

    private JLabel lbl(String t, int size, Color c) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Impact", Font.PLAIN, size));
        l.setForeground(c);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JProgressBar bar(int val, int max, Color c) {
        JProgressBar b = new JProgressBar(0, max);
        b.setValue(val);
        b.setForeground(c);
        b.setBackground(new Color(40, 40, 50));
        b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(0, 12));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        return b;
    }
}