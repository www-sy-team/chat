package com.luohuo.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("game_drawing_room")
public class DrawingRoom {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("room_name")
    private String roomName;

    @TableField("password")
    private String password;

    @TableField("max_players")
    private Integer maxPlayers = 8;

    @TableField("status")
    private Integer status = 0;

    @TableField("current_round")
    private Integer currentRound = 1;

    @TableField("current_drawer")
    private Long currentDrawer;

    @TableField("creator_id")
    private Long creatorId;

    @TableField("created_at")
    private Date createdAt;

    @TableField(exist = false)
    private Integer currentPlayerCount;
}