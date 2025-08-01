import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;

/**
 * Represents a bullet shot by Mario when holding a blaster.
 * The bullet moves horizontally, has a max range, and disappears upon hitting a platform or target.
 */
public class Bullet {
    private static final double SPEED = 3.8;
    private static final double MAX_DISTANCE = 300;
    private static final Image BULLET_LEFT = new Image("res/bullet_left.png");
    private static final Image BULLET_RIGHT = new Image("res/bullet_right.png");

    private double x;
    private final double y;
    private final boolean movingRight;
    private double distanceTravelled = 0;
    private boolean spent = false;

    /**
     * Constructs a new bullet at the specified location and direction.
     * @param startX Initial x-coordinate
     * @param startY Initial y-coordinate
     * @param movingRight Whether the bullet moves to the right
     */
    public Bullet(double startX, double startY, boolean movingRight) {
        this.x = startX;
        this.y = startY;
        this.movingRight = movingRight;
    }


    /**
     * Updates bullet position each frame and checks for termination conditions.
     * A bullet is marked spent if it exceeds its range, exits the screen,
     * or collides with a platform.
     *
     * @param platforms All platforms in the level
     * @param screenWidth Width of the game screen
     */
    public void update(Platform[] platforms, double screenWidth) {
        if (spent) return;

        // Move bullet in current direction
        double dx = movingRight ? SPEED : -SPEED;
        x += dx;
        distanceTravelled += Math.abs(dx);

        // Mark bullet as spent if it travels too far or exits the screen
        if (distanceTravelled > MAX_DISTANCE || x < 0 || x > screenWidth) {
            spent = true;
        }

        // Mark bullet as spent if it hits any platform
        for (Platform p : platforms) {
            if (getBoundingBox().intersects(p.getBoundingBox())) {
                spent = true;
                break;
            }
        }
    }

    /**
     * Draws the bullet on screen if it is still active.
     */
    public void draw() {
        if (!spent) {
            getImage().draw(x, y);
        }
    }

    /**
     * Returns the appropriate image based on direction.
     */
    private Image getImage() {
        return movingRight ? BULLET_RIGHT : BULLET_LEFT;
    }

    /**
     * Returns whether the bullet has been spent (should be removed).
     */
    public boolean isSpent() {
        return spent;
    }

    /**
     * Marks this bullet as spent (to be removed).
     */
    public void markSpent() {
        spent = true;
    }

    /**
     * Returns the bounding box of the bullet for collision detection.
     */
    public Rectangle getBoundingBox() {
        return getImage().getBoundingBoxAt(new Point(x, y));
    }
}
