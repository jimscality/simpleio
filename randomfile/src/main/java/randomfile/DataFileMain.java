package randomfile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		long start = System.nanoTime();
		make(fileName, fileSize, bData);
		long end = System.nanoTime();
		long delta = end - start;
		System.out.println("Created " + fileName + " of size " + (double)fileSize/1024/1024 + " MB in " + delta/1000000 + "ms.");
		System.out.println("Average throughput is " + ((double)fileSize/1024/1024)*1000000000/delta + " MB/s.");
		System.out.println("DataBlock class is " + bData.getClass().getName());
	}

	private long read(String fileName, BlockData bData) throws FileNotFoundException, IOException {
		long count = 0;
		try (FileInputStream fs = new FileInputStream(fileName)) {
			byte[] b = bData.get();
			int readBytes;
			while (-1 != (readBytes = fs.read(b))) {
				count += readBytes;
			}
		}
		return count;
	}

	private void measureAndRead(String fileName, BlockData  bData) throws FileNotFoundException, IOException {
		long start = System.nanoTime();
		long fileSize = read(fileName, bData);
		long end = System.nanoTime();
		long delta = end - start;
		System.out.println("Read " + fileName + " of size " + (double)fileSize/1024/1024 + " MB in " + delta/1000000 + "ms.");
		System.out.println("Average throughput is " + ((double)fileSize/1024/1024)*1000000000/delta + " MB/s.");
		System.out.println("DataBlock class is " + bData.getClass().getName());
	}

	private static final Option helpOption = Option.builder("h").desc("help").build();
	private static final Option opTypeOption = Option.builder("p").required().hasArg().desc("operation type").build();
	private static final Option fileNameOption = Option.builder("f").hasArg().desc("file name").build();
	private static final Option fileSizeOption = Option.builder("s").hasArg().desc("file sized [kb|mb|gb]").build();
	private static final Option linearOption = Option.builder("l").desc("linear content").build();

	private static final String helpMsg = "Usage:\n[-h] --help\n"
			+ "-p <read|write>\n"
			+ "\nread:\n"
			+ "-f <file name> -- file name to be read\n"
			+ "\nwrite: \n"
			+ "-f <file name> -- file name of generated file\n"
			+ "-s <file size [kb|mb|gb]> -- size of the generated file\n"
			+ "[-l] -- linear content. default is random content";
	
	private static Options initOptions() {
		Options options = new Options();
		
		options.addOption(helpOption);
		options.addOption(opTypeOption);
		options.addOption(fileNameOption);
		options.addOption(fileSizeOption);
		options.addOption(linearOption);
		
		return options;
	}

	public static void main(String[] args) {
		String fileName = null;
		long fileSize = 0;
		boolean bLinear = false;
		boolean bRead = false;

		Options options = initOptions();
		
		try {
			CommandLineParser parser = new DefaultParser();
		
			CommandLine cmd = parser.parse( options, args);
		
			if (cmd.hasOption('h')) {
				System.err.println(helpMsg);
				System.exit(1);
			}
			String opType = cmd.getOptionValue('p');
			if ("read".equals(opType.toLowerCase())) {
				bRead = true;
			}
			else if ("write".equals(opType.toLowerCase())) {
				String s = cmd.getOptionValue('s');
				Pattern p = Pattern.compile("(?<size>[0-9]+)(?<unit>(?:kb)?|(?:mb)|(?:gb))?$", Pattern.CASE_INSENSITIVE);
				Matcher m = p.matcher(s);
				if (m.matches()) {
					String sizeUnit = m.group("unit");
					String size = m.group("size");
					fileSize = Long.parseLong(size);
					switch (sizeUnit.toLowerCase()) {
					case "":
						break;
					case "kb":
						fileSize *= 1024;
						break;
					case "mb":
						fileSize *= 1024*1024;
						break;
					case "gb":
						fileSize *= 1024*1024*1024;
						break;
					default:
						System.err.println(helpMsg);
						System.exit(1);
					}
				}
				else {
					System.err.println(helpMsg);
					System.exit(1);
				}
				bLinear = cmd.hasOption('l');
			}
			else {
				System.err.println(helpMsg);
				System.exit(1);
			}
			fileName = cmd.getOptionValue('f');
		}
		catch (ParseException exp) {
			System.err.println(helpMsg);
			System.exit(-1);
		}
		
		DataFileMain main = new DataFileMain();
		try {
			if (bRead) {
				main.measureAndRead(fileName, new EmptyData(blockSize));
			}
			else {
				if (bLinear) {
					main.measureAndMake(fileName, fileSize, new LinearData(blockSize));
				}
				else {
					main.measureAndMake(fileName, fileSize, new ContinuousRandomData(blockSize));
				}
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
