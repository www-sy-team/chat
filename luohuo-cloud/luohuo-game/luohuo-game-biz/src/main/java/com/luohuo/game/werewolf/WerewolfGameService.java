package com.luohuo.game.werewolf;

import com.luohuo.game.entity.WerewolfPlayer;
import com.luohuo.game.entity.WerewolfRole;
import com.luohuo.game.entity.WerewolfRoom;
import com.luohuo.game.mapper.WerewolfPlayerMapper;
import com.luohuo.game.mapper.WerewolfRoleMapper;
import com.luohuo.game.mapper.WerewolfRoomMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WerewolfGameService {

    private final WerewolfRoomMapper roomMapper;
    private final WerewolfPlayerMapper playerMapper;
    private final WerewolfRoleMapper roleMapper;

    private static final int[] ROLE_COUNT_12 = {4, 4, 1, 1, 1, 1};

    @Transactional
    public void startGame(Long roomId) {
        WerewolfRoom room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }

        List<WerewolfPlayer> players = playerMapper.selectByRoomId(roomId);
        if (players.size() < 6) {
            throw new RuntimeException("玩家人数不足，至少需要6人");
        }

        room.setStatus(1);
        roomMapper.updateById(room);

        assignRoles(roomId, players);
    }

    private void assignRoles(Long roomId, List<WerewolfPlayer> players) {
        List<Integer> roleList = generateRoleList(players.size());
        Collections.shuffle(roleList);

        for (int i = 0; i < players.size(); i++) {
            WerewolfPlayer player = players.get(i);
            player.setRole(roleList.get(i));
            player.setStatus(0);
            playerMapper.updateById(player);
        }
    }

    private List<Integer> generateRoleList(int playerCount) {
        List<Integer> roles = new ArrayList<>();
        
        int wolfCount = playerCount / 3;
        int villagerCount = wolfCount;
        int specialCount = playerCount - wolfCount - villagerCount;

        for (int i = 0; i < wolfCount; i++) roles.add(1);
        for (int i = 0; i < villagerCount; i++) roles.add(2);
        
        List<Integer> specialRoles = Arrays.asList(3, 4, 5, 6);
        for (int i = 0; i < specialCount && i < specialRoles.size(); i++) {
            roles.add(specialRoles.get(i));
        }

        return roles;
    }

    @Transactional
    public void nightAction(Long roomId, Long userId, String actionType, Long targetId) {
        WerewolfRoom room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }

        WerewolfPlayer actor = playerMapper.selectOne(
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.<WerewolfPlayer>query()
                .eq(WerewolfPlayer::getRoomId, roomId)
                .eq(WerewolfPlayer::getUserId, userId)
        );
        if (actor == null || actor.getStatus() != 0) {
            throw new RuntimeException("玩家不存在或已死亡");
        }

        switch (actionType.toLowerCase()) {
            case "kill":
                handleWolfKill(roomId, userId, targetId);
                break;
            case "check":
                handleSeerCheck(roomId, userId, targetId);
                break;
            case "heal":
                handleWitchHeal(roomId, userId, targetId);
                break;
            case "poison":
                handleWitchPoison(roomId, userId, targetId);
                break;
            default:
                throw new RuntimeException("无效的操作类型");
        }
    }

    private void handleWolfKill(Long roomId, Long userId, Long targetId) {
        WerewolfPlayer target = playerMapper.selectById(targetId);
        if (target != null && target.getStatus() == 0) {
            target.setStatus(1);
            playerMapper.updateById(target);
        }
    }

    private String handleSeerCheck(Long roomId, Long userId, Long targetId) {
        WerewolfPlayer target = playerMapper.selectById(targetId);
        if (target == null) {
            throw new RuntimeException("目标玩家不存在");
        }
        
        WerewolfRole role = roleMapper.selectById(target.getRole());
        if (role != null && role.getCamp() == 0) {
            return "狼人";
        }
        return "好人";
    }

    private void handleWitchHeal(Long roomId, Long userId, Long targetId) {
        WerewolfPlayer target = playerMapper.selectById(targetId);
        if (target != null && target.getStatus() == 1) {
            target.setStatus(0);
            playerMapper.updateById(target);
        }
    }

    private void handleWitchPoison(Long roomId, Long userId, Long targetId) {
        WerewolfPlayer target = playerMapper.selectById(targetId);
        if (target != null && target.getStatus() == 0) {
            target.setStatus(1);
            playerMapper.updateById(target);
        }
    }

    @Transactional
    public Map<String, Object> vote(Long roomId, Long userId, Long targetId) {
        WerewolfPlayer voter = playerMapper.selectOne(
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.<WerewolfPlayer>query()
                .eq(WerewolfPlayer::getRoomId, roomId)
                .eq(WerewolfPlayer::getUserId, userId)
        );
        if (voter == null || voter.getStatus() != 0) {
            throw new RuntimeException("玩家不存在或已死亡");
        }

        voter.setVoteTarget(targetId);
        playerMapper.updateById(voter);

        Map<String, Object> result = new HashMap<>();
        result.put("votedOut", false);
        result.put("votedPlayerId", null);

        return result;
    }

    @Transactional
    public void endGame(Long roomId) {
        WerewolfRoom room = roomMapper.selectById(roomId);
        if (room != null) {
            room.setStatus(2);
            roomMapper.updateById(room);
        }
    }

    public List<WerewolfPlayer> getPlayers(Long roomId) {
        return playerMapper.selectByRoomId(roomId);
    }

    public String getRoleName(Integer roleId) {
        WerewolfRole role = roleMapper.selectById(roleId);
        return role != null ? role.getRoleName() : "未知";
    }
}