import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Base class for all monkeys.
 * Handles gravity, movement, and animation.
 */
public abstract class Monkey {
    protected double x, y;// Position
    protected double velocityY = 0;// Vertical velocity
    protected boolean landed = false;
    protected boolean dead = false;
    protected final Image monkeyLeftImage;
    protected final Image monkeyRightImage;

    protected double width;
    protected double height;

    private static final double GRAVITY = 0.4;
    private static final double TERMINAL_VELOCITY = 5.0;
    private static final double SPEED = 1.0; // 默认速度，子类可重写

    // Movement pattern
    protected List<Integer> routeDistances;
    protected int currentRouteIndex = 0;
    protected double distanceMovedInCurrentSegment = 0;
    protected boolean movingRight ;

    /**
     * Constructs a monkey using a config string.
     * Format: "x,y;direction;route1,route2,..."
     */
    public Monkey(String configEntry, String leftImagePath, String rightImagePath) {
        this.monkeyLeftImage = new Image(leftImagePath);
        this.monkeyRightImage = new Image(rightImagePath);
        // Format: x,y;direction;route1,route2,...
        String[] parts = configEntry.split(";");
        String[] pos = parts[0].split(",");
        this.x = Double.parseDouble(pos[0].trim());
        this.y = Double.parseDouble(pos[1].trim());
        this.width = monkeyLeftImage.getWidth();
        this.height = monkeyLeftImage.getHeight();

        this.movingRight = parts[1].trim().equalsIgnoreCase("right");

        this.routeDistances = parseRoute(parts[2]);
    }

    // Parses comma-separated walk distances from string
    private List<Integer> parseRoute(String routeStr) {
        String[] distances = routeStr.split(",");
        return distances.length == 0
                ? List.of(30) // fallback default
                : Arrays.stream(distances)  // ✅ 修正
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());  // ✅ 兼容 Java 8+
    }


    /**
     * Updates monkey logic every frame: gravity → move → draw
     */
    public void update(Platform[] platforms) {
        applyGravity(platforms);

        if (!dead) {
            move(platforms);
            draw();
        }
    }

    /**
     * Applies gravity if monkey is in the air.
     * Checks if feet are touching a platform.
     */
    protected void applyGravity(Platform[] platforms) {
        landed = false;  // 每帧重设为未落地，重新判断

        velocityY += GRAVITY;
        if (velocityY > TERMINAL_VELOCITY) {
            velocityY = TERMINAL_VELOCITY;
        }
        y += velocityY;

        // Create a small rectangle just under the feet
        Rectangle feet = new Rectangle(x, y + height, width, 1);

        for (Platform p : platforms) {
            if (feet.intersects(p.getBoundingBox())) {
                y = p.getTopY() - height;
                velocityY = 0;
                landed = true;
                break;
            }
        }
    }


    /**
     * Handles monkey walking according to its route.
     * Turns back early if walking off a platform.
     */
    protected void move(Platform[] platforms) {
        if (routeDistances.isEmpty()) return;

        double step = getSpeed();
        double dx = movingRight ? step : -step;
        double nextX = x + dx;

        Platform platformBelow = getPlatformUnderMonkey(platforms);

        // Don't move if monkey hasn't landed or isn't above a platform
        if (!landed || platformBelow == null) {
            return;
        }

        // Create a 1-pixel rectangle at the next edge of foot
        double edgeX = movingRight ? nextX + width : nextX;
        Rectangle edgeFeet = new Rectangle(edgeX, y + height, 1, 1);

        boolean nextStepWillFall = true;

        for (Platform p : platforms) {
            if (edgeFeet.intersects(p.getBoundingBox())) {
                nextStepWillFall = false;
                break;
            }
        }



        // Turn around if next step would fall off
        if (nextStepWillFall) {
            distanceMovedInCurrentSegment = 0;
            currentRouteIndex = (currentRouteIndex + 1) % routeDistances.size();
            movingRight = !movingRight;
            return;
        }


        // Proceed with walking
        x = nextX;
        distanceMovedInCurrentSegment += Math.abs(dx);

        // Reached the end of current distance segment
        if (distanceMovedInCurrentSegment >= routeDistances.get(currentRouteIndex)) {
            distanceMovedInCurrentSegment = 0;
            currentRouteIndex = (currentRouteIndex + 1) % routeDistances.size();
            movingRight = !movingRight;
        }
    }

    /**
     * Returns movement speed.
     * Can be overridden in subclasses.
     */
    protected double getSpeed() {
        return SPEED; // 子类可 override
    }

    /**
     * Draws the monkey sprite based on direction.
     */
    public void draw() {
        if (movingRight) {
            monkeyRightImage.drawFromTopLeft(x, y);
        } else {
            monkeyLeftImage.drawFromTopLeft(x, y);
        }
    }

    /**
     * Returns the collision box of the monkey.
     */
    public Rectangle getBoundingBox() {
        Image currentImage = movingRight ? monkeyRightImage : monkeyLeftImage;
        return currentImage.getBoundingBoxAt(new Point(x + width / 2, y + height / 2));
    }

    /**
     * Returns whether monkey is dead.
     */
    public boolean isDead() {
        return dead;
    }

    /**
     * Kills the monkey.
     */
    public void die() {
        dead = true;
    }

    public double getX() { return x; }
    public double getY() { return y; }

    /**
     * Finds the closest platform directly under the monkey.
     */
    private Platform getPlatformUnderMonkey(Platform[] platforms) {
        Platform bestPlatform = null;
        double closestDY = Double.MAX_VALUE;

        for (Platform p : platforms) {
            Rectangle feet = new Rectangle(x, y + height, width, 1);
            if (feet.intersects(p.getBoundingBox())) {
                double dy = Math.abs(p.getTopY() - (y + height));
                if (dy < closestDY) {
                    closestDY = dy;
                    bestPlatform = p;
                }
            }
        }
        return bestPlatform;
    }


}
