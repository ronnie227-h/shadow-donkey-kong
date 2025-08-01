import bagel.*;
import java.util.Properties;

/**
 * Represents the screen displayed at the end of the game.
 * It shows whether the player won or lost, displays the final score,
 * and waits for the player to press SPACE to continue.
 */
public class GameEndScreen {
    private final Image BACKGROUND_IMAGE; // Background image for the end screen

    private final String GAME_WON_TXT;  // Message displayed when the player wins
    private final String GAME_LOST_TXT; // Message displayed when the player loses
    private final String CONTINUE_GAME_TXT;
    private final String SCORE_MESSAGE;

    // Fonts for displaying win/loss message and final score
    private final Font STATUS_FONT;
    private final Font SCORE_FONT;

    // Vertical position for the win/loss message
    private final int STATUS_Y;
    private final int MESSAGE_DIFF_Y_1 = 60;
    private final int MESSAGE_DIFF_Y_2 = 100;

    // Weight of the scores
    private final double TIME_WEIGHT = 3.0;


    // The final score from this playthrough
    private double finalScore = 0.0;

    // Indicates whether the player won or lost
    private boolean isWon;

    /**
     * Constructs the GameEndScreen, loading required resources such as images, fonts, and text.
     *
     * @param gameProps Properties file containing file paths and layout configurations.
     * @param msgProps  Properties file containing game messages and prompts.
     */
    public GameEndScreen(Properties gameProps, Properties msgProps, int finalScore, boolean win){
    // Load the background image and end-game messages from properties
        this.BACKGROUND_IMAGE = new Image(gameProps.getProperty("backgroundImage"));
        this.GAME_WON_TXT = msgProps.getProperty("gameEnd.won");
        this.GAME_LOST_TXT = msgProps.getProperty("gameEnd.lost");
        this.CONTINUE_GAME_TXT = msgProps.getProperty("gameEnd.continue");
        this.SCORE_MESSAGE = msgProps.getProperty("gameEnd.score");

        // Load the vertical position of the status text
        this.STATUS_Y = Integer.parseInt(gameProps.getProperty("gameEnd.status.y"));

        // Load fonts for status message and final score
        String fontFile = gameProps.getProperty("font");
        this.STATUS_FONT = new Font(fontFile,
                Integer.parseInt(gameProps.getProperty("gameEnd.status.fontSize")));
        this.SCORE_FONT = new Font(fontFile,
                Integer.parseInt(gameProps.getProperty("gameEnd.scores.fontSize")));

        this.isWon = win;
        this.finalScore = finalScore;
    }

    /**
     * Renders the game end screen, including the final score, win/loss message,
     * and a prompt for the player to continue. Also checks for user input to exit the screen.
     *
     * @param input The current user input.
     * @return {@code true} if the player presses SPACE to continue, {@code false} otherwise.
     */
    public boolean update(Input input) {
        // 1) Draw the background image
        BACKGROUND_IMAGE.drawFromTopLeft(0, 0);

        // 2) Display game outcome message ("Game Won" or "Game Lost")
        String statusText = isWon ? GAME_WON_TXT : GAME_LOST_TXT;
        STATUS_FONT.drawString(
                statusText,
                Window.getWidth() / 2.0 - STATUS_FONT.getWidth(statusText) / 2.0,
                STATUS_Y
        );

        // 3) Display the final score below the status message
        String finalScoreText = SCORE_MESSAGE + " " + (int) finalScore;
        double finalScoreX = Window.getWidth() / 2.0 - SCORE_FONT.getWidth(finalScoreText) / 2.0;
        double finalScoreY = STATUS_Y + MESSAGE_DIFF_Y_1;
        SCORE_FONT.drawString(finalScoreText, finalScoreX, finalScoreY);

        // 4) Display a prompt instructing the player to continue
        String promptText = CONTINUE_GAME_TXT;
        double promptX = Window.getWidth() / 2.0 - SCORE_FONT.getWidth(promptText) / 2.0;
        double promptY = Window.getHeight() - MESSAGE_DIFF_Y_2; // Positioned near the bottom
        SCORE_FONT.drawString(promptText, promptX, promptY);

        // 5) Check if the player presses SPACE to exit the end screen
        if (input.wasPressed(Keys.SPACE)) {
            return true;
        }

        // 6) Otherwise, remain on the game end screen
        return false;
    }
}
