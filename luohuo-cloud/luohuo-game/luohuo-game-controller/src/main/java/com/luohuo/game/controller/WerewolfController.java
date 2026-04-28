package com.luohuo.game.controller;

import com.luohuo.game.entity.WerewolfPlayer;
import com.luohuo.game.entity.WerewolfRoom;
import com.luohuo.game.werewolf.WerewolfGameService;
import com.luohuo.game.werewolf.WerewolfRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/game/werewolf")
@RequiredArgsConstructor
public class WerewolfController {

    private final WerewolfRoomService roomService;
    private final WerewolfGameService gameService;

    @PostMapping("/room/create")
    public Map<String, Object> createRoom(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String roomName = (String) request.get("roomName");
            String password = (String) request.get("password");
            Integer maxPlayers = request.get("maxPlayers") != null ? ((Number) request.get("maxPlayers")).intValue() : null;
            Long creatorId = request.get("creatorId") != null ? ((Number) request.get("creatorId")).longValue() : 1L;

            WerewolfRoom room = roomService.createRoom(roomName, password, maxPlayers, creatorId);

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
            List<WerewolfRoom> rooms = roomService.getRoomList();
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

            WerewolfRoom room = roomService.joinRoom(roomId, userId);
            List<WerewolfPlayer> players = roomService.getPlayers(roomId);

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
            List<WerewolfPlayer> players = gameService.getPlayers(roomId);

            response.put("code", 200);
            response.put("message", "success");
            response.put("data", Map.of(
                "roomId", roomId,
                "players", players
            ));
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/game/vote")
    public Map<String, Object> vote(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long roomId = ((Number) request.get("roomId")).longValue();
            Long userId = request.get("userId") != null ? ((Number) request.get("userId")).longValue() : 1L;
            Long targetPlayerId = ((Number) request.get("targetPlayerId")).longValue();

            Map<String, Object> result = gameService.vote(roomId, userId, targetPlayerId);

            response.put("code", 200);
            response.put("message", "success");
            response.put("data", result);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/game/skill")
    public Map<String, Object> useSkill(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long roomId = ((Number) request.get("roomId")).longValue();
            Long userId = request.get("userId") != null ? ((Number) request.get("userId")).longValue() : 1L;
            String skillType = (String) request.get("skillType");
            Long targetPlayerId = ((Number) request.get("targetPlayerId")).longValue();

            gameService.nightAction(roomId, userId, skillType, targetPlayerId);

            response.put("code", 200);
            response.put("message", "success");
            response.put("data", Map.of("result", "操作成功"));
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
            List<WerewolfPlayer> players = roomService.getPlayers(roomId);
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", players);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
        }
        return response;
    }
}