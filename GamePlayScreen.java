import bagel.*;
import bagel.util.Point;
import java.util.*;

/**
 * Represents the main gameplay screen where the player controls Mario.
 * This class manages game objects, updates their states, and handles game logic.
 */
public class GamePlayScreen {
    private final Properties GAME_PROPS;
    private final Font STATUS_FONT;
    private final int MAX_FRAMES;

    private Mario mario;
    private Donkey donkey;
    private Platform[] platforms;
    private Ladder[] ladders;
    private Barrel[] barrels;
    private List<Hammer> hammers;
    private List<Monkey> monkeys;
    private List<Blaster> blasters;
    private final List<Bullet> bullets;

    private final Image background;
    private Point scoreDisplay;
    private Point timeDisplay;
    private Point bulletDisplayPoint;
    private Point donkeyHealthDisplayPoint;



    private int currFrame = 0;
    private boolean isGameOver = false;
    private boolean isGameWon = false;
    private int score = 0;
    private int destroyedBarrels = 0;
    private int jumpedBarrels = 0;
    private int destroyedMonkeys = 0;
    private int finalScore = 0;


    private final int SCORE_X;
    private final int SCORE_Y;
    private final int TIME_DISPLAY_DIFF_Y = 30;
    private final String SCORE_MESSAGE = "SCORE ";
    private final String TIME_MESSAGE = "Time Left ";
    private int timeRemaining = 0;
    private final int currentLevel;
    private final int startingScore;




    /**
     * Constructs the gameplay screen, loading resources and initializing game objects.
     *
     * @param gameProps Properties file containing game settings.
     * @param level     Game level to start (1 or 2).
     */
    public GamePlayScreen(Properties gameProps, int level, int startingScore) {
        this.score = startingScore;
        this.startingScore = startingScore;
        this.GAME_PROPS = gameProps;
        this.currentLevel = level;

        // General config
        this.MAX_FRAMES = Integer.parseInt(gameProps.getProperty("gamePlay.maxFrames"));
        this.SCORE_X = Integer.parseInt(gameProps.getProperty("gamePlay.score.x"));
        this.SCORE_Y = Integer.parseInt(gameProps.getProperty("gamePlay.score.y"));
        this.STATUS_FONT = new Font(
                gameProps.getProperty("font"),
                Integer.parseInt(gameProps.getProperty("gamePlay.score.fontSize"))
        );
        this.background = new Image(gameProps.getProperty("backgroundImage"));

        // UI display points
        this.scoreDisplay = new Point(SCORE_X, SCORE_Y);
        this.timeDisplay = new Point(SCORE_X, SCORE_Y + TIME_DISPLAY_DIFF_Y);

        String[] coords = IOUtils.getProperty("gamePlay.donkeyhealth.coords").split(",");
        this.donkeyHealthDisplayPoint = new Point(
                Double.parseDouble(coords[1].trim()),
                Double.parseDouble(coords[0].trim())
        );
        this.bulletDisplayPoint = new Point(
                Double.parseDouble(coords[1].trim()),
                Double.parseDouble(coords[0].trim()) + TIME_DISPLAY_DIFF_Y
        );

        // Game object lists
        this.monkeys = new ArrayList<>();
        this.hammers = new ArrayList<>();
        this.blasters = new ArrayList<>();
        this.bullets = new ArrayList<>();

        // Initialize all game objects for the given level
        initializeGameObjects(level);
        System.out.println("HEALTH UI AT: x=" + donkeyHealthDisplayPoint.x + ", y=" + donkeyHealthDisplayPoint.y);
        System.out.println("BULLET UI AT: x=" + bulletDisplayPoint.x + ", y=" + bulletDisplayPoint.y);

    }


    /**
     * Initializes game objects such as Mario, Donkey Kong, barrels, ladders, platforms, and the hammer.
     */
    private void initializeGameObjects(int level) {
        // 1. Mario & Donkey
        mario = new Mario(level);
        donkey = new Donkey(level);

        // 2. Barrels
        String barrelKey = "barrel.level" + level + ".count";
        int barrelCount = Integer.parseInt(IOUtils.getProperty(barrelKey));
        barrels = new Barrel[barrelCount];
        for (int i = 1; i <= barrelCount; i++) {
            barrels[i - 1] = new Barrel(level, i);
        }

        // 3. Ladders
        String ladderKey = "ladder.level" + level + ".count";
        int ladderCount = Integer.parseInt(IOUtils.getProperty(ladderKey));
        ladders = new Ladder[ladderCount];
        for (int i = 1; i <= ladderCount; i++) {
            ladders[i - 1] = new Ladder(level, i);
        }

        // 4. Platforms
        String platformData = GAME_PROPS.getProperty("platforms.level" + level);
        if (platformData != null && !platformData.isEmpty()) {
            String[] platformEntries = platformData.split(";");
            platforms = new Platform[platformEntries.length];
            for (int i = 0; i < platformEntries.length; i++) {
                String[] coords = platformEntries[i].trim().split(",");
                platforms[i] = new Platform(Double.parseDouble(coords[0].trim()), Double.parseDouble(coords[1].trim()));
            }
        } else {
            platforms = new Platform[0];
        }

        // 5. Hammers
        int hammerCount = Integer.parseInt(IOUtils.getPropertyOrDefault("hammer.level" + level + ".count", "0"));
        hammers = new ArrayList<>();
        for (int i = 1; i <= hammerCount; i++) {
            hammers.add(new Hammer(level, i));
        }

        // 6. Blasters
        int blasterCount = Integer.parseInt(IOUtils.getPropertyOrDefault("blaster.level" + level + ".count", "0"));
        blasters = new ArrayList<>();
        for (int i = 1; i <= blasterCount; i++) {
            blasters.add(new Blaster(level, i));
        }


        // 7. Monkeys
        monkeys = new ArrayList<>();
        int normalCount = Integer.parseInt(IOUtils.getPropertyOrDefault("normalMonkey.level" + level + ".count", "0"));
        for (int i = 1; i <= normalCount; i++) {
            monkeys.add(new NormalMonkey(IOUtils.getProperty("normalMonkey.level" + level + "." + i)));
        }


        int intelligentCount = Integer.parseInt(IOUtils.getPropertyOrDefault("intelligentMonkey.level" + level + ".count", "0"));
        for (int i = 1; i <= intelligentCount; i++) {
            monkeys.add(new IntelligentMonkey(IOUtils.getProperty("intelligentMonkey.level" + level + "." + i)));
        }


    }
    /**
         * Updates game state each frame.
         *
         * @param input The current player input.
         * @return {@code true} if the game ends, {@code false} otherwise.
         */
    public boolean update(Input input) {
        currFrame++;

        // 1) Draw background
        background.drawFromTopLeft(0, 0);

        // 2) Draw platforms
        for (Platform platform : platforms) {
            platform.draw();
        }

        // 3) Update ladders
        for (Ladder ladder : ladders) {
            ladder.update(platforms);
        }

        // 4) Update and draw barrels
        for (Barrel barrel : barrels) {
            barrel.update(platforms);

            if (!barrel.isDestroyed() && mario.isTouchingBarrel(barrel)) {
                if (mario.hasHammer()) {
                    barrel.destroy();
                    incrementDestroyedBarrels();
                    score += 100;
                } else {
                    isGameOver = true;
                }
            }

            if (mario.jumpOver(barrel)) {
                incrementJumpedBarrels();
                score += 30;
            }

            barrel.draw();
        }

        // 5) Draw hammers
        for (Hammer hammer : hammers) {
            if (!hammer.isCollected()) {
                hammer.draw();
            }
        }

        // 6) Draw blasters
        for (Blaster blaster : blasters) {
            if (!blaster.isCollected()) {
                blaster.draw();
            }
        }

        // 7) Update and draw donkey
        donkey.update(platforms);
        donkey.draw();

        // 8) Update monkeys
        for (Monkey monkey : monkeys) {
            monkey.update(platforms);
        }

        // 9) Update bananas from intelligent monkeys
        for (Monkey monkey : monkeys) {
            if (monkey instanceof IntelligentMonkey im) {
                Iterator<Banana> bananaIterator = im.getBananas().iterator();
                while (bananaIterator.hasNext()) {
                    Banana banana = bananaIterator.next();
                    banana.update();
                    banana.draw();

                    // Remove banana if expired
                    if (banana.hasExpired()) {
                        bananaIterator.remove();
                        continue;
                    }

                    // Mario hit by banana: only if no hammer or blaster
                    if (banana.getBoundingBox().intersects(mario.getBoundingBox())){
                        isGameOver = true;
                    }
                }
            }
        }


        // 10) Update and draw bullets
        List<Bullet> toRemove = new ArrayList<>();
        for (Bullet bullet : bullets) {
            bullet.update(platforms, Window.getWidth());
            bullet.draw();

            // Check collision with monkeys
            for (Monkey monkey : monkeys) {
                if (!monkey.isDead() && bullet.getBoundingBox().intersects(monkey.getBoundingBox())) {
                    monkey.die();
                    incrementDestroyedMonkeys();
                    score += 100;
                    bullet.markSpent();
                }
            }

            // Check collision with Donkey
            if (!donkey.isDead() && bullet.getBoundingBox().intersects(donkey.getBoundingBox())) {
                donkey.takeDamage();
                bullet.markSpent();

                if (donkey.isDead()) {
                    isGameWon = true;// Mark game as won
                    computeFinalScore(false);
                }
            }

            // Queue bullet for removal if spent
            if (bullet.isSpent()) {
                toRemove.add(bullet);
            }
        }
        // Remove bullets that hit something or expired
        bullets.removeAll(toRemove);


        // 11) Update Mario
        mario.update(input, ladders, platforms, hammers, blasters, bullets);

        // 12) Mario vs Monkey collision
        for (Monkey monkey : monkeys) {
            if (!monkey.isDead() && mario.getBoundingBox().intersects(monkey.getBoundingBox())) {
                if (mario.hasHammer()) {
                    monkey.die();
                    incrementDestroyedMonkeys();
                    score += 100;
                } else {
                    isGameOver = true;
                }
            }
        }

        // 13) Win or lose on donkey touch
        if (mario.hasReached(donkey)) {
            if (mario.hasHammer() || donkey.isDead()) {
                isGameWon = true;// Win condition met
            } else {
                isGameOver = true;
            }
        }

        if (checkingGameTime()) {
            isGameOver = true;
        }

        // 14) Draw UI
        displayInfo();
        displayCombatInfo();

        // 15) Return game end condition
        return isGameOver || isGameWon || isLevelCompleted();


    }


    /**
     * Displays the player's score & time left on the screen.
     */
    public void displayInfo() {
        STATUS_FONT.drawString(SCORE_MESSAGE + score, SCORE_X, SCORE_Y);

        int secondsLeft = (MAX_FRAMES - currFrame) / 60;
        STATUS_FONT.drawString(TIME_MESSAGE + secondsLeft, SCORE_X, SCORE_Y + TIME_DISPLAY_DIFF_Y);

        // update timeRemaining for final score
        timeRemaining = secondsLeft;
    }

    /**
     * Displays Donkey’s health and bullet count (left-aligned, only in level 2).
     */
    private void displayCombatInfo() {
        if (currentLevel != 2) return;

        // Always show bullet count (even if not picked up)
        int bulletCount = mario.getBulletCount();  // defaults to 0 if not holding
        STATUS_FONT.drawString("BULLET " + bulletCount,
                bulletDisplayPoint.x, bulletDisplayPoint.y);

        // Show Donkey health only if Donkey is alive
        if (donkey != null && !donkey.isDead()) {
            STATUS_FONT.drawString("DONKEY HEALTH " + donkey.getHealth(),
                    donkeyHealthDisplayPoint.x, donkeyHealthDisplayPoint.y);
        }
    }

    /**
     * Determines whether Mario has completed the level.
     * He must reach Donkey Kong while holding the hammer OR Donkey is dead.
     */
    public boolean isLevelCompleted() {
        return mario.hasReached(donkey) && (mario.hasHammer() || donkey.isDead());
    }

    /**
     * Checks if the time limit has been exceeded.
     */
    public boolean checkingGameTime() {
        return currFrame >= MAX_FRAMES;
    }



    /**
     * Increments score counters.
     */
    public void incrementDestroyedBarrels() {
        destroyedBarrels++;
    }

    public void incrementJumpedBarrels() {
        jumpedBarrels++;
    }

    public void incrementDestroyedMonkeys() {
        destroyedMonkeys++;
    }

    /**
     * Final score formula: 100 per barrel destroyed,
     * 30 per barrel jumped, 3 per second left, 100 per monkey killed.
     * If player dies, finalScore is 0.
     * Includes score from previous level (startingScore).
     */
    public void computeFinalScore(boolean playerDied) {
        if (playerDied) {
            finalScore = 0;
        } else {
            finalScore = startingScore
                    + (100 * destroyedBarrels)
                    + (30 * jumpedBarrels)
                    + (3 * timeRemaining)
                    + (100 * destroyedMonkeys);
        }
    }

    public int getFinalScore() {
        return finalScore;
    }


    public boolean hasWon() {
        return isGameWon;
    }

    public int getScore() {
        return score;
    }


}
