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

/**
 * The ticket expires based on the kerberos settings, this means you should auto
 * renew.
 * 
 * @author Shrikanth
 *
 */
@Configuration
@ComponentScan(basePackages = { "com.exp.ihadoop.hiveapp" })
public class HiveAppConfig {

	/**
	 * You should have krb5.conf in default location of Java installation
	 * <jre_home>/lib/security or <java_home>/conf/security for version 9 or set the
	 * path manually by un-commenting below line.s
	 */
	static {
		// System.setProperty("sun.security.krb5.debug", "true");
		// System.setProperty("java.security.krb5.conf",
		// "C:\\Users\\Shrikanth\\etc\\krb5.conf");

		org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
		conf.set("hadoop.security.authentication", "Kerberos");
		conf.addResource("src/main/resources/hive-site.xml");
		try {
			UserGroupInformation.setConfiguration(conf);
			UserGroupInformation.loginUserFromKeytab("hiveuser", "src/main/resources/hiveuser.keytab");
		} catch (IllegalArgumentException e) {
			if (e.getMessage().equals("Can't get Kerberos realm")) {
				System.out.println("Please make sure krb5.conf is there in default path");
			}
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
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