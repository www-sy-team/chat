package com.luohuo.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("game_drawing_player")
public class DrawingPlayer {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("room_id")
    private Long roomId;

    @TableField("user_id")
    private Long userId;

    @TableField("score")
    private Integer score = 0;

    @TableField("is_drawer")
    private Boolean isDrawer = false;

    @TableField("created_at")
    private Date createdAt;

    @TableField(exist = false)
    private String nickname;

    @TableField(exist = false)
    private String avatar;
}