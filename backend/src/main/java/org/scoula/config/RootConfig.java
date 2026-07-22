package org.scoula.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * 루트 컨텍스트 설정 (DataSource / MyBatis / 트랜잭션 + 도메인 서비스·매퍼 스캔).
 *
 * <p>[스캔 규칙]
 * <ul>
 *   <li>서비스(@Service 등)와 매퍼는 여기(루트 컨텍스트)에서 스캔한다.</li>
 *   <li>컨트롤러는 {@link ServletConfig}(서블릿 컨텍스트)에서만 스캔한다.</li>
 *   <li>security 패키지는 SecurityConfig 가 자체적으로 스캔하므로 여기서 제외한다.</li>
 * </ul>
 *
 * <p>[도메인 추가 방법] 새 도메인을 만들면 아래 두 목록에
 * {@code org.scoula.<도메인>.service} 와 {@code org.scoula.<도메인>.mapper} 를 추가한다.</p>
 */
@Configuration
@MapperScan(basePackages = {
        "org.scoula.member.mapper",
        "org.scoula.main.mapper",
        "org.scoula.dashboard.mapper",
        "org.scoula.simulator.mapper",
        "org.scoula.product.mapper",
        "org.scoula.travel.mapper",
        "org.scoula.rent.mapper",
        "org.scoula.car.mapper",
        "org.scoula.job.mapper",
        "org.scoula.regret.mapper",
        "org.scoula.social.mapper",
        "org.scoula.bookmark.mapper"
})
@ComponentScan(basePackages = {
        "org.scoula.member.service",
        "org.scoula.main.service",
        "org.scoula.dashboard.service",
        "org.scoula.simulator.service",
        "org.scoula.product.service",
        "org.scoula.travel.service",
        "org.scoula.rent.service",
        "org.scoula.car.service",
        "org.scoula.job.service",
        "org.scoula.regret.service",
        "org.scoula.social.service",
        "org.scoula.bookmark.service"
})
// application-secret.properties 는 API 키 등 비밀값 (gitignore 대상).
// 파일이 없어도 서버가 뜨도록 ignoreResourceNotFound = true
@PropertySource(
        value = {
                "classpath:/application.properties",
                "classpath:/application-secret.properties"
        },
        ignoreResourceNotFound = true
)
@EnableTransactionManagement
public class RootConfig {

    @Autowired
    ApplicationContext applicationContext;

    @Value("${jdbc.driver}")
    String driver;

    @Value("${jdbc.url}")
    String url;

    @Value("${jdbc.username}")
    String username;

    @Value("${jdbc.password}")
    String password;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driver);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        return new HikariDataSource(config);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
        sqlSessionFactory.setConfigLocation(applicationContext.getResource("classpath:/mybatis-config.xml"));
        sqlSessionFactory.setDataSource(dataSource());
        return (SqlSessionFactory) sqlSessionFactory.getObject();
    }

    @Bean
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }
}
