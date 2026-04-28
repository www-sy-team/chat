package com.luohuo.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luohuo.game.entity.DrawingRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DrawingRoomMapper extends BaseMapper<DrawingRoom> {

    @Select("SELECT r.*, COUNT(p.id) as current_player_count FROM game_drawing_room r " +
            "LEFT JOIN game_drawing_player p ON r.id = p.room_id " +
            "WHERE r.status = 0 GROUP BY r.id ORDER BY r.created_at DESC")
    List<DrawingRoom> listRoomsWithPlayerCount();
}