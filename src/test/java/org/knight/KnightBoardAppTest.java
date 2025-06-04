package org.knight;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KnightBoardAppTest {

    static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = new ObjectMapper();
    }

    // Metodo di utilità per modificare temporaneamente le environment variables
    private static void setEnv(String key, String value) {
        System.setProperty(key, value);
    }

    @Test
    void testMainApp_GenericError_InvalidBoardUrl() throws Exception {
        // URL inesistente provoca eccezione
        setEnv("BOARD_API", "http://localhost:12345/nonexistent.json");
        setEnv("COMMANDS_API", "http://localhost:12345/nonexistent.json");

        ByteArrayOutputStream errBaos = new ByteArrayOutputStream();
        PrintStream originalErr = System.out;
        System.setOut(new PrintStream(errBaos));
        try {
            KnightBoardApp.main(new String[]{});
        } finally {
            System.setErr(originalErr);
        }
        String errOut = errBaos.toString();

        assertTrue(errOut.contains("{\r\n  \"status\" : \"GENERIC_ERROR\"\r\n}\r\n"));
    }



    @Test
    void testInvalidStartPosition() throws Exception {
        KnightBoardApp.Board board = new KnightBoardApp.Board();
        board.width = 3;
        board.height = 3;
        board.obstacles = List.of(new KnightBoardApp.Point() {{ x = 0; y = 0; }});

        KnightBoardApp.Commands cmds = new KnightBoardApp.Commands();
        cmds.commands = List.of("START 0,0,SOUTH");

        String status = new KnightBoardApp.Knight(board).executeCommands(cmds);
        assertEquals("INVALID_START_POSITION", status);
    }

    @Test
    void testMoveOutOfBoard() throws Exception {
        KnightBoardApp.Board board = new KnightBoardApp.Board();
        board.width = 2;
        board.height = 2;
        board.obstacles = List.of();

        KnightBoardApp.Commands cmds = new KnightBoardApp.Commands();
        cmds.commands = List.of("START 1,1,NORTH", "MOVE 1");
        String status = new KnightBoardApp.Knight(board).executeCommands(cmds);
        assertEquals("OUT_OF_THE_BOARD", status);
    }

    @Test
    void testStopAtObstacleDuringMove() throws Exception {
        KnightBoardApp.Board board = new KnightBoardApp.Board();
        board.width = 5;
        board.height = 5;
        board.obstacles = List.of(new KnightBoardApp.Point() {{ x = 2; y = 2; }});

        KnightBoardApp.Knight knight = new KnightBoardApp.Knight(board);
        KnightBoardApp.Commands cmds = new KnightBoardApp.Commands();
        cmds.commands = List.of("START 2,0,NORTH", "MOVE 5");
        String status = knight.executeCommands(cmds);
        assertEquals("SUCCESS", status);
        // Dopo MOVE 5, si ferma sull'ostacolo in (2,2): coordinate finali (2,1)
        assertEquals(2, knight.x);
        assertEquals(1, knight.y);
    }
}
