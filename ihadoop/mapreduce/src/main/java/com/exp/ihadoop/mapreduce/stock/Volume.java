package com.exp.ihadoop.mapreduce.stock;

import java.io.IOException;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * Given the list of trades happened on the certain set of stocks in NYSE
 * -compute the total amount of trade happened on each stock
 * <p>
 * --input
 * 
 * <pre>
 * exchange	symbol 	date 		open 	high 	low 	close  	volume  adj_close
 * NYSE		CLI		2009-12-03	32.55	33.43	32.48	32.65	1170600	32.22
 * NYSE		CLI		2009-12-02	31.72	32.62	31.41	32.47	975400	32.05
 * NYSE		CLI		2009-12-01	31.14	31.74	30.79	31.69	1284400	31.28
 * NYSE		CLI		2009-11-30	29.59	30.86	29.29	30.69	1411200	30.29
 * NYSE		CLI		2009-11-27	29.48	30.19	29.24	29.47	453600	29.09
 * 
 * -- output
 * Symbol	volume
 * CLI		2344
 * CLF		12334
 * 
 * </pre>
 * 
 * </p>
 * 
 * @author Shrikanth
 *
 */
public class Volume extends Configured implements Tool {

	public static void main(String[] args) throws Exception {
		int ret = ToolRunner.run(new Volume(), args);
		System.exit(ret);
	}

	@Override
	public int run(String[] args) throws Exception {

		String[] userargs = new GenericOptionsParser(args).getRemainingArgs();
		String jobName = "StockVolume";
		Job job = Job.getInstance(getConf(), jobName);

		job.setJarByClass(Volume.class);
		job.setMapperClass(StockVolumeMapper.class);
		job.setReducerClass(StockVolumeReducer.class);
		// job.setSortComparatorClass(LongWritable.Comparator.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(LongWritable.class);
		// job.setOutputKeyClass(Text.class);
		// job.setOutputValueClass(LongWritable.class);

		// job.setInputFormatClass(TextInputFormat.class);
		// job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.setInputPaths(job, new Path(userargs[0]));
		FileOutputFormat.setOutputPath(job, new Path(userargs[1]));
		int ret = job.waitForCompletion(true) ? 0 : 1;
		return ret;
	}

	public static class StockVolumeMapper extends Mapper<LongWritable, Text, Text, LongWritable> {

		Text emitKey = new Text();
		LongWritable emitVal = new LongWritable();

		@Override
		protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String[] split = value.toString().split("\\t");
			emitKey.set(split[1]);
			emitVal.set(Long.valueOf(split[7]));
			context.write(emitKey, emitVal);
			// Thread.sleep(1);
		}

	}

	public static class StockVolumeReducer extends Reducer<Text, LongWritable, Text, LongWritable> {

		@Override
		protected void reduce(Text key, Iterable<LongWritable> values, Context context)
				throws IOException, InterruptedException {
			LongWritable value = new LongWritable();
			long sum = 0L;
			for (LongWritable longWritable : values) {
				sum = sum + longWritable.get();
			}
			value.set(sum);
			context.write(key, value);
			// Thread.sleep(1000);
		}
	}

}
