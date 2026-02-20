# Pacman (Java Swing)

A lightweight Pac-Man clone built with Java Swing/AWT.

![pacman-ss](https://github.com/user-attachments/assets/9f16553b-9092-4894-b740-b8903ed24fa9)

## Controls

- Arrow keys: move
- P: pause/resume
- R: restart
- Enter: restart after Win/Game Over

## How to run

### VS Code (recommended)

This repo is set up for the VS Code Java extension:

- Install the Java extensions (e.g., Java Extension Pack)
- Open this folder in VS Code
- Run `src/pacman/App.java` (Run ▶ button)

Note: `.vscode/settings.json` sets `src` as the source path and `bin` as the output folder.

### Terminal

Requires Java 8+.

macOS/Linux:

```bash
javac -d bin $(find src -name "*.java")
java -cp "bin:src" pacman.App
```

Windows (PowerShell):

```powershell
javac -d bin (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object FullName)
java -cp "bin;src" pacman.App
```

If you see `Missing resource: wall.png`, make sure assets are on the classpath (the commands above include `src`), or copy `src/assets` to `bin/assets`.

## Features

- Tile-based maps (21×19 grid) with multiple levels
- Pellets, power pellets (frightened ghosts), score, lives, and HUD
- Ghost movement that picks a valid random direction at intersections/walls
- Pause/Game Over/Win overlays and a simple death animation

## Project layout

- `src/pacman/App.java`: app entry point (`JFrame`)
- `src/pacman/ui/PacManPanel.java`: Swing panel + game loop + keyboard input
- `src/pacman/game/PacManGame.java`: core gameplay, maps, rendering, collisions
- `src/assets/images`: sprites

## Credit

Based on this coding tutorial: https://youtu.be/lB_J-VNMVpE
