package com.example.yiliaoxitong.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.yiliaoxitong.entity.Drug;

/**
 * 药品服务：药品目录与库存查询。
 */
public interface DrugService {

    IPage<Drug> page(String keyword, long pageNum, long pageSize);

    Drug detail(Long id);
}
