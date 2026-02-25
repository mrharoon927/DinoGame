import java.awt.*;

/**
 * A decorative cloud that scrolls across the background of the game panel.
 *
 * <p>
 * Clouds move at one-third of the current game speed so they appear
 * further away than the ground obstacles (parallax effect).
 * </p>
 */
public class Cloud {

    /** Current top-left x position of the cloud (updated each tick). */
    public int x;

    /** Fixed y position of the cloud (set at construction). */
    public int y;

    /** Total pixel width of the cloud shape (used for off-screen detection). */
    private final int width = 70;

    /** Total pixel height of the cloud shape. */
    private final int height = 25;

    /**
     * Creates a new cloud at the specified position.
     *
     * @param x the initial x coordinate (top-left)
     * @param y the initial y coordinate (top-left)
     */
    public Cloud(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Moves the cloud one step to the left.
     * The cloud travels at {@code max(1, speed / 3)} pixels per tick to
     * create a parallax depth effect relative to ground obstacles.
     *
     * @param speed the current game speed in pixels per tick
     */
    public void update(int speed) {
        x -= Math.max(1, speed / 3);
    }

    /**
     * Returns {@code true} when the cloud has scrolled fully off the left edge
     * of the panel and can be removed from the scene.
     *
     * @return {@code true} if the cloud is no longer visible
     */
    public boolean isOffScreen() {
        return x + width < 0;
    }

    /**
     * Draws the cloud using three overlapping ovals to create a fluffy silhouette.
     *
     * @param g2d the {@link Graphics2D} context to draw on
     */
    public void draw(Graphics2D g2d) {
        g2d.setColor(new Color(220, 220, 220));
        // Three overlapping ovals form the cloud puff shape
        g2d.fillOval(x, y + 10, 40, 20);
        g2d.fillOval(x + 15, y, 35, height);
        g2d.fillOval(x + 35, y + 8, 35, 20);
    }
}
