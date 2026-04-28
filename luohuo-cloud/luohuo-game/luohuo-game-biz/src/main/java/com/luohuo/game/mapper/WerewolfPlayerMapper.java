package com.luohuo.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luohuo.game.entity.WerewolfPlayer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WerewolfPlayerMapper extends BaseMapper<WerewolfPlayer> {

    @Select("SELECT * FROM game_werewolf_player WHERE room_id = #{roomId}")
    List<WerewolfPlayer> selectByRoomId(Long roomId);
}