import java.awt.*;

/**
 * Represents the player-controlled T-Rex dinosaur.
 *
 * <p>
 * The {@code Dino} class is responsible for:
 * </p>
 * <ul>
 * <li>Maintaining the dinosaur's position and physics (jump / gravity).</li>
 * <li>Tracking the duck/crouch state when the player holds the down key.</li>
 * <li>Running a two-frame leg animation that syncs with game speed.</li>
 * <li>Drawing the complete T-Rex sprite using {@link Graphics2D}
 * primitives.</li>
 * <li>Flashing the sprite after a fatal collision to signal game over.</li>
 * </ul>
 */
public class Dino {

    // ── Layout constants ──────────────────────────────────────────────

    /** Y coordinate of the dino's feet when standing on the ground. */
    public static final int GROUND_Y = 220;

    /** Sprite width in pixels. */
    public static final int WIDTH = 44;

    /** Sprite height in pixels when standing upright. */
    public static final int HEIGHT = 52;

    /** Sprite height in pixels when crouching (duck pose). */
    public static final int DUCK_HEIGHT = 28;

    /** Fixed horizontal position of the dino on the panel. */
    public static final int X = 60;

    // ── Physics constants ─────────────────────────────────────────────

    /** Initial upward velocity applied when a jump is triggered. */
    private static final int JUMP_VELOCITY = 16;

    /** Downward acceleration applied every tick while airborne. */
    private static final int GRAVITY = 1;

    /** Seconds between each visibility toggle during the death flash animation. */
    private static final double FLASH_INTERVAL = 0.3;

    // ── Runtime state ─────────────────────────────────────────────────

    /** Current top-left y coordinate of the dino's bounding box. */
    public int y;

    /** Vertical velocity (negative = upward). */
    private int velY = 0;

    /** Whether the dino is currently airborne. */
    private boolean jumping = false;

    /** Whether the dino is crouching (down key held). */
    private boolean ducking = false;

    /** Whether a fatal collision has been detected. */
    private boolean dead = false;

    // ── Animation state ───────────────────────────────────────────────

    /** Current leg animation frame index (0 or 1). */
    private int animFrame = 0;

    /** Ticks elapsed since the last frame change. */
    private int animTick = 0;

    /** Number of ticks before switching to the next animation frame. */
    private int animRate = 6;

    /** Elapsed seconds since the dino died (drives the flash timer). */
    private double deadTimer = 0;

    /** Whether the sprite is currently visible during the death flash animation. */
    private boolean flashVisible = true;

    // ── Colour palette ────────────────────────────────────────────────

    private final Color bodyColor = new Color(83, 83, 83);
    private final Color eyeColor = new Color(240, 240, 240);
    private final Color pupilColor = new Color(30, 30, 30);

    // ── Constructor ───────────────────────────────────────────────────

    /**
     * Creates a new {@code Dino} positioned at the ground level.
     */
    public Dino() {
        y = GROUND_Y - HEIGHT;
    }

    // ── Public API ────────────────────────────────────────────────────

    /**
     * Initiates a jump if the dino is currently on the ground and alive.
     * Cancels any active duck before jumping.
     */
    public void jump() {
        if (!jumping && !dead) {
            jumping = true;
            ducking = false;
            velY = -JUMP_VELOCITY;
        }
    }

    /**
     * Enables or disables the duck (crouch) pose.
     * Has no effect while the dino is airborne or dead.
     *
     * @param on {@code true} to start ducking; {@code false} to stand back up
     */
    public void duck(boolean on) {
        if (!jumping && !dead) {
            ducking = on;
        }
    }

    /**
     * Marks the dino as dead, triggering the death flash animation.
     * Should be called immediately after a collision is detected.
     */
    public void die() {
        dead = true;
    }

    /**
     * Returns whether a fatal collision has been registered.
     *
     * @return {@code true} if the dino is dead
     */
    public boolean isDead() {
        return dead;
    }

    /**
     * Returns whether the dino is currently airborne.
     *
     * @return {@code true} if a jump is in progress
     */
    public boolean isJumping() {
        return jumping;
    }

    /**
     * Updates the dino's physics and animation state for one game tick.
     *
     * <p>
     * When dead, the method only advances the flash timer.
     * When alive, it applies gravity (if airborne) and advances the leg animation.
     * </p>
     *
     * @param deltaSeconds elapsed time for this tick in seconds
     */
    public void update(double deltaSeconds) {
        if (dead) {
            deadTimer += deltaSeconds;
            flashVisible = ((int) (deadTimer / FLASH_INTERVAL) % 2 == 0);
            return;
        }

        // Apply gravity while airborne
        if (jumping) {
            y += velY;
            velY += GRAVITY;
            int floor = groundY();
            if (y >= floor) {
                y = floor;
                jumping = false;
                velY = 0;
            }
        }

        // Advance leg animation while on the ground
        if (!jumping) {
            animTick++;
            if (animTick >= animRate) {
                animTick = 0;
                animFrame = 1 - animFrame;
            }
        }
    }

    /**
     * Sets how many ticks elapse between each leg animation frame.
     * Smaller values produce faster leg movement (used to sync with game speed).
     * The value is clamped to a minimum of 2 to avoid invisible frames.
     *
     * @param ticksPerFrame desired number of ticks per animation frame
     */
    public void setAnimRate(int ticksPerFrame) {
        this.animRate = Math.max(2, ticksPerFrame);
    }

    /**
     * Returns the dino's collision hitbox, inset by 5 px on all sides for fairness.
     *
     * @return a {@link Rectangle} representing the active collision region
     */
    public Rectangle getHitbox() {
        int h = currentHeight();
        return new Rectangle(X + 5, y + 5, WIDTH - 10, h - 10);
    }

    /**
     * Draws the complete T-Rex sprite at its current position and state.
     * While the death flash animation is active, the sprite is hidden on
     * alternating intervals and this method returns early without drawing.
     *
     * @param g2d the {@link Graphics2D} context to draw on
     */
    public void draw(Graphics2D g2d) {
        if (!flashVisible) {
            return; // Hidden frame during post-death flash animation
        }

        int h = currentHeight();

        // Body
        g2d.setColor(bodyColor);
        g2d.fillRoundRect(X, y, WIDTH, h, 10, 10);

        // Head and facial features (hidden while ducking for a flatter silhouette)
        if (!ducking) {
            // Head
            g2d.fillRoundRect(X + 10, y - 20, 28, 24, 8, 8);
            // Eye white
            g2d.setColor(eyeColor);
            g2d.fillOval(X + 25, y - 17, 10, 10);
            // Pupil
            g2d.setColor(pupilColor);
            g2d.fillOval(X + 28, y - 14, 5, 5);
            // Mouth hint
            g2d.setColor(bodyColor.darker());
            g2d.drawLine(X + 34, y - 8, X + 38, y - 6);
        } else {
            // Duck pose: flattened head shifted to the right
            g2d.setColor(bodyColor);
            g2d.fillRoundRect(X + WIDTH - 10, y - 10, 28, 16, 6, 6);
            g2d.setColor(eyeColor);
            g2d.fillOval(X + WIDTH + 10, y - 8, 8, 8);
            g2d.setColor(pupilColor);
            g2d.fillOval(X + WIDTH + 13, y - 5, 4, 4);
        }

        // Arm (not visible in duck pose)
        if (!ducking) {
            g2d.setColor(bodyColor.darker());
            g2d.fillRoundRect(X + WIDTH - 8, y + 10, 14, 8, 4, 4);
        }

        // Legs
        drawLegs(g2d, h);
    }

    // ── Private helpers ───────────────────────────────────────────────

    /**
     * Calculates the y coordinate of the ground level relative to the
     * current pose height.
     *
     * @return the y value at which the dino's top-left corner rests when standing
     */
    private int groundY() {
        return GROUND_Y - currentHeight();
    }

    /**
     * Returns the sprite height for the current pose.
     *
     * @return {@link #DUCK_HEIGHT} when ducking, {@link #HEIGHT} otherwise
     */
    private int currentHeight() {
        return ducking ? DUCK_HEIGHT : HEIGHT;
    }

    /**
     * Draws both legs of the T-Rex in the appropriate pose for the current state:
     * tucked when jumping, flattened when ducking, or alternating when running.
     *
     * @param g2d   the {@link Graphics2D} context
     * @param bodyH the current body height (used to compute the leg attachment
     *              point)
     */
    private void drawLegs(Graphics2D g2d, int bodyH) {
        g2d.setColor(bodyColor.darker());
        int legW = 12;
        int legH = 22;
        int legY = y + bodyH - 4; // Attach legs at the bottom of the body

        if (jumping) {
            // Legs tucked upward while airborne
            g2d.fillRoundRect(X + 6, legY - 10, legW, 14, 4, 4);
            g2d.fillRoundRect(X + 22, legY - 6, legW, 14, 4, 4);
        } else if (ducking) {
            // Short flattened legs in duck pose
            g2d.fillRoundRect(X + 4, legY - 6, legW + 4, 10, 4, 4);
            g2d.fillRoundRect(X + 22, legY - 4, legW + 4, 10, 4, 4);
        } else {
            // Alternating two-frame run animation
            if (animFrame == 0) {
                g2d.fillRoundRect(X + 6, legY, legW, legH, 4, 4);
                g2d.fillRoundRect(X + 22, legY, legW, legH - 8, 4, 4);
            } else {
                g2d.fillRoundRect(X + 6, legY, legW, legH - 8, 4, 4);
                g2d.fillRoundRect(X + 22, legY, legW, legH, 4, 4);
            }
        }
    }
}
