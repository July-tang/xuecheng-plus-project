package com.xuecheng.search.service;

/**
 * 课程索引service
 *
 * @author july
 */
public interface IndexService {

    /**
     * 添加索引
     *
     * @param id        主键
     * @param object    索引对象
     * @return Boolean true表示成功, false失败
     */
    Boolean addCourseIndex(String id, Object object);


    /**
     * 更新索引
     *
     * @param id        主键
     * @param object    索引对象
     * @return Boolean true表示成功, false失败
     */
    Boolean updateCourseIndex(String id, Object object);

    /**
     * 删除索引
     *
     * @param id        主键
     * @return java.lang.Boolean
     */
    Boolean deleteCourseIndex(String id);

}
