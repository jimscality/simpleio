package randomfile;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class DataFileMain {
	static final int blockSize = 1024*1024;
	private void make(String fileName, long size, BlockData bData) throws FileNotFoundException, IOException {
		long count = 0;

		try (FileOutputStream fs = new FileOutputStream(fileName)) {
			while (count < size) {
				byte[] b = bData.get();
				fs.write(b, 0, (count + b.length) < size ? b.length : (int)(size - count));
				count += b.length;
			}
		}
	}
	
	private void measureAndMake(String fileName, long fileSize, BlockData  bData) throws FileNotFoundException, IOException {
		long start = new Date().getTime();
		make(fileName, fileSize, bData);
		long end = new Date().getTime();
		long delta = end - start;
		System.out.println("Created " + fileName + " of size " + fileSize/1024/1024 + "MB in " + delta + "ms.");
		System.out.println("Average throughput is " + (fileSize/1024/1024)*1000/delta + "MB/s.");
		System.out.println("DataBlock class is " + bData.getClass().getName());
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: <file_name> <file_size_in MB>");
		}
		String fileName = args[0];
		long fileSize = Long.parseLong(args[1])*1024*1024;
		
		DataFileMain main = new DataFileMain();
		try {
			main.measureAndMake(fileName+"r", fileSize, new ContinuousRandomData(blockSize));
			main.measureAndMake(fileName+"l", fileSize, new LinearData(blockSize));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
