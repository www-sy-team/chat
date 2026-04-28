package com.luohuo.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luohuo.game.entity.WerewolfPlayer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WerewolfPlayerMapper extends BaseMapper<WerewolfPlayer> {

    @Select("SELECT p.* FROM game_werewolf_player p WHERE p.room_id = #{roomId} ORDER BY p.created_at")
    List<WerewolfPlayer> selectByRoomId(@Param("roomId") Long roomId);

    @Select("SELECT COUNT(*) FROM game_werewolf_player WHERE room_id = #{roomId} AND status = 0")
    int countAlivePlayers(@Param("roomId") Long roomId);

    @Select("SELECT COUNT(*) FROM game_werewolf_player WHERE room_id = #{roomId} AND role = #{role} AND status = 0")
    int countAlivePlayersByRole(@Param("roomId") Long roomId, @Param("role") Integer role);
}