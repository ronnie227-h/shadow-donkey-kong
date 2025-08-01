import bagel.*;
import bagel.util.Rectangle;
import bagel.util.Colour;
import java.util.List;
import java.awt.event.InputEvent;

/**
 * Represents the player-controlled character, Mario.
 * Mario can move, jump, climb ladders, pick up a hammer, and interact with platforms.
 */
public class Mario {
    private double x, y; // Mario's position
    private double velocityY = 0; // Vertical velocity
    private boolean isJumping = false; // Whether Mario is currently jumping
    private boolean hasHammer = false; // Whether Mario has collected a hammer
    private boolean hasBlaster = false;
    private int bulletCount = 0;

    // Mario images for different states
    private Image marioImage;
    private final Image MARIO_RIGHT_IMAGE;
    private final Image MARIO_LEFT_IMAGE;
    private final Image MARIO_HAMMER_LEFT_IMAGE;
    private final Image MARIO_HAMMER_RIGHT_IMAGE;
    private final Image MARIO_BLASTER_RIGHT_IMAGE = new Image("res/mario_blaster_right.png");
    private final Image MARIO_BLASTER_LEFT_IMAGE = new Image("res/mario_blaster_left.png");



    // Movement physics constants
    private static final double JUMP_STRENGTH = -5;
    private static final double MOVE_SPEED = 3.5;
    private static final double CLIMB_SPEED = 2;

    private static double height;
    private static double width;
    private boolean isFacingRight = true; // Mario's facing direction



    /**
     * Constructs a Mario character using the starting position defined
     * in the app.properties file for the given level.
     *
     * @param level the level number (1 or 2) used to look up the starting position
     */

    public Mario(int level) {
        String key = "mario.level" + level;
        String value = IOUtils.getProperty(key);
        String[] coords = value.split(",");
        this.x = Double.parseDouble(coords[0].trim());
        this.y = Double.parseDouble(coords[1].trim());
        // Load images for left and right-facing Mario
        this.MARIO_RIGHT_IMAGE = new Image("res/mario_right.png");
        this.MARIO_LEFT_IMAGE = new Image("res/mario_left.png");
        this.MARIO_HAMMER_RIGHT_IMAGE = new Image("res/mario_hammer_right.png");
        this.MARIO_HAMMER_LEFT_IMAGE = new Image("res/mario_hammer_left.png");

        // Default Mario starts facing right
        this.marioImage = MARIO_HAMMER_RIGHT_IMAGE;

        width = marioImage.getWidth();
        height = marioImage.getHeight();
    }

    /**
     * Sets whether Mario has picked up the hammer.
     *
     * @param status {@code true} if Mario has the hammer, {@code false} otherwise.
     */
    public void setHasHammer(boolean status) {
        this.hasHammer = status;
    }

    /**
     * Checks if Mario has the hammer.
     *
     * @return {@code true} if Mario has the hammer, {@code false} otherwise.
     */
    public boolean holdHammer() {
        return this.hasHammer;
    }

    /**
     * Gets Mario's bounding box for collision detection.
     *
     * @return A {@link Rectangle} representing Mario's collision area.
     */
    public Rectangle getBoundingBox() {
        return new Rectangle(
                x - (width / 2),
                y - (height / 2),
                width,
                height
        );
    }

    /**
     * Updates Mario's movement, jumping, climbing, weapon collection, shooting, and rendering.
     *
     * @param input     The player's input (keyboard/mouse).
     * @param ladders   The ladders in the level.
     * @param platforms The platforms in the level.
     * @param hammers   All hammer objects in the level.
     * @param blasters  All blaster objects in the level.
     * @param bullets   The global bullet list (to add bullets to).
     */
    public void update(Input input, Ladder[] ladders, Platform[] platforms,
                       List<Hammer> hammers, List<Blaster> blasters, List<Bullet> bullets) {

        // 1) Handle left/right movement
        handleHorizontalMovement(input);

        // 2) Handle hammer/blaster collection
        for (Hammer hammer : hammers) {
            collectHammer(hammer);
        }
        for (Blaster blaster : blasters) {
            collectBlaster(blaster);
        }

        // 3) Update sprite after weapon pickup or direction change
        updateSprite();

        // 4) Ladder logic
        boolean isOnLadder = handleLadders(input, ladders);

        // 5) Jumping input
        boolean wantsToJump = input.wasPressed(Keys.SPACE);

        // 6) Gravity (if not on ladder)
        if (!isOnLadder) {
            velocityY += Physics.MARIO_GRAVITY;
            velocityY = Math.min(Physics.MARIO_TERMINAL_VELOCITY, velocityY);
        }

        // 7) Vertical movement
        y += velocityY;

        // 8) Platform snapping & landing
        boolean onPlatform = handlePlatforms(platforms);

        // 9) Handle jump logic
        handleJumping(onPlatform, wantsToJump);

        // 10) Fire bullets if 'S' pressed and has blaster
        if (input.wasPressed(Keys.S) && hasBlaster && bulletCount > 0) {
            bullets.add(new Bullet(x, y, isFacingRight)); // 👈 你需要有 Bullet 构造器支持
            consumeBullet();;
        }

        // 11) Prevent going out of screen
        enforceBoundaries();

        // 12) Draw Mario
        draw();
    }


    /**
     * Handles Mario's interaction with platforms to determine if he is standing on one.
     * Mario will only snap to a platform if he is moving downward (velocityY >= 0),
     * preventing his jump from being interrupted in mid-air.
     *
     * @param platforms An array of {@link Platform} objects representing the platforms in the game.
     * @return {@code true} if Mario is standing on a platform, {@code false} otherwise.
     */
    private boolean handlePlatforms(Platform[] platforms) {
        boolean onPlatform = false;

        // We'll only snap Mario to a platform if he's moving downward (velocityY >= 0)
        // so we don't kill his jump in mid-air.
        if (velocityY >= 0) {
            for (Platform platform : platforms) {
                Rectangle marioBounds    = getBoundingBox();
                Rectangle platformBounds = platform.getBoundingBox();

                if (marioBounds.intersects(platformBounds)) {
                    double marioBottom = marioBounds.bottom();
                    double platformTop = platformBounds.top();

                    // If Mario's bottom is at or above the platform's top
                    // and not far below it (a small threshold based on velocity)
                    if (marioBottom <= platformTop + velocityY) {
                        // Snap Mario so his bottom = the platform top
                        y = platformTop - (marioImage.getHeight() / 2);
                        velocityY = 0;
                        isJumping = false;
                        onPlatform = true;
                        break; // We found a platform collision
                    }
                }
            }
        }
        return onPlatform;
    }

    /**
     * Handles Mario's interaction with ladders, allowing him to climb up or down
     * based on user input and position relative to the ladder.
     *
     * Mario can only climb if he is within the horizontal boundaries of the ladder.
     * He stops sliding unintentionally when not pressing movement keys.
     *
     * @param input   The {@link Input} object that checks for user key presses.
     * @param ladders An array of {@link Ladder} objects representing ladders in the game.
     * @return {@code true} if Mario is on a ladder, {@code false} otherwise.
     */
    private boolean handleLadders(Input input, Ladder[] ladders) {
        boolean isOnLadder = false;
        for (Ladder ladder : ladders) {
            double ladderLeft  = ladder.getX() - (ladder.getWidth() / 2);
            double ladderRight = ladder.getX() + (ladder.getWidth() / 2);
            double marioRight  = x + (marioImage.getWidth() / 2);
            double marioBottom = y + (marioImage.getHeight() / 2);
            double ladderTop    = ladder.getY() - (ladder.getHeight() / 2);
            double ladderBottom = ladder.getY() + (ladder.getHeight() / 2);

            if (isTouchingLadder(ladder)) {
                // Check horizontal overlap so Mario is truly on the ladder
                if (marioRight - marioImage.getWidth() / 2 > ladderLeft && marioRight - marioImage.getWidth() / 2 < ladderRight) {
                    isOnLadder = true;

                    // Stop Mario from sliding up when not moving**
                    if (!input.isDown(Keys.UP) && !input.isDown(Keys.DOWN)) {
                        velocityY = 0;  // Prevent sliding inertia effect
                    }

                    // ----------- Climb UP -----------
                    if (input.isDown(Keys.UP)) {
                        y -= CLIMB_SPEED;
                        velocityY = 0;
                    }

                    // ----------- Climb DOWN -----------
                    if (input.isDown(Keys.DOWN)) {
                        double nextY = y + CLIMB_SPEED;
                        double nextBottom = nextY + (marioImage.getHeight() / 2);

                        if (marioBottom > ladderTop && nextBottom <= ladderBottom) {
                            y = nextY;
                            velocityY = 0;
                        } else if (marioBottom == ladderBottom) {
                            velocityY = 0;
                        } else if (ladderBottom - marioBottom < CLIMB_SPEED) {
                            y = y + ladderBottom - marioBottom;
                            velocityY = 0;
                        }
                    }
                }
            } else if (marioBottom == ladderTop && input.isDown(Keys.DOWN) && (marioRight - marioImage.getWidth() / 2 > ladderLeft && marioRight - marioImage.getWidth() / 2  < ladderRight)) {
                double nextY = y + CLIMB_SPEED;
                y = nextY;
                velocityY = 0; // ignore gravity
            } else if (marioBottom == ladderBottom && input.isDown(Keys.DOWN) && (marioRight - marioImage.getWidth() / 2 > ladderLeft && marioRight - marioImage.getWidth() / 2  < ladderRight)) {
                velocityY = 0; // ignore gravity
            }
        }
        return isOnLadder;
    }

    /** Handles horizontal movement based on player input. */
    private void handleHorizontalMovement(Input input) {
        if (input.isDown(Keys.LEFT)) {
            x -= MOVE_SPEED;
            isFacingRight = false;
        } else if (input.isDown(Keys.RIGHT)) {
            x += MOVE_SPEED;
            isFacingRight = true;
        }
    }

    /** Handles collecting the hammer if Mario is in contact with it. */


    private void collectHammer(Hammer hammer) {
        if (!hammer.isCollected() && isTouchingHammer(hammer)) {
            hasHammer = true;

            if (hasBlaster) {
                hasBlaster = false;
                bulletCount = 0;
            }

            hammer.collect();
            updateSprite();
            System.out.println("Hammer collected!");
        }
    }

    private void collectBlaster(Blaster blaster) {
        if (!blaster.isCollected() && getBoundingBox().intersects(blaster.getBoundingBox())) {
            if (hasHammer) {
                hasHammer = false;
            }

            hasBlaster = true;
            pickUpBlaster(blaster.getAmmo());
            blaster.collect();
            updateSprite();
            System.out.println("Blaster collected! Bullets: " + bulletCount);
        }
    }






    /** Handles jumping if Mario is on a platform and jump is requested. */
    private void handleJumping(boolean onPlatform, boolean wantsToJump) {
        if (onPlatform && wantsToJump) {
            velocityY = JUMP_STRENGTH;
            isJumping = true;
            System.out.println("Jumping!");
        }
        double bottomOfMario = y + (marioImage.getHeight() / 2);
        if (bottomOfMario > ShadowDonkeyKong.getScreenHeight()) {
            y = ShadowDonkeyKong.getScreenHeight() - (marioImage.getHeight() / 2);
            velocityY = 0;
            isJumping = false;
        }
    }

    /**
     * Enforces screen boundaries to prevent Mario from moving out of bounds.
     * Ensures Mario stays within the left, right, and bottom limits of the game window.
     */
    private void enforceBoundaries() {
        // Calculate half the width of the Mario image (used for centering and boundary checks)
        double halfW = marioImage.getWidth() / 2;

        // Prevent Mario from moving beyond the left edge of the screen
        if (x < halfW) {
            x = halfW;
        }

        // Prevent Mario from moving beyond the right edge of the screen
        double maxX = ShadowDonkeyKong.getScreenWidth() - halfW;
        if (x > maxX) {
            x = maxX;
        }

        // Calculate Mario's bottom edge position
        double bottomOfMario = y + (marioImage.getHeight() / 2);

        // Prevent Mario from falling below the bottom of the screen
        if (bottomOfMario > ShadowDonkeyKong.getScreenHeight()) {
            // Reposition Mario to stand on the bottom edge
            y = ShadowDonkeyKong.getScreenHeight() - (marioImage.getHeight() / 2);

            // Stop vertical movement and reset jumping state
            velocityY = 0;
            isJumping = false;
        }
    }


    /**
     * Switch Mario's sprite (left/right, or hammer/no-hammer).
     * Adjust Mario's 'y' so that the bottom edge stays consistent.
     */
    private void updateSprite() {
        // 1) Remember the old image and its bottom
        Image oldImage = marioImage;
        double oldHeight = oldImage.getHeight();
        double oldBottom = y + (oldHeight / 2);

        // 2) Assign the new image based on facing & state
        if (hasHammer) {
            marioImage = isFacingRight ? MARIO_HAMMER_RIGHT_IMAGE : MARIO_HAMMER_LEFT_IMAGE;
        } else if (hasBlaster) {
            marioImage = isFacingRight ? MARIO_BLASTER_RIGHT_IMAGE : MARIO_BLASTER_LEFT_IMAGE;
        } else {
            marioImage = isFacingRight ? MARIO_RIGHT_IMAGE : MARIO_LEFT_IMAGE;
        }

        // 3) Preserve bottom position
        double newHeight = marioImage.getHeight();
        double newBottom = y + (newHeight / 2);
        y -= (newBottom - oldBottom);

        // 4) Update width/height
        width  = marioImage.getWidth();
        height = newHeight;
    }



    /**
     * Draws Mario on the screen.
     */
    public void draw() {
        marioImage.draw(x, y);
//    drawBoundingBox(); // Uncomment for debugging
    }


    /**
     * Checks if Mario is touching a ladder.
     *
     * @param ladder The ladder object to check collision with.
     * @return {@code true} if Mario is touching the ladder, {@code false} otherwise.
     */
    private boolean isTouchingLadder(Ladder ladder) {
        Rectangle marioBounds = getBoundingBox();
        return marioBounds.intersects(ladder.getBoundingBox());
    }

    /**
     * Checks if Mario is touching the hammer.
     *
     * @param hammer The hammer object to check collision with.
     * @return {@code true} if Mario is touching the hammer, {@code false} otherwise.
     */
    private boolean isTouchingHammer(Hammer hammer) {
        Rectangle marioBounds = getBoundingBox();
        return marioBounds.intersects(hammer.getBoundingBox());
    }

    /**
     * Checks if Mario is touching a barrel.
     *
     * @param barrel The barrel object to check collision with.
     * @return {@code true} if Mario is touching the barrel, {@code false} otherwise.
     */
    public boolean isTouchingBarrel(Barrel barrel) {
        Rectangle marioBounds = getBoundingBox();
        return marioBounds.intersects(barrel.getBoundingBox());
    }

    /**
     * Checks if Mario has reached Donkey Kong.
     *
     * @param donkey The Donkey object to check collision with.
     * @return {@code true} if Mario has reached Donkey Kong, {@code false} otherwise.
     */
    public boolean hasReached(Donkey donkey) {
        Rectangle marioBounds = getBoundingBox();
        return marioBounds.intersects(donkey.getBoundingBox());
    }

    /**
     * Determines if Mario successfully jumps over a barrel.
     *
     * @param barrel The barrel object to check.
     * @return {@code true} if Mario successfully jumps over the barrel, {@code false} otherwise.
     */
    public boolean jumpOver(Barrel barrel) {
        return isJumping
                && Math.abs(this.x - barrel.getX()) <= 1
                && (this.y < barrel.getY())
                && ((this.y + height / 2) >= (barrel.getY() + barrel.getBarrelImage().getHeight() / 2
                - (JUMP_STRENGTH * JUMP_STRENGTH) / (2 * Physics.MARIO_GRAVITY) - height / 2));
    }

    public void pickUpBlaster(int bulletsFromBlaster) {
        if (hasHammer) {
            hasHammer = false;
            bulletCount = 0; // 失去 blaster 后子弹归零
        }
        hasBlaster = true;
        bulletCount += bulletsFromBlaster;
        updateSprite(); // 更新图像
    }

    public void consumeBullet() {
        if (hasBlaster && bulletCount > 0) {
            bulletCount--;
            if (bulletCount == 0) {
                hasBlaster = false;
                updateSprite();
            }
        }
    }

    public boolean hasHammer() {
        return this.hasHammer;
    }

    public boolean hasBlaster() {
        return hasBlaster;
    }

    public int getBulletCount() {
        return bulletCount;
    }





}

