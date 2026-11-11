package com.example.yiliaoxitong.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

/**
 * MyBatis-Plus 代码生成器。
 *
 * <p>使用前提：MySQL 已导入 sql/medical_db.sql，库 yiliaoxitong 存在。</p>
 * <p>运行方式：在 IDEA 中右键运行 main 方法，或执行
 * {@code mvn exec:java -Dexec.mainClass=com.example.yiliaoxitong.generator.CodeGenerator}。</p>
 *
 * <p>生成的代码包含 entity / mapper / mapper.xml / service / serviceImpl / controller，
 * 输出到 src/main/java 的对应包下。重复运行会覆盖同名文件，因此调整过的业务代码建议先备份。</p>
 */
public final class CodeGenerator {

    private static final String URL =
            "jdbc:mysql://localhost:3306/yiliaoxitong?useUnicode=true&characterEncoding=utf8"
                    + "&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";

    private static final String USERNAME = "root";

    /** 本机 MySQL root 密码，运行时按实际情况修改 */
    private static final String PASSWORD = "123456";

    private static final String BASE_PACKAGE = "com.example.yiliaoxitong";

    private static final String AUTHOR = "qiongqi";

    private CodeGenerator() {
    }

    public static void main(String[] args) {
        String projectPath = System.getProperty("user.dir");
        String mapperXmlPath = projectPath + "/src/main/resources/mapper";

        FastAutoGenerator.create(URL, USERNAME, PASSWORD)
                .globalConfig(builder -> builder
                        .author(AUTHOR)
                        .outputDir(projectPath + "/src/main/java")
                        .disableOpenDir()
                        .commentDate("yyyy-MM-dd"))
                .packageConfig(builder -> builder
                        .parent(BASE_PACKAGE)
                        .entity("entity")
                        .mapper("mapper")
                        .service("service")
                        .serviceImpl("service.impl")
                        .controller("controller")
                        .xml("mapper")
                        .pathInfo(Collections.singletonMap(OutputFile.xml, mapperXmlPath)))
                .strategyConfig(builder -> builder
                        .addInclude("department", "doctor", "patient", "registration",
                                "prescription", "prescription_item")
                        .addTablePrefix("t_")
                        .entityBuilder()
                        .enableFileOverride()
                        .logicDeleteColumnName("deleted")
                        .mapperBuilder()
                        .enableBaseResultMap()
                        .enableBaseColumnList()
                        .serviceBuilder()
                        .formatServiceFileName("%sService")
                        .formatServiceImplFileName("%sServiceImpl")
                        .controllerBuilder()
                        .enableRestStyle()
                        .enableHyphenStyle())
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}
