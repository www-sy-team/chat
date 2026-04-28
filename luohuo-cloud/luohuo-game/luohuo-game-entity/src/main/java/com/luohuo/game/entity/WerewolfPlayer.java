package com.luohuo.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("game_werewolf_player")
public class WerewolfPlayer {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("room_id")
    private Long roomId;

    @TableField("user_id")
    private Long userId;

    @TableField("role")
    private Integer role = 0;

    @TableField("status")
    private Integer status = 0;

    @TableField("vote_target")
    private Long voteTarget;

    @TableField("is_speaking")
    private Boolean isSpeaking = false;

    @TableField("created_at")
    private Date createdAt;

    @TableField(exist = false)
    private String nickname;

    @TableField(exist = false)
    private String avatar;
}