package simpleio.benchmarks;

import java.io.IOException;

import randomfile.BlockData;
import randomfile.FileOps;
import simpleio.job.Job;
import simpleio.job.JobType;

public class WriteJob extends Job {
	protected WriteJob(JobType type, String path, long size, BlockData blockData) {
		super(type, path, size, blockData);
	}

	@Override
	protected void operation() throws IOException {
		FileOps.write(path, size, blockData);
	}
}
