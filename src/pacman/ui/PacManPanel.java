package pacman.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.*;

import pacman.game.PacManGame;

public class PacManPanel extends JPanel implements ActionListener, KeyListener {
    // Controls:
    // - Arrow keys: move
    // - P: pause/resume
    // - R: restart
    // - Enter: restart after Win/Game Over

    private final PacManGame game;
    private final Timer gameLoop;

    public PacManPanel() {
        this.game = new PacManGame(PacManGame.Assets.load(PacManPanel.class));

        setPreferredSize(new Dimension(game.getBoardWidth(), game.getBoardHeight()));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        this.gameLoop = new Timer(PacManGame.TICK_MS, this);
        this.gameLoop.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        game.draw(g);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        game.tick();
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        game.onKeyPressed(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
