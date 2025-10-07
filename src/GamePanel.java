import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNITS = (SCREEN_WIDTH*SCREEN_HEIGHT)/(UNIT_SIZE*UNIT_SIZE);
    // base delay (ms). Actual delay will shrink as snake grows.
    static final int BASE_DELAY = 120;
    final int x[] = new int[GAME_UNITS];
    final int y[] = new int[GAME_UNITS];
    int bodyParts = 6;
    int applesEaten;
    int appleX;
    int appleY;
    char direction = 'R'; // U, D, L, R
    boolean running = false;
    Timer timer;
    Random random;

    // configurable key codes
    private int keyUp = KeyEvent.VK_W;
    private int keyDown = KeyEvent.VK_S;
    private int keyLeft = KeyEvent.VK_A;
    private int keyRight = KeyEvent.VK_D;
    private int keyRestart = KeyEvent.VK_Z;

    public GamePanel() {
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        startGame();
    }

    public void startGame() {
        newApple();
        running = true;
        timer = new Timer(BASE_DELAY, this);
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        // nicer gradient background
        GradientPaint gp = new GradientPaint(0, 0, new Color(10, 24, 36), 0, SCREEN_HEIGHT, new Color(2, 60, 82));
        g2.setPaint(gp);
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        if (running) {
            // draw grid faintly
            g2.setColor(new Color(0,0,0,40));
            for (int i = 0; i < SCREEN_HEIGHT/UNIT_SIZE; i++) {
                g2.drawLine(i*UNIT_SIZE, 0, i*UNIT_SIZE, SCREEN_HEIGHT);
                g2.drawLine(0, i*UNIT_SIZE, SCREEN_WIDTH, i*UNIT_SIZE);
            }

            // draw apple with glow
            g2.setColor(new Color(220, 30, 30));
            g2.fillOval(appleX+2, appleY+2, UNIT_SIZE-4, UNIT_SIZE-4);
            g2.setColor(new Color(255,120,120,90));
            g2.fillOval(appleX-UNIT_SIZE/4, appleY-UNIT_SIZE/4, UNIT_SIZE + UNIT_SIZE/2, UNIT_SIZE + UNIT_SIZE/2);

            // draw snake with rounded segments and gradient head
            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    g2.setColor(new Color(30,200,80));
                    g2.fillRoundRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE, 12, 12);
                } else {
                    float t = (float)i / Math.max(1, bodyParts);
                    Color seg = blend(new Color(45,180,0), new Color(12,120,60), t);
                    g2.setColor(seg);
                    g2.fillRoundRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE, 10, 10);
                }
            }

            // HUD - top-left
            g2.setColor(new Color(255,255,255,200));
            g2.setFont(new Font("SansSerif", Font.BOLD, 18));
            g2.drawString("Score: " + applesEaten, 10, 22);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.drawString("Length: " + bodyParts, 10, 40);

            // adjust speed based on length
            adjustSpeed();
        } else {
            gameOver(g2);
        }

        g2.dispose();
    }

    private Color blend(Color a, Color b, float t) {
        t = Math.min(1f, Math.max(0f, t));
        int r = (int)(a.getRed()*(1-t) + b.getRed()*t);
        int g = (int)(a.getGreen()*(1-t) + b.getGreen()*t);
        int bl = (int)(a.getBlue()*(1-t) + b.getBlue()*t);
        return new Color(r,g,bl);
    }

    private void adjustSpeed() {
        // as bodyParts grows, reduce delay down to a minimum
        int maxExtra = 40; // how much faster it can get
        int extra = Math.min(maxExtra, bodyParts * 2);
        int delay = Math.max(30, BASE_DELAY - extra);
        if (timer != null && timer.getDelay() != delay) {
            timer.setDelay(delay);
        }
    }

    public void newApple() {
        appleX = random.nextInt((int)(SCREEN_WIDTH/UNIT_SIZE))*UNIT_SIZE;
        appleY = random.nextInt((int)(SCREEN_HEIGHT/UNIT_SIZE))*UNIT_SIZE;
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i-1];
            y[i] = y[i-1];
        }

        switch(direction) {
            case 'U': y[0] = y[0] - UNIT_SIZE; break;
            case 'D': y[0] = y[0] + UNIT_SIZE; break;
            case 'L': x[0] = x[0] - UNIT_SIZE; break;
            case 'R': x[0] = x[0] + UNIT_SIZE; break;
        }
    }

    public void checkApple() {
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            newApple();
        }
    }

    public void checkCollisions() {
        // check if head collides with body
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
                break;
            }
        }
        // check if head touches left border
        if (x[0] < 0) running = false;
        // right border
        if (x[0] >= SCREEN_WIDTH) running = false;
        // top
        if (y[0] < 0) running = false;
        // bottom
        if (y[0] >= SCREEN_HEIGHT) running = false;

        if (!running) timer.stop();
    }

    public void gameOver(Graphics g) {
        // Score
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics1.stringWidth("Score: " + applesEaten))/2, SCREEN_HEIGHT/2 - 50);

        // Game Over text
        g.setColor(Color.white);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Game Over", (SCREEN_WIDTH - metrics2.stringWidth("Game Over"))/2, SCREEN_HEIGHT/2 + 25);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            // WASD controls
            // map using configurable keys first
            if (key == keyLeft) {
                if (direction != 'R') direction = 'L';
            } else if (key == keyRight) {
                if (direction != 'L') direction = 'R';
            } else if (key == keyUp) {
                if (direction != 'D') direction = 'U';
            } else if (key == keyDown) {
                if (direction != 'U') direction = 'D';
            } else if (key == KeyEvent.VK_LEFT) {
                if (direction != 'R') direction = 'L';
            } else if (key == KeyEvent.VK_RIGHT) {
                if (direction != 'L') direction = 'R';
            } else if (key == KeyEvent.VK_UP) {
                if (direction != 'D') direction = 'U';
            } else if (key == KeyEvent.VK_DOWN) {
                if (direction != 'U') direction = 'D';
            } else if (key == keyRestart) {
                // restart when game over
                if (!running) restart();
            }
        }
    }

    // Public method to restart the game (also used by Replay button)
    public void restart() {
        // reset state
        bodyParts = 6;
        applesEaten = 0;
        direction = 'R';
        for (int i = 0; i < x.length; i++) { x[i] = 0; y[i] = 0; }
        running = true;
        newApple();
        if (timer != null) timer.stop();
        timer = new Timer(BASE_DELAY, this);
        timer.start();
        requestFocusInWindow();
        repaint();
    }

    // Key mapping getters/setters used by Settings dialog
    public int getKeyUp() { return keyUp; }
    public int getKeyDown() { return keyDown; }
    public int getKeyLeft() { return keyLeft; }
    public int getKeyRight() { return keyRight; }
    public int getKeyRestart() { return keyRestart; }

    public void setKeyUp(int code) { keyUp = code; }
    public void setKeyDown(int code) { keyDown = code; }
    public void setKeyLeft(int code) { keyLeft = code; }
    public void setKeyRight(int code) { keyRight = code; }
    public void setKeyRestart(int code) { keyRestart = code; }
}
