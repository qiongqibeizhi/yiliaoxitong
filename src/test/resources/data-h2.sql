-- 测试初始数据
INSERT INTO department (dept_code, dept_name, location, phone, description, status) VALUES
('NK', '内科', '门诊楼2层东区', '0731-88880001', '内科常见病诊治', 1),
('WK', '外科', '门诊楼3层西区', '0731-88880002', '外科诊疗与手术', 1);

INSERT INTO doctor (doctor_no, name, gender, title, department_id, phone, id_card, specialty, introduction, status) VALUES
('D0001', '张明', '男', '主任医师', 1, '13800001001', '430102198001011234', '高血压、冠心病诊治', '从医25年', 1);
