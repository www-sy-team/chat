package com.luohuo.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("game_werewolf_role")
public class WerewolfRole {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("role_name")
    private String roleName;

    @TableField("camp")
    private Integer camp;

    @TableField("description")
    private String description;

    @TableField("icon")
    private String icon;
}