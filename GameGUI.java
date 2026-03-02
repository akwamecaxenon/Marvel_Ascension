import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class GameGUI extends JFrame implements ActionListener {
    
    private JButton startButton, settingsButton, helpButton, exitButton, aboutButton;
    private JLabel titleLabel;
    private JPanel mainMenuPanel, settingsPanel, helpPanel, aboutPanel;
    
    private GameModes modesPanel; 
    private CharacterSelector selectorPanel; 
    
    private boolean isFullscreen = false;

    public GameGUI() {
        setTitle("MARVEL ASCENSION");
        setSize(1024, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new CardLayout());
        
        // Initialize Panels
        createMainMenuPanel();
        createSettingsPanel();
        createAboutPanel();
        createHelpPanel();
        modesPanel = new GameModes(this);
        selectorPanel = new CharacterSelector(this);
        
        // Register in CardLayout
        add(mainMenuPanel, "main");
        add(settingsPanel, "settings");
        add(aboutPanel, "about");
        add(helpPanel, "help");
        add(modesPanel, "modes");       
        add(selectorPanel, "selector"); 
        
        showPanel("main");
    }

    public void navigateTo(String panelName) {
        showPanel(panelName);
    }

    public void exitNavigation(String panelName) {
        int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        } else if (result == JOptionPane.NO_OPTION) {
            showPanel(panelName);
        }
    }

    private void showPanel(String panelName) {
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), panelName);
    }

    private void createMainMenuPanel() {
        mainMenuPanel = new JPanel(new GridBagLayout());
        mainMenuPanel.setBackground(new Color(15, 15, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // FIXED EPIC TITLE: Using HTML for a "Glow" effect since setShadow is undefined
        titleLabel = new JLabel("<html><div style='text-align: center; color: #FFD700; font-family: serif; font-size: 50pt;'>MARVEL<br>ASCENSION</div></html>", SwingConstants.CENTER);
        
        gbc.gridy = 0; gbc.gridwidth = 2;
        mainMenuPanel.add(titleLabel, gbc);
        

     /* The buttons are here */

        startButton = createEpicButton("INITIATE MISSION", new Color(180, 0, 0));
        settingsButton = createEpicButton("SYSTEM CONFIG", new Color(50, 50, 70));
        helpButton = createEpicButton("DATABASE", new Color(50, 50, 70));
        aboutButton = createEpicButton("ABOUT", new Color(50, 50, 70));
        exitButton = createEpicButton("ABORT", new Color(30, 30, 30));

        
        JButton[] btns = {startButton, settingsButton, helpButton, aboutButton, exitButton};
        for(int i=0; i<btns.length; i++) {
            btns[i].addActionListener(this);
            gbc.gridy = i + 1;
            mainMenuPanel.add(btns[i], gbc);
        }
    }

    private void createAboutPanel() {
        // Placeholder for future "About" section, currently unused
        aboutPanel = new JPanel(new BorderLayout());
        aboutPanel.setBackground(new Color(20, 20, 25));

        JLabel title = new JLabel("ABOUT MARVEL ASCENSION", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 40));
        title.setForeground(Color.CYAN);
        title.setBorder(new EmptyBorder(30,0,30,0));    
        JTextArea info = new JTextArea("MARVEL ASCENSION is a fan-made project by Group Unturned,inspired by the Marvel Universe.\nThis game is a passion project and is not affiliated with Marvel or Disney.\nAll characters and lore are used under fair use for educational and entertainment purposes.\n\n\n\nDevs:\nReuben\nJan Clark\nMicoh\nJaffe\nJustine.");
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
        
        JLabel title = new JLabel("SYSTEM CONFIGURATION", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 40));
        title.setForeground(Color.CYAN);
        title.setBorder(new EmptyBorder(30,0,30,0));

        JPanel optionsGrid = new JPanel(new GridLayout(3, 1, 20, 20));
        optionsGrid.setOpaque(false);
        optionsGrid.setBorder(new EmptyBorder(0, 100, 0, 100));

        // RESTORED FULLSCREEN
        JCheckBox fsCheck = new JCheckBox("ACTIVATE FULLSCREEN MODE");
        fsCheck.setFont(new Font("Monospaced", Font.BOLD, 22));
        fsCheck.setForeground(Color.WHITE);
        fsCheck.setOpaque(false);
        fsCheck.setSelected(isFullscreen);

        fsCheck.addItemListener(e -> {
            dispose(); // Must dispose to change decoration
            if (fsCheck.isSelected()) {
                setUndecorated(true);
                setExtendedState(JFrame.MAXIMIZED_BOTH);
                isFullscreen = true;
            } else {
                setUndecorated(false);
                setExtendedState(JFrame.NORMAL);
                setSize(1024, 800);
                setLocationRelativeTo(null);
                isFullscreen = false;
            }
            setVisible(true);
        });

        JButton back = createEpicButton("RETURN TO HUB", Color.GRAY);
        back.addActionListener(e -> showPanel("main"));

        optionsGrid.add(fsCheck);
        settingsPanel.add(title, BorderLayout.NORTH);
        settingsPanel.add(optionsGrid, BorderLayout.CENTER);
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
