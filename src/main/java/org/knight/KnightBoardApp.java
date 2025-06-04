package org.knight;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class KnightBoardApp {

    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> output = new LinkedHashMap<>();

        try{
            String boardApi = System.getenv("BOARD_API");
            String commandsApi = System.getenv("COMMANDS_API");

            Board board = mapper.readValue(new URL(boardApi), Board.class);
            Commands commands = mapper.readValue(new URL(commandsApi), Commands.class);

            Knight knight = new Knight(board);
            String status = knight.executeCommands(commands);

            if ("SUCCESS".equals(status)) {
                Map<String, Object> position = new LinkedHashMap<>();
                position.put("x", knight.x);
                position.put("y", knight.y);
                position.put("direction", knight.direction);
                output.put("position", position);
            }
            output.put("status", status);

        } catch (Exception e) {
            output.put("status", "GENERIC_ERROR");
        }
        String jsonOutput = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(output);
        System.out.println(jsonOutput);
    }

    static class Board {
        public int width;
        public int height;
        public List<Point> obstacles;
    }

    static class Point {
        public int x;
        public int y;
    }

    static class Commands {
        public List<String> commands;
    }

    static class Knight {
        int x, y;
        String direction;
        Board board;
        Set<String> obstacleSet;

        Knight(Board board) {
            this.board = board;
            obstacleSet = new HashSet<>();
            for (Point p : board.obstacles) {
                obstacleSet.add(p.x + "," + p.y);
            }
        }

        String executeCommands(Commands cmds) {
            for (String cmd : cmds.commands) {
                    if (cmd.startsWith("START")) {
                        String[] parts = cmd.split(" ")[1].split(",");
                        x = Integer.parseInt(parts[0]);
                        y = Integer.parseInt(parts[1]);
                        direction = parts[2];
                        if (!validPosition(x, y)) return "INVALID_START_POSITION";
                    } else if (cmd.startsWith("ROTATE")) {
                        direction = cmd.split(" ")[1];
                    } else if (cmd.startsWith("MOVE")) {
                        int steps = Integer.parseInt(cmd.split(" ")[1]);
                        for (int i = 0; i < steps; i++) {
                            int nextX = x, nextY = y;
                            switch (direction) {
                                case "NORTH": nextY++; break;
                                case "SOUTH": nextY--; break;
                                case "EAST": nextX++; break;
                                case "WEST": nextX--; break;
                            }
                            if (nextX < 0 || nextX >= board.width || nextY < 0 || nextY >= board.height)
                                return "OUT_OF_THE_BOARD";
                            if (obstacleSet.contains(nextX + "," + nextY)) break;
                            x = nextX;
                            y = nextY;
                        }
                    }
            }
            return "SUCCESS";
        }

        boolean validPosition(int x, int y) {
            return (x >= 0 && x < board.width && y >= 0 && y < board.height && !obstacleSet.contains(x + "," + y));
        }
    }
}
