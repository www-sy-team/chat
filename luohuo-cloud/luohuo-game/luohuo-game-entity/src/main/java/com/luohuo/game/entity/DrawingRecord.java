package com.luohuo.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("game_drawing_record")
public class DrawingRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("room_id")
    private Long roomId;

    @TableField("round")
    private Integer round;

    @TableField("word_id")
    private Long wordId;

    @TableField("drawer_id")
    private Long drawerId;

    @TableField("drawing_data")
    private String drawingData;

    @TableField("guess_result")
    private String guessResult;

    @TableField("created_at")
    private Date createdAt;
}