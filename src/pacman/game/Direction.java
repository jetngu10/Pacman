package pacman.game;

import java.awt.event.KeyEvent;

public enum Direction {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0),
    NONE(0, 0);

    final int dx;
    final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public Direction opposite() {
        if (this == UP) {
            return DOWN;
        }
        if (this == DOWN) {
            return UP;
        }
        if (this == LEFT) {
            return RIGHT;
        }
        if (this == RIGHT) {
            return LEFT;
        }
        return NONE;
    }

    public static Direction fromKeyCode(int keyCode) {
        if (keyCode == KeyEvent.VK_UP) {
            return UP;
        }
        if (keyCode == KeyEvent.VK_DOWN) {
            return DOWN;
        }
        if (keyCode == KeyEvent.VK_LEFT) {
            return LEFT;
        }
        if (keyCode == KeyEvent.VK_RIGHT) {
            return RIGHT;
        }
        return null;
    }
}
