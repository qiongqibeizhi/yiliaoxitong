package com.example.yiliaoxitong.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.yiliaoxitong.dto.RegistrationQuery;
import com.example.yiliaoxitong.dto.RegistrationRequest;
import com.example.yiliaoxitong.entity.Registration;

/**
 * 挂号服务：挂号事务、取消挂号与挂号记录查询。
 */
public interface RegistrationService {

    Registration register(RegistrationRequest request);

    void cancel(Long id);

    IPage<Registration> page(RegistrationQuery query);

    Registration detail(Long id);
}
