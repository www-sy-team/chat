package com.luohuo.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("game_drawing_word")
public class DrawingWord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("word")
    private String word;

    @TableField("category")
    private String category;

    @TableField("difficulty")
    private Integer difficulty = 1;

    @TableField("created_at")
    private Date createdAt;
}