package org.cxk.infrastructure.adapter.dao.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author KJH
 * @description 通用验证码类，对应 verification_code 表
 * @create 2025/8/1 10:48
 */
@Data
@TableName("verification_code")
public class VerificationCode {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 业务类型：如 register、login、reset_password */
    private String bizType;

    /** 身份类型：如 email、phone、username、client_id */
    private String identityType;

    /** 身份标识，如邮箱、手机号等 */
    private String identity;

    /** 验证码内容 */
    private String code;

    /** 是否已使用（0：未使用，1：已使用） */
    private Boolean used;

    /** 过期时间 */
    private Date expireTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
