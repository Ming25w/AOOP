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






} 