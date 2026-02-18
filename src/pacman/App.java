package pacman;

import javax.swing.JFrame;

import pacman.ui.PacManPanel;

public class App {
    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame("Pac Man");
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        PacManPanel pacmanGame = new PacManPanel();

        frame.add(pacmanGame);
        frame.pack();
        frame.setVisible(true);
        pacmanGame.requestFocusInWindow();

    }
}
