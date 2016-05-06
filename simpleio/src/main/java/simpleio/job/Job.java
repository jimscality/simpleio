package simpleio.job;

import java.io.IOException;
import java.util.concurrent.Callable;

import randomfile.BlockData;

/**
 * Base class of an IO job
 * 
 * @author jimyang
 *
 */
public abstract class Job implements Callable<JobResult> {
	public enum Status {
		NEW,
		RUNNING,
		OK,
		FAILED;
	};
	
	protected final JobType type;
	protected long size;
	protected final String path;
	protected final BlockData blockData;
	private Status status;

	protected Job(JobType type, String path, BlockData blockData) {
		this.type = type;
		this.path = path;
		this.size = 0;
		this.blockData = blockData;
	}

	protected Job(JobType type, String path, long size, BlockData blockData) {
		this.type = type;
		this.path = path;
		this.size = size;
		this.blockData = blockData;
	}
	
	public String getPath() {
		return path;
	}
	
	public JobType getType() {
		return type;
	}
	
	public Status getStatus() {
		return status;
	}

	public JobResult call() throws Exception {
		long delta = -1;
		Exception e = null;
		this.status = Status.FAILED;
		try {
			delta = measure();
			this.status = Status.OK;
		} catch (IOException ioe) {
			e = ioe;
		}
		return new JobResult(this, size, delta, e);
	}

	private long measure() throws IOException {
		long start = System.nanoTime();
		operation();
		return System.nanoTime() - start;
	}
	
	abstract protected void operation() throws IOException;
}
