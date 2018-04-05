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

public class VolumeSorted extends Configured implements Tool {

	public static void main(String[] args) throws Exception {
		int ret = ToolRunner.run(new VolumeSorted(), args);
		System.exit(ret);
	}

	@Override
	public int run(String[] args) throws Exception {
		String[] userargs = new GenericOptionsParser(args).getRemainingArgs();
		String jobName = "StockVolumeSorted";
		Job job = Job.getInstance(getConf(), jobName);

		job.setJarByClass(Volume.class);
		job.setMapperClass(VolumeSortedMapper.class);
		job.setReducerClass(VolumeSortedReducer.class);

		job.setMapOutputKeyClass(LongWritable.class);
		job.setMapOutputValueClass(Text.class);

		FileInputFormat.setInputPaths(job, new Path(userargs[0]));
		FileOutputFormat.setOutputPath(job, new Path(userargs[1]));
		int ret = job.waitForCompletion(true) ? 0 : 1;
		return ret;
	}

	public static class VolumeSortedMapper extends Mapper<LongWritable, Text, LongWritable, Text> {

		LongWritable emitKey = new LongWritable();
		Text emitVal = new Text();

		@Override
		protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String[] cols = value.toString().split("\\t");
			emitKey.set(Long.valueOf(cols[1]));
			emitVal.set(cols[0]);
			context.write(emitKey, emitVal);
		}

	}

	public static class VolumeSortedReducer extends Reducer<LongWritable, Text, Text, LongWritable> {

		@Override
		protected void reduce(LongWritable key, Iterable<Text> values, Context ctx)
				throws IOException, InterruptedException {
			for (Text text : values) {
				ctx.write(text, key);
			}
		}

	}
}
