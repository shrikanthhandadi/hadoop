package com.exp.ihadoop.mapreduce.stock;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.util.ToolRunner;
import org.junit.Assert;
import org.junit.Test;

public class VolumeIT {

	@Test
	public void testRunLocally() throws Exception {
		String output = "src/test/resources/data/NYSE/output";
		FileUtils.deleteDirectory(new File(output));

		String[] args = { "src/test/resources/data/NYSE/input", output };
		int ret = ToolRunner.run(new Volume(), args);
		Assert.assertTrue(0 == ret);
	}

	@Test
	public void testRunAgainstCluster() throws Exception {
		Volume volume = new Volume() {
			@Override
			public Configuration getConf() {
				Configuration conf = buildConf();
				return conf;
			};
		};

		FileSystem fs = FileSystem.get(volume.getConf());
		Path f = new Path("mr/nyse");
		if (fs.exists(f)) {
			fs.delete(f, true);
		}
		fs.mkdirs(new Path("mr/nyse/input/"));
		fs.copyFromLocalFile(new Path("src/test/resources/data/NYSE/input/NYSE_daily"), new Path("mr/nyse/input/"));
		String[] args = { "mr/nyse/input", "mr/nyse/output" };
		int ret = ToolRunner.run(new Volume(), args);
		Assert.assertTrue(0 == ret);
	}

	private Configuration buildConf() {
		Configuration conf = new Configuration();
		// this should be like defined in your yarn-site.xml
		//conf.set("yarn.resourcemanager.address", "server1.exp.com:50001");

		// framework is now "yarn", should be defined like this in mapred-site.xm
		//conf.set("mapreduce.framework.name", "yarn");
		//conf.set("dfs.client.use.datanode.hostname", "true");
conf.addResource(new Path("core-site.xml"));
conf.addResource(new Path("hdfs-site.xml"));
conf.addResource(new Path("yarn-site.xml"));
		// like defined in hdfs-site.xml
		//conf.set("fs.default.name", "hdfs://server1.exp.com:9000");
		
        conf.set("hadoop.security.authentication", "kerberos");

		try {
			UserGroupInformation.setConfiguration(conf);
			UserGroupInformation.loginUserFromKeytab("hduser", "src/test/resources/hduser.keytab");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return conf;
	}
}
