import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameFrame extends JFrame {
    private GamePanel gamePanel;

    public GameFrame() {
        this.setTitle("Snake");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        gamePanel = new GamePanel();
        this.setLayout(new BorderLayout());
        this.add(gamePanel, BorderLayout.CENTER);

        // control panel with Replay and Settings buttons
        JPanel control = new JPanel();
        JButton replay = new JButton("Replay");
        replay.addActionListener(e -> {
            gamePanel.restart();
            gamePanel.requestFocusInWindow();
        });
        control.add(replay);

        JButton settings = new JButton("Settings");
        settings.addActionListener(e -> {
            SettingsDialog dlg = new SettingsDialog(this, gamePanel);
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);
            // restore focus to game panel after closing
            gamePanel.requestFocusInWindow();
        });
        control.add(settings);

        this.add(control, BorderLayout.SOUTH);

        this.pack();
        this.setLocationRelativeTo(null);
        // ensure game panel gets focus when window opens
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                gamePanel.requestFocusInWindow();
            }
        });
    }

    // Simple modal dialog to remap keys
    private static class SettingsDialog extends JDialog {
        private final GamePanel panel;

        public SettingsDialog(Frame owner, GamePanel panel) {
            super(owner, "Controls", true);
            this.panel = panel;
            initUI();
            pack();
        }

        private void initUI() {
            setLayout(new BorderLayout(8,8));
            JPanel grid = new JPanel(new GridLayout(5,2,8,8));

            // labels and buttons for each action
            grid.add(new JLabel("Up:"));
            JButton btnUp = new JButton(KeyEvent.getKeyText(panel.getKeyUp()));
            grid.add(btnUp);

            grid.add(new JLabel("Down:"));
            JButton btnDown = new JButton(KeyEvent.getKeyText(panel.getKeyDown()));
            grid.add(btnDown);

            grid.add(new JLabel("Left:"));
            JButton btnLeft = new JButton(KeyEvent.getKeyText(panel.getKeyLeft()));
            grid.add(btnLeft);

            grid.add(new JLabel("Right:"));
            JButton btnRight = new JButton(KeyEvent.getKeyText(panel.getKeyRight()));
            grid.add(btnRight);

            grid.add(new JLabel("Restart:"));
            JButton btnRestart = new JButton(KeyEvent.getKeyText(panel.getKeyRestart()));
            grid.add(btnRestart);

            add(grid, BorderLayout.CENTER);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton close = new JButton("Close");
            close.addActionListener(e -> dispose());
            bottom.add(close);
            add(bottom, BorderLayout.SOUTH);

            // Helper to capture the next key press and assign
            class KeyAssigner extends KeyAdapter {
                private JButton target;
                void startAssign(JButton button) {
                    this.target = button;
                    target.setText("Press a key...");
                    // request focus so key events come to dialog
                    requestFocusInWindow();
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    if (target == null) return;
                    int code = e.getKeyCode();
                    target.setText(KeyEvent.getKeyText(code));
                    if (target == btnUp) panel.setKeyUp(code);
                    else if (target == btnDown) panel.setKeyDown(code);
                    else if (target == btnLeft) panel.setKeyLeft(code);
                    else if (target == btnRight) panel.setKeyRight(code);
                    else if (target == btnRestart) panel.setKeyRestart(code);
                    target = null;
                }
            }

            KeyAssigner assigner = new KeyAssigner();

            // Attach action listeners to begin assignment
            btnUp.addActionListener(ae -> assigner.startAssign(btnUp));
            btnDown.addActionListener(ae -> assigner.startAssign(btnDown));
            btnLeft.addActionListener(ae -> assigner.startAssign(btnLeft));
            btnRight.addActionListener(ae -> assigner.startAssign(btnRight));
            btnRestart.addActionListener(ae -> assigner.startAssign(btnRestart));

            // Add key listener to dialog
            addKeyListener(assigner);
            // Also add to content pane so it receives focus
            getContentPane().addKeyListener(assigner);
            // Make sure dialog is focusable
            setFocusable(true);
            // Request focus when shown
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    requestFocusInWindow();
                }
            });
        }
    }
}
