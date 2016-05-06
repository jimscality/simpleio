package randomfile;

/**
 * Empty data block
 * 
 * @author jimyang
 *
 */
public class EmptyData extends BlockData {

	public EmptyData(int size) {
		super(size);
	}

	@Override
	protected void fill() {
	}
}
