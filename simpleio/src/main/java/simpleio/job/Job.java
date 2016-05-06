package simpleio.job;

import java.io.IOException;
import java.util.Date;
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
	protected Date opStart = null;
	protected Date opEnd = null;
	private Status status;

	protected Job(JobType type, String path, BlockData blockData) {
		this(type, path, 0, blockData);
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
			throw ioe;
		}
		return new JobResult(this, size, delta, e);
	}

	private long measure() throws IOException {
		opStart = new Date();
		long start = System.nanoTime();
		operation();
		long end = System.nanoTime();
		opEnd = new Date();
		return end - start;
	}
	
	abstract protected void operation() throws IOException;
}
