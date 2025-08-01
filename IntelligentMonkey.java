import java.util.ArrayList;
import java.util.List;

/**
 * Represents an intelligent monkey that can walk and throw bananas at regular intervals.
 * Extends the base Monkey class.
 */
public class IntelligentMonkey extends Monkey {

    private static final double INTELLIGENT_MONKEY_SPEED = 0.5;
    private static final int FIRE_INTERVAL_FRAMES = 300;

    private final List<Banana> bananas = new ArrayList<>();
    private int frameCounter = 0;

    /**
     * Constructs an IntelligentMonkey using its configuration string.
     * Loads sprite images and movement route from app.properties.
     *
     * @param config Configuration string (format: x,y;direction;distance1,distance2,...)
     */
    public IntelligentMonkey(String config) {
        super(config, "res/intelli_monkey_left.png", "res/intelli_monkey_right.png");
    }


    /**
     * Returns the movement speed of this intelligent monkey.
     */
    @Override
    protected double getSpeed() {
        return INTELLIGENT_MONKEY_SPEED;
    }

    /**
     * Updates the monkey’s movement, shooting logic, and banana behavior.
     * Bananas are fired every 5 seconds if the monkey is on a platform.
     */
    @Override
    public void update(Platform[] platforms) {
        super.update(platforms);

        if (!dead && landed) {
            frameCounter++;
            // Shoot banana every FIRE_INTERVAL_FRAMES
            if (frameCounter >= FIRE_INTERVAL_FRAMES) {
                frameCounter = 0;
                shootBanana();
            }

            // Update and draw all bananas
            for (Banana b : bananas) {
                b.update();
                b.draw();
            }

            // Remove expired bananas
            bananas.removeIf(Banana::hasExpired);
        }
    }

    /**
     * Creates and launches a new banana in the current facing direction.
     */
    private void shootBanana() {
        double bananaX = movingRight ? (x + width - 5) : (x - 5);
        double bananaY = y + height * 0.25;  // 发射点靠近肩膀
        bananas.add(new Banana(bananaX, bananaY, movingRight));
    }

    /**
     * Returns the list of bananas currently active for this monkey.
     */
    public List<Banana> getBananas() {
        return bananas;
    }
}
