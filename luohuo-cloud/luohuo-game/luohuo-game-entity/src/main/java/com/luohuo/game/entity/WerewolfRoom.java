package com.luohuo.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("game_werewolf_room")
public class WerewolfRoom {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("room_name")
    private String roomName;

    @TableField("password")
    private String password;

    @TableField("max_players")
    private Integer maxPlayers = 12;

    @TableField("status")
    private Integer status = 0;

    @TableField("creator_id")
    private Long creatorId;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;

    @TableField(exist = false)
    private Integer currentPlayerCount;
}