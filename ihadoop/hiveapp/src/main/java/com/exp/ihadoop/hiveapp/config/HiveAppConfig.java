package com.exp.ihadoop.hiveapp.config;

import java.io.IOException;

import javax.sql.DataSource;

import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.exp.ihadoop.hiveapp.core.HiveDataSource;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@ComponentScan(basePackages = { "com.exp.ihadoop.hiveapp" })
public class HiveAppConfig {

	static {
		System.setProperty("sun.security.krb5.debug", "true");
		System.setProperty("java.security.krb5.conf", "C:\\Users\\Shrikanth\\etc\\krb5.conf");
		 
		org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
		conf.set("hadoop.security.authentication", "Kerberos");
		conf.addResource("src/main/resources/hive-site.xml");
		UserGroupInformation.setConfiguration(conf);
		try {
			UserGroupInformation.loginUserFromKeytab("hiveuser", "src/main/resources/hiveuser.keytab");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Bean
	public DataSource hikariDataSource() {
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setDriverClassName("org.apache.hive.jdbc.HiveDriver");
		dataSource.setJdbcUrl("jdbc:hive2://server1.exp.com:10000/default;principal=hive/server1.exp.com@EXP.COM");
		dataSource.setConnectionTestQuery("select * from bigleaf");
		dataSource.setMinimumIdle(2);
		dataSource.setInitializationFailFast(false);
		return dataSource;
	}

	@Bean
	public DataSource hiveDataSource() {
		String url = "jdbc:hive2://server1.exp.com:10000/default;principal=hive/server1.exp.com@EXP.COM";
		HiveDataSource dataSource = new HiveDataSource(url);
		return dataSource;
	}

	@Bean
	public JdbcTemplate JdbcTemplate(DataSource hikariDataSource) {
		return new JdbcTemplate(hikariDataSource);
	}

}