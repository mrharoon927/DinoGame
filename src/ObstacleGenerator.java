import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages the spawning, movement, and rendering of all in-game obstacles.
 *
 * <p>
 * All obstacle logic runs on the Swing Event Dispatch Thread through the
 * main game {@link javax.swing.Timer}, avoiding any multi-threading complexity.
 * </p>
 *
 * <p>
 * Supported obstacle types are defined by the {@link ObstacleType} enum:
 * three cactus variants and a flying bird. Obstacles spawn off the
 * right edge of the screen and scroll left at the current game speed.
 * </p>
 */
public class ObstacleGenerator {

    // ── Obstacle inner class ──────────────────────────────────────────

    /**
     * A single obstacle instance, including its bounding rectangle,
     * assigned colour, and (for birds) vertical oscillation state.
     */
    public static class Obstacle {

        /** The bounding rectangle used for position and off-screen checks. */
        public Rectangle bounds;

        /** The type of this obstacle, determining its appearance and behaviour. */
        public ObstacleType type;

        /** Fill colour for this obstacle's sprite. */
        private Color color;

        // Bird vertical oscillation
        private int baseY; // Y position at spawn time (oscillation centre)
        private int oscOffset = 0;
        private int oscDir = 1;
        private int oscSpeed = 2;

        /**
         * Constructs a new obstacle of the given type at the specified position and
         * size.
         *
         * @param type the {@link ObstacleType} that determines appearance and behaviour
         * @param x    initial x coordinate (typically just off the right edge of the
         *             panel)
         * @param y    initial y coordinate (top of the bounding box)
         * @param w    width of the bounding box in pixels
         * @param h    height of the bounding box in pixels
         */
        public Obstacle(ObstacleType type, int x, int y, int w, int h) {
            this.type = type;
            this.bounds = new Rectangle(x, y, w, h);
            this.baseY = y;

            // Assign a fixed colour per obstacle category
            switch (type) {
                case CACTUS_SMALL:
                case CACTUS_LARGE:
                case CACTUS_CLUSTER:
                    color = new Color(50, 140, 60); // earthy green
                    break;
                case BIRD:
                    color = new Color(110, 90, 160); // purple-grey
                    break;
            }
        }

        /**
         * Moves this obstacle one step to the left and, for birds,
         * advances the vertical oscillation to simulate wing-flap motion.
         *
         * @param speed the current game speed in pixels per tick
         */
        public void update(int speed) {
            bounds.x -= speed;
            if (type == ObstacleType.BIRD) {
                oscOffset += oscDir * oscSpeed;
                if (Math.abs(oscOffset) > 15) {
                    oscDir = -oscDir; // Reverse oscillation direction at the amplitude limit
                }
                bounds.y = baseY + oscOffset;
            }
        }

        /**
         * Returns {@code true} when the obstacle has scrolled completely off the
         * left edge of the panel and can safely be removed from the list.
         *
         * @return {@code true} if the obstacle is no longer visible
         */
        public boolean isOffScreen() {
            return bounds.x + bounds.width < 0;
        }

        /**
         * Returns the collision hitbox for this obstacle, inset by 4 px on every
         * side to give the player a small margin of forgiveness.
         *
         * @return a {@link Rectangle} representing the active collision region
         */
        public Rectangle getHitbox() {
            return new Rectangle(
                    bounds.x + 4, bounds.y + 4,
                    bounds.width - 8, bounds.height - 8);
        }

        /**
         * Draws the obstacle sprite using the appropriate rendering method
         * for its {@link ObstacleType}.
         *
         * @param g2d the {@link Graphics2D} context to draw on
         */
        public void draw(Graphics2D g2d) {
            switch (type) {
                case CACTUS_SMALL:
                    drawCactus(g2d, 1);
                    break;
                case CACTUS_LARGE:
                    drawCactus(g2d, 2);
                    break;
                case CACTUS_CLUSTER:
                    drawCactus(g2d, 3);
                    break;
                case BIRD:
                    drawPtero(g2d);
                    break;
            }
        }

        /**
         * Draws one or more cactus segments side-by-side, each with a trunk
         * and two lateral arms drawn as rounded rectangles.
         *
         * @param g2d   the {@link Graphics2D} context to draw on
         * @param count number of individual cactus segments (1 = small, 2 = large, 3 =
         *              cluster)
         */
        private void drawCactus(Graphics2D g2d, int count) {
            int cw = 16; // Width of a single cactus segment
            int bx = bounds.x;
            int by = bounds.y;
            int bh = bounds.height;

            for (int i = 0; i < count; i++) {
                int cx = bx + i * (cw + 4); // Offset each segment to the right

                g2d.setColor(color);
                // Central trunk
                g2d.fillRoundRect(cx + 4, by, cw - 8, bh, 4, 4);
                // Left arm and tip
                g2d.fillRoundRect(cx, by + bh / 3, 8, bh / 3, 4, 4);
                g2d.fillRoundRect(cx, by + bh / 3 - 8, 8, 10, 4, 4);
                // Right arm and tip
                g2d.fillRoundRect(cx + cw - 8, by + bh / 2, 8, bh / 3, 4, 4);
                g2d.fillRoundRect(cx + cw - 8, by + bh / 2 - 8, 8, 10, 4, 4);
            }
        }

        /**
         * Draws the bird sprite: body, head, beak, and two wings using
         * filled ovals and polygons. Vertical oscillation (set via {@link #update})
         * moves the whole sprite up and down to simulate flight.
         *
         * @param g2d the {@link Graphics2D} context to draw on
         */
        private void drawPtero(Graphics2D g2d) {
            int px = bounds.x;
            int py = bounds.y;

            g2d.setColor(color);
            // Body
            g2d.fillOval(px + 10, py + 10, 22, 14);
            // Head
            g2d.fillOval(px + 28, py + 6, 14, 11);
            // Beak
            g2d.fillPolygon(
                    new int[] { px + 38, px + 50, px + 38 },
                    new int[] { py + 10, py + 12, py + 16 }, 3);
            // Upper wing
            g2d.fillPolygon(
                    new int[] { px + 12, px + 2, px + 25, px + 30 },
                    new int[] { py + 13, py + 2, py + 2, py + 12 }, 4);
            // Lower wing
            g2d.fillPolygon(
                    new int[] { px + 22, px + 14, px + 38, px + 32 },
                    new int[] { py + 20, py + 32, py + 32, py + 20 }, 4);
        }
    }

    // ── ObstacleType enum ─────────────────────────────────────────────

    /**
     * Enumerates the distinct obstacle varieties the generator can spawn.
     *
     * <ul>
     * <li>{@link #CACTUS_SMALL} – A single narrow cactus (easiest to avoid).</li>
     * <li>{@link #CACTUS_LARGE} – A taller, two-segment cactus.</li>
     * <li>{@link #CACTUS_CLUSTER} – Three cacti grouped together (widest
     * footprint).</li>
     * <li>{@link #BIRD} – A flying enemy that oscillates vertically.</li>
     * </ul>
     */
    public enum ObstacleType {
        CACTUS_SMALL,
        CACTUS_LARGE,
        CACTUS_CLUSTER,
        BIRD
    }

    // ── Generator state ───────────────────────────────────────────────

    /** Active obstacles currently on screen. */
    private final List<Obstacle> obstacles = new ArrayList<>();

    /** Random number generator for obstacle type selection and spawn intervals. */
    private final Random random = new Random();

    /** Ticks elapsed since the last obstacle was spawned. */
    private int ticksSinceLastObstacle = 0;

    /**
     * Number of ticks to wait before spawning the next obstacle. Randomised after
     * each spawn.
     */
    private int nextObstacleInterval = 100;

    /**
     * Horizontal width of the game panel; obstacles spawn just beyond this
     * boundary.
     */
    private static final int PANEL_WIDTH = 900;

    /**
     * Y coordinate of the ground surface; used to position ground-based obstacles.
     */
    private static final int GROUND_Y = 220;

    // ── Public API ────────────────────────────────────────────────────

    /**
     * Resets the generator to its initial state, clearing all active obstacles
     * and resetting spawn timing. Should be called at the start of each new game.
     */
    public void reset() {
        obstacles.clear();
        ticksSinceLastObstacle = 0;
        nextObstacleInterval = 100;
    }

    /**
     * Advances the obstacle system by one game tick.
     *
     * <p>
     * On each tick this method:
     * </p>
     * <ol>
     * <li>Checks if it is time to spawn a new obstacle and does so if needed.</li>
     * <li>Moves every active obstacle to the left by {@code speed} pixels.</li>
     * <li>Removes obstacles that have scrolled off the left edge of the panel.</li>
     * </ol>
     *
     * @param speed the current game scroll speed in pixels per tick
     */
    public void update(int speed) {
        ticksSinceLastObstacle++;
        if (ticksSinceLastObstacle >= nextObstacleInterval) {
            spawnObstacle();
            nextObstacleInterval = 80 + random.nextInt(80); // Randomise gap between obstacles
            ticksSinceLastObstacle = 0;
        }

        // Update positions and cull off-screen obstacles (iterate in reverse to allow
        // safe removal)
        for (int i = obstacles.size() - 1; i >= 0; i--) {
            Obstacle o = obstacles.get(i);
            o.update(speed);
            if (o.isOffScreen()) {
                obstacles.remove(i);
            }
        }
    }

    /**
     * Returns the live list of obstacles currently on screen.
     * Used by the game loop for collision detection and the renderer for drawing.
     *
     * @return the current list of active {@link Obstacle} objects
     */
    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    // ── Private helpers ───────────────────────────────────────────────

    /**
     * Randomly selects an obstacle type and spawns it just off the right edge of
     * the panel.
     *
     * <p>
     * Spawn probability weights (out of 10):
     * </p>
     * <ul>
     * <li>Small cactus – 30 %</li>
     * <li>Large cactus – 30 %</li>
     * <li>Cluster cactus – 20 %</li>
     * <li>Bird – 20 %</li>
     * </ul>
     */
    private void spawnObstacle() {
        ObstacleType type;
        int roll = random.nextInt(10);

        if (roll < 3)
            type = ObstacleType.CACTUS_SMALL;
        else if (roll < 6)
            type = ObstacleType.CACTUS_LARGE;
        else if (roll < 8)
            type = ObstacleType.CACTUS_CLUSTER;
        else
            type = ObstacleType.BIRD;

        int x = PANEL_WIDTH + 20; // Start just beyond the visible area

        switch (type) {
            case CACTUS_SMALL:
                obstacles.add(new Obstacle(type, x, GROUND_Y - 35, 20, 35));
                break;
            case CACTUS_LARGE:
                obstacles.add(new Obstacle(type, x, GROUND_Y - 52, 24, 52));
                break;
            case CACTUS_CLUSTER:
                obstacles.add(new Obstacle(type, x, GROUND_Y - 42, 60, 42));
                break;
            case BIRD: {
                // Birds appear at three different heights to vary difficulty
                int[] pteroHeights = { GROUND_Y - 80, GROUND_Y - 55, GROUND_Y - 110 };
                int py = pteroHeights[random.nextInt(pteroHeights.length)];
                obstacles.add(new Obstacle(type, x, py, 52, 34));
                break;
            }
        }
    }
}
