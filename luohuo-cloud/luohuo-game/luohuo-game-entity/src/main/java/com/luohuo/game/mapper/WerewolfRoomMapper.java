package com.luohuo.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luohuo.game.entity.WerewolfRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WerewolfRoomMapper extends BaseMapper<WerewolfRoom> {

    @Select("SELECT r.*, COUNT(p.id) as current_player_count FROM game_werewolf_room r " +
            "LEFT JOIN game_werewolf_player p ON r.id = p.room_id " +
            "WHERE r.status = 0 GROUP BY r.id ORDER BY r.created_at DESC")
    List<WerewolfRoom> listRoomsWithPlayerCount();

    @Select("SELECT COUNT(*) FROM game_werewolf_player WHERE room_id = #{roomId}")
    int countPlayersByRoomId(@Param("roomId") Long roomId);
}