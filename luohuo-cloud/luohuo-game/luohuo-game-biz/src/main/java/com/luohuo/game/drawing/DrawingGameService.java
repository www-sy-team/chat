package com.luohuo.game.drawing;

import com.luohuo.game.entity.DrawingPlayer;
import com.luohuo.game.entity.DrawingRoom;
import com.luohuo.game.entity.DrawingWord;
import com.luohuo.game.mapper.DrawingPlayerMapper;
import com.luohuo.game.mapper.DrawingRoomMapper;
import com.luohuo.game.mapper.DrawingWordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrawingGameService {

    private final DrawingRoomMapper roomMapper;
    private final DrawingPlayerMapper playerMapper;
    private final DrawingWordMapper wordMapper;

    @Transactional
    public void startGame(Long roomId) {
        DrawingRoom room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }

        List<DrawingPlayer> players = playerMapper.selectByRoomIdOrderByScore(roomId);
        if (players.size() < 2) {
            throw new RuntimeException("玩家人数不足，至少需要2人");
        }

        room.setStatus(1);
        roomMapper.updateById(room);

        setFirstDrawer(roomId, players);
    }

    private void setFirstDrawer(Long roomId, List<DrawingPlayer> players) {
        if (!players.isEmpty()) {
            DrawingPlayer firstPlayer = players.get(0);
            firstPlayer.setIsDrawer(true);
            
            DrawingRoom room = roomMapper.selectById(roomId);
            room.setCurrentDrawer(firstPlayer.getUserId());
            roomMapper.updateById(room);
            
            playerMapper.updateById(firstPlayer);
        }
    }

    public DrawingWord getRandomWord(Long roomId) {
        return wordMapper.selectRandomWord();
    }

    @Transactional
    public boolean guessWord(Long roomId, Long userId, String guessWord) {
        DrawingRoom room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }

        DrawingPlayer player = playerMapper.selectOne(
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.<DrawingPlayer>query()
                .eq(DrawingPlayer::getRoomId, roomId)
                .eq(DrawingPlayer::getUserId, userId)
        );
        if (player == null) {
            throw new RuntimeException("玩家不在房间内");
        }

        String currentWord = getCurrentWord(roomId);
        
        if (currentWord != null && currentWord.equalsIgnoreCase(guessWord)) {
            int score = calculateScore(room.getCurrentRound());
            player.setScore(player.getScore() + score);
            playerMapper.updateById(player);
            
            nextRound(roomId);
            return true;
        }
        
        return false;
    }

    private String getCurrentWord(Long roomId) {
        return "";
    }

    private int calculateScore(Integer round) {
        return 100 * round;
    }

    @Transactional
    public void nextRound(Long roomId) {
        DrawingRoom room = roomMapper.selectById(roomId);
        List<DrawingPlayer> players = playerMapper.selectByRoomIdOrderByScore(roomId);

        int currentDrawerIndex = -1;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getUserId().equals(room.getCurrentDrawer())) {
                currentDrawerIndex = i;
                break;
            }
        }

        if (currentDrawerIndex >= 0) {
            DrawingPlayer currentDrawer = players.get(currentDrawerIndex);
            currentDrawer.setIsDrawer(false);
            playerMapper.updateById(currentDrawer);

            int nextIndex = (currentDrawerIndex + 1) % players.size();
            DrawingPlayer nextDrawer = players.get(nextIndex);
            nextDrawer.setIsDrawer(true);
            playerMapper.updateById(nextDrawer);

            room.setCurrentDrawer(nextDrawer.getUserId());
            room.setCurrentRound(room.getCurrentRound() + 1);
            roomMapper.updateById(room);
        }
    }

    @Transactional
    public void endGame(Long roomId) {
        DrawingRoom room = roomMapper.selectById(roomId);
        if (room != null) {
            room.setStatus(2);
            roomMapper.updateById(room);
        }
    }

    public List<DrawingPlayer> getRanking(Long roomId) {
        return playerMapper.selectByRoomIdOrderByScore(roomId);
    }
}