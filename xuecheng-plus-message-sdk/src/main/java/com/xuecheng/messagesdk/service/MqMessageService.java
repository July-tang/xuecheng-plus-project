package com.xuecheng.messagesdk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.messagesdk.model.po.MqMessage;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author itcast
 * @since 2022-09-21
 */
public interface MqMessageService extends IService<MqMessage> {

    /**
     * 添加消息
     *
     * @param messageType 消息类型
     * @param businessKey1 业务id
     * @param businessKey2 业务id
     * @param businessKey3 业务id
     * @return com.xuecheng.messagesdk.model.po.MqMessage 消息内容
     */
    MqMessage addMessage(String messageType, String businessKey1, String businessKey2, String businessKey3);

    /**
     * 标记发送失败的消息
     *
     * @param id 消息Id
     * @return 处理结果
     */
    boolean failMessage(String id);

    /**
     * 完成全部任务
     *
     * @param id 消息id
     * @return int 更新成功：1
     */
    int completed(long id);

    /**
     * 完成任务1
     *
     * @param id 消息id
     * @return int 更新成功：1
     */
    int completedStageOne(long id);

    int completedStageTwo(long id);

    int completedStageThree(long id);

    int completedStageFour(long id);

    /**
     * 查询阶段状态
     *
     * @param id 消息id
     * @return int
     */
    String getStageOne(long id);

    String getStageTwo(long id);

    String getStageThree(long id);

    String getStageFour(long id);

}
