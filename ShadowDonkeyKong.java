import bagel.*;
import java.util.Properties;

/**
 * The main class for the Shadow Donkey Kong game.
 * This class extends {@code AbstractGame} and is responsible for managing game initialization,
 * updates, rendering, and handling user input.
 * It sets up the game world, initializes characters, platforms, ladders, and other game objects,
 * and runs the game loop to ensure smooth gameplay.
 */
public class ShadowDonkeyKong extends AbstractGame {

    private final Properties gameProps;
    private final Properties messageProps;
    private GameEndScreen gameEndScreen;

    public static double screenWidth;

    public static double screenHeight;

    private enum GameState { HOME, LEVEL1, LEVEL2, END }
    private GameState state = GameState.HOME;

    private HomeScreen home;
    private GamePlayScreen level1;
    private GamePlayScreen level2;
    private GameEndScreen endScreen;



    /**
     * Constructs a new instance of the ShadowDonkeyKong game.
     * Initializes the game window using provided properties and sets up the home screen.
     *
     * @param gameProps     A {@link Properties} object containing game configuration settings
     *                      such as window width and height.
     * @param messageProps  A {@link Properties} object containing localized messages or UI labels,
     *                      including the title for the home screen.
     */
    public ShadowDonkeyKong(Properties gameProps, Properties messageProps) {
        super(Integer.parseInt(gameProps.getProperty("window.width")),
                Integer.parseInt(gameProps.getProperty("window.height")),
                messageProps.getProperty("home.title"));

        this.gameProps = gameProps;
        this.messageProps = messageProps;

        screenWidth = Integer.parseInt(gameProps.getProperty("window.width"));
        screenHeight = Integer.parseInt(gameProps.getProperty("window.height"));

        home= new HomeScreen(gameProps, messageProps);
    }


    /**
     * Render the relevant screen based on the keyboard input given by the user and the status of the gameplay.
     * @param input The current mouse/keyboard input.
     */


    @Override
    public void update(Input input) {
        switch (state) {
            case HOME -> {
                // Wait for user to select level
                Integer levelChoice = home.update(input);
                if (levelChoice != null) {
                    if (levelChoice == 1) {
                        level1 = new GamePlayScreen(gameProps, 1,0);
                        state = GameState.LEVEL1;
                    } else if (levelChoice == 2) {
                        level2 = new GamePlayScreen(gameProps, 2, 0);
                        state = GameState.LEVEL2;
                    }
                }
            }

            case LEVEL1 -> {
                // Update level 1 gameplay
                boolean levelEnded = level1.update(input);

                if (levelEnded) {
                    if (level1.hasWon()) {
                        // Win: proceed to level 2
                        // Assumption: The score from Level 1 carries over to Level 2.
                        // This is required for consistent total scoring across both levels.
                        level2 = new GamePlayScreen(gameProps, 2, level1.getScore());
                        state = GameState.LEVEL2;
                    } else {
                        // Lose: show fail screen
                        endScreen = new GameEndScreen(gameProps, messageProps, level1.getFinalScore(), false);
                        state = GameState.END;
                    }
                }
            }

            case LEVEL2 -> {
                // Update level 2 gameplay
                boolean levelEnded = level2.update(input);

                if (levelEnded) {
                    boolean playerWon = level2.hasWon();
                    endScreen = new GameEndScreen(gameProps, messageProps, level2.getFinalScore(), playerWon);
                    state = GameState.END;
                }
            }

            case END -> {
                // Wait for SPACE to return to home screen
                if (endScreen.update(input)) {
                    home = new HomeScreen(gameProps, messageProps);
                    state = GameState.HOME;
                }
            }
        }
    }



    /**
     * Retrieves the width of the game screen.
     *
     * @return The width of the screen in pixels.
     */
    public static double getScreenWidth() {
        return screenWidth;
    }

    /**
     * Retrieves the height of the game screen.
     *
     * @return The height of the screen in pixels.
     */
    public static double getScreenHeight() {
        return screenHeight;
    }

    /**
     * The main entry point of the Shadow Donkey Kong game.
     * This method loads the game properties and message files, initializes the game,
     * and starts the game loop.
     *
     * @param args Command-line arguments (not used in this game).
     */
    public static void main(String[] args) {
        Properties gameProps = IOUtils.readPropertiesFile("res/app.properties");
        Properties messageProps = IOUtils.readPropertiesFile("res/message.properties");
        ShadowDonkeyKong game = new ShadowDonkeyKong(gameProps, messageProps);
        game.run();

    }


}
