package com.xuecheng.messagesdk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.messagesdk.config.RabbitMqConfig;
import com.xuecheng.messagesdk.mapper.MqMessageHistoryMapper;
import com.xuecheng.messagesdk.mapper.MqMessageMapper;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.model.po.MqMessageHistory;
import com.xuecheng.messagesdk.service.MqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 服务实现类
 * @author july
 */
@Slf4j
@Service
public class MqMessageServiceImpl extends ServiceImpl<MqMessageMapper, MqMessage> implements MqMessageService {

    public static final String NOT_FINISH_STATE = "0";

    public static final String FINISH_STATE = "1";

    @Resource
    MqMessageMapper mqMessageMapper;

    @Resource
    MqMessageService mqMessageService;

    @Resource
    MqMessageHistoryMapper mqMessageHistoryMapper;

    @Resource
    RabbitTemplate rabbitTemplate;

    @Override
    public MqMessage addMessage(Long id, String businessKey1, String businessKey2, String businessKey3) {
        MqMessage mqMessage = new MqMessage();
        mqMessage.setId(id);
        mqMessage.setBusinessKey1(businessKey1);
        mqMessage.setBusinessKey2(businessKey2);
        mqMessage.setBusinessKey3(businessKey3);
        boolean save = mqMessageService.saveOrUpdate(mqMessage);
        if (save) {
            rabbitTemplate.convertAndSend(RabbitMqConfig.COURSE_PUBLISH_EXCHANGE_NAME,
                    businessKey1, String.valueOf(id));
            rabbitTemplate.convertAndSend(RabbitMqConfig.COURSE_PUBLISH_EXCHANGE_NAME,
                    businessKey2, String.valueOf(id));
            return mqMessage;
        } else {
            return null;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int completed(long id) {
        MqMessage mqMessage = new MqMessage();
        //完成任务
        mqMessage.setState(FINISH_STATE);
        int update = mqMessageMapper.update(mqMessage, new LambdaQueryWrapper<MqMessage>().eq(MqMessage::getId, id));
        if (update > 0){
            mqMessage = mqMessageMapper.selectById(id);
            //添加到历史表
            MqMessageHistory mqMessageHistory = new MqMessageHistory();
            BeanUtils.copyProperties(mqMessage,mqMessageHistory);
            mqMessageHistoryMapper.insert(mqMessageHistory);
            //删除消息表
            mqMessageMapper.deleteById(id);
            return 1;
        }
        return 0;

    }

    @Override
    public int completedStageOne(long id) {
        while (NOT_FINISH_STATE.equals(getStageOne(id))) {
            MqMessage mqMessage = new MqMessage();
            //完成阶段1任务
            mqMessage.setStageState1(FINISH_STATE);
            mqMessageMapper.update(mqMessage,new LambdaQueryWrapper<MqMessage>().eq(MqMessage::getId,id));
        }
        return 1;
    }

    @Override
    public int completedStageTwo(long id) {
        MqMessage mqMessage = new MqMessage();
        //完成阶段2任务
        mqMessage.setStageState2(FINISH_STATE);
        return mqMessageMapper.update(mqMessage,new LambdaQueryWrapper<MqMessage>().eq(MqMessage::getId,id));
    }

    @Override
    public int completedStageThree(long id) {
        MqMessage mqMessage = new MqMessage();
        //完成阶段3任务
        mqMessage.setStageState3(FINISH_STATE);
        return mqMessageMapper.update(mqMessage,new LambdaQueryWrapper<MqMessage>().eq(MqMessage::getId,id));
    }

    @Override
    public int completedStageFour(long id) {
        MqMessage mqMessage = new MqMessage();
        //完成阶段4任务
        mqMessage.setStageState4(FINISH_STATE);
        return mqMessageMapper.update(mqMessage,new LambdaQueryWrapper<MqMessage>().eq(MqMessage::getId,id));
    }

    @Override
    public String getStageOne(long id) {
        return mqMessageMapper.selectById(id).getStageState1();
    }

    @Override
    public String getStageTwo(long id) {
        return mqMessageMapper.selectById(id).getStageState2();
    }

    @Override
    public String getStageThree(long id) {
        return mqMessageMapper.selectById(id).getStageState3();
    }

    @Override
    public String getStageFour(long id) {
        return mqMessageMapper.selectById(id).getStageState4();
    }

}
