package main.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the Weaver game model
 */
public class WeaverModel extends Observable implements WeaverModelInterface {
    private Set<String> dictionary;
    private String startWord;
    private String endWord;
    private List<String> wordHistory;
    private boolean showErrorMessage;
    private boolean showPath;
    private boolean randomWords;
    private Random random;

    /**
     * Constructor initializes the model with default values
     */
    public WeaverModel() {
        this.dictionary = new HashSet<>();
        this.wordHistory = new ArrayList<>();
        this.random = new Random();
        this.showErrorMessage = true; // Default value
        this.showPath = false;      // Default value
        this.randomWords = false;     // Default value
        // Note: startWord and endWord are null until newGame() is called
    }

    @Override
    public String getStartWord() {
        assert startWord != null : "Start word should have been initialized by newGame()";
        return startWord;
    }

    @Override
    public String getEndWord() {
        assert endWord != null : "End word should have been initialized by newGame()";
        return endWord;
    }

    @Override
    public String getCurrentWord() {
        if (wordHistory.isEmpty()) {
            assert startWord != null : "Start word must be initialized to get current word (if history is empty)";
            return startWord;
        }
        return wordHistory.get(wordHistory.size() - 1);
    }

    @Override
    public List<String> getWordHistory() {
        return new ArrayList<>(wordHistory); // Return a copy
    }

    @Override
    public boolean processInput(String word) {
        assert word != null && word.length() == 4 : "Input word must be non-null and 4 letters long";
        assert dictionary != null && !dictionary.isEmpty() : "Dictionary must be loaded and non-empty";
        assert startWord != null && startWord.length() == 4 : "Start word must be initialized and 4 letters";
        assert endWord != null && endWord.length() == 4 : "End word must be initialized and 4 letters";
        assert !isGameOver() : "Cannot process input if game is already over";

        word = word.toLowerCase();

        if (!isValidWord(word)) {
            // Error display should be handled by controller/view based on showErrorMessage
            return false;
        }

        wordHistory.add(word);
        setChanged();
        notifyObservers();
        return true;
    }

    @Override
    public boolean isGameOver() {
        // No specific assertions needed here beyond what getCurrentWord and getEndWord ensure if called
        return !wordHistory.isEmpty() && getCurrentWord().equals(getEndWord());
    }

    @Override
    public void resetGame() {
        assert startWord != null : "Start word should be initialized before resetting game";
        assert endWord != null : "End word should be initialized before resetting game";
        wordHistory.clear();
        setChanged();
        notifyObservers();
    }

    @Override
    public void newGame() {
        assert dictionary != null && !dictionary.isEmpty() : "Dictionary must be loaded and non-empty before starting a new game";
        wordHistory.clear();

        if (randomWords) {
            List<String> words = new ArrayList<>(dictionary);
            assert !words.isEmpty() : "Cannot pick random words from an effectively empty dictionary set";
            int dictionarySize = words.size();
            assert dictionarySize > 0 : "Dictionary word list for random selection cannot be empty";
            if (dictionarySize < 2 && !words.get(0).isEmpty() ) { // Need at least 2 distinct words for a game generally
                System.err.println("Warning: Dictionary has fewer than 2 words, random game might not be meaningful.");
                // If only one word, make start and end the same, which findWordLadder handles by returning a list with one element.
                startWord = words.get(0);
                endWord = words.get(0);
            } else if (dictionarySize == 0) {
                // This case should ideally be caught by dictionary load checks or earlier assertions.
                // Fallback or throw an error. For now, let's assume this won't happen due to prior assertions.
                // Setting placeholder to satisfy not-null assertions later, though game is unplayable.
                System.err.println("Critical Error: Dictionary is empty for random word selection in newGame.");
                startWord = "err "; // Invalid length to signal issue
                endWord = "word";
            }
            else { // dictionarySize >= 2
                do {
                    startWord = words.get(random.nextInt(dictionarySize));
                    do {
                        endWord = words.get(random.nextInt(dictionarySize));
                    } while (endWord.equals(startWord)); // Ensure start and end are different
                } while (findWordLadder(startWord, endWord).isEmpty()); // Ensure a path exists
            }
            assert dictionary.contains(startWord) : "Randomly selected startWord must be in dictionary";
            assert dictionary.contains(endWord) : "Randomly selected endWord must be in dictionary";

        } else { // Fixed words
            startWord = "love";
            endWord = "hate";
            // These assertions depend on the specific dictionary file used for fixed mode.
            // If a different dictionary is loaded that doesn't contain "love" or "hate", these will fail.
            assert dictionary.contains(startWord) : "Fixed startWord 'love' must be in dictionary for a valid game";
            assert dictionary.contains(endWord) : "Fixed endWord 'hate' must be in dictionary for a valid game";

            if (findWordLadder(startWord, endWord).isEmpty()) {
                System.err.println("Warning: No path found for fixed words 'love' to 'hate'. Game might be unplayable with current dictionary.");
            }
        }

        assert startWord != null && startWord.length() == 4 : "Start word must be set and be 4 letters after newGame logic";
        assert endWord != null && endWord.length() == 4 : "End word must be set and be 4 letters after newGame logic";
        // If dictionarySize < 2, startWord can be equal to endWord.
        if (dictionary == null || dictionary.size() >=2) {
            assert !startWord.equals(endWord) : "Start and end words must be different after newGame (unless dictionary is too small)";
        }


        setChanged();
        notifyObservers();
    }

    @Override
    public void loadDictionary(String filePath) throws IOException {
        assert filePath != null && !filePath.isEmpty() : "File path cannot be null or empty for loading dictionary";
        dictionary = Files.lines(Paths.get(filePath))
                .map(String::toLowerCase)
                .filter(word -> word.length() == 4)
                .collect(Collectors.toSet());

        if (dictionary.isEmpty()) {
            // This exception is good, assertion not strictly needed before it,
            // but can be added for internal consistency if desired before the throw.
            // assert !dictionary.isEmpty() : "Dictionary should not be empty after loading if file was valid and contained 4-letter words";
            throw new IOException("No 4-letter words found in dictionary, or dictionary file is empty/invalid.");
        }
        assert !dictionary.isEmpty() : "Dictionary must not be empty after successful loading and filtering.";
    }









}