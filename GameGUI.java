import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class GameGUI extends JFrame implements ActionListener, Interfaces.GameNavigator {
    
    private JButton startButton, settingsButton, helpButton, exitButton, aboutButton;
    private JLabel titleLabel;
    private JPanel mainMenuPanel, settingsPanel, helpPanel, aboutPanel;
    
    private GameModes modesPanel; 
    private CharacterSelector selectorPanel; 
    private Maps mapsPanel;
    
    private String selectedHeroName;
    private boolean isFullscreen = false;
    private String customFontName = "Serif";

    public class ScaledBackgroundPanel extends JPanel {
        private Image background;
        public ScaledBackgroundPanel(String path) {
            background = new ImageIcon(path).getImage();
            setLayout(new GridBagLayout());
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        }
    }

    public GameGUI() {
        // Font must be registered before UI components are created
        registerCustomFont("BitcountGridSingle_Cursive-SemiBold.ttf");

        setTitle("MARVEL ASCENSION");
        setSize(1024, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new CardLayout());
        
        createMainMenuPanel();
        createSettingsPanel();
        createAboutPanel();
        createHelpPanel();
        modesPanel = new GameModes(this);
        selectorPanel = new CharacterSelector(this);
        mapsPanel = new Maps(this);
        
        add(mainMenuPanel, "main");
        add(settingsPanel, "settings");
        add(aboutPanel, "about");
        add(helpPanel, "help");
        add(modesPanel, "modes");       
        add(selectorPanel, "selector");
        add(mapsPanel, "maps");
        
        showPanel("main");
    }

    private void registerCustomFont(String fileName) {
        try {
            File fontFile = new File("fonts/" + fileName);
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
            this.customFontName = customFont.getFontName(); 
        } catch (Exception e) {
            System.err.println("Font loading failed: " + e.getMessage());
        }
    }

    private void createMainMenuPanel() {
        mainMenuPanel = new ScaledBackgroundPanel("images/main_menu.png");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Cinematic Title with Custom Font and Dr. Strange aesthetic
        titleLabel = new JLabel(
            "<html><div style='text-align: center; color: #FFD700; " +
            "font-family: \"" + customFontName + "\", serif; " +
            "font-size: 55pt; letter-spacing: 8px; " +
            "text-shadow: 2px 2px 5px #000000;'>" +
            "MARVEL<br>ASCENSION</div></html>",
            SwingConstants.CENTER
        );
        titleLabel.setOpaque(false);
        
        gbc.gridy = 0;
        mainMenuPanel.add(titleLabel, gbc);

        startButton    = createEpicButton("INITIATE MISSION", new Color(180, 0, 0));
        settingsButton = createEpicButton("SYSTEM CONFIG",   new Color(50, 50, 70));
        helpButton     = createEpicButton("DATABASE",        new Color(50, 50, 70));
        aboutButton    = createEpicButton("ABOUT",           new Color(50, 50, 70));
        exitButton     = createEpicButton("ABORT",           new Color(30, 30, 30));

        JButton[] btns = {startButton, settingsButton, helpButton, aboutButton, exitButton};
        for (int i = 0; i < btns.length; i++) {
            btns[i].addActionListener(this);
            gbc.gridy = i + 1;
            mainMenuPanel.add(btns[i], gbc);
        }
    }

    private void createAboutPanel() {
        aboutPanel = new JPanel(new BorderLayout());
        aboutPanel.setBackground(new Color(20, 20, 25));
        JLabel title = new JLabel("ABOUT MARVEL ASCENSION", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 40));
        title.setForeground(Color.CYAN);
        JTextArea info = new JTextArea("MARVEL ASCENSION is a fan-made project.\nDevs: Reuben, Jan Clark, Micoh, Jaffe, Justine.");
        info.setFont(new Font("Monospaced", Font.PLAIN, 16));
        info.setForeground(Color.GREEN);
        info.setBackground(Color.BLACK);
        info.setEditable(false);
        JButton back = createEpicButton("BACK TO HUB", Color.GRAY);
        back.addActionListener(e -> showPanel("main"));
        aboutPanel.add(title, BorderLayout.NORTH);
        aboutPanel.add(new JScrollPane(info), BorderLayout.CENTER);
        aboutPanel.add(back, BorderLayout.SOUTH);
    }

    private void createSettingsPanel() {   
        settingsPanel = new JPanel(new BorderLayout());
        settingsPanel.setBackground(new Color(20, 20, 25));
        JCheckBox fsCheck = new JCheckBox("ACTIVATE FULLSCREEN MODE");
        fsCheck.setFont(new Font("Monospaced", Font.BOLD, 22));
        fsCheck.setForeground(Color.WHITE);
        fsCheck.setOpaque(false);
        fsCheck.addItemListener(e -> {
            dispose();
            if (fsCheck.isSelected()) {
                setUndecorated(true);
                setExtendedState(JFrame.MAXIMIZED_BOTH);
            } else {
                setUndecorated(false);
                setExtendedState(JFrame.NORMAL);
                setSize(1024, 800);
                setLocationRelativeTo(null);
            }
            setVisible(true);
        });
        JButton back = createEpicButton("RETURN TO HUB", Color.GRAY);
        back.addActionListener(e -> showPanel("main"));
        settingsPanel.add(fsCheck, BorderLayout.CENTER);
        settingsPanel.add(back, BorderLayout.SOUTH);
    }

    private JButton createEpicButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Impact", Font.PLAIN, 24));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, bg.brighter(), bg.darker()));
        b.setPreferredSize(new Dimension(350, 60));
        return b;
    }

    private void createHelpPanel() {
        helpPanel = new JPanel(new BorderLayout());
        helpPanel.setBackground(new Color(15, 15, 20));
        JTextArea info = new JTextArea("S.H.I.E.L.D. DATABASE\n\n- Use Mouse to navigate.\n- Select mode.\n- Choose your Hero.");
        info.setFont(new Font("Monospaced", Font.PLAIN, 18));
        info.setForeground(Color.GREEN);
        info.setBackground(Color.BLACK);
        info.setEditable(false);
        JButton back = createEpicButton("BACK", Color.GRAY);
        back.addActionListener(e -> showPanel("main"));
        helpPanel.add(new JScrollPane(info), BorderLayout.CENTER);
        helpPanel.add(back, BorderLayout.SOUTH);
    }

    public void navigateTo(String panelName) { showPanel(panelName); }
    public void setSelectedHero(String name) { this.selectedHeroName = name; }
    public String getSelectedHeroName() { return selectedHeroName; }

    public void exitNavigation(String panelName) {
        int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) System.exit(0);
        else showPanel(panelName);
    }

    private void showPanel(String panelName) {
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), panelName);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startButton) showPanel("modes");
        else if (e.getSource() == settingsButton) showPanel("settings");
        else if (e.getSource() == helpButton) showPanel("help");
        else if (e.getSource() == aboutButton) showPanel("about");
        else if (e.getSource() == exitButton) exitNavigation("main"); 
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameGUI().setVisible(true));
    }
}
