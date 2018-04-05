package com.exp.ihadoop.mapreduce.stock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
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
 * -compute the min and max of value based on user input
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
public class MinMaxAll extends Configured implements Tool {

	public static void main(String[] args) throws Exception {
		int run = ToolRunner.run(new MinMaxAll(), args);
		System.exit(run);
	}

	@Override
	public int run(String[] args) throws Exception {
		String[] userargs = new GenericOptionsParser(args).getRemainingArgs();
		Configuration conf = getConf();

		if (userargs.length < 3) {
			System.out.println(
					"Usage: hadoop jar <jarfile> <mainclass> <input> <output> <minmax col as open|high|low|close|adj_close>");
			System.exit(1);
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("open", "3");
		map.put("high", "4");
		map.put("low", "5");
		map.put("close", "6");
		map.put("adj_close", "8");

		String userarg2 = userargs[2].toLowerCase();
		String value = map.get(userarg2);
		if (value == null) {
			System.out.println("Invalied userargs " + userarg2);
			System.exit(1);
		}
		System.out.println("conf%%%%%%%%%%   " + conf);

		conf.set("minmax.colname", value);

		System.out.println("@@@@@@@@@@@@@@@@@@@@@  " + conf.get("minmax.colname"));

		Job job = Job.getInstance(conf, "StockOpenMinMax");
		job.setJarByClass(MinMaxAll.class);
		job.setMapperClass(StockMinMaxAllMapper.class);
		job.setReducerClass(StockMinMaxAllReducer.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(DoubleWritable.class);

		FileInputFormat.setInputPaths(job, new Path(userargs[0]));
		FileOutputFormat.setOutputPath(job, new Path(userargs[1]));

		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static class StockMinMaxAllMapper extends Mapper<LongWritable, Text, Text, DoubleWritable> {

		int index = 3;

		Text emitKey = new Text();
		DoubleWritable emitVal = new DoubleWritable();

		@Override
		protected void setup(Mapper<LongWritable, Text, Text, DoubleWritable>.Context context)
				throws IOException, InterruptedException {
			String string = context.getConfiguration().get("minmax.colname");
			index = Integer.valueOf(string);
		}

		@Override
		protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String line = value.toString();
			String[] cols = line.split("\\t");
			String stock = cols[1];
			Double val = Double.valueOf(cols[index]);
			emitKey.set(stock);
			emitVal.set(val);
			context.write(emitKey, emitVal);
		}

	}

	public static class StockMinMaxAllReducer extends Reducer<Text, DoubleWritable, Text, Text> {

		Text emitVal = new Text();

		@Override
		protected void reduce(Text key, Iterable<DoubleWritable> values, Context ctx)
				throws IOException, InterruptedException {
			double min = 99999999;
			double max = -9999999;
			System.out.println("key " + key);
			for (DoubleWritable value : values) {
				if (value.get() < min) {
					min = value.get();
				}
				if (value.get() > max) {
					max = value.get();
				}
			}
			emitVal.set("min: " + min + "   max: " + max);
			ctx.write(key, emitVal);
		}

	}
}
