package main.view;

import main.model.WeaverModelInterface;
import main.model.LetterStatus;
import main.controller.WeaverController;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Graphical User Interface (GUI) view for the Weaver game.
 * Displays the game state, handles user input via keyboard and virtual keyboard,
 * and observes the model for changes.
 */
public class WeaverGUIView extends JPanel implements Observer {

    private WeaverModelInterface model;
    private WeaverController controller;
    private JPanel wordGridPanel; // Panel in the center to display user history
    private JScrollPane wordGridScrollPane; // Scroll pane for the word grid

    private JPanel keyboardPanel; // Panel for the virtual keyboard
    private JButton resetButton;
    private JButton newGameButton;
    private JCheckBox showErrorMessageBox;
    private JCheckBox showPathBox; // Checkbox for the showPath feature
    private JCheckBox randomWordsBox;
    private StringBuilder currentInput; // Stores the user's current input word

    private JPanel startWordTiledPanel;
    private JPanel endWordTiledPanel;
    private JPanel gameAreaPanel; // Panel to hold the game area components (Start, History, End)

    private JPanel solutionPathPanel; // Panel to display the calculated solution path
    private JScrollPane solutionPathScrollPane; // Scroll pane for the solution path

    private static final int MAX_WORD_LENGTH = 4; // Fixed word length
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245); // Light gray background
    private static final Color CORRECT_COLOR = new Color(106, 170, 100); // Green for correct position
    private static final Color KEYBOARD_COLOR = new Color(211, 214, 218); // Light grey for keyboard keys and start/end words background
    private static final Color PATH_COLOR = new Color(173, 216, 230); // Light blue for path words background
    private static final Font LETTER_FONT = new Font("Arial", Font.BOLD, 24); // Font for letters in word tiles
    private static final Font KEYBOARD_FONT = new Font("Arial", Font.BOLD, 16); // Font for keyboard keys
    private static final int GRID_GAP = 5; // Gap between word rows in the grid

    /**
     * Constructor initializes the GUI view.
     * @param model The game model.
     * @param controller The game controller.
     */
    public WeaverGUIView(WeaverModelInterface model, WeaverController controller) {
        this.model = model;
        this.controller = controller;
        this.currentInput = new StringBuilder();
        initializeUI();
    }

    /**
     * Initializes and arranges the GUI components.
     */
    private void initializeUI() {
        // Use BorderLayout for the main panel to separate keyboard from game area
        setLayout(new BorderLayout(0, 10)); // Main panel layout (gap between center and south)
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(BACKGROUND_COLOR);

        // Panel for all content except the keyboard (will go in BorderLayout.CENTER)
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.setBackground(BACKGROUND_COLOR);

        // Top panel for controls + options
        JPanel topControlsPanel = new JPanel();
        topControlsPanel.setLayout(new BoxLayout(topControlsPanel, BoxLayout.Y_AXIS));
        topControlsPanel.setBackground(BACKGROUND_COLOR);
        topControlsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonOptionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonOptionsPanel.setBackground(BACKGROUND_COLOR);
        resetButton = new JButton("Reset Game");
        newGameButton = new JButton("New Game");
        resetButton.setEnabled(false);
        resetButton.addActionListener(e -> controller.handleReset());
        newGameButton.addActionListener(e -> controller.handleNewGame());
        buttonOptionsPanel.add(resetButton);
        buttonOptionsPanel.add(newGameButton);

        JPanel checkboxesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        checkboxesPanel.setBackground(BACKGROUND_COLOR);
        showErrorMessageBox = new JCheckBox("Show Errors");
        showPathBox = new JCheckBox("Show Path");
        randomWordsBox = new JCheckBox("Random Words");
        showErrorMessageBox.setSelected(model.getShowErrorMessage());
        showPathBox.setSelected(model.getShowPath());
        randomWordsBox.setSelected(model.getRandomWords());
        showErrorMessageBox.addActionListener(e -> controller.handleToggleShowError(showErrorMessageBox.isSelected()));
        showPathBox.addActionListener(e -> controller.handleToggleShowPath(showPathBox.isSelected()));
        randomWordsBox.addActionListener(e -> controller.handleToggleRandomWords(randomWordsBox.isSelected()));
        checkboxesPanel.add(showErrorMessageBox);
        checkboxesPanel.add(showPathBox);
        checkboxesPanel.add(randomWordsBox);

        JLabel instructionLabel = new JLabel("Type or use the keyboard to transform the start word into the target word.");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        topControlsPanel.add(buttonOptionsPanel);
        topControlsPanel.add(checkboxesPanel);
        topControlsPanel.add(instructionLabel);

        // Panel to display the solution path (wrapped in a scroll pane)
        solutionPathPanel = new JPanel();
        solutionPathPanel.setLayout(new BoxLayout(solutionPathPanel, BoxLayout.Y_AXIS));
        solutionPathPanel.setBackground(BACKGROUND_COLOR);
        // solutionPathPanel.setAlignmentX(Component.CENTER_ALIGNMENT); // Alignment handled by scrollpane viewport if needed

        solutionPathScrollPane = new JScrollPane(solutionPathPanel);
        solutionPathScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        solutionPathScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        solutionPathScrollPane.setBackground(BACKGROUND_COLOR); // Match background
        solutionPathScrollPane.getViewport().setBackground(BACKGROUND_COLOR); // Ensure viewport matches
        solutionPathScrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove default scrollpane border
        solutionPathScrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        solutionPathScrollPane.setVisible(false); // Initially hidden
        // Set a preferred size for the solution path area to control its height
        solutionPathScrollPane.setPreferredSize(new Dimension(250, 180)); // Adjust width/height as needed
        solutionPathScrollPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 150)); // Prevent it from getting too tall

        // Game area panel (holds start word, user history grid, end word)
        gameAreaPanel = new JPanel();
        gameAreaPanel.setLayout(new BoxLayout(gameAreaPanel, BoxLayout.Y_AXIS));
        gameAreaPanel.setBackground(BACKGROUND_COLOR);
        gameAreaPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        startWordTiledPanel = createTiledWordPanel(model.getStartWord(), KEYBOARD_COLOR);
        gameAreaPanel.add(startWordTiledPanel);
        gameAreaPanel.add(Box.createRigidArea(new Dimension(0, GRID_GAP)));

        // Word grid panel (for user history, wrapped in a scroll pane)
        wordGridPanel = new JPanel();
        wordGridPanel.setLayout(new BoxLayout(wordGridPanel, BoxLayout.Y_AXIS));
        wordGridPanel.setBackground(BACKGROUND_COLOR);

        wordGridScrollPane = new JScrollPane(wordGridPanel);
        wordGridScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        wordGridScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        wordGridScrollPane.setBackground(BACKGROUND_COLOR);
        wordGridScrollPane.getViewport().setBackground(BACKGROUND_COLOR);
        wordGridScrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove default scrollpane border
        // Set a preferred size for the word grid area to control its height before scrolling
        wordGridScrollPane.setPreferredSize(new Dimension(350, 250)); // Adjust width/height as needed
        wordGridScrollPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 300)); // Prevent it from getting too tall

        gameAreaPanel.add(wordGridScrollPane); // Add scroll pane to gameAreaPanel
        gameAreaPanel.add(Box.createRigidArea(new Dimension(0, GRID_GAP)));

        endWordTiledPanel = createTiledWordPanel(model.getEndWord(), KEYBOARD_COLOR);
        gameAreaPanel.add(endWordTiledPanel);
        // gameAreaPanel.add(Box.createVerticalGlue()); // Pushes elements up if gameAreaPanel is taller than preferred content

        // Keyboard panel - will go in BorderLayout.SOUTH
        keyboardPanel = createKeyboardPanel();
        keyboardPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components to mainContentPanel (for BorderLayout.CENTER)
        mainContentPanel.add(topControlsPanel);
        mainContentPanel.add(Box.createRigidArea(new Dimension(0, GRID_GAP)));
        mainContentPanel.add(solutionPathScrollPane); // Add scroll pane for solution path
        mainContentPanel.add(Box.createRigidArea(new Dimension(0, GRID_GAP)));
        mainContentPanel.add(gameAreaPanel);
        mainContentPanel.add(Box.createVerticalGlue()); // Pushes game content up if space available in center panel


        // Add main content and keyboard to the main panel (this)
        add(mainContentPanel, BorderLayout.CENTER);
        add(keyboardPanel, BorderLayout.SOUTH);

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = Character.toUpperCase(e.getKeyChar());
                if (Character.isLetter(c)) {
                    controller.handleCharacterInput(c);
                } else if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    controller.handleEnter();
                } else if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
                    controller.handleBackspace();
                }
            }
        });
    }

    private JPanel createTiledWordPanel(String word, Color bgColor) {
        JPanel wordPanel = new JPanel(new GridLayout(1, MAX_WORD_LENGTH, 2, 2));
        wordPanel.setBackground(BACKGROUND_COLOR); // Panel background
        // wordPanel.setMaximumSize(new Dimension(MAX_WORD_LENGTH * 62 + 10, 62 + 10)); // Constrain size slightly
        wordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);


        if (word == null) {
            word = "";
        }
        for (int i = 0; i < MAX_WORD_LENGTH; i++) {
            JLabel letterLabel = new JLabel(
                    i < word.length() ? String.valueOf(word.charAt(i)) : " ",
                    SwingConstants.CENTER);
            letterLabel.setOpaque(true);
            letterLabel.setFont(LETTER_FONT);
            letterLabel.setBackground(bgColor);
            letterLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            letterLabel.setPreferredSize(new Dimension(60, 60));
            wordPanel.add(letterLabel);
        }
        // To ensure the panel doesn't stretch too much in a BoxLayout when few items
        // but respects the preferred size of its contents.
        // We set a maximum size that's slightly larger than the preferred size.
        int prefWidth = MAX_WORD_LENGTH * (60 + 2) + (MAX_WORD_LENGTH -1) * 2; // letters + gaps between letters
        int prefHeight = 60 + 2; // letter height + border
        wordPanel.setPreferredSize(new Dimension(prefWidth, prefHeight));
        wordPanel.setMaximumSize(new Dimension(prefWidth + 20, prefHeight + 10)); // Allow some flex

        return wordPanel;
    }

    private JPanel createPathWordPanel(String word) {
        JPanel wordPanel = new JPanel(new GridLayout(1, MAX_WORD_LENGTH, 2, 2));
        wordPanel.setBackground(BACKGROUND_COLOR);
        wordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);


        if (word == null) {
            word = "";
        }
        for (int i = 0; i < MAX_WORD_LENGTH; i++) {
            JLabel letterLabel = new JLabel(
                    i < word.length() ? String.valueOf(word.charAt(i)) : " ",
                    SwingConstants.CENTER);
            letterLabel.setOpaque(true);
            letterLabel.setFont(LETTER_FONT);
            letterLabel.setBackground(PATH_COLOR);
            letterLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            letterLabel.setPreferredSize(new Dimension(40, 40)); // Smaller for path
            wordPanel.add(letterLabel);
        }
        int prefWidth = MAX_WORD_LENGTH * (40 + 2) + (MAX_WORD_LENGTH -1) * 2;
        int prefHeight = 40 + 2;
        wordPanel.setPreferredSize(new Dimension(prefWidth, prefHeight));
        wordPanel.setMaximumSize(new Dimension(prefWidth + 20, prefHeight + 10));
        return wordPanel;
    }

    private JPanel createKeyboardPanel() {
        JPanel mainKeyboardPanel = new JPanel();
        mainKeyboardPanel.setLayout(new BoxLayout(mainKeyboardPanel, BoxLayout.Y_AXIS));
        mainKeyboardPanel.setBackground(BACKGROUND_COLOR);
        mainKeyboardPanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0)); // Some top padding

        JPanel topTwoRows = new JPanel(new GridLayout(3, 10, 4, 4));
        topTwoRows.setBackground(BACKGROUND_COLOR);
        String[] topRowsChars = {"QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM"};
        for (String row : topRowsChars) {
            for (char c : row.toCharArray()) {
                JButton key = new JButton(String.valueOf(c));
                key.setBackground(KEYBOARD_COLOR);
                key.setForeground(Color.BLACK);
                key.setFont(KEYBOARD_FONT);
                key.setPreferredSize(new Dimension(40, 40));
                key.setMargin(new Insets(2,2,2,2)); // Reduce margin for smaller look if needed
                key.addActionListener(e -> controller.handleCharacterInput(c));
                topTwoRows.add(key);
            }
        }

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        bottomRow.setBackground(BACKGROUND_COLOR);
        JButton enterKey = new JButton("Enter");
        enterKey.addActionListener(e -> controller.handleEnter());
        enterKey.setBackground(KEYBOARD_COLOR);
        enterKey.setForeground(Color.BLACK);
        enterKey.setFont(KEYBOARD_FONT);
        enterKey.setPreferredSize(new Dimension(90, 40));
        bottomRow.add(enterKey);

        JButton backspaceKey = new JButton("Delete");
        backspaceKey.addActionListener(e -> controller.handleBackspace());
        backspaceKey.setBackground(KEYBOARD_COLOR);
        backspaceKey.setForeground(Color.BLACK);
        backspaceKey.setFont(KEYBOARD_FONT);
        backspaceKey.setPreferredSize(new Dimension(90, 40));
        bottomRow.add(backspaceKey);

        mainKeyboardPanel.add(topTwoRows);
        mainKeyboardPanel.add(bottomRow);

        // Let keyboard determine its own size based on components
        // The user's previous fixed size:
        // mainKeyboardPanel.setPreferredSize(new Dimension(800, 250));
        // mainKeyboardPanel.setMinimumSize(new Dimension(800, 200));
        // It's generally better to let the layout manager and component preferred sizes dictate this,
        // especially when in BorderLayout.SOUTH. The preferred height will be respected.
        // If you still want a fixed size, you can uncomment the lines above.
        // For now, we'll let it compute its preferred size.
        // To ensure it doesn't get too small or too large if the window is resized drastically:
        Dimension preferredKeyboardSize = mainKeyboardPanel.getPreferredSize();
        mainKeyboardPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, preferredKeyboardSize.height + 20));


        return mainKeyboardPanel;
    }

    private void updateWordGrid() {
        wordGridPanel.removeAll();
        List<String> userHistory = model.getWordHistory();
        for (String word : userHistory) {
            addWordToGrid(word, true);
        }
        if (currentInput.length() > 0) {
            addWordToGrid(currentInput.toString(), false);
        }
        wordGridPanel.revalidate();
        wordGridPanel.repaint();
        // wordGridScrollPane.revalidate(); // Usually not needed if panel inside is revalidated
        // wordGridScrollPane.repaint();
    }

    private void addWordToGrid(String word, boolean isHistory) {
        JPanel wordPanel = new JPanel(new GridLayout(1, MAX_WORD_LENGTH, 2, 2));
        wordPanel.setBackground(BACKGROUND_COLOR);
        wordPanel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center word rows in BoxLayout

        LetterStatus[] statuses = isHistory ? model.getGuessResult(word) : null;
        if (word == null) {
            word = "";
        }
        for (int i = 0; i < MAX_WORD_LENGTH; i++) {
            JLabel letterLabel = new JLabel(
                    i < word.length() ? String.valueOf(word.charAt(i)) : " ",
                    SwingConstants.CENTER);
            letterLabel.setOpaque(true);
            letterLabel.setFont(LETTER_FONT);
            letterLabel.setPreferredSize(new Dimension(60, 60));
            if (isHistory) {
                if (statuses != null && statuses[i] == LetterStatus.CORRECT_POSITION) {
                    letterLabel.setBackground(CORRECT_COLOR);
                } else {
                    letterLabel.setBackground(Color.LIGHT_GRAY);
                }
            } else {
                letterLabel.setBackground(Color.WHITE);
            }
            letterLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            wordPanel.add(letterLabel);
        }
        // Ensure individual word rows don't stretch excessively
        int prefWidth = MAX_WORD_LENGTH * (60 + 2) + (MAX_WORD_LENGTH -1) * 2;
        int prefHeight = 60 + 2;
        wordPanel.setPreferredSize(new Dimension(prefWidth, prefHeight));
        wordPanel.setMaximumSize(new Dimension(prefWidth + 20, prefHeight + 10)); // Allow some flex

        wordGridPanel.add(wordPanel);
        wordGridPanel.add(Box.createRigidArea(new Dimension(0, GRID_GAP / 2))); // Smaller gap between words in the grid
    }


    private void updateSolutionPathDisplay() {
        solutionPathPanel.removeAll(); // Clear previous path display from the panel itself

        if (model.getShowPath()) {
            List<String> solutionPath = model.getSolutionPath();
            if (solutionPath != null && !solutionPath.isEmpty()) {
                for (String word : solutionPath) {
                    solutionPathPanel.add(createPathWordPanel(word));
                    solutionPathPanel.add(Box.createRigidArea(new Dimension(0, 2)));
                }
                if (solutionPathPanel.getComponentCount() > 0) { // Remove last spacer
                    solutionPathPanel.remove(solutionPathPanel.getComponentCount() - 1);
                }
                solutionPathScrollPane.setBorder(BorderFactory.createTitledBorder("Solution Path"));
            } else {
                JLabel noPathLabel = new JLabel("No solution path found.", SwingConstants.CENTER);
                noPathLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                solutionPathPanel.add(noPathLabel);
                solutionPathScrollPane.setBorder(BorderFactory.createTitledBorder("Solution Path"));
            }
            solutionPathScrollPane.setVisible(true);
        } else {
            solutionPathScrollPane.setVisible(false);
            solutionPathScrollPane.setBorder(BorderFactory.createEmptyBorder()); // Clear border when hidden
        }

        solutionPathPanel.revalidate();
        solutionPathPanel.repaint();
        solutionPathScrollPane.revalidate(); // Revalidate scroll pane too
        solutionPathScrollPane.repaint();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == model) {
            // Update start and end words (they are direct children of gameAreaPanel)
            // We can replace them directly if their references are stable,
            // or rebuild parts of gameAreaPanel if structure might change.
            // For simplicity here, let's assume gameAreaPanel's structure around these is fixed.

            // Remove old ones and add new ones if they were direct children of gameAreaPanel
            // and their position is fixed. If they are inside another panel, update that sub-panel.
            gameAreaPanel.remove(startWordTiledPanel);
            gameAreaPanel.remove(endWordTiledPanel);
            // We also need to remove the rigid areas and the scrollpane temporarily
            // if we are re-adding in a specific order.

            // Simpler: Just recreate and re-add to gameAreaPanel in order
            // This is robust but might cause slight flicker if not done carefully.
            // A more optimized way would be to update the content of start/endWordTiledPanel directly.
            // For now, we keep the existing logic of rebuilding the gameAreaPanel content.

            gameAreaPanel.removeAll(); // Clear gameAreaPanel

            startWordTiledPanel = createTiledWordPanel(model.getStartWord(), KEYBOARD_COLOR);
            endWordTiledPanel = createTiledWordPanel(model.getEndWord(), KEYBOARD_COLOR);

            // Re-add components to gameAreaPanel in the correct order
            gameAreaPanel.add(startWordTiledPanel);
            gameAreaPanel.add(Box.createRigidArea(new Dimension(0, GRID_GAP)));
            gameAreaPanel.add(wordGridScrollPane); // Add the scroll pane, not the panel directly
            gameAreaPanel.add(Box.createRigidArea(new Dimension(0, GRID_GAP)));
            gameAreaPanel.add(endWordTiledPanel);
            // gameAreaPanel.add(Box.createVerticalGlue()); // if you want to push content up within gameAreaPanel

            gameAreaPanel.revalidate();
            gameAreaPanel.repaint();

            resetButton.setEnabled(!model.getWordHistory().isEmpty());
            updateWordGrid(); // This updates content of wordGridPanel (inside wordGridScrollPane)
            updateSolutionPathDisplay(); // This updates content of solutionPathPanel and visibility of solutionPathScrollPane

            showErrorMessageBox.setSelected(model.getShowErrorMessage());
            showPathBox.setSelected(model.getShowPath());
            randomWordsBox.setSelected(model.getRandomWords());

            if (model.isGameOver()) {
                JDialog winDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Congratulations!", true);
                winDialog.setLayout(new BorderLayout(10, 10));
                JPanel contentPanel = new JPanel();
                contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
                contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                contentPanel.setBackground(BACKGROUND_COLOR);

                JLabel congratsLabel = new JLabel("Congratulations!");
                congratsLabel.setFont(new Font("Arial", Font.BOLD, 24));
                congratsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel summaryLabel = new JLabel(String.format("You've successfully transformed '%s' into '%s'",
                        model.getStartWord(), model.getEndWord()));
                summaryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel movesLabel = new JLabel(String.format("Number of moves: %d", model.getWordHistory().size()));
                movesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                contentPanel.add(congratsLabel);
                contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                contentPanel.add(summaryLabel);
                contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                contentPanel.add(movesLabel);

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                buttonPanel.setBackground(BACKGROUND_COLOR);
                JButton newGameButtonDialog = new JButton("New Game"); // Renamed to avoid conflict
                JButton closeButton = new JButton("Close");

                newGameButtonDialog.addActionListener(e -> {
                    winDialog.dispose();
                    controller.handleNewGame();
                });

                closeButton.addActionListener(e -> winDialog.dispose());
                buttonPanel.add(newGameButtonDialog);
                buttonPanel.add(closeButton);

                winDialog.add(contentPanel, BorderLayout.CENTER);
                winDialog.add(buttonPanel, BorderLayout.SOUTH);
                winDialog.pack();
                winDialog.setLocationRelativeTo(this);
                winDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                winDialog.setResizable(false);
                SwingUtilities.invokeLater(() -> winDialog.setVisible(true));
            }
        }
    }

    public void updateCurrentInput(String input) {
        this.currentInput = new StringBuilder(input);
        updateWordGrid();
    }
}