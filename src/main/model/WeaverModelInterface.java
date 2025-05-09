package main.model;

/**
 * Interface for the Weaver game model
 */
public interface WeaverModelInterface {
    /**
     * Get the starting word
     * @return the starting word
     */
    String getStartWord();

    /**
     * Get the target word
     * @return the target word
     */
    String getEndWord();

    /**
     * Get the current word in play
     * @return the current word
     */
    String getCurrentWord();

    /**
     * Get the history of valid words entered
     * @return list of valid words entered
     */
    java.util.List<String> getWordHistory();

    /**
     * Process a word input from the player
     * @param word the word to process
     * @return true if the word is valid and accepted, false otherwise
     */
    boolean processInput(String word);

    /**
     * Check if the game is over (target word reached)
     * @return true if game is over, false otherwise
     */
    boolean isGameOver();

    /**
     * Reset the current game to initial state
     */
    void resetGame();

    /**
     * Start a new game with new words
     */
    void newGame();

    /**
     * Load the dictionary from file
     * @param filePath path to dictionary file
     * @throws java.io.IOException if file cannot be read
     */
    void loadDictionary(String filePath) throws java.io.IOException;

    /**
     * Get the letter status for each position in a guess
     * @param guess the word to check
     * @return array of letter statuses
     */
    LetterStatus[] getGuessResult(String guess);

    /**
     * Set whether to show error messages
     * @param show true to show errors, false to hide
     */
    void setShowErrorMessage(boolean show);

    /**
     * Get whether error messages are shown
     * @return true if errors are shown, false otherwise
     */
    boolean getShowErrorMessage();

    /**
     * Set whether to show solution path
     * @param show true to show path, false to hide
     */
    void setShowPath(boolean show);

    /**
     * Get whether solution path is shown
     * @return true if path is shown, false otherwise
     */
    boolean getShowPath();

    /**
     * Set whether to use random words
     * @param random true for random words, false for fixed
     */
    void setRandomWords(boolean random);

    /**
     * Get whether random words are used
     * @return true if random words are used, false otherwise
     */
    boolean getRandomWords();

    /**
     * Get the solution path from start to target word
     * @return list of words forming a solution path
     */
    java.util.List<String> getSolutionPath();
} 