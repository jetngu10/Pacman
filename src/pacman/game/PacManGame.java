package pacman.game;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import javax.swing.ImageIcon;

public class PacManGame {
    public static final int ROW_COUNT = 21;
    public static final int COLUMN_COUNT = 19;
    public static final int TILE_SIZE = 32;
    public static final int SPEED = TILE_SIZE / 4;
    public static final int BOARD_WIDTH = COLUMN_COUNT * TILE_SIZE;
    public static final int BOARD_HEIGHT = ROW_COUNT * TILE_SIZE;
    public static final int TICK_MS = 50;

    public static final class Assets {
        final Image wall;
        final Image blueGhost;
        final Image orangeGhost;
        final Image pinkGhost;
        final Image redGhost;
        final Image scaredGhost;
        final Image pacmanUp;
        final Image pacmanDown;
        final Image pacmanLeft;
        final Image pacmanRight;
        final Image powerPellet;

        private Assets(
                Image wall,
                Image blueGhost,
                Image orangeGhost,
                Image pinkGhost,
                Image redGhost,
                Image scaredGhost,
                Image pacmanUp,
                Image pacmanDown,
                Image pacmanLeft,
                Image pacmanRight,
                Image powerPellet) {
            this.wall = wall;
            this.blueGhost = blueGhost;
            this.orangeGhost = orangeGhost;
            this.pinkGhost = pinkGhost;
            this.redGhost = redGhost;
            this.scaredGhost = scaredGhost;
            this.pacmanUp = pacmanUp;
            this.pacmanDown = pacmanDown;
            this.pacmanLeft = pacmanLeft;
            this.pacmanRight = pacmanRight;
            this.powerPellet = powerPellet;
        }

        public static Assets load(Class<?> resourceBase) {
            Image wall = loadImage(resourceBase, "wall.png");
            Image blueGhost = loadImage(resourceBase, "blueGhost.png");
            Image orangeGhost = loadImage(resourceBase, "orangeGhost.png");
            Image pinkGhost = loadImage(resourceBase, "pinkGhost.png");
            Image redGhost = loadImage(resourceBase, "redGhost.png");
            Image scaredGhost = loadImage(resourceBase, "scaredGhost.png");
            Image pacmanUp = loadImage(resourceBase, "pacmanUp.png");
            Image pacmanDown = loadImage(resourceBase, "pacmanDown.png");
            Image pacmanLeft = loadImage(resourceBase, "pacmanLeft.png");
            Image pacmanRight = loadImage(resourceBase, "pacmanRight.png");
            Image powerPellet = loadImage(resourceBase, "powerFood.png");

            return new Assets(
                    wall,
                    blueGhost,
                    orangeGhost,
                    pinkGhost,
                    redGhost,
                    scaredGhost,
                    pacmanUp,
                    pacmanDown,
                    pacmanLeft,
                    pacmanRight,
                    powerPellet);
        }

        private static Image loadImage(Class<?> resourceBase, String filename) {
            // Resources live under src/assets/images and are copied to bin/assets/images by VS Code.
            URL url = resourceBase.getResource("/assets/images/" + filename);
            if (url == null) {
                // Backwards-compatible fallbacks (older project structure).
                url = resourceBase.getResource("/images/" + filename);
            }
            if (url == null) {
                url = resourceBase.getResource("/" + filename);
            }
            if (url == null) {
                throw new IllegalStateException("Missing resource: " + filename);
            }
            return new ImageIcon(url).getImage();
        }
    }

    private static final Direction[] MOVE_DIRECTIONS = {
            Direction.UP,
            Direction.DOWN,
            Direction.LEFT,
            Direction.RIGHT
    };

    private final Random random = new Random();
    private final Assets assets;

    private GameState gameState = GameState.RUNNING;
    private int score = 0;
    private int lives = 3;
    private int levelIndex = 0;

    private Direction requestedDirection = Direction.RIGHT;

    private int powerTicksRemaining = 0;
    private final int powerDurationTicks = 140; // ~7 seconds at 50ms per tick

    private int deathTicksRemaining = 0;
    private final int deathDurationTicks = 60; // ~3 seconds
    private final int deathMouthCloseTicks = 40;
    private Direction deathDirection = Direction.RIGHT;

    private Player pacman;
    private final List<Wall> walls = new ArrayList<>();
    private final List<Pellet> pellets = new ArrayList<>();
    private final List<Ghost> ghosts = new ArrayList<>();

    // Tile legend:
    // - X: wall
    // - ' ': pellet
    // - F: power pellet
    // - P: Pac-Man start
    // - b/o/r/p: ghost spawns
    // - O: empty path (no pellet)
    private final String[][] levelMaps = {
            {
                    "XXXXXXXXXXXXXXXXXXX",
                    "XF       X       FX",
                    "X XX XXX X XXX XX X",
                    "X                 X",
                    "X XX X XXXXX X XX X",
                    "X    X       X    X",
                    "XXXX XXXX XXXX XXXX",
                    "OOOX X       X XOOO",
                    "XXXX X XXrXX X XXXX",
                    "OX       bpo     XO",
                    "XXXX X XXXXX X XXXX",
                    "OOOX X       X XOOO",
                    "XXXX X XXXXX X XXXX",
                    "X        X        X",
                    "X XX XXX X XXX XX X",
                    "X  X     P     X  X",
                    "XX X X XXXXX X X XX",
                    "X    X   X   X    X",
                    "X XXXXXX X XXXXXX X",
                    "XF               FX",
                    "XXXXXXXXXXXXXXXXXXX"
            },
            {
                    "XXXXXXXXXXXXXXXXXXX",
                    "XF  X    X    X  FX",
                    "X XX XXX X XXX XX X",
                    "X  X           X  X",
                    "X XX X XXXXX X XX X",
                    "X    X X   X X    X",
                    "XXXX XXXX XXXX XXXX",
                    "OOOX X       X XOOO",
                    "XXXX X XXrXX X XXXX",
                    "OX       bpo     XO",
                    "XXXX X XXXXX X XXXX",
                    "OOOX X       X XOOO",
                    "XXXX X XXXXX X XXXX",
                    "X XXX    X    XXX X",
                    "X XX XXX X XXX XX X",
                    "X  X     P     X  X",
                    "XX X X XXXXX X X XX",
                    "X    X   X   X    X",
                    "X XXXXXX X XXXXXX X",
                    "X   F         F   X",
                    "XXXXXXXXXXXXXXXXXXX"
            },
            {
                    "XXXXXXXXXXXXXXXXXXX",
                    "XF   X       X   FX",
                    "X XXX XXXXXXX XXX X",
                    "X   X         X   X",
                    "XXX X XXX XXX X XXX",
                    "X                 X",
                    "XXXX XXXX XXXX XXXX",
                    "OOOX X       X XOOO",
                    "XXXX X XXrXX X XXXX",
                    "OX       bpo     XO",
                    "XXXX X XXXXX X XXXX",
                    "OOOX X       X XOOO",
                    "XXXX X XXXXX X XXXX",
                    "X                 X",
                    "X XXX XXXXXXX XXX X",
                    "X   X    P    X   X",
                    "XXX X XXX XXX X XXX",
                    "X   X         X   X",
                    "X XXX XXXXXXX XXX X",
                    "XF               FX",
                    "XXXXXXXXXXXXXXXXXXX"
            }
    };

    public PacManGame(Assets assets) {
        this.assets = Objects.requireNonNull(assets, "assets");
        loadLevel(0);
        resetRound();
    }

    public int getBoardWidth() {
        return BOARD_WIDTH;
    }

    public int getBoardHeight() {
        return BOARD_HEIGHT;
    }

    public void onKeyPressed(int keyCode) {
        if (keyCode == KeyEvent.VK_P) {
            togglePause();
            return;
        }

        if (keyCode == KeyEvent.VK_R) {
            restartGame();
            return;
        }

        if (keyCode == KeyEvent.VK_ENTER) {
            if (gameState == GameState.GAME_OVER || gameState == GameState.WIN) {
                restartGame();
            }
            return;
        }

        if (gameState != GameState.RUNNING) {
            return;
        }

        Direction direction = Direction.fromKeyCode(keyCode);
        if (direction != null) {
            requestedDirection = direction;
        }
    }

    public void tick() {
        if (gameState == GameState.DYING) {
            updateDeathAnimation();
            return;
        }

        if (gameState != GameState.RUNNING) {
            return;
        }

        // Update order matters:
        // player move -> eat -> ghosts move -> collisions -> level progression -> timers
        movePacman();
        eatPellets();
        moveGhosts();
        handleGhostCollisions();
        if (gameState != GameState.RUNNING) {
            return;
        }
        advanceLevelIfComplete();
        updatePowerMode();
    }

    public void draw(Graphics g) {
        drawWalls(g);
        drawPellets(g);
        if (gameState != GameState.DYING) {
            pacman.draw(g);
        }
        for (Ghost ghost : ghosts) {
            ghost.draw(g);
        }
        if (gameState == GameState.DYING) {
            drawDeathAnimation(g);
        }
        drawHud(g);
        drawOverlay(g);
    }

    private void togglePause() {
        if (gameState == GameState.RUNNING) {
            gameState = GameState.PAUSED;
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.RUNNING;
        }
    }

    private void restartGame() {
        score = 0;
        lives = 3;
        levelIndex = 0;
        requestedDirection = Direction.RIGHT;
        powerTicksRemaining = 0;
        gameState = GameState.RUNNING;

        loadLevel(0);
        resetRound();
    }

    private void loadLevel(int newLevelIndex) {
        levelIndex = newLevelIndex;
        walls.clear();
        pellets.clear();
        ghosts.clear();
        pacman = null;

        String[] map = levelMaps[levelIndex];
        validateMap(map);

        for (int row = 0; row < ROW_COUNT; row++) {
            String mapRow = map[row];
            for (int col = 0; col < COLUMN_COUNT; col++) {
                char tile = mapRow.charAt(col);

                int x = col * TILE_SIZE;
                int y = row * TILE_SIZE;

                if (tile == 'X') {
                    walls.add(new Wall(x, y, assets.wall));
                } else if (tile == ' ') {
                    pellets.add(Pellet.normal(x + 14, y + 14));
                } else if (tile == 'F') {
                    pellets.add(Pellet.power(x, y, assets.powerPellet));
                } else if (tile == 'P') {
                    pacman = new Player(x, y, assets.pacmanUp, assets.pacmanDown, assets.pacmanLeft, assets.pacmanRight);
                } else if (tile == 'b') {
                    ghosts.add(new Ghost(x, y, assets.blueGhost, assets.scaredGhost));
                } else if (tile == 'o') {
                    ghosts.add(new Ghost(x, y, assets.orangeGhost, assets.scaredGhost));
                } else if (tile == 'p') {
                    ghosts.add(new Ghost(x, y, assets.pinkGhost, assets.scaredGhost));
                } else if (tile == 'r') {
                    ghosts.add(new Ghost(x, y, assets.redGhost, assets.scaredGhost));
                }
            }
        }

        if (pacman == null) {
            throw new IllegalStateException("Level " + (levelIndex + 1) + " is missing Pac-Man start tile 'P'");
        }
    }

    private void validateMap(String[] map) {
        if (map.length != ROW_COUNT) {
            throw new IllegalStateException("Invalid level: expected " + ROW_COUNT + " rows, found " + map.length);
        }
        for (int row = 0; row < ROW_COUNT; row++) {
            if (map[row].length() != COLUMN_COUNT) {
                throw new IllegalStateException(
                        "Invalid level row " + row + ": expected " + COLUMN_COUNT + " cols, found " + map[row].length());
            }
        }
    }

    private void resetRound() {
        pacman.reset();
        pacman.setDirection(Direction.RIGHT);
        pacman.updateSprite();

        for (Ghost ghost : ghosts) {
            ghost.reset();
            ghost.setDirection(pickGhostDirection(ghost));
            ghost.setFrightened(powerTicksRemaining > 0);
        }
    }

    private void movePacman() {
        if (isAlignedToTile(pacman) && canMove(pacman, requestedDirection)) {
            pacman.setDirection(requestedDirection);
            pacman.updateSprite();
        }

        if (!canMove(pacman, pacman.getDirection())) {
            return;
        }

        pacman.move();
        wrapHorizontally(pacman);
    }

    private void eatPellets() {
        Iterator<Pellet> iterator = pellets.iterator();
        while (iterator.hasNext()) {
            Pellet pellet = iterator.next();
            if (intersects(pacman, pellet)) {
                iterator.remove();
                if (pellet.power) {
                    score += 50;
                    powerTicksRemaining = powerDurationTicks;
                    setGhostsFrightened(true);
                } else {
                    score += 10;
                }
            }
        }
    }

    private void moveGhosts() {
        for (Ghost ghost : ghosts) {
            if (isAlignedToTile(ghost) || !canMove(ghost, ghost.getDirection())) {
                ghost.setDirection(pickGhostDirection(ghost));
            }

            if (!canMove(ghost, ghost.getDirection())) {
                continue;
            }

            ghost.move();
            wrapHorizontally(ghost);
        }
    }

    private void handleGhostCollisions() {
        for (Ghost ghost : ghosts) {
            if (!intersects(pacman, ghost)) {
                continue;
            }

            if (powerTicksRemaining > 0) {
                score += 200;
                ghost.reset();
                ghost.setDirection(pickGhostDirection(ghost));
                ghost.setFrightened(true);
                continue;
            }

            powerTicksRemaining = 0;
            setGhostsFrightened(false);

            lives--;
            if (lives < 0) {
                lives = 0;
            }
            startDeathAnimation();
            return;
        }
    }

    private void advanceLevelIfComplete() {
        if (!pellets.isEmpty()) {
            return;
        }

        if (levelIndex < levelMaps.length - 1) {
            requestedDirection = Direction.RIGHT;
            powerTicksRemaining = 0;
            setGhostsFrightened(false);
            loadLevel(levelIndex + 1);
            resetRound();
            return;
        }

        gameState = GameState.WIN;
    }

    private void updatePowerMode() {
        if (powerTicksRemaining <= 0) {
            return;
        }

        powerTicksRemaining--;
        if (powerTicksRemaining == 0) {
            setGhostsFrightened(false);
        }
    }

    private void startDeathAnimation() {
        // Freeze gameplay and play the classic "mouth closes then disappears" animation.
        deathDirection = pacman.getDirection();
        if (deathDirection == null || deathDirection == Direction.NONE) {
            deathDirection = Direction.RIGHT;
        }
        deathTicksRemaining = deathDurationTicks;
        gameState = GameState.DYING;
    }

    private void updateDeathAnimation() {
        if (deathTicksRemaining > 0) {
            deathTicksRemaining--;
            return;
        }

        if (lives <= 0) {
            gameState = GameState.GAME_OVER;
            return;
        }

        requestedDirection = Direction.RIGHT;
        gameState = GameState.RUNNING;
        resetRound();
    }

    private void drawDeathAnimation(Graphics g) {
        if (deathTicksRemaining <= 0) {
            return;
        }

        int elapsedTicks = deathDurationTicks - deathTicksRemaining;
        float mouthOpenDegrees = 90f;
        float scale = 1f;

        if (elapsedTicks < deathMouthCloseTicks) {
            float t = elapsedTicks / (float) deathMouthCloseTicks;
            mouthOpenDegrees = mouthOpenDegrees * (1f - t);
        } else {
            mouthOpenDegrees = 0f;
            int shrinkElapsed = elapsedTicks - deathMouthCloseTicks;
            int shrinkTicks = Math.max(1, deathDurationTicks - deathMouthCloseTicks);
            float t = shrinkElapsed / (float) shrinkTicks;
            scale = Math.max(0f, 1f - t);
        }

        int size = Math.round(TILE_SIZE * scale);
        if (size <= 0) {
            return;
        }

        int centerX = pacman.x + (pacman.width / 2);
        int centerY = pacman.y + (pacman.height / 2);
        int drawX = centerX - (size / 2);
        int drawY = centerY - (size / 2);

        int mouthAngle = Math.max(0, Math.min(359, Math.round(mouthOpenDegrees)));
        int startAngle = directionToAngle(deathDirection) + (mouthAngle / 2);
        int extent = 360 - mouthAngle;

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.YELLOW);
            g2.fillArc(drawX, drawY, size, size, startAngle, extent);
        } finally {
            g2.dispose();
        }
    }

    private int directionToAngle(Direction direction) {
        if (direction == Direction.UP) {
            return 90;
        }
        if (direction == Direction.DOWN) {
            return 270;
        }
        if (direction == Direction.LEFT) {
            return 180;
        }
        return 0; // RIGHT
    }

    private void setGhostsFrightened(boolean frightened) {
        for (Ghost ghost : ghosts) {
            ghost.setFrightened(frightened);
        }
    }

    private Direction pickGhostDirection(Ghost ghost) {
        List<Direction> possibleDirections = new ArrayList<>();
        for (Direction direction : MOVE_DIRECTIONS) {
            if (canMove(ghost, direction)) {
                possibleDirections.add(direction);
            }
        }

        if (possibleDirections.isEmpty()) {
            return ghost.getDirection();
        }

        if (possibleDirections.size() > 1) {
            possibleDirections.remove(ghost.getDirection().opposite());
        }

        if (possibleDirections.isEmpty()) {
            return ghost.getDirection().opposite();
        }

        return possibleDirections.get(random.nextInt(possibleDirections.size()));
    }

    private boolean canMove(Actor actor, Direction direction) {
        if (direction == null || direction == Direction.NONE) {
            return false;
        }

        int nextX = actor.x + (direction.dx * SPEED);
        int nextY = actor.y + (direction.dy * SPEED);

        for (Wall wall : walls) {
            if (rectanglesIntersect(nextX, nextY, actor.width, actor.height, wall.x, wall.y, wall.width, wall.height)) {
                return false;
            }
        }

        return true;
    }

    private boolean isAlignedToTile(Actor actor) {
        return actor.x % TILE_SIZE == 0 && actor.y % TILE_SIZE == 0;
    }

    private void wrapHorizontally(Actor actor) {
        if (actor.x < 0) {
            actor.x = BOARD_WIDTH - TILE_SIZE;
        } else if (actor.x > BOARD_WIDTH - TILE_SIZE) {
            actor.x = 0;
        }
    }

    private void drawWalls(Graphics g) {
        for (Wall wall : walls) {
            wall.draw(g);
        }
    }

    private void drawPellets(Graphics g) {
        g.setColor(Color.WHITE);
        for (Pellet pellet : pellets) {
            pellet.draw(g);
        }
    }

    private void drawHud(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.setColor(Color.WHITE);

        g.drawString("Score: " + score, 10, 20);

        String levelText = "Level: " + (levelIndex + 1) + "/" + levelMaps.length;
        FontMetrics fontMetrics = g.getFontMetrics();
        g.drawString(levelText, (BOARD_WIDTH - fontMetrics.stringWidth(levelText)) / 2, 20);

        String livesText = "Lives: " + lives;
        g.drawString(livesText, BOARD_WIDTH - fontMetrics.stringWidth(livesText) - 10, 20);
    }

    private void drawOverlay(Graphics g) {
        if (gameState == GameState.PAUSED) {
            drawCenteredText(g, "Paused (P to resume)");
        } else if (gameState == GameState.GAME_OVER) {
            drawCenteredText(g, "Game Over (Enter to restart)");
        } else if (gameState == GameState.WIN) {
            drawCenteredText(g, "You Win! (Enter to restart)");
        }
    }

    private void drawCenteredText(Graphics g, String text) {
        g.setFont(new Font("Arial", Font.BOLD, 28));
        FontMetrics fontMetrics = g.getFontMetrics();
        int textX = (BOARD_WIDTH - fontMetrics.stringWidth(text)) / 2;
        int textY = (BOARD_HEIGHT - fontMetrics.getHeight()) / 2 + fontMetrics.getAscent();
        g.drawString(text, textX, textY);
    }

    private boolean intersects(Entity a, Entity b) {
        return rectanglesIntersect(a.x, a.y, a.width, a.height, b.x, b.y, b.width, b.height);
    }

    private boolean rectanglesIntersect(int ax, int ay, int aw, int ah, int bx, int by, int bw, int bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }

    private static abstract class Entity {
        int x;
        int y;
        final int width;
        final int height;

        Entity(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        abstract void draw(Graphics g);
    }

    private static abstract class Actor extends Entity {
        final int startX;
        final int startY;
        private Direction direction = Direction.RIGHT;

        Actor(int x, int y, int width, int height) {
            super(x, y, width, height);
            this.startX = x;
            this.startY = y;
        }

        void reset() {
            this.x = startX;
            this.y = startY;
        }

        void setDirection(Direction direction) {
            if (direction != null && direction != Direction.NONE) {
                this.direction = direction;
            }
        }

        Direction getDirection() {
            return direction;
        }

        void move() {
            this.x += direction.dx * SPEED;
            this.y += direction.dy * SPEED;
        }
    }

    private static final class Wall extends Entity {
        private final Image image;

        Wall(int x, int y, Image image) {
            super(x, y, TILE_SIZE, TILE_SIZE);
            this.image = image;
        }

        @Override
        void draw(Graphics g) {
            g.drawImage(image, x, y, width, height, null);
        }
    }

    private static final class Pellet extends Entity {
        final boolean power;
        private final Image image;

        static Pellet normal(int x, int y) {
            return new Pellet(x, y, 4, 4, false, null);
        }

        static Pellet power(int x, int y, Image image) {
            return new Pellet(x, y, TILE_SIZE, TILE_SIZE, true, image);
        }

        private Pellet(int x, int y, int width, int height, boolean power, Image image) {
            super(x, y, width, height);
            this.power = power;
            this.image = image;
        }

        @Override
        void draw(Graphics g) {
            if (!power) {
                g.fillRect(x, y, width, height);
                return;
            }

            if (image != null) {
                g.drawImage(image, x, y, width, height, null);
            } else {
                g.fillOval(x + 8, y + 8, 16, 16);
            }
        }
    }

    private static final class Player extends Actor {
        private final Image upImage;
        private final Image downImage;
        private final Image leftImage;
        private final Image rightImage;
        private Image image;

        Player(int x, int y, Image up, Image down, Image left, Image right) {
            super(x, y, TILE_SIZE, TILE_SIZE);
            this.upImage = Objects.requireNonNull(up, "up");
            this.downImage = Objects.requireNonNull(down, "down");
            this.leftImage = Objects.requireNonNull(left, "left");
            this.rightImage = Objects.requireNonNull(right, "right");
            this.image = right;
        }

        void updateSprite() {
            if (getDirection() == Direction.UP) {
                image = upImage;
            } else if (getDirection() == Direction.DOWN) {
                image = downImage;
            } else if (getDirection() == Direction.LEFT) {
                image = leftImage;
            } else if (getDirection() == Direction.RIGHT) {
                image = rightImage;
            }
        }

        @Override
        void draw(Graphics g) {
            g.drawImage(image, x, y, width, height, null);
        }
    }

    private static final class Ghost extends Actor {
        private final Image baseImage;
        private final Image frightenedImage;
        private Image image;

        Ghost(int x, int y, Image baseImage, Image frightenedImage) {
            super(x, y, TILE_SIZE, TILE_SIZE);
            this.baseImage = Objects.requireNonNull(baseImage, "baseImage");
            this.frightenedImage = Objects.requireNonNull(frightenedImage, "frightenedImage");
            this.image = baseImage;
        }

        void setFrightened(boolean frightened) {
            image = frightened ? frightenedImage : baseImage;
        }

        @Override
        void draw(Graphics g) {
            g.drawImage(image, x, y, width, height, null);
        }
    }
}
