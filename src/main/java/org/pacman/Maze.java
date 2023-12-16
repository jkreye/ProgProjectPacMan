/* Maze.java */

package org.pacman;

import java.awt.*;

public class Maze {
    private char[][] grid, grid2, grid3;
    private static int cellSize = 24;

    private int mazeStartX;
    private int mazeStartY;

    public Maze() {
        grid2 = new char[][] {
                // Obere Grenze
                "###########################".toCharArray(),
                "#............#............#".toCharArray(),
                "#.####.#####.#.#####.####.#".toCharArray(),
                "#o####.#####.#.#####.####o#".toCharArray(),
                "#.####.#####.#.#####.####.#".toCharArray(),
                "#.........................#".toCharArray(),
                "#.####.##.#######.##.####.#".toCharArray(),
                "#.####.##.#######.##.####.#".toCharArray(),
                "#......##....#....##......#".toCharArray(),
                "######.##### # #####.######".toCharArray(),
                "     #.##### # #####.#     ".toCharArray(),
                "     #.##         ##.#     ".toCharArray(),
                "     #.## ###-### ##.#     ".toCharArray(),
                "######.## #     # ##.######".toCharArray(),
                "      .   #     #   .      ".toCharArray(),
                "######.## #     # ##.######".toCharArray(),
                "     #.## ####### ##.#     ".toCharArray(),
                "     #.##   P     ##.#     ".toCharArray(), // 'P' als Startpunkt
                "     #.## ####### ##.#     ".toCharArray(),
                "######.## ####### ##.######".toCharArray(),
                "#............#............#".toCharArray(),
                "#.####.#####.#.#####.####.#".toCharArray(),
                "#.####.#####.#.#####.####.#".toCharArray(),
                "#o..##....... .......##..o#".toCharArray(),
                "###.##.##.#######.##.##.###".toCharArray(),
                "###.##.##.#######.##.##.###".toCharArray(),
                "#......##....#....##......#".toCharArray(),
                "#.##########.#.##########.#".toCharArray(),
                "#.##########.#.##########.#".toCharArray(),
                "#.........................#".toCharArray(),
                // Untere Grenze
                "###########################".toCharArray()
        };
        grid3 = new char[][] {
                // Obere Grenze
                "############################".toCharArray(),
                "#............##............#".toCharArray(),
                "#.####.#####.##.#####.####.#".toCharArray(),
                "#o####.#####.##.#####.####o#".toCharArray(),
                "#.####.#####.##.#####.####.#".toCharArray(),
                "#..........................#".toCharArray(),
                "#.####.##.########.##.####.#".toCharArray(),
                "#.####.##.########.##.####.#".toCharArray(),
                "#......##....##....##......#".toCharArray(),
                "######.##### ## #####.######".toCharArray(),
                "     #.##### ## #####.#     ".toCharArray(),
                "     #.##          ##.#     ".toCharArray(),
                "     #.## ###--### ##.#     ".toCharArray(),
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
                // Untere Grenze
                "############################".toCharArray()
        };
        // ms. pac-man
        grid = new char[][] {
                // Obere Grenze
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
                "     #.##     I    ##.#     ".toCharArray(),
                "     #.## ###--### ##.#     ".toCharArray(),
                "######.## #      # ##.######".toCharArray(),
                "T     .   # CBS  #   .     T".toCharArray(),
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
                // Untere Grenze
                "############################".toCharArray()
        };

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

        return grid[gridY][gridX] == '#'; // '#' repräsentiert eine Wand
    }

    public boolean isWallOrTeleportForGhost(int gridX, int gridY) {
        // Überprüfen, ob die Koordinaten innerhalb der Grenzen des Labyrinths liegen
        if (gridX < 0 || gridX >= grid[0].length || gridY < 0 || gridY >= grid.length) {
            return true; // Position außerhalb des Labyrinths wird als Wand betrachtet
        }

        // '#'-Zeichen repräsentiert eine Wand, 'T' repräsentiert einen Teleportationspunkt
        return grid[gridY][gridX] == '#' || grid[gridY][gridX] == 'T';
    }

}
