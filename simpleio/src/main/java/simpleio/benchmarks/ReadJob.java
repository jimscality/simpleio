package simpleio.benchmarks;

import java.io.File;
import java.io.IOException;

import randomfile.BlockData;
import randomfile.FileOps;
import simpleio.job.Job;
import simpleio.job.JobType;

/**
 * Read an existing file
 * 
 * @author jimyang
 *
 */
public class ReadJob extends Job {

	protected ReadJob(JobType type, String path, BlockData blockData) {
		super(type, path, blockData);
	}

	@Override
	protected void operation() throws IOException {
		File existingfile = new File(path);
		if (!existingfile.exists()) {
			throw new IllegalArgumentException(path + " does not exists for read.");
		}
		size = FileOps.read(path, blockData);
	}
}
