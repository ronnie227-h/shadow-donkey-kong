/**
 * Represents a normal monkey.
 * Walks on platforms and follows a fixed route.
 */
public class NormalMonkey extends Monkey {

    private static final double NORMAL_MONKEY_SPEED = 0.5;

    /**
     * Creates a normal monkey using the config string from app.properties.
     * Format: x,y;direction;distance1,distance2,...
     *
     * @param config Configuration entry for this monkey.
     */
    public NormalMonkey(String config) {
        super(config, "res/normal_monkey_left.png", "res/normal_monkey_right.png");
    }

    /**
     * Returns the walking speed of a normal monkey.
     */
    @Override
    protected double getSpeed() {
        return NORMAL_MONKEY_SPEED;
    }

    /**
     * Updates this monkey (gravity, movement, drawing).
     */
    @Override
    public void update(Platform[] platforms) {
        super.update(platforms);
    }

}
