/**
 * Represents the three possible states of the Dino Runner game.
 *
 * <ul>
 * <li>{@link #IDLE} – The start screen is displayed; waiting for the player's
 * first input.</li>
 * <li>{@link #RUNNING} – The game is active; the dino is running and obstacles
 * are spawning.</li>
 * <li>{@link #GAME_OVER} – A collision occurred; the game-over screen is
 * displayed.</li>
 * </ul>
 */
public enum GameState {
    /** Waiting for the first key press (start screen). */
    IDLE,

    /** Game is actively running; physics and obstacles are updated every tick. */
    RUNNING,

    /** A collision has occurred; the game-over overlay is shown. */
    GAME_OVER
}
