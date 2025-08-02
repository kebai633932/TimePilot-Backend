package org.cxk.infrastructure.adapter.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.cxk.infrastructure.adapter.dao.po.User;
import org.cxk.infrastructure.adapter.dao.po.VerificationCode;

/**
 * @author KJH
 * @description
 * @create 2025/8/1 10:50
 */
@Mapper
public interface IVerificationCodeDao extends BaseMapper<VerificationCode> {
}
