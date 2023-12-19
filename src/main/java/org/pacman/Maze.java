/* Maze.java */

package org.pacman;

import java.awt.*;

public class Maze {
    private char[][][] grids;
    private char[][] grid;
    private int currentLevel = 0;

    private static int cellSize = 30;

    private int mazeStartX;
    private int mazeStartY;

    public Maze() {

        grids = new char[][][] {
                {
                        "############################".toCharArray(),
                        "#............##............#".toCharArray(),
                        "#.####.#####.##.#####.####.#".toCharArray(),
                        "#o####.#####.##.#####.####o#".toCharArray(),
                        "#.####.#####.##.#####.####.#".toCharArray(),
                        "T..........................T".toCharArray(),
                        "#.####.##.########.##.####.#".toCharArray(),
                        "#.####.##.########.##.####.#".toCharArray(),
                        "#......##....##....##......#".toCharArray(),
                        "######.##### ## #####.######".toCharArray(),
                        "     #.##### ## #####.#     ".toCharArray(),
                        "     #.##     R    ##.#     ".toCharArray(),
                        "     #.## ###--### ##.#     ".toCharArray(),
                        "######.## #      # ##.######".toCharArray(),
                        "T     .   # CBSI #   .     T".toCharArray(),
                        "######.## #      # ##.######".toCharArray(),
                        "     #.## ######## ##.#     ".toCharArray(),
                        "     #.##    P     ##.#     ".toCharArray(), // 'P' als Startpunkt
                        "     #.## ######## ##.#     ".toCharArray(),
                        "######.## ######## ##.######".toCharArray(),
                        "#............##............#".toCharArray(),
                        "#.####.#####.##.#####.####.#".toCharArray(),
                        "#.####.#####.##.#####.####.#".toCharArray(),
                        "#o..##.......  .......##..o#".toCharArray(),
                        "###.##.##.########.##.##.###".toCharArray(),
                        "###.##.##.########.##.##.###".toCharArray(),
                        "#......##....##....##......#".toCharArray(),
                        "#.##########.##.##########.#".toCharArray(),
                        "#.##########.##.##########.#".toCharArray(),
                        "#..........................#".toCharArray(),
                        "############################".toCharArray()
                },
                {
                        // Layout für Level 2
                        "############################".toCharArray(),
                        "#............##............#".toCharArray(),
                        "#.####.#####.##.#####.####.#".toCharArray(),
                        "#o####.#####.##.#####.####o#".toCharArray(),
                        "#.####.#####.##.#####.####.#".toCharArray(),
                        "T..........................T".toCharArray(),
                        "#.####.##.########.##.####.#".toCharArray(),
                        "#.####.##.########.##.####.#".toCharArray(),
                        "#......##....##....##......#".toCharArray(),
                        "######.##### ## #####.######".toCharArray(),
                        "     #.##### ## #####.#     ".toCharArray(),
                        "     #.##          ##.#     ".toCharArray(),
                        "     #.## ######## ##.#     ".toCharArray(),
                        "######.## #      # ##.######".toCharArray(),
                        "T     .   # CBSI #   .     T".toCharArray(),
                        "######.## #      # ##.######".toCharArray(),
                        "     #.## ######## ##.#     ".toCharArray(),
                        "     #.##    P     ##.#     ".toCharArray(), // 'P' als Startpunkt
                        "     #.## ######## ##.#     ".toCharArray(),
                        "######.## ######## ##.######".toCharArray(),
                        "#............##............#".toCharArray(),
                        "#.####.#####.##.#####.####.#".toCharArray(),
                        "#.####.#####.##.#####.####.#".toCharArray(),
                        "#o..##.......  .......##..o#".toCharArray(),
                        "###.##.##.########.##.##.###".toCharArray(),
                        "###.##.##.########.##.##.###".toCharArray(),
                        "#......##....##....##......#".toCharArray(),
                        "#.##########.##.##########.#".toCharArray(),
                        "#.##########.##.##########.#".toCharArray(),
                        "#..........................#".toCharArray(),
                        "############################".toCharArray()
                },
                {
                        // Layout für Level 3
                        "############################".toCharArray(),
                        "#............##....CBSI....#".toCharArray(),
                        "#.####.#####.##.#####.####.#".toCharArray(),
                        "#o####.#####.##.#####.####o#".toCharArray(),
                        "#.####.#####.##.#####.####.#".toCharArray(),
                        "T..........................T".toCharArray(),
                        "#.####.##.########.##.####.#".toCharArray(),
                        "#.####.##.########.##.####.#".toCharArray(),
                        "#......##....##....##......#".toCharArray(),
                        "######.##### ## #####.######".toCharArray(),
                        "     #.##### ## #####.#     ".toCharArray(),
                        "     #.##          ##.#     ".toCharArray(),
                        "     #.## ######## ##.#     ".toCharArray(),
                        "######.## #      # ##.######".toCharArray(),
                        "T     .   #      #   .     T".toCharArray(),
                        "######.## #      # ##.######".toCharArray(),
                        "     #.## ######## ##.#     ".toCharArray(),
                        "     #.##    P     ##.#     ".toCharArray(), // 'P' als Startpunkt
                        "     #.## ######## ##.#     ".toCharArray(),
                        "######.## ######## ##.######".toCharArray(),
                        "#............##............#".toCharArray(),
                        "#.####.#####.##.#####.####.#".toCharArray(),
                        "#.####.#####.##.#####.####.#".toCharArray(),
                        "#o..##.......  .......##..o#".toCharArray(),
                        "###.##.##.########.##.##.###".toCharArray(),
                        "###.##.##.########.##.##.###".toCharArray(),
                        "#......##....##....##......#".toCharArray(),
                        "#.##########.##.##########.#".toCharArray(),
                        "#.##########.##.##########.#".toCharArray(),
                        "#..........................#".toCharArray(),
                        "############################".toCharArray()
                }

        };

        grid = copyGrid(grids[currentLevel]);

    }

    private char[][] copyGrid(char[][] source) {
        char[][] copy = new char[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = source[i].clone();
        }
        return copy;
    }

    public void resetMaze() {
        grid = copyGrid(grids[currentLevel]);
    }

    public void changeLevel(int level) {
        if (level >= 0 && level < grids.length) {
            currentLevel = level;
            resetMaze();
        }
    }

    public char[][] getGrid() {
        return grid;
    }

    public Point findPacmanStart() {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                if (grid[row][col] == 'P') {
                    int startX = col * cellSize;
                    int startY = row * cellSize;
                    // System.out.println("P "+cellSize + "*" + col + " " + row);

                    return new Point(startX, startY);
                }
            }
        }
        return null; // oder Standard-Startposition, falls 'P' nicht gefunden wurde
    }

    // Methode zum Finden des Freigabepunktes
    public Point findReleasePoint() {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                if (grid[row][col] == 'R') { // 'R' markiert den Freigabepunkt
                    int startX = col * cellSize;
                    int startY = row * cellSize;
                    // System.out.println("P "+cellSize + "*" + col + " " + row);

                    return new Point(startX, startY);
                }
            }
        }
        return null; // Falls kein Freigabepunkt gefunden wird
    }

    public Point findGhostStart(char ghostChar) {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                if (grid[row][col] == ghostChar) {
                    int startX = col * cellSize;
                    int startY = row * cellSize;
                    // System.out.println("P "+cellSize + "*" + col + " " + row);

                    return new Point(startX, startY);
                }
            }
        }
        return null; // oder Standard-Startposition, falls 'P' nicht gefunden wurde
    }

    public static void calculateCellSize(int panelWidth, int panelHeight, char[][] grid, int padding) {
        cellSize = Math.min(panelWidth / grid[0].length, panelHeight / (grid.length + padding));
    }
    public static int getCellSize() {
        return cellSize;
    }


    public int getMazeStartY() {
        return mazeStartY;
    }

    public void setMazeStartY(int mazeStartY) {
        this.mazeStartY = mazeStartY;
    }

    public int getMazeStartX() {
        return mazeStartX;
    }

    public void setMazeStartX(int mazeStartX) {
        this.mazeStartX = mazeStartX;
    }

    public boolean isWall(int gridX, int gridY) {
        // Überprüfen, ob die Koordinaten innerhalb der Grenzen des Labyrinths liegen
        if (gridX < 0 || gridX >= grid[0].length || gridY < 0 || gridY >= grid.length) {
            return true; // Position außerhalb des Labyrinths wird als Wand betrachtet
        }

        return grid[gridY][gridX] == '-' || grid[gridY][gridX] == '#'; // '#' repräsentiert eine Wand
    }

    public boolean isWallOrTeleportForGhost(int gridX, int gridY) {
        // Überprüfen, ob die Koordinaten innerhalb der Grenzen des Labyrinths liegen
        if (gridX < 0 || gridX >= grid[0].length || gridY < 0 || gridY >= grid.length) {
            return true; // Position außerhalb des Labyrinths wird als Wand betrachtet
        }

        // '#'-Zeichen repräsentiert eine Wand, 'T' repräsentiert einen Teleportationspunkt
        return grid[gridY][gridX] == '#' || grid[gridY][gridX] == '-' || grid[gridY][gridX] == 'T';
    }

}
