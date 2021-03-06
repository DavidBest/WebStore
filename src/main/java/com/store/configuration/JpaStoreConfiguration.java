package com.store.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@PropertySource("classpath:jpa.properties")
@EnableJpaRepositories("com.store.database.repository")
@EnableTransactionManagement
@ComponentScan({"com.store.database", "com.store.other", "com.store.component.service"})
public class JpaStoreConfiguration {

    private final Environment environment;

    @Value("sql_script/role_creation.sql")
    private Resource roleScript;

    @Autowired
    public JpaStoreConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory factory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(factory);

        return transactionManager;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(@Qualifier("dataSource") DataSource dataSource
            , @Qualifier("entityManagerProperties") Properties properties) {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource);
        entityManagerFactoryBean.setPackagesToScan("com.store.database.model");
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        entityManagerFactoryBean.setJpaProperties(properties);

        return entityManagerFactoryBean;
    }

    @Bean
    public Properties entityManagerProperties(String autoProperty) {
        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", autoProperty);
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
        properties.setProperty("hibernate.enable_lazy_load_no_trans", "true");

        return properties;
    }

    @Bean
    public DataSourceInitializer dataSourceInitializer(DataSource dataSource, DatabasePopulator databasePopulator){
        DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(dataSource);
        dataSourceInitializer.setDatabasePopulator(databasePopulator);

        return dataSourceInitializer;
    }

    @Bean
    @Profile({"create","create-delete"})
    public DatabasePopulator databasePopulator(){
        ResourceDatabasePopulator resourcePopulator = new ResourceDatabasePopulator();
        resourcePopulator.addScript(roleScript);

        return resourcePopulator;
    }

    @Bean
    @Profile("update")
    public DatabasePopulator databasePopulatorUpdate(){
        return new ResourceDatabasePopulator();
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(environment.getProperty("mysql.driver"));
        dataSource.setUrl(environment.getProperty("mysql.db"));
        dataSource.setUsername(environment.getProperty("mysql.username"));
        dataSource.setPassword(environment.getProperty("mysql.password"));

        return dataSource;
    }

    @Bean("autoProperty")
    @Profile("create")
    public String autoPropertyCreate() {
        return "create";
    }

    @Bean("autoProperty")
    @Profile("create-drop")
    public String autoPropertyCreateDrop() {
        return "create-drop";
    }

    @Bean(name = "autoProperty")
    @Profile("update")
    public String autoPropertyUpdate() {
        return "update";
    }
}
