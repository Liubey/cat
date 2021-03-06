package com.dianping.cat.job.sql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.InvalidInputException;
import org.apache.hadoop.mapreduce.security.TokenCache;

public abstract class DirectoryInputFormat<K, V> extends FileInputFormat<K, V> {

	public List<FileStatus> listStatus(JobContext job) throws IOException {

		List<FileStatus> result = new ArrayList<FileStatus>();
		Path[] dirs = getInputPaths(job);
		if (dirs.length == 0) {
			throw new IOException("No input paths specified in job");
		}

		TokenCache.obtainTokensForNamenodes(job.getCredentials(), dirs, job.getConfiguration());

		List<IOException> errors = new ArrayList<IOException>();
		List<PathFilter> filters = new ArrayList<PathFilter>();
		PathFilter jobFilter = getInputPathFilter(job);
		if (jobFilter != null) {
			filters.add(jobFilter);
		}
		// Add Default Hidden file
		PathFilter inputFilter = new MultiPathFilter(filters);

		filters.add(hiddenFileFilter);
		for (int i = 0; i < dirs.length; ++i) {
			Path p = dirs[i];
			FileSystem fs = p.getFileSystem(job.getConfiguration());
			FileStatus[] matches = fs.globStatus(p, inputFilter);
			if (matches == null) {
				errors.add(new IOException("Input path does not exist: " + p));
			} else if (matches.length == 0) {
				errors.add(new IOException("Input Pattern " + p + " matches 0 files"));
			} else {

				for (FileStatus globStat : matches) {
					addFileStat(result, inputFilter, fs, globStat);
				}
			}
		}

		if (!errors.isEmpty()) {
			throw new InvalidInputException(errors);
		}
		return result;
	}

	public void addFileStat(List<FileStatus> result, PathFilter inputFilter, FileSystem fs, FileStatus globStat)
	      throws IOException {
		if (globStat.isDir()) {
			for (FileStatus stat : fs.listStatus(globStat.getPath(), inputFilter)) {
				addFileStat(result, inputFilter, fs, stat);
			}
		} else {
			System.out.println(globStat.getPath().getName());
			result.add(globStat);
		}
	}

	private static final PathFilter hiddenFileFilter = new PathFilter() {
		public boolean accept(Path p) {
			String name = p.getName();
			return !name.startsWith("_") && !name.startsWith(".");
		}
	};

	private static class MultiPathFilter implements PathFilter {
		private List<PathFilter> filters;

		public MultiPathFilter(List<PathFilter> filters) {
			this.filters = filters;
		}

		public boolean accept(Path path) {
			for (PathFilter filter : filters) {
				if (!filter.accept(path)) {
					return false;
				}
			}
			return true;
		}
	}
}
