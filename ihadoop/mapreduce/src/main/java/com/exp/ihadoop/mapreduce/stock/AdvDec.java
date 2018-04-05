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
 * -compute the number of times stock increased and decreased
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
public class AdvDec extends Configured implements Tool {

	public static void main(String[] args) throws Exception {
		int ret = ToolRunner.run(new AdvDec(), args);
		System.exit(ret);
	}

	@Override
	public int run(String[] args) throws Exception {
		String[] userArgs = new GenericOptionsParser(args).getRemainingArgs();

		Job job = Job.getInstance(getConf(), "StockAdvDec");

		job.setJarByClass(AdvDec.class);
		job.setMapperClass(StockAdvDecMapper.class);
		job.setReducerClass(StockAdvDecReducer.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(LongWritable.class);

		FileInputFormat.setInputPaths(job, new Path(userArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(userArgs[1]));

		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static class StockAdvDecMapper extends Mapper<LongWritable, Text, Text, LongWritable> {

		Text emitKey = new Text();
		LongWritable emitVal = new LongWritable();

		@Override
		protected void map(LongWritable key, Text value, Context ctx) throws IOException, InterruptedException {
			String line = value.toString();
			String[] cols = line.split("\\t");
			String date = cols[2];
			Double open = Double.valueOf(cols[3]);
			Double close = Double.valueOf(cols[6]);
			int flag = 0;
			if (close > open) {
				flag = 1;
			} else {
				flag = -1;
			}
			emitKey.set(date);
			emitVal.set(flag);
			ctx.write(emitKey, emitVal);
		}

	}

	public static class StockAdvDecReducer extends Reducer<Text, LongWritable, Text, Text> {

		Text emitVal = new Text();

		@Override
		protected void reduce(Text key, Iterable<LongWritable> values, Context ctx)
				throws IOException, InterruptedException {
			int inc = 0;
			int dec = 0;
			for (LongWritable val : values) {
				if (val.get() > 0) {
					inc++;
				} else {
					dec++;
				}
			}
			emitVal.set("inc: " + inc + "   dec: " + dec);
			ctx.write(key, emitVal);
		}

	}
}
