package com.example.yiliaoxitong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yiliaoxitong.entity.Drug;
import com.example.yiliaoxitong.exception.BusinessException;
import com.example.yiliaoxitong.mapper.DrugMapper;
import com.example.yiliaoxitong.service.DrugService;
import org.springframework.stereotype.Service;

/**
 * 药品服务实现：按名称或编码模糊查询药品与库存。
 */
@Service
public class DrugServiceImpl extends ServiceImpl<DrugMapper, Drug> implements DrugService {

    @Override
    public IPage<Drug> page(String keyword, long pageNum, long pageSize) {
        LambdaQueryWrapper<Drug> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Drug::getDrugName, keyword)
                    .or()
                    .like(Drug::getDrugCode, keyword));
        }
        wrapper.orderByAsc(Drug::getId);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Drug detail(Long id) {
        Drug drug = getById(id);
        if (drug == null) {
            throw new BusinessException("药品不存在");
        }
        return drug;
    }
}
