package randomfile;

import java.util.Random;

/**
 * Generate a block of random data
 * 
 * @author jimyang
 *
 */
public class RandomData extends BlockData {
	private static final Random ran = new Random();
	public RandomData(int size) {
		super(size);
	}

	@Override
	protected void fill() {
		ran.nextBytes(super.getBuffer());
	}
}
