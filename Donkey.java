import bagel.*;
import bagel.util.Rectangle;

/**
 * Represents Donkey Kong in the game, affected by gravity and platform collisions.
 * The Donkey object moves downward due to gravity and lands on platforms when applicable.
 */
public class Donkey {
    private static final int MAX_HEALTH = 5;

    private final double x;
    private double y;
    private final Image DONKEY_IMAGE;
    private double velocityY = 0;
    private int health = MAX_HEALTH;
    private boolean isDead = false;

    /**
     * Constructs a new Donkey Kong at the starting position defined in app.properties for the given level.
     *
     * @param level     The current game level (1 or 2).
     */
    public Donkey(int level) {
        String key = "donkey.level" + level;
        String value = IOUtils.getProperty(key);
        String[] coords = value.split(",");
        this.x = Double.parseDouble(coords[0].trim());
        this.y = Double.parseDouble(coords[1].trim());

        this.DONKEY_IMAGE = new Image("res/donkey_kong.png");
    }


    /**
     * Applies gravity and checks for platform collisions.
     * Stops falling if Donkey lands on a platform.
     */
    public void update(Platform[] platforms) {
        // Apply gravity
        velocityY += Physics.DONKEY_GRAVITY;
        y += velocityY;
        if (velocityY > Physics.DONKEY_TERMINAL_VELOCITY) {
            velocityY = Physics.DONKEY_TERMINAL_VELOCITY;
        }

        // Check for platform collisions
        for (Platform platform : platforms) {
            if (isTouchingPlatform(platform)) {
                // Position Donkey on top of the platform
                y = platform.getY() - (platform.getHeight() / 2) - (DONKEY_IMAGE.getHeight() / 2);
                velocityY = 0; // Stop downward movement
                break;
            }
        }

        // Draw Donkey
        draw();
    }

    /**
     * Checks if Donkey is colliding with a given platform.
     *
     * @param platform The platform to check for collision.
     * @return {@code true} if Donkey is touching the platform, {@code false} otherwise.
     */
    private boolean isTouchingPlatform(Platform platform) {
        Rectangle donkeyBounds = getBoundingBox();
        return donkeyBounds.intersects(platform.getBoundingBox());
    }

    /**
     * Draws Donkey on the screen.
     */
    public void draw() {
        DONKEY_IMAGE.draw(x, y);
//        drawBoundingBox(); // Uncomment for debugging
    }

    /**
     * Returns Donkey's bounding box for collision detection.
     *
     * @return A {@link Rectangle} representing Donkey's bounding box.
     */
    public Rectangle getBoundingBox() {
        return new Rectangle(
                x - (DONKEY_IMAGE.getWidth() / 2),
                y - (DONKEY_IMAGE.getHeight() / 2),
                DONKEY_IMAGE.getWidth(),
                DONKEY_IMAGE.getHeight()
        );
    }

    /**
     * Reduces Donkey’s health by 1.
     * Marks Donkey as dead if health reaches 0.
     */
    public void takeDamage() {
        if (health > 0) {
            health--;
            if (health == 0) {
                isDead = true;
            }
        }
    }

    /**
     * Returns true if Donkey is dead.
     */
    public boolean isDead() {
        return isDead;
    }

    /**
     * Returns current health value.
     */
    public int getHealth() {
        return health;
    }

    /**
     * Gets Donkey's current x-coordinate.
     */
    public double getX() {
        return x;
    }

    /**
     * Gets Donkey's current y-coordinate.
     */
    public double getY() {
        return y;
    }


}
