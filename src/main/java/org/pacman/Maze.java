/* Maze.java */

package org.pacman;

import java.awt.*;

public class Maze {
    private char[][] grid;
    private char[][] grid2;
    private static int cellSize;

    public Maze() {
        grid2 = new char[][] {
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
                "      .   #      #   .      ".toCharArray(),
                "######.## #      # ##.######".toCharArray(),
                "     #.## ######## ##.#     ".toCharArray(),
                "     #.##    P     ##.#     ".toCharArray() // 'P' als Startpunkt
        };
        grid = new char[][] {
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
                "      .   #      #   .      ".toCharArray(),
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

    public static int calculateStartX(int panelWidth, int mazeWidth) {
        return (panelWidth - mazeWidth) / 2;
    }

    public static int calculateStartY(int panelHeight, int mazeHeight, int paddingTop) {
        return paddingTop + (panelHeight - mazeHeight - paddingTop) / 2;
    }
}
