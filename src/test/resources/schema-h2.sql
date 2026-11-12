-- 测试用 H2 建表脚本（MODE=MySQL），结构与 sql/medical_db.sql 保持一致
DROP TABLE IF EXISTS prescription_item;
DROP TABLE IF EXISTS prescription;
DROP TABLE IF EXISTS registration;
DROP TABLE IF EXISTS doctor_schedule;
DROP TABLE IF EXISTS drug;
DROP TABLE IF EXISTS doctor;
DROP TABLE IF EXISTS patient;
DROP TABLE IF EXISTS department;

CREATE TABLE department (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    dept_code   VARCHAR(20)  NOT NULL UNIQUE,
    dept_name   VARCHAR(50)  NOT NULL,
    location    VARCHAR(100),
    phone       VARCHAR(20),
    description VARCHAR(255),
    status      INT          NOT NULL DEFAULT 1,
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE doctor (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_no     VARCHAR(20)  NOT NULL UNIQUE,
    name          VARCHAR(30)  NOT NULL,
    gender        CHAR(1)      NOT NULL,
    title         VARCHAR(20),
    department_id BIGINT       NOT NULL,
    phone         VARCHAR(20)  NOT NULL UNIQUE,
    id_card       VARCHAR(18)  UNIQUE,
    specialty     VARCHAR(100),
    introduction  VARCHAR(500),
    status        INT          NOT NULL DEFAULT 1,
    create_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_doctor_dept FOREIGN KEY (department_id) REFERENCES department (id)
);

CREATE TABLE doctor_schedule (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id     BIGINT       NOT NULL,
    schedule_date DATE         NOT NULL,
    time_slot     VARCHAR(20)  NOT NULL,
    total_slots   INT          NOT NULL DEFAULT 0,
    booked_slots  INT          NOT NULL DEFAULT 0,
    reg_fee       DECIMAL(8,2) NOT NULL DEFAULT 0.00,
    status        INT          NOT NULL DEFAULT 1,
    create_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_schedule_doctor_slot UNIQUE (doctor_id, schedule_date, time_slot),
    CONSTRAINT fk_schedule_doctor FOREIGN KEY (doctor_id) REFERENCES doctor (id)
);

CREATE TABLE drug (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    drug_code   VARCHAR(20)  NOT NULL UNIQUE,
    drug_name   VARCHAR(100) NOT NULL,
    spec        VARCHAR(50),
    unit        VARCHAR(10),
    price       DECIMAL(8,2) NOT NULL DEFAULT 0.00,
    stock       INT          NOT NULL DEFAULT 0,
    status      INT          NOT NULL DEFAULT 1,
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE patient (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_no  VARCHAR(20)  NOT NULL UNIQUE,
    name        VARCHAR(30)  NOT NULL,
    gender      CHAR(1)      NOT NULL,
    id_card     VARCHAR(18)  NOT NULL UNIQUE,
    phone       VARCHAR(20)  NOT NULL,
    birth_date  DATE,
    address     VARCHAR(200),
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE registration (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    reg_no        VARCHAR(32)  NOT NULL UNIQUE,
    patient_id    BIGINT       NOT NULL,
    doctor_id     BIGINT       NOT NULL,
    department_id BIGINT       NOT NULL,
    schedule_id   BIGINT,
    visit_date    DATE         NOT NULL,
    time_slot     VARCHAR(20)  NOT NULL,
    reg_fee       DECIMAL(8,2) NOT NULL DEFAULT 0.00,
    status        INT          NOT NULL DEFAULT 0,
    create_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reg_patient FOREIGN KEY (patient_id) REFERENCES patient (id),
    CONSTRAINT fk_reg_doctor FOREIGN KEY (doctor_id) REFERENCES doctor (id),
    CONSTRAINT fk_reg_dept FOREIGN KEY (department_id) REFERENCES department (id),
    CONSTRAINT fk_reg_schedule FOREIGN KEY (schedule_id) REFERENCES doctor_schedule (id)
);

CREATE TABLE prescription (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    presc_no        VARCHAR(32)  NOT NULL UNIQUE,
    registration_id BIGINT       NOT NULL UNIQUE,
    patient_id      BIGINT       NOT NULL,
    doctor_id       BIGINT       NOT NULL,
    diagnosis       VARCHAR(200) NOT NULL,
    presc_time      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    status          INT          NOT NULL DEFAULT 0,
    create_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_presc_reg FOREIGN KEY (registration_id) REFERENCES registration (id),
    CONSTRAINT fk_presc_patient FOREIGN KEY (patient_id) REFERENCES patient (id),
    CONSTRAINT fk_presc_doctor FOREIGN KEY (doctor_id) REFERENCES doctor (id)
);

CREATE TABLE prescription_item (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT       NOT NULL,
    drug_id         BIGINT,
    drug_name       VARCHAR(100) NOT NULL,
    spec            VARCHAR(50),
    quantity        INT          NOT NULL DEFAULT 1,
    unit            VARCHAR(10),
    dosage          VARCHAR(50),
    price           DECIMAL(8,2) NOT NULL DEFAULT 0.00,
    create_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_item_presc FOREIGN KEY (prescription_id) REFERENCES prescription (id) ON DELETE CASCADE,
    CONSTRAINT fk_item_drug FOREIGN KEY (drug_id) REFERENCES drug (id)
);
