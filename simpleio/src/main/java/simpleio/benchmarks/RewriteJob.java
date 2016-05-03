package simpleio.benchmarks;

import java.io.File;
import java.io.IOException;

import randomfile.BlockData;
import simpleio.job.JobType;

public class RewriteJob extends WriteJob {
	public RewriteJob(String path, long size, BlockData blockData) {
		super(JobType.REWRITE, path, size, blockData);
	}

	@Override
	protected void operation() throws IOException {
		File existingfile = new File(path);
		if (!existingfile.exists()) {
			throw new IllegalArgumentException(path + " does not exists for re-write");
		}
		super.operation();
	}
}
