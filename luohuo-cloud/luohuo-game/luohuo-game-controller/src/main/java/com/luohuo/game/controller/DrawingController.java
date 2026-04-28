package com.luohuo.game.controller;

import com.luohuo.game.entity.DrawingPlayer;
import com.luohuo.game.entity.DrawingRoom;
import com.luohuo.game.entity.DrawingWord;
import com.luohuo.game.drawing.DrawingGameService;
import com.luohuo.game.drawing.DrawingRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/game/drawing")
@RequiredArgsConstructor
public class DrawingController {

    private final DrawingRoomService roomService;
    private final DrawingGameService gameService;

    @PostMapping("/room/create")
    public Map<String, Object> createRoom(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String roomName = (String) request.get("roomName");
            String password = (String) request.get("password");
            Integer maxPlayers = request.get("maxPlayers") != null ? ((Number) request.get("maxPlayers")).intValue() : null;
            Long creatorId = request.get("creatorId") != null ? ((Number) request.get("creatorId")).longValue() : 1L;

            DrawingRoom room = roomService.createRoom(roomName, password, maxPlayers, creatorId);

            response.put("code", 200);
            response.put("message", "success");
            response.put("data", Map.of(
                "roomId", room.getId(),
                "roomName", room.getRoomName()
            ));
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @GetMapping("/room/list")
    public Map<String, Object> getRoomList() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<DrawingRoom> rooms = roomService.getRoomList();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", rooms);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/room/join")
    public Map<String, Object> joinRoom(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long roomId = ((Number) request.get("roomId")).longValue();
            Long userId = request.get("userId") != null ? ((Number) request.get("userId")).longValue() : 1L;

            DrawingRoom room = roomService.joinRoom(roomId, userId);
            List<DrawingPlayer> players = roomService.getPlayers(roomId);

            response.put("code", 200);
            response.put("message", "success");
            response.put("data", Map.of(
                "roomId", room.getId(),
                "players", players
            ));
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/room/leave")
    public Map<String, Object> leaveRoom(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long roomId = ((Number) request.get("roomId")).longValue();
            Long userId = request.get("userId") != null ? ((Number) request.get("userId")).longValue() : 1L;

            roomService.leaveRoom(roomId, userId);

            response.put("code", 200);
            response.put("message", "success");
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/game/start")
    public Map<String, Object> startGame(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long roomId = ((Number) request.get("roomId")).longValue();

            gameService.startGame(roomId);

            response.put("code", 200);
            response.put("message", "success");
            response.put("data", Map.of("roomId", roomId));
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @GetMapping("/game/word")
    public Map<String, Object> getWord(@RequestParam Long roomId) {
        Map<String, Object> response = new HashMap<>();
        try {
            DrawingWord word = gameService.getRandomWord(roomId);

            response.put("code", 200);
            response.put("message", "success");
            response.put("data", word);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/game/guess")
    public Map<String, Object> guessWord(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long roomId = ((Number) request.get("roomId")).longValue();
            Long userId = request.get("userId") != null ? ((Number) request.get("userId")).longValue() : 1L;
            String guessWord = (String) request.get("guessWord");

            boolean isCorrect = gameService.guessWord(roomId, userId, guessWord);
            List<DrawingPlayer> ranking = gameService.getRanking(roomId);

            response.put("code", 200);
            response.put("message", "success");
            response.put("data", Map.of(
                "isCorrect", isCorrect,
                "ranking", ranking
            ));
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/game/draw")
    public Map<String, Object> syncDrawing(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long roomId = ((Number) request.get("roomId")).longValue();
            String drawingData = (String) request.get("drawingData");

            response.put("code", 200);
            response.put("message", "success");
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/game/end")
    public Map<String, Object> endGame(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long roomId = ((Number) request.get("roomId")).longValue();

            gameService.endGame(roomId);

            response.put("code", 200);
            response.put("message", "success");
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @GetMapping("/room/{roomId}/players")
    public Map<String, Object> getPlayers(@PathVariable Long roomId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<DrawingPlayer> players = roomService.getPlayers(roomId);
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", players);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @GetMapping("/game/{roomId}/ranking")
    public Map<String, Object> getRanking(@PathVariable Long roomId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<DrawingPlayer> ranking = gameService.getRanking(roomId);
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", ranking);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
        }
        return response;
    }
}