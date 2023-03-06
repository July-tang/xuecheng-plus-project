package com.xuecheng.messagesdk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.messagesdk.model.po.MqMessage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author july
 */
public interface MqMessageMapper extends BaseMapper<MqMessage> {

}
