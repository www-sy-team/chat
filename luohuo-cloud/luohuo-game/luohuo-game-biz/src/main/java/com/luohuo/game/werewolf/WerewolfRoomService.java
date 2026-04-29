package com.luohuo.game.werewolf;

import com.luohuo.game.entity.WerewolfPlayer;
import com.luohuo.game.entity.WerewolfRoom;
import com.luohuo.game.mapper.WerewolfPlayerMapper;
import com.luohuo.game.mapper.WerewolfRoomMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WerewolfRoomService {

    private final WerewolfRoomMapper roomMapper;
    private final WerewolfPlayerMapper playerMapper;

    @Transactional
    public WerewolfRoom createRoom(String roomName, String password, Integer maxPlayers, Long creatorId) {
        WerewolfRoom room = new WerewolfRoom();
        room.setRoomName(roomName);
        room.setPassword(password);
        room.setMaxPlayers(maxPlayers != null ? maxPlayers : 12);
        room.setStatus(0);
        room.setCreatorId(creatorId);
        room.setCreatedAt(new Date());
        room.setUpdatedAt(new Date());

        roomMapper.insert(room);

        joinRoom(room.getId(), creatorId);

        return room;
    }

    public WerewolfRoom getRoomById(Long roomId) {
        return roomMapper.selectById(roomId);
    }

    public List<WerewolfRoom> getRoomList() {
        return roomMapper.selectList(null);
    }

    @Transactional
    public WerewolfRoom joinRoom(Long roomId, Long userId) {
        WerewolfRoom room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }

        if (room.getStatus() != 0) {
            throw new RuntimeException("房间已开始游戏");
        }

        List<WerewolfPlayer> players = playerMapper.selectByRoomId(roomId);
        if (players.size() >= room.getMaxPlayers()) {
            throw new RuntimeException("房间已满");
        }

        WerewolfPlayer existingPlayer = playerMapper.selectOne(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(WerewolfPlayer.class)
                .eq(WerewolfPlayer::getRoomId, roomId)
                .eq(WerewolfPlayer::getUserId, userId)
        );

        if (existingPlayer != null) {
            return room;
        }

        WerewolfPlayer player = new WerewolfPlayer();
        player.setRoomId(roomId);
        player.setUserId(userId);
        player.setRole(0);
        player.setStatus(0);
        playerMapper.insert(player);

        return room;
    }

    @Transactional
    public void leaveRoom(Long roomId, Long userId) {
        WerewolfRoom room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }

        WerewolfPlayer player = playerMapper.selectOne(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(WerewolfPlayer.class)
                .eq(WerewolfPlayer::getRoomId, roomId)
                .eq(WerewolfPlayer::getUserId, userId)
        );

        if (player != null) {
            playerMapper.deleteById(player.getId());
        }

        List<WerewolfPlayer> remainingPlayers = playerMapper.selectByRoomId(roomId);
        if (remainingPlayers.isEmpty()) {
            roomMapper.deleteById(roomId);
        }
    }

    public List<WerewolfPlayer> getPlayers(Long roomId) {
        return playerMapper.selectByRoomId(roomId);
    }
}