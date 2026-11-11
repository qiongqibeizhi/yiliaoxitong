-- ============================================================
-- 医疗系统数据库建表脚本（实验二）
-- 数据库：yiliaoxitong   字符集：utf8mb4   存储引擎：InnoDB
-- 设计遵循数据库第三范式（3NF），主键、非空、唯一、外键约束完整
-- 执行方式：mysql -uroot -p < medical_db.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS yiliaoxitong
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE yiliaoxitong;

-- ------------------------------------------------------------
-- 1. 科室表 department
--    科室为独立实体，医生、挂号均通过外键引用，避免冗余存储科室名称
-- ------------------------------------------------------------
DROP TABLE IF EXISTS prescription_item;
DROP TABLE IF EXISTS prescription;
DROP TABLE IF EXISTS registration;
DROP TABLE IF EXISTS doctor;
DROP TABLE IF EXISTS patient;
DROP TABLE IF EXISTS department;

CREATE TABLE department (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '科室主键',
    dept_code   VARCHAR(20)  NOT NULL                COMMENT '科室编码，唯一',
    dept_name   VARCHAR(50)  NOT NULL                COMMENT '科室名称',
    location    VARCHAR(100) NULL                    COMMENT '科室位置',
    phone       VARCHAR(20)  NULL                    COMMENT '科室联系电话',
    description VARCHAR(255) NULL                    COMMENT '科室简介',
    status      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1启用 0停用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dept_code (dept_code),
    KEY idx_dept_name (dept_name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '科室信息表';

-- ------------------------------------------------------------
-- 2. 医生表 doctor
--    医生与科室为多对一关系，department_id 外键引用 department(id)
--    手机号、身份证号在医生范围内唯一，防止重复建档
-- ------------------------------------------------------------
CREATE TABLE doctor (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '医生主键',
    doctor_no     VARCHAR(20)  NOT NULL                COMMENT '医生工号，唯一',
    name          VARCHAR(30)  NOT NULL                COMMENT '医生姓名',
    gender        CHAR(1)      NOT NULL                COMMENT '性别：男/女',
    title         VARCHAR(20)  NULL                    COMMENT '职称：主任医师/副主任医师/主治医师/住院医师',
    department_id BIGINT       NOT NULL                COMMENT '所属科室外键',
    phone         VARCHAR(20)  NOT NULL                COMMENT '联系电话，唯一',
    id_card       VARCHAR(18)  NULL                    COMMENT '身份证号，唯一',
    specialty     VARCHAR(100) NULL                    COMMENT '擅长领域',
    introduction  VARCHAR(500) NULL                    COMMENT '医生简介',
    status        TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1在岗 0离岗',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_doctor_no (doctor_no),
    UNIQUE KEY uk_doctor_phone (phone),
    UNIQUE KEY uk_doctor_id_card (id_card),
    KEY idx_doctor_dept (department_id),
    CONSTRAINT fk_doctor_dept FOREIGN KEY (department_id)
        REFERENCES department (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_doctor_gender CHECK (gender IN ('男', '女'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '医生信息表';

-- ------------------------------------------------------------
-- 3. 患者表 patient
--    身份证号唯一标识患者，手机号必填用于就诊通知
-- ------------------------------------------------------------
CREATE TABLE patient (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '患者主键',
    patient_no  VARCHAR(20)  NOT NULL                COMMENT '患者编号，唯一',
    name        VARCHAR(30)  NOT NULL                COMMENT '患者姓名',
    gender      CHAR(1)      NOT NULL                COMMENT '性别：男/女',
    id_card     VARCHAR(18)  NOT NULL                COMMENT '身份证号，唯一',
    phone       VARCHAR(20)  NOT NULL                COMMENT '联系电话',
    birth_date  DATE         NULL                    COMMENT '出生日期',
    address     VARCHAR(200) NULL                    COMMENT '联系地址',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_patient_no (patient_no),
    UNIQUE KEY uk_patient_id_card (id_card),
    KEY idx_patient_name (name),
    KEY idx_patient_phone (phone),
    CONSTRAINT ck_patient_gender CHECK (gender IN ('男', '女'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '患者信息表';

-- ------------------------------------------------------------
-- 4. 挂号记录表 registration
--    挂号是患者、医生、科室之间的业务联系表，只保存外键与业务字段
--    (doctor_id, visit_date, time_slot) 唯一，保证同一医生同一时段号源不重复
-- ------------------------------------------------------------
CREATE TABLE registration (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '挂号主键',
    reg_no        VARCHAR(32)  NOT NULL                COMMENT '挂号单号，唯一',
    patient_id    BIGINT       NOT NULL                COMMENT '患者外键',
    doctor_id     BIGINT       NOT NULL                COMMENT '医生外键',
    department_id BIGINT       NOT NULL                COMMENT '科室外键',
    visit_date    DATE         NOT NULL                COMMENT '就诊日期',
    time_slot     VARCHAR(20)  NOT NULL                COMMENT '就诊时段：上午/下午/具体时间',
    reg_fee       DECIMAL(8,2) NOT NULL DEFAULT 0.00   COMMENT '挂号费',
    status        TINYINT      NOT NULL DEFAULT 0      COMMENT '状态：0待就诊 1已就诊 2已取消',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_reg_no (reg_no),
    UNIQUE KEY uk_doctor_slot (doctor_id, visit_date, time_slot),
    KEY idx_reg_patient (patient_id),
    KEY idx_reg_dept (department_id),
    KEY idx_reg_date (visit_date),
    CONSTRAINT fk_reg_patient FOREIGN KEY (patient_id)
        REFERENCES patient (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_reg_doctor FOREIGN KEY (doctor_id)
        REFERENCES doctor (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_reg_dept FOREIGN KEY (department_id)
        REFERENCES department (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_reg_status CHECK (status IN (0, 1, 2))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '挂号记录表';

-- ------------------------------------------------------------
-- 5. 电子处方表 prescription（处方主表：一次挂号对应一张处方）
-- ------------------------------------------------------------
CREATE TABLE prescription (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '处方主键',
    presc_no        VARCHAR(32)  NOT NULL                COMMENT '处方编号，唯一',
    registration_id BIGINT       NOT NULL                COMMENT '挂号记录外键，唯一',
    patient_id      BIGINT       NOT NULL                COMMENT '患者外键',
    doctor_id       BIGINT       NOT NULL                COMMENT '开方医生外键',
    diagnosis       VARCHAR(200) NOT NULL                COMMENT '诊断结论',
    presc_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开方时间',
    status          TINYINT      NOT NULL DEFAULT 0      COMMENT '状态：0待缴费 1已发药 2已作废',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_presc_no (presc_no),
    UNIQUE KEY uk_presc_reg (registration_id),
    KEY idx_presc_patient (patient_id),
    KEY idx_presc_doctor (doctor_id),
    CONSTRAINT fk_presc_reg FOREIGN KEY (registration_id)
        REFERENCES registration (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_presc_patient FOREIGN KEY (patient_id)
        REFERENCES patient (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_presc_doctor FOREIGN KEY (doctor_id)
        REFERENCES doctor (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_presc_status CHECK (status IN (0, 1, 2))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '电子处方主表';

-- ------------------------------------------------------------
-- 6. 处方明细表 prescription_item
--    一张处方可开多种药品，处方明细必须独立成表才满足第一范式（无重复组）
-- ------------------------------------------------------------
CREATE TABLE prescription_item (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '明细主键',
    prescription_id BIGINT       NOT NULL                COMMENT '处方外键',
    drug_name       VARCHAR(100) NOT NULL                COMMENT '药品名称',
    spec            VARCHAR(50)  NULL                    COMMENT '规格',
    quantity        INT          NOT NULL DEFAULT 1      COMMENT '数量',
    unit            VARCHAR(10)  NULL                    COMMENT '单位',
    dosage          VARCHAR(50)  NULL                    COMMENT '用法用量',
    price           DECIMAL(8,2) NOT NULL DEFAULT 0.00   COMMENT '单价',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_item_presc (prescription_id),
    CONSTRAINT fk_item_presc FOREIGN KEY (prescription_id)
        REFERENCES prescription (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT ck_item_quantity CHECK (quantity > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '处方明细表';

-- ------------------------------------------------------------
-- 初始化样例数据（覆盖 10 个科室、20 名医生、15 名患者、24 条挂号、10 张处方）
-- 所有身份证号均按 GB 11643-1999 计算校验位，手机号符合 11 位号段规则
-- ------------------------------------------------------------
INSERT INTO department (dept_code, dept_name, location, phone, description, status) VALUES
('NK', '内科', '门诊楼2层东区', '0731-88880001', '诊治呼吸、消化、心血管、内分泌等内科常见病与慢性病管理', 1),
('WK', '外科', '门诊楼3层西区', '0731-88880002', '开展普外、肝胆、胃肠等外科诊疗与围手术期管理', 1),
('FK', '妇产科', '门诊楼4层', '0731-88880003', '提供妇科炎症、月经失调、孕期保健与产后康复服务', 1),
('EK', '儿科', '门诊楼1层东区', '0731-88880004', '诊治儿童呼吸道感染、发热、腹泻及生长发育问题', 1),
('YK', '眼科', '门诊楼5层东区', '0731-88880005', '开展视力检查、屈光矫治、白内障与眼底病诊疗', 1),
('EBH', '耳鼻喉科', '门诊楼5层西区', '0731-88880006', '诊治鼻炎、中耳炎、咽喉疾病及听力障碍', 1),
('KQ', '口腔科', '门诊楼3层东区', '0731-88880007', '提供龋齿充填、牙周治疗、拔牙与口腔修复', 1),
('PF', '皮肤科', '门诊楼4层西区', '0731-88880008', '诊治湿疹、痤疮、真菌感染及过敏性皮肤病', 1),
('GK', '骨科', '门诊楼2层西区', '0731-88880009', '开展骨折复位、关节镜、颈肩腰腿痛诊疗', 1),
('JZ', '急诊科', '门诊楼1层西侧', '0731-88880010', '24小时接诊急危重症，承担院前急救与分流', 1);

INSERT INTO doctor (doctor_no, name, gender, title, department_id, phone, id_card, specialty, introduction, status) VALUES
('D0001', '张明', '男', '主任医师', 1, '13800001001', '430102196503050014', '高血压、冠心病、心律失常', '内科主任，从事心血管临床工作25年，年门诊量逾6000人次', 1),
('D0002', '李红', '女', '副主任医师', 1, '13800001002', '43010219660612002X', '糖尿病、甲状腺疾病、血脂异常', '内分泌专业，主持省级课题1项，发表核心期刊论文10余篇', 1),
('D0003', '王强', '男', '主治医师', 2, '13800001003', '430102196709180017', '腹腔镜胆囊切除、疝修补', '普外专业，独立完成腹腔镜手术800余例', 1),
('D0004', '赵敏', '女', '副主任医师', 3, '13800001004', '430102196811280022', '孕期保健、妇科炎症、产后康复', '产科临床经验15年，擅长高危妊娠管理', 1),
('D0005', '陈晨', '女', '住院医师', 4, '13800001005', '430102196903050021', '儿童呼吸道感染、小儿腹泻', '儿科门急诊一线医师，熟悉儿童常见急症处理', 1),
('D0006', '刘建国', '男', '主任医师', 2, '13800001006', '430102197006120012', '肝胆外科、胃肠道肿瘤', '外科主任，开展胰十二指肠切除术等复杂术式', 1),
('D0007', '孙丽', '女', '主治医师', 5, '13800001007', '430102197109180026', '近视防控、干眼症、白内障', '眼科门诊骨干，年均完成白内障手术300例', 1),
('D0008', '周涛', '男', '副主任医师', 6, '13800001008', '430102197211280015', '过敏性鼻炎、慢性鼻窦炎、中耳炎', '耳鼻喉专业，擅长鼻内镜微创手术', 1),
('D0009', '吴敏', '女', '主治医师', 7, '13800001009', '430102197303050022', '龋齿充填、牙周治疗、儿童口腔', '口腔科医师，注重舒适化诊疗', 1),
('D0010', '郑凯', '男', '副主任医师', 8, '13800001010', '430102197406120118', '痤疮、湿疹、真菌性皮肤病', '皮肤科专业，开展光疗与激光治疗', 1),
('D0011', '冯雪', '女', '主任医师', 3, '13800001011', '430102197509180121', '子宫肌瘤、卵巢囊肿、不孕不育', '妇产科主任，妇科微创手术经验丰富', 1),
('D0012', '何军', '男', '主治医师', 9, '13800001012', '430102197611280110', '四肢骨折、关节镜、运动损伤', '骨科运动医学方向，服务多项赛事医疗保障', 1),
('D0013', '许芳', '女', '住院医师', 10, '13800001013', '430102197703050128', '急危重症抢救、创伤处置', '急诊科医师，具备ACLS资质', 1),
('D0014', '邓超', '男', '主治医师', 4, '13800001014', '430102197806120117', '儿童哮喘、支气管炎', '儿科呼吸专业，擅长儿童雾化治疗', 1),
('D0015', '罗静', '女', '副主任医师', 5, '13800001015', '430102197909180120', '青光眼、眼底病变', '眼科眼底病方向，熟练OCT与眼底照相判读', 1),
('D0016', '蒋伟', '男', '主治医师', 9, '13800001016', '430102198011280111', '腰椎间盘突出、颈椎病', '骨科脊柱方向，开展椎间孔镜手术', 1),
('D0017', '韩梅', '女', '主治医师', 8, '13800001017', '430102198103050129', '皮肤过敏、荨麻疹、脱发', '皮肤科门诊医师，擅长过敏原检测解读', 1),
('D0018', '杨帆', '男', '副主任医师', 6, '13800001018', '430102198206120118', '咽喉疾病、声音嘶哑、睡眠呼吸暂停', '耳鼻喉科，开展电子喉镜检查', 1),
('D0019', '曹琳', '女', '住院医师', 7, '13800001019', '430102198309180121', '口腔洁治、窝沟封闭', '口腔预防方向，负责社区口腔筛查', 1),
('D0020', '谢勇', '男', '主任医师', 10, '13800001020', '430102198411280217', '心肺复苏、多发性创伤、中毒', '急诊科主任，牵头院内急救绿色通道建设', 1);

INSERT INTO patient (patient_no, name, gender, id_card, phone, birth_date, address) VALUES
('P0001', '刘洋', '男', '430102199003030010', '13900002001', '1990-03-03', '长沙市岳麓区麓山南路123号'),
('P0002', '周婷', '女', '430102199205050028', '13900002002', '1992-05-05', '长沙市芙蓉区人民中路45号'),
('P0003', '孙浩', '男', '430102201001010016', '13900002003', '2010-01-01', '长沙市天心区书院路78号'),
('P0004', '郑爽', '女', '430102199608080029', '13900002004', '1996-08-08', '长沙市雨花区韶山北路210号'),
('P0005', '黄磊', '男', '430102198511120018', '13900002005', '1985-11-12', '长沙市开福区湘江中路66号'),
('P0006', '林小雨', '女', '430102200102140026', '13900002006', '2001-02-14', '长沙市望城区雷锋大道18号'),
('P0007', '马超', '男', '430102197807210018', '13900002007', '1978-07-21', '长沙市岳麓区桐梓坡路89号'),
('P0008', '高颖', '女', '43010219880930002X', '13900002008', '1988-09-30', '长沙市芙蓉区五一大道320号'),
('P0009', '唐宇', '男', '430102199504160016', '13900002009', '1995-04-16', '长沙市天心区芙蓉南路101号'),
('P0010', '谢琳', '女', '430102200312050125', '13900002010', '2003-12-05', '长沙市雨花区万家丽路77号'),
('P0011', '曾国强', '男', '430102196506180113', '13900002011', '1965-06-18', '长沙市开福区东风路52号'),
('P0012', '彭佳', '女', '430102199910220125', '13900002012', '1999-10-22', '长沙市岳麓区银盆岭路33号'),
('P0013', '钟伟', '男', '43010219820309011X', '13900002013', '1982-03-09', '长沙市望城区金星北路9号'),
('P0014', '夏雨欣', '女', '430102201505280124', '13900002014', '2015-05-28', '长沙市雨花区劳动东路128号'),
('P0015', '范志远', '男', '430102197001250117', '13900002015', '1970-01-25', '长沙市天心区湘府东路206号');

INSERT INTO registration (reg_no, patient_id, doctor_id, department_id, visit_date, time_slot, reg_fee, status) VALUES
('R202611110001', 1, 1, 1, '2026-11-11', '上午', 20.00, 1),
('R202611110002', 2, 4, 3, '2026-11-11', '下午', 15.00, 1),
('R202611120003', 3, 5, 4, '2026-11-12', '上午', 10.00, 0),
('R202611110004', 4, 7, 5, '2026-11-11', '上午', 18.00, 1),
('R202611110005', 5, 3, 2, '2026-11-11', '下午', 15.00, 1),
('R202611120006', 6, 2, 1, '2026-11-12', '上午', 20.00, 0),
('R202611120007', 7, 12, 9, '2026-11-12', '下午', 15.00, 1),
('R202611110008', 8, 11, 3, '2026-11-11', '上午', 20.00, 1),
('R202611130009', 9, 10, 8, '2026-11-13', '上午', 12.00, 0),
('R202611120010', 10, 9, 7, '2026-11-12', '下午', 12.00, 1),
('R202611120011', 11, 1, 1, '2026-11-12', '上午', 20.00, 1),
('R202611130012', 12, 8, 6, '2026-11-13', '下午', 15.00, 0),
('R202611130013', 13, 2, 1, '2026-11-13', '上午', 20.00, 1),
('R202611120014', 14, 14, 4, '2026-11-12', '上午', 10.00, 1),
('R202611140015', 15, 15, 5, '2026-11-14', '上午', 18.00, 0),
('R202611130016', 1, 7, 5, '2026-11-13', '下午', 18.00, 1),
('R202611140017', 3, 2, 1, '2026-11-14', '下午', 20.00, 2),
('R202611140018', 5, 12, 9, '2026-11-14', '上午', 15.00, 0),
('R202611130019', 13, 16, 9, '2026-11-13', '上午', 15.00, 1),
('R202611140020', 8, 11, 3, '2026-11-14', '上午', 20.00, 0),
('R202611140021', 11, 20, 10, '2026-11-14', '夜间', 25.00, 1),
('R202611140022', 6, 17, 8, '2026-11-14', '下午', 12.00, 2),
('R202611130023', 2, 18, 6, '2026-11-13', '上午', 15.00, 0),
('R202611140024', 9, 19, 7, '2026-11-14', '下午', 12.00, 0);

INSERT INTO prescription (presc_no, registration_id, patient_id, doctor_id, diagnosis, presc_time, status) VALUES
('PR202611101', 1, 1, 1, '原发性高血压2级', '2026-11-11 10:05:00', 1),
('PR202611102', 2, 2, 4, '妊娠期轻度贫血', '2026-11-11 10:08:00', 1),
('PR202611103', 4, 4, 7, '干眼症', '2026-11-11 10:11:00', 1),
('PR202611104', 5, 5, 3, '慢性胆囊炎伴胆囊结石', '2026-11-11 10:14:00', 1),
('PR202611105', 8, 8, 11, '子宫肌瘤', '2026-11-11 10:17:00', 1),
('PR202611106', 10, 10, 9, '龋齿（右下第一磨牙）', '2026-11-11 10:20:00', 1),
('PR202611107', 11, 11, 1, '2型糖尿病伴血脂异常', '2026-11-11 10:23:00', 1),
('PR202611108', 14, 14, 14, '急性支气管炎', '2026-11-11 10:26:00', 1),
('PR202611109', 16, 1, 7, '过敏性鼻炎', '2026-11-11 10:29:00', 0),
('PR202611110', 19, 13, 16, '腰椎间盘突出症', '2026-11-11 10:32:00', 0);

INSERT INTO prescription_item (prescription_id, drug_name, spec, quantity, unit, dosage, price) VALUES
(1, '苯磺酸氨氯地平片', '5mg*28片', 2, '盒', '一次1片，一日1次，晨服', 28.50),
(1, '阿托伐他汀钙片', '20mg*14片', 3, '盒', '一次1片，一日1次，睡前服', 42.00),
(2, '琥珀酸亚铁片', '0.1g*24片', 2, '盒', '一次1片，一日2次，饭后服', 18.50),
(2, '维生素C片', '0.1g*100片', 1, '瓶', '一次1片，一日3次', 6.00),
(3, '玻璃酸钠滴眼液', '0.1%*5ml', 2, '支', '一次1滴，一日4次', 36.00),
(4, '熊去氧胆酸胶囊', '250mg*24粒', 2, '盒', '一次1粒，一日3次', 68.00),
(4, '消炎利胆片', '0.26g*60片', 2, '瓶', '一次3片，一日3次', 15.80),
(5, '桂枝茯苓胶囊', '0.31g*100粒', 2, '盒', '一次3粒，一日3次', 45.00),
(6, '阿莫西林胶囊', '0.5g*24粒', 1, '盒', '一次2粒，一日3次', 12.60),
(6, '复方氯己定含漱液', '200ml', 1, '瓶', '一次10ml，含漱2分钟，一日3次', 22.00),
(7, '二甲双胍缓释片', '0.5g*30片', 3, '盒', '一次1片，一日2次，餐后服', 26.00),
(7, '瑞舒伐他汀钙片', '10mg*7片', 4, '盒', '一次1片，一日1次，睡前服', 39.00),
(8, '小儿氨溴索口服液', '100ml', 1, '瓶', '一次5ml，一日3次', 23.50),
(8, '阿奇霉素干混悬剂', '0.1g*6袋', 2, '盒', '一次1袋，一日1次，连服3天', 31.00),
(9, '糠酸莫米松鼻喷雾剂', '50μg*60喷', 1, '支', '每侧鼻腔1喷，一日1次', 58.00),
(9, '氯雷他定片', '10mg*12片', 1, '盒', '一次1片，一日1次', 19.80),
(10, '塞来昔布胶囊', '0.2g*12粒', 2, '盒', '一次1粒，一日1次', 48.00),
(10, '甲钴胺片', '0.5mg*24片', 2, '盒', '一次1片，一日3次', 32.00);
