import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Main game panel for the T-Rex Dino Runner.
 *
 * <p>
 * This class extends {@link JPanel} and drives the entire game lifecycle:
 * </p>
 * <ul>
 * <li><b>Start screen</b> – shown until the player presses Space or ↑.</li>
 * <li><b>Running</b> – the game loop updates physics, obstacles, and rendering
 * at ~60 fps.</li>
 * <li><b>Game over</b> – a blinking overlay is shown; Space or Enter restarts
 * the game.</li>
 * </ul>
 *
 * <p>
 * <b>Controls:</b>
 * </p>
 * <ul>
 * <li>{@code SPACE} / {@code ↑} – Jump (also starts or restarts the game).</li>
 * <li>{@code ↓} – Duck (hold to crouch; release to stand).</li>
 * <li>{@code ENTER} – Restart after Game Over.</li>
 * </ul>
 *
 * <p>
 * <b>Game mechanics:</b>
 * </p>
 * <ul>
 * <li>Speed starts at {@value #BASE_SPEED} px/tick and increases every 200
 * score points.</li>
 * <li>Day and night alternate every {@value #NIGHT_TOGGLE_SCORE} score
 * points.</li>
 * <li>High score is preserved in memory for the lifetime of the session.</li>
 * </ul>
 */
public class DinoGame extends JPanel implements ActionListener, KeyListener {

    // ── Layout constants ──────────────────────────────────────────────

    /** Width of the game panel in pixels. */
    private static final int PANEL_W = 900;

    /** Height of the game panel in pixels. */
    private static final int PANEL_H = 300;

    /** Y coordinate of the ground surface line. */
    private static final int GROUND_Y = 220;

    /**
     * Timer interval in milliseconds; targets approximately 60 frames per second.
     */
    private static final int TICK_MS = 16;

    /** Starting scroll speed in pixels per game tick. */
    private static final int BASE_SPEED = 6;

    // ── Game objects ──────────────────────────────────────────────────

    /** The player-controlled T-Rex dinosaur. */
    private Dino dino;

    /** Manages obstacle spawning, movement, and rendering. */
    private ObstacleGenerator obstacleGen;

    /** Background clouds that scroll at a reduced speed for a parallax effect. */
    private List<Cloud> clouds;

    /**
     * Small pebble/notch marks that scroll across the top of the ground surface.
     */
    private List<GroundMark> groundMarks;

    // ── Game state ────────────────────────────────────────────────────

    /** Current phase of the game (idle / running / game-over). */
    private GameState state = GameState.IDLE;

    /** Player's current score; incremented by one each game tick. */
    private int score = 0;

    /** Highest score achieved so far in this session. */
    private int highScore = 0;

    /** Current scroll speed in pixels per tick (increases with score). */
    private int speed = BASE_SPEED;

    // ── Day / night cycle ─────────────────────────────────────────────

    /** Whether the game is currently rendered in night mode. */
    private boolean isNight = false;

    /** Score value at which the last day/night toggle occurred. */
    private int lastToggleScore = 0;

    /** Number of score points between each day/night mode toggle. */
    private static final int NIGHT_TOGGLE_SCORE = 700;

    // ── Timers ────────────────────────────────────────────────────────

    /** Main game loop timer; fires every {@value #TICK_MS} ms during play. */
    private final Timer gameTimer;

    /**
     * Secondary timer used to blink the "GAME OVER" text after a collision.
     * Stopped and restarted on each new game.
     */
    private Timer flashTimer;

    /** Tracks which blink phase the game-over text is currently in. */
    private boolean showGameOverText = true;

    // ── Input state ───────────────────────────────────────────────────

    /**
     * Tracks whether the jump key (Space or ↑) is held down.
     * Prevents repeated jump triggers from a single key press.
     */
    private boolean spaceDown = false;

    /**
     * Tracks whether the duck key (↓) is held down.
     * Used to release the duck when the key is released.
     */
    private boolean downDown = false;

    // ── Shared random instance ────────────────────────────────────────

    /** Shared random number generator for cloud and ground-mark placement. */
    private final Random random = new Random();

    // ── Inner class: GroundMark ───────────────────────────────────────

    /**
     * A small decorative mark (pebble/notch) that scrolls across the top
     * of the ground to create the illusion of a textured surface.
     */
    private static class GroundMark {

        /** Current x position of the mark. */
        int x;

        /** Width of the mark in pixels. */
        int w;

        /** Height of the mark in pixels. */
        int h;

        /**
         * Creates a ground mark at the given position and size.
         *
         * @param x initial x coordinate
         * @param w width in pixels
         * @param h height in pixels
         */
        GroundMark(int x, int w, int h) {
            this.x = x;
            this.w = w;
            this.h = h;
        }
    }

    // ── Constructor ───────────────────────────────────────────────────

    /**
     * Creates the game panel, registers the keyboard listener, seeds initial
     * scene objects, and starts the game loop timer.
     */
    public DinoGame() {
        setPreferredSize(new Dimension(PANEL_W, PANEL_H));
        setBackground(new Color(247, 247, 247));
        setFocusable(true);
        addKeyListener(this);

        gameTimer = new Timer(TICK_MS, this);
        initGame();
        gameTimer.start();
    }

    /**
     * Resets all game state to its initial values, creating fresh game objects
     * and re-seeding clouds and ground marks. Sets the state to
     * {@link GameState#IDLE}.
     * Also stops any running {@code flashTimer} from a previous game.
     */
    private void initGame() {
        dino = new Dino();
        obstacleGen = new ObstacleGenerator();
        clouds = new ArrayList<>();
        groundMarks = new ArrayList<>();

        score = 0;
        speed = BASE_SPEED;
        isNight = false;
        lastToggleScore = 0;
        spaceDown = false;
        downDown = false;
        state = GameState.IDLE;

        // Seed initial clouds spread across the panel width
        for (int i = 0; i < 4; i++) {
            int cx = 100 + i * 220 + random.nextInt(80);
            int cy = 30 + random.nextInt(60);
            clouds.add(new Cloud(cx, cy));
        }

        // Seed initial ground texture marks
        for (int i = 0; i < 12; i++) {
            int gx = random.nextInt(PANEL_W);
            int gw = 4 + random.nextInt(20);
            int gh = 2 + random.nextInt(3);
            groundMarks.add(new GroundMark(gx, gw, gh));
        }

        if (flashTimer != null && flashTimer.isRunning()) {
            flashTimer.stop();
        }
    }

    // ── Game loop ─────────────────────────────────────────────────────

    /**
     * Called by the game loop {@link Timer} every {@value #TICK_MS} ms.
     *
     * <p>
     * When the state is {@link GameState#RUNNING}, this method:
     * </p>
     * <ol>
     * <li>Increments the score and adjusts the scroll speed.</li>
     * <li>Toggles day/night mode when the score threshold is reached.</li>
     * <li>Updates the dino, obstacles, clouds, and ground marks.</li>
     * <li>Checks for obstacle–dino collisions.</li>
     * <li>Requests a repaint.</li>
     * </ol>
     *
     * <p>
     * When in any other state, only a repaint is requested to keep animations
     * (such as the game-over flash) up to date.
     * </p>
     *
     * @param e the action event fired by the timer (not used directly)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (state != GameState.RUNNING) {
            repaint();
            return;
        }

        double dt = TICK_MS / 1000.0; // Delta-time in seconds for physics calculations

        // Scoring and speed ramp-up
        score++;
        speed = Math.min(16, BASE_SPEED + score / 200); // Cap speed at 16 px/tick
        dino.setAnimRate(Math.max(3, 8 - speed / 3)); // Faster legs at higher speeds

        // Day/night cycle toggle
        if (score - lastToggleScore >= NIGHT_TOGGLE_SCORE) {
            isNight = !isNight;
            lastToggleScore = score;
        }

        // Update game entities
        dino.update(dt);
        obstacleGen.update(speed);

        // Update clouds; spawn a new one randomly when below the cap
        for (int i = clouds.size() - 1; i >= 0; i--) {
            clouds.get(i).update(speed);
            if (clouds.get(i).isOffScreen()) {
                clouds.remove(i);
            }
        }
        if (clouds.size() < 5 && random.nextInt(120) == 0) {
            clouds.add(new Cloud(PANEL_W + 10, 20 + random.nextInt(70)));
        }

        // Scroll ground marks; recycle each mark when it leaves the left edge
        for (GroundMark gm : groundMarks) {
            gm.x -= speed;
            if (gm.x + gm.w < 0) {
                gm.x = PANEL_W + random.nextInt(60);
                gm.w = 4 + random.nextInt(20);
                gm.h = 2 + random.nextInt(3);
            }
        }

        // Collision detection between the dino's hitbox and each obstacle's hitbox
        Rectangle dinoHit = dino.getHitbox();
        for (ObstacleGenerator.Obstacle obs : obstacleGen.getObstacles()) {
            if (dinoHit.intersects(obs.getHitbox())) {
                triggerGameOver();
                break;
            }
        }

        repaint();
    }

    /**
     * Transitions the game to {@link GameState#GAME_OVER}.
     *
     * <p>
     * Marks the dino as dead, updates the high score if a new record was set,
     * and starts the flashing timer for the game-over overlay text.
     * </p>
     */
    private void triggerGameOver() {
        state = GameState.GAME_OVER;
        dino.die();
        if (score > highScore) {
            highScore = score;
        }

        // Blink the "GAME OVER" label at 350 ms intervals
        showGameOverText = true;
        flashTimer = new Timer(350, ev -> {
            showGameOverText = !showGameOverText;
            repaint();
        });
        flashTimer.start();
    }

    /**
     * Resets the game and transitions to {@link GameState#RUNNING}.
     * Stops any active flash timer before reinitialising.
     */
    private void startGame() {
        if (flashTimer != null && flashTimer.isRunning()) {
            flashTimer.stop();
        }
        initGame();
        state = GameState.RUNNING;
    }

    // ── Rendering ─────────────────────────────────────────────────────

    /**
     * Renders the complete game scene in the correct back-to-front draw order:
     * background → clouds → ground → obstacles → dino → HUD → state overlays.
     *
     * @param g the {@link Graphics} context provided by Swing (cast to
     *          {@link Graphics2D})
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2d);

        for (Cloud c : clouds) {
            c.draw(g2d);
        }

        drawGround(g2d);

        for (ObstacleGenerator.Obstacle o : obstacleGen.getObstacles()) {
            o.draw(g2d);
        }

        dino.draw(g2d);
        drawHUD(g2d);

        if (state == GameState.IDLE)
            drawStartScreen(g2d);
        if (state == GameState.GAME_OVER)
            drawGameOverScreen(g2d);
    }

    /**
     * Draws the sky gradient and, during night mode, a fixed pattern of stars.
     *
     * @param g2d the {@link Graphics2D} context
     */
    private void drawBackground(Graphics2D g2d) {
        Color skyTop, skyBot;
        if (isNight) {
            skyTop = new Color(10, 10, 40);
            skyBot = new Color(20, 20, 70);
        } else {
            skyTop = new Color(235, 245, 255);
            skyBot = new Color(247, 247, 247);
        }

        GradientPaint sky = new GradientPaint(0, 0, skyTop, 0, GROUND_Y, skyBot);
        g2d.setPaint(sky);
        g2d.fillRect(0, 0, PANEL_W, GROUND_Y);

        // Render a fixed star field when in night mode (seed=42 keeps positions
        // constant)
        if (isNight) {
            g2d.setColor(new Color(255, 255, 255, 180));
            Random rng = new Random(42);
            for (int i = 0; i < 50; i++) {
                int sx = rng.nextInt(PANEL_W);
                int sy = rng.nextInt(GROUND_Y - 20);
                int sr = rng.nextInt(2) + 1;
                g2d.fillOval(sx, sy, sr, sr);
            }
        }
    }

    /**
     * Draws the ground area below {@link #GROUND_Y}, including a gradient fill,
     * a solid top-edge line, and the scrolling texture marks.
     *
     * @param g2d the {@link Graphics2D} context
     */
    private void drawGround(Graphics2D g2d) {
        Color groundTop = isNight ? new Color(60, 60, 80) : new Color(210, 200, 185);
        Color groundBot = isNight ? new Color(40, 40, 60) : new Color(180, 165, 145);

        GradientPaint gp = new GradientPaint(0, GROUND_Y, groundTop, 0, PANEL_H, groundBot);
        g2d.setPaint(gp);
        g2d.fillRect(0, GROUND_Y, PANEL_W, PANEL_H - GROUND_Y);

        // Top edge separator line
        g2d.setColor(isNight ? new Color(100, 100, 130) : new Color(150, 140, 120));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(0, GROUND_Y, PANEL_W, GROUND_Y);
        g2d.setStroke(new BasicStroke(1));

        // Scrolling pebble/texture marks
        Color markColor = isNight ? new Color(80, 80, 100) : new Color(160, 148, 130);
        g2d.setColor(markColor);
        for (GroundMark gm : groundMarks) {
            g2d.fillRoundRect(gm.x, GROUND_Y + 4, gm.w, gm.h, 2, 2);
        }
    }

    /**
     * Draws the heads-up display: current score, high score, live speed readout,
     * and a day/night mode badge. Text colours adapt to the current mode.
     *
     * @param g2d the {@link Graphics2D} context
     */
    private void drawHUD(Graphics2D g2d) {
        String scoreText = "SCORE " + String.format("%05d", score);
        String hiText = "HI " + String.format("%05d", highScore);
        Color textColor = isNight ? new Color(200, 200, 255) : new Color(80, 80, 80);

        g2d.setFont(new Font("Consolas", Font.BOLD, 18));
        g2d.setColor(textColor);
        FontMetrics fm = g2d.getFontMetrics();

        // Right-align the HI score and place the current score to its left
        int hiX = PANEL_W - fm.stringWidth(hiText) - 20;
        int scoreX = hiX - fm.stringWidth(scoreText) - 20;
        g2d.drawString(hiText, hiX, 28);
        g2d.drawString(scoreText, scoreX, 28);

        // Subtle speed readout in the top-left corner
        g2d.setFont(new Font("Consolas", Font.PLAIN, 12));
        g2d.setColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 120));
        g2d.drawString("SPD " + speed, 20, 28);

        // Day/night badge below the speed readout
        String badge = isNight ? "\uD83C\uDF19 NIGHT" : "\u2600 DAY";
        g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        g2d.setColor(textColor);
        g2d.drawString(badge, 20, 48);
    }

    /**
     * Draws the start-screen overlay with the game title and control hints.
     *
     * @param g2d the {@link Graphics2D} context
     */
    private void drawStartScreen(Graphics2D g2d) {
        drawOverlayPanel(g2d);

        // Title
        g2d.setFont(new Font("Consolas", Font.BOLD, 28));
        g2d.setColor(new Color(50, 50, 50));
        String title = "T-REX RUNNER";
        int tw = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, (PANEL_W - tw) / 2, 120);

        // Start prompt
        g2d.setFont(new Font("Consolas", Font.PLAIN, 16));
        g2d.setColor(new Color(80, 80, 80));
        String sub = "Press  SPACE  to Start";
        int sw = g2d.getFontMetrics().stringWidth(sub);
        g2d.drawString(sub, (PANEL_W - sw) / 2, 160);

        // Control hints
        g2d.setFont(new Font("Consolas", Font.PLAIN, 13));
        g2d.setColor(new Color(120, 120, 120));
        String hint = "SPACE / \u2191  = Jump      \u2193  = Duck";
        int hw = g2d.getFontMetrics().stringWidth(hint);
        g2d.drawString(hint, (PANEL_W - hw) / 2, 195);
    }

    /**
     * Draws the game-over overlay showing the (optionally blinking) "GAME OVER"
     * label, the current score and best score, and a replay prompt.
     *
     * @param g2d the {@link Graphics2D} context
     */
    private void drawGameOverScreen(Graphics2D g2d) {
        drawOverlayPanel(g2d);

        // "GAME OVER" label – toggled by the flash timer
        if (showGameOverText) {
            g2d.setFont(new Font("Consolas", Font.BOLD, 26));
            g2d.setColor(new Color(200, 50, 50));
            String go = "GAME  OVER";
            int gw = g2d.getFontMetrics().stringWidth(go);
            g2d.drawString(go, (PANEL_W - gw) / 2, 120);
        }

        // Current score and best score
        g2d.setFont(new Font("Consolas", Font.BOLD, 16));
        g2d.setColor(new Color(60, 60, 60));
        String scoreStr = "Score: " + score + "   Best: " + highScore;
        int ssw = g2d.getFontMetrics().stringWidth(scoreStr);
        g2d.drawString(scoreStr, (PANEL_W - ssw) / 2, 151);

        // Replay prompt
        g2d.setFont(new Font("Consolas", Font.PLAIN, 15));
        g2d.setColor(new Color(80, 80, 80));
        String replay = "Press  SPACE  or  ENTER  to Replay";
        int rw = g2d.getFontMetrics().stringWidth(replay);
        g2d.drawString(replay, (PANEL_W - rw) / 2, 185);
    }

    /**
     * Draws the frosted-glass background panel used by both the start and
     * game-over overlays.
     *
     * @param g2d the {@link Graphics2D} context
     */
    private void drawOverlayPanel(Graphics2D g2d) {
        g2d.setColor(new Color(255, 255, 255, 170));
        g2d.fillRoundRect(PANEL_W / 2 - 240, 90, 480, 120, 20, 20);
        g2d.setColor(new Color(180, 180, 180, 120));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(PANEL_W / 2 - 240, 90, 480, 120, 20, 20);
        g2d.setStroke(new BasicStroke(1));
    }

    // ── Keyboard input ────────────────────────────────────────────────

    /**
     * Handles key-press events for all game states:
     * <ul>
     * <li>{@link GameState#IDLE} – Space or ↑ starts the game and triggers a
     * jump.</li>
     * <li>{@link GameState#GAME_OVER} – Space or Enter restarts the game.</li>
     * <li>{@link GameState#RUNNING} – Space/↑ jumps; ↓ starts ducking.</li>
     * </ul>
     *
     * @param e the key event
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (state == GameState.IDLE) {
            if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_UP) {
                startGame();
                dino.jump(); // The very first input also initiates a jump
            }
            return;
        }

        if (state == GameState.GAME_OVER) {
            if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ENTER) {
                startGame();
            }
            return;
        }

        // Running state
        if ((code == KeyEvent.VK_SPACE || code == KeyEvent.VK_UP) && !spaceDown) {
            spaceDown = true;
            dino.jump();
        }
        if (code == KeyEvent.VK_DOWN && !downDown) {
            downDown = true;
            dino.duck(true);
        }
    }

    /**
     * Handles key-release events to stop ducking when the down key is released.
     *
     * @param e the key event
     */
    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_UP) {
            spaceDown = false;
        }
        if (code == KeyEvent.VK_DOWN) {
            downDown = false;
            dino.duck(false);
        }
    }

    /**
     * Not used; required by the {@link KeyListener} interface.
     *
     * @param e the key event
     */
    @Override
    public void keyTyped(KeyEvent e) {
        // No action required for typed events
    }

    // ── Application entry point ───────────────────────────────────────

    /**
     * Application entry point. Creates the {@link JFrame} window, adds the
     * game panel, centres it on screen, and makes it visible.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("T-Rex Dino Runner");
            DinoGame game = new DinoGame();

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.add(game);
            frame.pack();
            frame.setLocationRelativeTo(null); // Centre the window on the screen
            frame.setVisible(true);
            game.requestFocusInWindow();
        });
    }
}
