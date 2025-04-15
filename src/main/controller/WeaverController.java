package main.controller;

import main.model.WeaverModelInterface;
import main.view.WeaverGUIView;

import javax.swing.JOptionPane;

public class WeaverController {
    private WeaverModelInterface model;
    private WeaverGUIView view;
    private StringBuilder currentInput;
    private static final int MAX_WORD_LENGTH = 4;

    public WeaverController(WeaverModelInterface model) {
        this.model = model;
        this.currentInput = new StringBuilder();
    }

    public void setView(WeaverGUIView view) {
        this.view = view;
    }

    public void handleCharacterInput(char c) {
        if (currentInput.length() < MAX_WORD_LENGTH) {
            currentInput.append(Character.toLowerCase(c));
            view.updateCurrentInput(currentInput.toString());
        }
    }

    public void handleBackspace() {
        if (currentInput.length() > 0) {
            currentInput.setLength(currentInput.length() - 1);
            view.updateCurrentInput(currentInput.toString());
        }
    }

    public void handleEnter() {
        if (currentInput.length() == MAX_WORD_LENGTH) {
            String word = currentInput.toString();
            if (!model.processInput(word) && model.getShowErrorMessage()) {
                JOptionPane.showMessageDialog(view,
                    "Invalid word! Must be in dictionary and differ by one letter.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
            currentInput.setLength(0);
            view.updateCurrentInput("");
        }
    }

    public void handleReset() {
        model.resetGame();
        currentInput.setLength(0);
        view.updateCurrentInput("");
    }

    public void handleNewGame() {
        model.newGame();
        currentInput.setLength(0);
        view.updateCurrentInput("");
    }

    public void handleToggleShowError(boolean show) {
        model.setShowErrorMessage(show);
    }

    public void handleToggleShowPath(boolean show) {
        model.setShowPath(show);
    }

    public void handleToggleRandomWords(boolean random) {
        model.setRandomWords(random);
    }
} 