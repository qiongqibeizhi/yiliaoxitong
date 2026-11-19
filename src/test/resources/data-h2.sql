-- 测试初始数据
INSERT INTO department (dept_code, dept_name, location, phone, description, status) VALUES
('NK', '内科', '门诊楼2层东区', '0731-88880001', '内科常见病诊治', 1),
('WK', '外科', '门诊楼3层西区', '0731-88880002', '外科诊疗与手术', 1);

INSERT INTO doctor (doctor_no, name, gender, title, department_id, phone, id_card, specialty, introduction, status) VALUES
('D0001', '张明', '男', '主任医师', 1, '13800001001', '430102198001011234', '高血压、冠心病诊治', '从医25年', 1);

INSERT INTO patient (patient_no, name, gender, id_card, phone, birth_date, address) VALUES
('P0001', '刘洋', '男', '430102199003031234', '13900002001', '1990-03-03', '长沙市岳麓区'),
('P0002', '周婷', '女', '43010219920505122X', '13900002002', '1992-05-05', '长沙市芙蓉区');

-- 药品库存：设置不同库存量，便于验证扣减与“库存不足回滚”
INSERT INTO drug (drug_code, drug_name, spec, unit, price, stock, status) VALUES
('DRG001', '阿莫西林胶囊', '0.5g*24粒', '盒', 12.60, 100, 1),
('DRG002', '布洛芬缓释胶囊', '0.3g*20粒', '盒', 18.80, 5, 1),
('DRG003', '维生素C片', '0.1g*100片', '瓶', 6.00, 200, 1),
('DRG019', '甲基苯丙胺', '50mg*1支', '支', 0.00, 0, 0); -- 教学演示占位

-- 实验四：三角色演示账号，口令均为 123456
INSERT INTO sys_user (username, password, role, real_name, patient_id, doctor_id, status) VALUES
('admin', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'ADMIN', '系统管理员', NULL, NULL, 1),
('doctor1', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'DOCTOR', '张明', NULL, 1, 1),
('patient1', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'PATIENT', '刘洋', 1, NULL, 1);
