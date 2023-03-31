# xuecheng-plus-project
原型为黑马学成在线项目

本人修改如下: 

1. 弃用xxl-job框架，改用rabbitmq异步处理，同时消息sdk集成rabbitmq实现。
2. 完善模块接口，如content模块的课程计划上下移接口，auth模块的学生注册，修改密码接口。
3. 视频文件格式转换由消息队列异步进行, 免去反复从minio文件系统下载分块的步骤。
