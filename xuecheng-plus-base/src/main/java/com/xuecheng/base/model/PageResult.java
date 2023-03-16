package com.xuecheng.base.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * 分页查询结果模型类
 *
 * @author July
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    /**
     * 数据列表
     */
    private List<T> items;

    /**
     * 总记录数
     */
    private long counts;

    /**
     * 当前页码
     */
    private long page;

    /**
     * 每页记录数
     */
    private long pageSize;
}
