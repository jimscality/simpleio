package randomfile;

public class LinearData extends BlockData {
	public LinearData(int size) {
		super(size);
	}
	
	@Override
	protected void fill() {
		byte[] b = super.getBuffer();
		for (int i = 0; i < b.length; i++) {
			b[i] = (byte)(i % 256);
		}
	}
}
