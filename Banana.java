import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;

/**
 * Represents a Banana projectile thrown by an IntelligentMonkey.
 * Bananas move horizontally and expire after traveling a set distance.
 */
public class Banana {
    private static final Image IMAGE = new Image("res/banana.png");
    private static final double SPEED = 1.8;
    private static final double MAX_DISTANCE = 300;

    private double x;
    private final double y;
    private final boolean movingRight;
    private double distanceTraveled = 0;
    private boolean expired = false;

    /**
     * Creates a Banana with a given starting position and direction.
     *
     * @param x           Starting x-coordinate
     * @param y           Starting y-coordinate
     * @param movingRight Direction of motion
     */
    public Banana(double x, double y, boolean movingRight) {
        this.x = x;
        this.y = y;
        this.movingRight = movingRight;
    }

    /**
     * Updates banana position and marks it expired if it exceeds travel limit.
     */
    public void update() {
        if (expired) return;

        double dx = movingRight ? SPEED : -SPEED;
        x += dx;
        distanceTraveled += Math.abs(dx);

        if (distanceTraveled >= MAX_DISTANCE) {
            expired = true;
        }
    }

    /**
     * Draws the banana if it's still active.
     */
    public void draw() {
        if (!expired) {
            IMAGE.draw(x, y);
        }
    }

    /**
     * Returns whether the banana has exceeded its max range.
     *
     * @return {@code true} if expired, {@code false} otherwise.
     */
    public boolean hasExpired() {
        return expired;
    }

    /**
     * Returns the bounding box used for collision detection.
     *
     * @return A {@link Rectangle} representing the banana's position.
     */
    public Rectangle getBoundingBox() {
        return IMAGE.getBoundingBoxAt(new Point(x + IMAGE.getWidth() / 2, y + IMAGE.getHeight() / 2));
    }

    /**
     * Forces the banana to expire immediately.
     * Currently unused but kept for future extension (e.g. global banana removal).
     */
    public void expire() {
        expired = true;
    }
}
