package simpleio.benchmarks;

import randomfile.BlockData;
import simpleio.job.JobType;

public class RereadJob extends ReadJob {
	public RereadJob(String path, BlockData blockData) {
		super(JobType.REREAD, path, blockData);
	}

}
