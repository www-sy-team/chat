package com.luohuo.game.drawing;

import com.luohuo.game.entity.DrawingPlayer;
import com.luohuo.game.entity.DrawingRoom;
import com.luohuo.game.mapper.DrawingPlayerMapper;
import com.luohuo.game.mapper.DrawingRoomMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrawingRoomService {

    private final DrawingRoomMapper roomMapper;
    private final DrawingPlayerMapper playerMapper;

    @Transactional
    public DrawingRoom createRoom(String roomName, String password, Integer maxPlayers, Long creatorId) {
        DrawingRoom room = new DrawingRoom();
        room.setRoomName(roomName);
        room.setPassword(password);
        room.setMaxPlayers(maxPlayers != null ? maxPlayers : 8);
        room.setStatus(0);
        room.setCurrentRound(1);
        room.setCreatorId(creatorId);
        room.setCreatedAt(new Date());

        roomMapper.insert(room);

        joinRoom(room.getId(), creatorId);

        return room;
    }

    public DrawingRoom getRoomById(Long roomId) {
        return roomMapper.selectById(roomId);
    }

    public List<DrawingRoom> getRoomList() {
        return roomMapper.selectList(null);
    }

    @Transactional
    public DrawingRoom joinRoom(Long roomId, Long userId) {
        DrawingRoom room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }

        if (room.getStatus() != 0) {
            throw new RuntimeException("房间已开始游戏");
        }

        List<DrawingPlayer> players = playerMapper.selectByRoomId(roomId);
        if (players.size() >= room.getMaxPlayers()) {
            throw new RuntimeException("房间已满");
        }

        DrawingPlayer existingPlayer = playerMapper.selectOne(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(DrawingPlayer.class)
                .eq(DrawingPlayer::getRoomId, roomId)
                .eq(DrawingPlayer::getUserId, userId)
        );

        if (existingPlayer != null) {
            return room;
        }

        DrawingPlayer player = new DrawingPlayer();
        player.setRoomId(roomId);
        player.setUserId(userId);
        player.setScore(0);
        player.setIsDrawer(false);
        playerMapper.insert(player);

        return room;
    }

    @Transactional
    public void leaveRoom(Long roomId, Long userId) {
        DrawingRoom room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }

        DrawingPlayer player = playerMapper.selectOne(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(DrawingPlayer.class)
                .eq(DrawingPlayer::getRoomId, roomId)
                .eq(DrawingPlayer::getUserId, userId)
        );

        if (player != null) {
            playerMapper.deleteById(player.getId());
        }

        List<DrawingPlayer> remainingPlayers = playerMapper.selectByRoomId(roomId);
        if (remainingPlayers.isEmpty()) {
            roomMapper.deleteById(roomId);
        }
    }

    public List<DrawingPlayer> getPlayers(Long roomId) {
        return playerMapper.selectByRoomId(roomId);
    }
}