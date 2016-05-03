package simpleio.benchmarks;

import java.io.File;
import java.io.IOException;

import randomfile.BlockData;
import simpleio.job.JobType;

public class FirstWriteJob extends WriteJob {	
	public FirstWriteJob(String path, long size, BlockData blockData) {
		super(JobType.FIRST_WRITE, path, size, blockData);
	}
	
	@Override
	protected void operation() throws IOException {
		File newFile = new File(path);
		if (newFile.exists()) {
			throw new IllegalArgumentException(path + " exists for first-write");
		}
		super.operation();
	}
}
