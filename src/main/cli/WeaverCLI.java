package main.cli;

import main.model.WeaverModel;
import main.model.LetterStatus;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class WeaverCLI {
    // ANSI color codes for terminal output
    private static final String ANSI_GREEN = "\u001B[32m"; // Green color
    private static final String ANSI_GRAY = "\u001B[37m";  // Gray color
    private static final String ANSI_RESET = "\u001B[0m"; // Reset color

    public static void main(String[] args) {
        WeaverModel model = new WeaverModel();
        Scanner scanner = new Scanner(System.in);

        try {
            // Load dictionary (This only needs to be done once at the start)
            model.loadDictionary("dictionary.txt");

            System.out.println("Welcome to Weaver CLI!");

            // Outer loop to control multiple games
            boolean playAgain = true; // Set to true initially to start the first game

            while (playAgain) {
                // --- Configure New Game (This block is now inside the outer loop) ---
                System.out.println("\n--- Configure Game ---"); // Added a heading for clarity

                // Ask about showing error messages
                boolean showError = askYesNo(scanner, "Show error messages for invalid input? (y/n): ");
                model.setShowErrorMessage(showError);

                // Ask about showing the solution path
                boolean showPath = askYesNo(scanner, "Show a possible solution path during the game? (y/n): ");
                model.setShowPath(showPath);

                // Ask about using random words
                boolean randomWords = askYesNo(scanner, "Use random start and target words? (y/n): ");
                model.setRandomWords(randomWords);

                // Start the new game with the just-configured settings
                model.newGame();

                System.out.println("\nGame started!");
                System.out.println("Transform one word into another by changing one letter at a time.");
                System.out.println("Each step must create a valid word.");

                // --- Inner loop for a single game until it's over (Stays inside the outer loop) ---
                while (!model.isGameOver()) {
                    System.out.println("\n--------------------");
                    System.out.println("Start word: " + model.getStartWord());
                    System.out.println("Target word: " + model.getEndWord());
                    System.out.println("--------------------");

                    // Display the user's input history with color feedback
                    System.out.println("Your attempts:");
                    if (model.getWordHistory().isEmpty()) {
                        System.out.println("(No attempts yet)");
                    } else {
                        for (String word : model.getWordHistory()) {
                            // Display history words with coloring based on matching the target word
                            displayWordWithColors(word, model.getGuessResult(word));
                        }
                    }

                    // Display the current word the user is working from
                    String currentWord = model.getCurrentWord();
                    System.out.println("Current position: " + currentWord); // Current word displayed as plain text

                    // If showPath is enabled, display a possible solution path
                    if (model.getShowPath()) {
                        System.out.println("\nPossible solution path:");
                        List<String> solutionPath = model.getSolutionPath();
                        if (solutionPath.isEmpty()) {
                            System.out.println("No solution path found from start to end.");
                        } else {
                            // Display the calculated full solution path without color feedback
                            for (String word : solutionPath) {
                                System.out.println(word);
                            }
                        }
                    }

                    // Get user's next word input
                    System.out.print("\nEnter next word (4 letters): ");
                    String input = scanner.nextLine().trim().toLowerCase();

                    // Basic input length validation
                    if (input.length() != 4) {
                        if (model.getShowErrorMessage()) {
                            System.out.println("Error: Input must be exactly 4 letters long.");
                        }
                        continue;
                    }

                    // Process user input
                    if (!model.processInput(input)) {
                        if (model.getShowErrorMessage()) {
                            System.out.println("Error: Invalid word. It must be in the dictionary and differ by only one letter from the current word.");
                        }
                        continue;
                    }
                } // End of inner while loop

                // Single game over - player wins
                System.out.println("\n--------------------");
                System.out.println("Congratulations! You've reached the target word!");
                System.out.println("Your final path:");
                for (String word : model.getWordHistory()) {
                    displayWordWithColors(word, model.getGuessResult(word));
                }
                System.out.println("--------------------");

                // Ask the user if they want to start a new game
                playAgain = askYesNo(scanner, "Play again? (y/n): ");

            } // End of the outer while loop

            // If the user chooses not to play again, print farewell message
            System.out.println("\nThanks for playing!");


        } catch (IOException e) {
            System.err.println("Error loading dictionary: " + e.getMessage());
            System.exit(1);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }
    /**
     * Helper method: Asks the user a yes/no question and returns a boolean result.
     * @param scanner The Scanner object for reading user input.
     * @param question The question string to ask the user.
     * @return True if the user enters 'y', false if the user enters 'n'.
     */
    private static boolean askYesNo(Scanner scanner, String question) {
        while (true) {
            System.out.print(question);
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y")) {
                return true;
            } else if (input.equals("n")) {
                return false;
            } else {
                System.out.println("Invalid input. Please enter 'y' or 'n'.");
            }
        }
    }


}