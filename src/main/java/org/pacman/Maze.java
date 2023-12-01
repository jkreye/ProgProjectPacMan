package org.pacman;

public class Maze {
    private char[][] grid;

    public Maze() {
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
                "     #.##          ##.#     ".toCharArray(),
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
}
