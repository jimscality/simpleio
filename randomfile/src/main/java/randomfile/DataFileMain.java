package randomfile;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
/**
 * Main function for generating data files with various kind of data pattern
 * 
 * @author jimyang
 *
 */
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

	private static final Option helpOption = Option.builder("h").desc("help").build();
	private static final Option fileNameOption = Option.builder("f").required().hasArg().desc("file name").build();
	private static final Option fileSizeOption = Option.builder("s").required().hasArg().desc("file sized (MB)").build();
	private static final Option linearOption = Option.builder("l").desc("linear content").build();
	
	private static final String helpMsg = "Usage:\n[-h] --help\n"
			+ "-f <file name> -- file name of generated file\n"
			+ "-s <file size (MB)> -- size of the generated file\n"
			+ "[-l] -- linear content. default is random content";
	
	private static Options initOptions() {
		Options options = new Options();
		
		options.addOption(helpOption);
		options.addOption(fileNameOption);
		options.addOption(fileSizeOption);
		options.addOption(linearOption);
		
		return options;
	}

	public static void main(String[] args) {
		String fileName = null;
		long fileSize = 0;
		boolean bLinear = false;

		Options options = initOptions();
		
		try {
			CommandLineParser parser = new DefaultParser();
		
			CommandLine cmd = parser.parse( options, args);
		
			if (cmd.hasOption('h')) {
				System.err.println(helpMsg);
				System.exit(1);
			}
			fileSize = 1024*1024*Long.parseLong(cmd.getOptionValue('s'));
			fileName = cmd.getOptionValue('f');
			bLinear = cmd.hasOption('l');
		}
		catch (ParseException exp) {
			System.err.println(helpMsg);
			System.exit(-1);
		}
		
		DataFileMain main = new DataFileMain();
		try {
			if (bLinear) {
				main.measureAndMake(fileName, fileSize, new LinearData(blockSize));
			}
			else {
				main.measureAndMake(fileName, fileSize, new ContinuousRandomData(blockSize));
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
