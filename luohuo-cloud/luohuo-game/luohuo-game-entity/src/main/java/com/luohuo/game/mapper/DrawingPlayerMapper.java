package com.luohuo.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luohuo.game.entity.DrawingPlayer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DrawingPlayerMapper extends BaseMapper<DrawingPlayer> {

    @Select("SELECT p.* FROM game_drawing_player p WHERE p.room_id = #{roomId} ORDER BY p.score DESC")
    List<DrawingPlayer> selectByRoomIdOrderByScore(@Param("roomId") Long roomId);
}