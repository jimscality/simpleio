package randomfile;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileOps {
	public static long read(String path, BlockData bData) throws IOException {
		long count = 0;
		
		assert path != null : "file path cannot be null for read.";

		byte[] b = bData.get();
		try (FileInputStream fs = new FileInputStream(path)) {
			int ret;
			for (count = 0; ((ret = fs.read(b)) != -1); count += ret) {}
		}
		return count;
	}

	public static void write(String path, long size, BlockData bData) throws IOException {
		long count = 0;
		
		assert path != null : "file path cannot be null for write.";

		try (FileOutputStream fs = new FileOutputStream(path)) {
			while (count < size) {
				byte[] b = bData.get();
				fs.write(b, 0, (count + b.length) < size ? b.length : (int)(size - count));
				count += b.length;
			}
		}
	}

}
