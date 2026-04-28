package com.luohuo.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luohuo.game.entity.DrawingPlayer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DrawingPlayerMapper extends BaseMapper<DrawingPlayer> {

    @Select("SELECT * FROM game_drawing_player WHERE room_id = #{roomId}")
    List<DrawingPlayer> selectByRoomId(Long roomId);

    @Select("SELECT * FROM game_drawing_player WHERE room_id = #{roomId} ORDER BY score DESC")
    List<DrawingPlayer> selectByRoomIdOrderByScore(Long roomId);
}