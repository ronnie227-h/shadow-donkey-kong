import bagel.*;
import bagel.util.Rectangle;

/**
 * Represents a ladder in the game.
 * The ladder falls under gravity until it lands on a platform.
 */
public class Ladder {
    private final Image LADDER_IMAGE;
    private final double x;
    private double y;
    public static double width;
    public static double height;

    private double velocityY = 0; // Current vertical velocity due to gravity

    /**
     * Constructs a Ladder by reading its position from app.properties.
     * The key must be in the format "ladder.levelZ.A", where:
     * - Z is the level number
     * - A is the ladder index number
     *
     * @param level The current level number (e.g. 1 or 2)
     * @param index The index of this ladder in the level (starting from 1)
     */
    public Ladder(int level, int index) {
        this.LADDER_IMAGE = new Image("res/ladder.png");
        String key = "ladder.level" + level + "." + index;
        String value = IOUtils.getProperty(key);
        String[] coords = value.split(",");
        this.x = Double.parseDouble(coords[0].trim());
        this.y = Double.parseDouble(coords[1].trim());

        width = LADDER_IMAGE.getWidth();
        height = LADDER_IMAGE.getHeight();
    }

    /**
     * Draws the ladder on the screen.
     */
    public void draw() {
        LADDER_IMAGE.draw(x, y);
//        drawBoundingBox(); // Uncomment for debugging
    }

    /**
     * Updates the ladder's position by applying gravity and checking for platform collisions.
     * If a collision is detected, the ladder stops falling and rests on the platform.
     *
     * @param platforms An array of platforms in the game.
     */
    public void update(Platform[] platforms) {
        // 1) Apply gravity
        velocityY += Physics.LADDER_GRAVITY;

        // 2) Limit falling speed to terminal velocity
        if (velocityY > Physics.LADDER_TERMINAL_VELOCITY) {
            velocityY = Physics.LADDER_TERMINAL_VELOCITY;
        }

        // 3) Move the ladder downward
        y += velocityY;

        // 4) Check for collision with platforms
        for (Platform platform : platforms) {
            if (getBoundingBox().intersects(platform.getBoundingBox())) {
                // Position the ladder on top of the platform
                y = platform.getY()
                        - (platform.getHeight() / 2)  // Platform top edge
                        - (this.getHeight() / 2);     // Ladder height offset

                velocityY = 0; // Stop falling
                break; // Stop checking further once the ladder lands
            }
        }

        // 5) Draw the ladder after updating position
        draw();
    }

    /**
     * Returns the bounding box of the ladder for collision detection.
     *
     * @return A {@link Rectangle} representing the ladder's bounding box.
     */
    public Rectangle getBoundingBox() {
        return new Rectangle(
                x - (LADDER_IMAGE.getWidth() / 2),
                y - (LADDER_IMAGE.getHeight() / 2),
                LADDER_IMAGE.getWidth(),
                LADDER_IMAGE.getHeight()
        );
    }


    /**
     * Gets the x-coordinate of the ladder.
     *
     * @return The current x-coordinate of the ladder.
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the y-coordinate of the ladder.
     *
     * @return The current y-coordinate of the ladder.
     */
    public double getY() {
        return y;
    }

    /**
     * Gets the width of the ladder.
     *
     * @return The width of the ladder.
     */
    public double getWidth() {
        return width;
    }

    /**
     * Gets the height of the ladder.
     *
     * @return The height of the ladder.
     */
    public double getHeight() {
        return height;
    }
}
