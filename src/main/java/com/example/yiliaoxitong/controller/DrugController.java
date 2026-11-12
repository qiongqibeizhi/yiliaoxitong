package com.example.yiliaoxitong.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.yiliaoxitong.common.Result;
import com.example.yiliaoxitong.entity.Drug;
import com.example.yiliaoxitong.service.DrugService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 药品目录接口：分页查询药品与库存、药品详情。
 */
@RestController
@RequestMapping("/api/drugs")
public class DrugController {

    private final DrugService drugService;

    public DrugController(DrugService drugService) {
        this.drugService = drugService;
    }

    @GetMapping
    public Result<IPage<Drug>> page(@RequestParam(required = false) String keyword,
                                    @RequestParam(defaultValue = "1") long pageNum,
                                    @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(drugService.page(keyword, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Drug> detail(@PathVariable Long id) {
        return Result.ok(drugService.detail(id));
    }
}
