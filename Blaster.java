import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;

/**
 * Represents a Blaster weapon in the game.
 * When collected by Mario, it grants bullets that can be used to damage enemies.
 */
public class Blaster {
    private final Image image = new Image("res/blaster.png");
    private final double x, y;
    private final double width, height;

    private int bullets = 5;
    private boolean collected = false;

    /**
     * Constructs a Blaster object using its position from app.properties.
     * @param level Level number the blaster belongs to
     * @param index Index of the blaster in that level
     */
    public Blaster(int level, int index) {
        String key = "blaster.level" + level + "." + index;
        String value = IOUtils.getProperty(key);
        String[] coords = value.split(",");
        this.x = Double.parseDouble(coords[0].trim());
        this.y = Double.parseDouble(coords[1].trim());


        this.width = image.getWidth();
        this.height = image.getHeight();
    }

    /**
     * Draws the blaster if it has not been collected.
     */
    public void draw() {
        if (!collected) {
            image.draw(x, y);
        }
    }

    /**
     * Returns the bounding box used for collision detection.
     * If collected, returns an off-screen rectangle.
     */
    public Rectangle getBoundingBox() {
        return image.getBoundingBoxAt(new Point(x + width / 2, y + height / 2));
    }

    /**
     * Checks whether the blaster has been collected.
     * @return true if collected, false otherwise
     */
    public boolean isCollected() {
        return collected;
    }

    /**
     * Marks the blaster as collected.
     */
    public void collect() {
        this.collected = true;
    }

    /**
     * Sets the bullet count to a new value.
     * @param b New bullet count to assign
     */
    public void setBullets(int b) {
        this.bullets = b;
    }

    /**
     * Returns the current number of bullets remaining in the blaster.
     * @return The number of bullets available
     */
    public int getAmmo() {
        return bullets;
    }



}
