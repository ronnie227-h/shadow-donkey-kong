import bagel.Image;
import bagel.util.Rectangle;

/**
 * Represents a Hammer collectible in the game.
 * The hammer can be collected by the player, at which point it disappears from the screen.
 */
public class Hammer {
    private final Image HAMMER_IMAGE;
    private final double WIDTH, HEIGHT;
    private final double x;
    private final double y;
    private boolean isCollected = false;



    /**
     * Constructs a Hammer using its position from the app.properties file.
     * Position is read using the key format "hammer.levelZ.A", where:
     * - Z is the level number
     * - A is the hammer index (starting from 1)
     * Example: hammer.level2.2=600,340
     * @param level The level number (e.g., 1 or 2).
     * @param index The hammer index in the level.
     */
    public Hammer(int level, int index) {
        String key = "hammer.level" + level + "." + index;
        String value = IOUtils.getProperty(key);
        String[] coords = value.split(",");
        this.x = Double.parseDouble(coords[0].trim());
        this.y = Double.parseDouble(coords[1].trim());
        this.HAMMER_IMAGE = new Image("res/hammer.png");
        this.WIDTH = HAMMER_IMAGE.getWidth();
        this.HEIGHT = HAMMER_IMAGE.getHeight();
    }



    /**
     * Returns the bounding box of the hammer for collision detection.
     * If the hammer has been collected, it returns an off-screen bounding box.
     *
     * @return A {@link Rectangle} representing the hammer's bounding box.
     */
    public Rectangle getBoundingBox() {
        if (isCollected) {
            return new Rectangle(-1000, -1000, 0, 0); // Move off-screen if collected
        }
        return new Rectangle(
                x - (WIDTH / 2),  // Center-based positioning
                y - (HEIGHT / 2),
                WIDTH,
                HEIGHT
        );
    }

    /**
     * Draws the hammer on the screen if it has not been collected.
     */
    public void draw() {
        if (!isCollected) {
            HAMMER_IMAGE.draw(x, y); // Bagel centers images automatically
//            drawBoundingBox(); // Uncomment for debugging
        }
    }

    /**
     * Marks the hammer as collected, removing it from the screen.
     */
    public void collect() {
        isCollected = true;
    }

    /**
     * Checks if the hammer has been collected.
     *
     * @return {@code true} if the hammer is collected, {@code false} otherwise.
     */
    public boolean isCollected() {
        return isCollected;
    }

}
