package com.xuecheng.search.dto;

import com.xuecheng.base.model.PageResult;
import lombok.*;

import java.util.List;

/**
 * @author july
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class SearchPageResultDto<T> extends PageResult<T> {

    /**
     * 大分类列表
     */
    List<String> mtList;
    /**
     * 小分类列表
     */
    List<String> stList;

    public SearchPageResultDto(List<T> items, long counts, long page, long pageSize) {
        super(items, counts, page, pageSize);
    }

}
