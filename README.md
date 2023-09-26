# xuecheng-plus-project
原型为黑马学成在线项目，B站视频地址：https://www.bilibili.com/video/BV1j8411N7Bm/

本人修改如下: 

1. 完善模块接口，如content模块的课程计划上下移接口，auth模块的学生注册，修改密码接口。
2. 视频文件格式转换由消息队列异步进行, 免去反复从minio文件系统下载分块的步骤。
3. 消息发送使用TransactionSynchronization.#afterCompletion进行发送，保证事务提交成功后才发送消息。
4. 优化缓存三大问题的处理方式

