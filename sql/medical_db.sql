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
-- 初始化样例数据（便于实验验收时直接演示分页查询与增删改）
-- ------------------------------------------------------------
INSERT INTO department (dept_code, dept_name, location, phone, description, status) VALUES
('NK',   '内科',   '门诊楼2层东区', '0731-88880001', '诊治呼吸、消化、心血管等内科常见病', 1),
('WK',   '外科',   '门诊楼3层西区', '0731-88880002', '开展普外、骨科等外科诊疗与手术', 1),
('FK',   '妇产科', '门诊楼4层',     '0731-88880003', '提供妇科、产科及孕产期保健服务', 1),
('EK',   '儿科',   '门诊楼1层东区', '0731-88880004', '诊治儿童常见病、多发病', 1),
('YK',   '眼科',   '门诊楼5层',     '0731-88880005', '开展视力检查、眼底病与屈光矫治', 1);

INSERT INTO doctor (doctor_no, name, gender, title, department_id, phone, id_card, specialty, introduction, status) VALUES
('D0001', '张明',   '男', '主任医师',   1, '13800001001', '430102198001011234', '高血压、冠心病诊治', '从事内科临床工作25年', 1),
('D0002', '李红',   '女', '副主任医师', 1, '13800001002', '430102198502022345', '糖尿病、甲状腺疾病', '内分泌方向，发表论文10余篇', 1),
('D0003', '王强',   '男', '主治医师',   2, '13800001003', '430102198703033456', '普外微创手术',       '擅长腹腔镜胆囊切除术', 1),
('D0004', '赵敏',   '女', '副主任医师', 3, '13800001004', '430102198204044567', '孕期保健、妇科炎症', '产科临床经验15年', 1),
('D0005', '陈晨',   '女', '住院医师',   4, '13800001005', '430102199505055678', '儿童呼吸道感染',     '儿科门急诊一线医师', 1);

INSERT INTO patient (patient_no, name, gender, id_card, phone, birth_date, address) VALUES
('P0001', '刘洋', '男', '430102199003031234', '13900002001', '1990-03-03', '长沙市岳麓区'),
('P0002', '周婷', '女', '430102199205052345', '13900002002', '1992-05-05', '长沙市芙蓉区'),
('P0003', '孙浩', '男', '430102201001013456', '13900002003', '2010-01-01', '长沙市天心区');

INSERT INTO registration (reg_no, patient_id, doctor_id, department_id, visit_date, time_slot, reg_fee, status) VALUES
('R202611110001', 1, 1, 1, '2026-11-11', '上午', 20.00, 0),
('R202611110002', 2, 4, 3, '2026-11-11', '下午', 15.00, 1),
('R202611110003', 3, 5, 4, '2026-11-12', '上午', 10.00, 0);

INSERT INTO prescription (presc_no, registration_id, patient_id, doctor_id, diagnosis, status) VALUES
('PR202611110001', 2, 2, 4, '妊娠期轻度贫血', 0);

INSERT INTO prescription_item (prescription_id, drug_name, spec, quantity, unit, dosage, price) VALUES
(1, '琥珀酸亚铁片', '0.1g*24片', 2, '盒', '一次1片，一日2次，饭后服用', 18.50),
(1, '维生素C片',     '0.1g*100片', 1, '瓶', '一次1片，一日3次', 6.00);
