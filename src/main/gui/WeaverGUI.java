package main.gui;

import main.model.WeaverModel;
import main.controller.WeaverController;
import main.view.WeaverGUIView;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;


public class WeaverGUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Create model
                WeaverModel model = new WeaverModel();
                model.loadDictionary("dictionary.txt");

                // Start new game FIRST to initialize start and end words
                model.newGame();

                // Create controller
                WeaverController controller = new WeaverController(model);

                // Create view (Now the model has start/end words set)
                WeaverGUIView view = new WeaverGUIView(model, controller);
                controller.setView(view);

                // Register view as observer
                model.addObserver(view);


                // Create and show frame
                JFrame frame = new JFrame("Weaver Game");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.add(view);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

                // Request focus for keyboard input
                view.requestFocusInWindow();

            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                        "Error loading dictionary: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}