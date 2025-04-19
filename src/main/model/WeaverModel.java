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

    @Override
    public LetterStatus[] getGuessResult(String guess) {
        assert guess != null && guess.length() == 4 : "Guess must be non-null and 4 letters long";
        assert endWord != null && endWord.length() == 4 : "End word must be initialized and 4 letters for getGuessResult";

        LetterStatus[] result = new LetterStatus[4];
        for (int i = 0; i < 4; i++) {
            result[i] = guess.charAt(i) == endWord.charAt(i) ?
                    LetterStatus.CORRECT_POSITION :
                    LetterStatus.INCORRECT;
        }
        return result;
    }

    private boolean isValidWord(String word) {
        // Word parameter validity (non-null, 4 letters) should be ensured by caller (processInput)
        assert word != null && word.length() == 4 : "Word to validate must be non-null and 4 letters";
        assert dictionary != null : "Dictionary must be initialized for isValidWord check";
        assert getCurrentWord() != null && getCurrentWord().length() == 4 : "Current game word must be valid for isValidWord check";

        if (!dictionary.contains(word)) {
            // Error logging/display is handled by view/controller based on showErrorMessage flag
            return false;
        }

        String currentWordInternal = getCurrentWord(); // Use internal reference
        int differences = 0;
        for (int i = 0; i < 4; i++) {
            if (currentWordInternal.charAt(i) != word.charAt(i)) {
                differences++;
            }
        }

        if (differences != 1) {
            // Error logging/display is handled by view/controller
            return false;
        }
        return true;
    }

    // Setters and Getters for flags usually don't need complex assertions
    // unless they interact with other state in a complex way.

    @Override
    public void setShowErrorMessage(boolean show) {
        this.showErrorMessage = show;
        setChanged();
        notifyObservers();
    }

    @Override
    public boolean getShowErrorMessage() {
        return showErrorMessage;
    }

    @Override
    public void setShowPath(boolean show) {
        this.showPath = show;
        setChanged();
        notifyObservers();
    }

    @Override
    public boolean getShowPath() {
        return showPath;
    }

    @Override
    public void setRandomWords(boolean random) {
        this.randomWords = random;
        setChanged();
        notifyObservers();
    }

    @Override
    public boolean getRandomWords() {
        return randomWords;
    }

    @Override
    public List<String> getSolutionPath() {
        assert startWord != null && startWord.length() == 4 : "Start word must be initialized for getSolutionPath";
        assert endWord != null && endWord.length() == 4 : "End word must be initialized for getSolutionPath";
        assert dictionary != null && !dictionary.isEmpty() : "Dictionary must be loaded and non-empty for getSolutionPath";
        // It's possible startWord or endWord are not in dictionary if newGame had issues with fixed words and a bad dictionary.
        // findWordLadder handles this gracefully by returning emptyList.
        return findWordLadder(startWord, endWord);
    }

    private List<String> findWordLadder(String start, String end) {
        assert start != null : "Start word parameter for findWordLadder cannot be null";
        assert end != null : "End word parameter for findWordLadder cannot be null";
        // Length check and dictionary presence are handled by the method's logic,
        // as this method is used exploratively by newGame.
        assert dictionary != null : "Dictionary must be initialized for findWordLadder";


        // Basic validation (runtime checks, not assertions as this method can be called with words not in dictionary by newGame)
        if (start.length() != 4 || end.length() != 4 || !dictionary.contains(start) || !dictionary.contains(end)) {
            // If start or end has wrong length, or not in dictionary, no path.
            if (start.equals(end) && start.length() == 4 && dictionary.contains(start)) { // Path from a word to itself
                return Collections.singletonList(start);
            }
            return Collections.emptyList();
        }
        if (start.equals(end)) { // Path from a word to itself
            return Collections.singletonList(start);
        }


        Queue<String> queue = new LinkedList<>();
        Map<String, String> parentMap = new HashMap<>();
        Set<String> visited = new HashSet<>();

        queue.offer(start);
        visited.add(start);
        parentMap.put(start, null);

        while (!queue.isEmpty()) {
            String currentWord = queue.poll();

            if (currentWord.equals(end)) {
                List<String> path = new LinkedList<>();
                String step = end;
                while (step != null) {
                    path.add(0, step);
                    step = parentMap.get(step);
                }
                assert !path.isEmpty() : "Path reconstruction should not result in an empty list if end is found";
                assert path.get(0).equals(start) : "Reconstructed path must start with the start word";
                assert path.get(path.size() - 1).equals(end) : "Reconstructed path must end with the end word";
                return path;
            }

            List<String> neighbors = getNeighbors(currentWord);
            for (String neighbor : neighbors) {
                if (!visited.contains(neighbor) && dictionary.contains(neighbor)) { // Neighbor must be in dictionary
                    visited.add(neighbor);
                    parentMap.put(neighbor, currentWord);
                    queue.offer(neighbor);
                }
            }
        }
        return Collections.emptyList(); // No path found
    }

    private List<String> getNeighbors(String word) {
        assert word != null && word.length() == 4 : "Word parameter for getNeighbors must be non-null and 4 letters";
        assert dictionary != null : "Dictionary must be initialized for getNeighbors";

        List<String> neighbors = new ArrayList<>();
        char[] chars = word.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char originalChar = chars[i];
            for (char c = 'a'; c <= 'z'; c++) {
                if (c == originalChar) continue;
                chars[i] = c;
                String neighbor = new String(chars);
                if (dictionary.contains(neighbor)) {
                    neighbors.add(neighbor);
                }
            }
            chars[i] = originalChar;
        }
        return neighbors;
    }
}