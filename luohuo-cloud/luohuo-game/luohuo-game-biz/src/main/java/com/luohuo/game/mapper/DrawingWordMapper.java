package com.luohuo.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luohuo.game.entity.DrawingWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DrawingWordMapper extends BaseMapper<DrawingWord> {

    @Select("SELECT * FROM game_drawing_word ORDER BY RAND() LIMIT 1")
    DrawingWord selectRandomWord();
}