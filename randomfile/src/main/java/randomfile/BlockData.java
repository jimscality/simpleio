package randomfile;

abstract public class BlockData {
	private final byte[] buffer;
	private final boolean bFill;
	
	protected BlockData(int size) {
		buffer = new byte[size];
		bFill = ContinuousData.class.isInstance(this);
		fill();
	}
	
	public byte[] get() {
		if (bFill) {
			return fillAndGet();
		}
		return buffer;
	}
	
	private byte[] fillAndGet() {
		fill();
		return getBuffer();
	}
	
	protected byte[] getBuffer() {
		return buffer;
	}
	
	abstract protected void fill();
}
