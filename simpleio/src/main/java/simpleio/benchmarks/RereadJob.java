package simpleio.benchmarks;

import randomfile.BlockData;
import simpleio.job.JobType;

/**
 * Second/subsequent read of a file
 * 
 * @author jimyang
 *
 */
public class RereadJob extends ReadJob {
	public RereadJob(String path, BlockData blockData) {
		super(JobType.REREAD, path, blockData);
	}

}
