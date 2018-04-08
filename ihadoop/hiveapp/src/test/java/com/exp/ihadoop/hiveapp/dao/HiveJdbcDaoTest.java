package com.exp.ihadoop.hiveapp.dao;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.exp.ihadoop.hiveapp.config.HiveAppConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { HiveAppConfig.class })
public class HiveJdbcDaoTest {

	@Autowired
	private HiveJdbcDao jdbcDao;

	@Test
	public void testExecuteQuery() {
		List<Map<String, Object>> out = jdbcDao.selectQuery("select * from bigleaf");
		for (Map<String, Object> map : out) {
			for (Entry<String, Object> entry : map.entrySet()) {
				System.out.println(entry.getKey() + " : " + entry.getValue());
			}
		}
	}
}
