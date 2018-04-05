package com.exp.ihadoop.hiveapp.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class HiveJdbcDao {

	@Inject
	private JdbcTemplate jdbcTemplate;

	public void executeQuery(String sql) {
		jdbcTemplate.execute(sql);
	}

	public List<Map<String, Object>> selectQuery(String sql) {
		List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(sql);
		return queryForList;
	}
}
