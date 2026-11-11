package com.example.yiliaoxitong.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 分页查询基类：pageNum 从 1 开始，pageSize 限制在 1~100。
 */
public class PageQuery {

    @Min(value = 1, message = "页码最小为 1")
    private long pageNum = 1;

    @Min(value = 1, message = "每页条数最小为 1")
    @Max(value = 100, message = "每页条数不能超过 100")
    private long pageSize = 10;

    public long getPageNum() {
        return pageNum;
    }

    public void setPageNum(long pageNum) {
        this.pageNum = pageNum;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }
}
