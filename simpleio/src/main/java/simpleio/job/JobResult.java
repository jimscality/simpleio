package simpleio.job;

import java.io.PrintStream;

import simpleio.job.Job.Status;

public class JobResult {
	private final Job job;
	private long execTime;
	private long byteCount;
	private final Exception exception;
	private long maxExecTime;
	private long minExecTime;
	private long maxByteCount;
	private long minByteCount;
	
	public JobResult(Job job, long byteCount, long execTime, Exception exception) {
		this.job = job;
		this.byteCount = byteCount;
		this.execTime = execTime;
		this.maxExecTime = execTime;
		this.minExecTime = execTime;
		this.maxByteCount = byteCount;
		this.minByteCount = byteCount;
		this.exception = exception;
	}
	
	public Job getJob() {
		return job;
	}
	
	public long getExecTime() {
		return execTime;
	}
	
	public long getByteCount() {
		return byteCount;
	}
	
	public Exception getException() {
		return exception;
	}
	
	public void display(PrintStream out) {
		display(out, job.getType(), job.getStatus(), byteCount, getExecTime());
	}
	
	public static void display(PrintStream out, JobType type, Status status, long totalBytes, long elapseTime) {
		display(out, type, status, totalBytes, elapseTime, false, 1);
	}

	public static void display(PrintStream out, JobType type, Status status, long totalBytes, long elapseTime, boolean bAverage, int count) {
		out.println(type + " is " + status);
		if (status == Status.OK) {
			double mb = totalBytes/1024.0/1024.0;
			if (bAverage) {
				out.println("\ttotal bytes (MB): " + mb/count);
			}
			else {
				out.println("\ttotal bytes (MB): " + mb);
			}
			out.println("\ttime for complete (ms); " + elapseTime*0.000001);
			out.println("\tthroughput (MB/s); " + mb/(elapseTime*0.000000001));
		}
	}
		
	public JobResult aggregate(JobResult result) {
		if (this.job.type == result.job.type) {
			this.execTime += result.execTime;
			if (this.maxExecTime < result.execTime) {
				this.maxExecTime = result.execTime;
			}
			if (this.minExecTime > result.execTime) {
				this.minExecTime = result.execTime;
			}
			this.byteCount += result.byteCount;
			if (this.maxByteCount < result.byteCount) {
				this.maxByteCount = result.byteCount;
			}
			if (this.minByteCount > result.byteCount) {
				this.minByteCount = result.byteCount;
			}
		}
		return this;
	}
}
