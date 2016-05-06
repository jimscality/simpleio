package simpleio.benchmarks;

import randomfile.BlockData;
import simpleio.job.JobType;

/**
 * First/initial read file
 * 
 * @author jimyang
 *
 */
public class FirstReadJob extends ReadJob{
	public FirstReadJob(String path, BlockData blockData) {
		super(JobType.FIRST_READ, path, blockData);
	}
}
