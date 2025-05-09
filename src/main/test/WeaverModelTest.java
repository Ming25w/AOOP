package main.test;

import main.model.WeaverModel;
import main.model.LetterStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.After; // Import After
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List; // Import List

public class WeaverModelTest {
    private WeaverModel model;
    private Path tempDictionaryFile;

    @Before
    public void setUp() throws IOException {
        model = new WeaverModel();
        // Create a temporary dictionary file with a valid path from love to hate
        tempDictionaryFile = Files.createTempFile("test_dictionary", ".txt");
        //  love -> live -> hive -> have -> hate
        Files.write(tempDictionaryFile, Arrays.asList("love", "live", "hive", "have", "hate"));
        model.loadDictionary(tempDictionaryFile.toString());
        model.setRandomWords(false); // Ensure fixed words are used for this test setup
        model.newGame(); // This will set up "love" and "hate" as default words
    }

    /**
     * Clean up the temporary dictionary file after each test.
     * @throws IOException if an I/O error occurs
     */
    @After // Add cleanup method
    public void tearDown() throws IOException {
        Files.deleteIfExists(tempDictionaryFile);
    }

    /**
     * Test Scenario 1: Valid word transition
     * Precondition: Game is initialized with start word "love"
     * Input: "live" (valid word, one letter different)
     * Expected: Word is accepted, added to history
     */
    @Test
    public void testValidWordTransition() {
        assertTrue("Valid word should be accepted", model.processInput("live"));
        assertEquals("Word should be added to history", "live", model.getCurrentWord());
        // Optionally check history size and content
        List<String> history = model.getWordHistory();
        assertEquals("History should contain one word", 1, history.size());
        assertEquals("The word in history should be 'live'", "live", history.get(0));
    }

    /**
     * Test Scenario 2: Invalid word - more than one letter different
     * Precondition: Game is initialized with start word "love"
     * Input: "hate" (invalid transition - multiple letters different)
     * Expected: Word is rejected, history unchanged
     */
    @Test
    public void testInvalidWordTransition() {
        assertFalse("Word with multiple letter differences should be rejected",
                model.processInput("hate"));
        assertEquals("Current word should remain unchanged", "love",
                model.getCurrentWord());
        // Ensure history is still empty
        assertTrue("History should be empty for invalid input", model.getWordHistory().isEmpty());
    }

    /**
     * Test Scenario 3: Invalid word - not in dictionary
     * Precondition: Game is initialized with start word "love"
     * Input: "test" (not in the test dictionary)
     * Expected: Word is rejected, history unchanged
     */
    @Test
    public void testInvalidWordNotInDictionary() {
        assertFalse("Word not in dictionary should be rejected",
                model.processInput("test"));
        assertEquals("Current word should remain unchanged", "love",
                model.getCurrentWord());
        assertTrue("History should be empty for invalid input", model.getWordHistory().isEmpty());
    }


    /**
     * Test Scenario 4: Game completion (Modified to use a valid path)
     * Precondition: Game is initialized with start word "love", target word "hate",
     * and a valid path exists in the dictionary.
     * Input: Sequence of valid words leading to the target ("live", "hive", "have", "hate")
     * Expected: Game recognizes completion after the final valid word is entered.
     */
    @Test
    public void testGameCompletion() {
        assertFalse("Game should not be over at start", model.isGameOver());

        // Follow a valid path from "love" to "hate" using the test dictionary words
        assertTrue("Processing 'live' should be valid", model.processInput("live")); // love -> live
        assertEquals("Current word should be 'live'", "live", model.getCurrentWord());
        assertFalse("Game should not be over after 'live'", model.isGameOver());
        assertEquals("History size after 'live'", 1, model.getWordHistory().size());

        assertTrue("Processing 'hive' should be valid", model.processInput("hive")); // live -> hive
        assertEquals("Current word should be 'hive'", "hive", model.getCurrentWord());
        assertFalse("Game should not be over after 'hive'", model.isGameOver());
        assertEquals("History size after 'hive'", 2, model.getWordHistory().size());

        assertTrue("Processing 'have' should be valid", model.processInput("have")); // hive -> have
        assertEquals("Current word should be 'have'", "have", model.getCurrentWord());
        assertFalse("Game should not be over after 'have'", model.isGameOver());
        assertEquals("History size after 'have'", 3, model.getWordHistory().size());

        // Entering the target word should make the game over
        assertTrue("Processing 'hate' should be valid and complete the game", model.processInput("hate")); // have -> hate
        assertEquals("Current word should be the target 'hate'", "hate", model.getCurrentWord());

        // The game should be over now because the last processed word was the target
        assertTrue("Game should be over after reaching target word 'hate'",
                model.isGameOver());
        assertEquals("History size at game completion", 4, model.getWordHistory().size());
    }

    /**
     * Test finding a solution path for fixed words.
     * Precondition: Game is initialized with start word "love", target word "hate",
     * and a valid path exists in the dictionary.
     * Expected: getSolutionPath returns a non-empty list representing a valid path.
     */
    @Test
    public void testGetSolutionPathFixedWords() {
        model.setRandomWords(false); // Ensure fixed words are used
        model.newGame(); // Re-initialize to ensure settings are applied

        List<String> solutionPath = model.getSolutionPath();

        assertNotNull("Solution path should not be null", solutionPath);
        assertFalse("Solution path should not be empty", solutionPath.isEmpty());

        // Optional: Verify the path content (example check)
        assertEquals("Solution path should start with the start word", "love", solutionPath.get(0));
        assertEquals("Solution path should end with the target word", "hate", solutionPath.get(solutionPath.size() - 1));
        // You could add more checks here to verify the steps are valid
        // and that the path length is reasonable if needed.
    }

    /**
     * Test finding a solution path for random words (requires a dictionary where paths exist).
     * Precondition: Dictionary loaded, randomWords set to true.
     * Expected: newGame selects random words with a path, and getSolutionPath returns a path.
     */
    @Test
    public void testGetSolutionPathRandomWords() {
        // This test is more complex as it depends on random word selection.
        // We rely on the model's newGame logic to select words with a path if randomWords is true.

        model.setRandomWords(true);
        model.newGame(); // newGame should select random words with a path

        String randomStart = model.getStartWord();
        String randomEnd = model.getEndWord();

        // Skip test if random words somehow ended up being the same (shouldn't happen with current newGame logic)
        if (randomStart.equals(randomEnd)) {
            System.out.println("Skipping testGetSolutionPathRandomWords: Start and end words are the same.");
            return;
        }

        List<String> solutionPath = model.getSolutionPath();

        assertNotNull("Solution path should not be null for random words", solutionPath);
        assertFalse("Solution path should not be empty for random words with a path", solutionPath.isEmpty());

        assertEquals("Solution path should start with the random start word", randomStart, solutionPath.get(0));
        assertEquals("Solution path should end with the random target word", randomEnd, solutionPath.get(solutionPath.size() - 1));

        // Optional: More rigorous checks could involve validating each step in the found path.
    }
}