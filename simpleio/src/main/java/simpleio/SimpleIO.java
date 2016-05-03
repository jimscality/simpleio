package simpleio;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class SimpleIO {
	private static final Option helpOption = Option.builder("h").desc("help").build();
	private static final Option targetDirOption = Option.builder("d").required().hasArg().desc("benchmark target directory").build();
	private static final Option fileSizeOption = Option.builder("s").required().hasArg().desc("file sized used during benchmarking").build();
	private static final Option threadCountOption = Option.builder("t").hasArgs().desc("number of concurrent I/O threads").build();
	private static final Option benchmarkNameOption = Option.builder("b").required().hasArg().desc("benchmark name").build();
	
	private static final String helpMsg = "Usage: -h -d <target directory> -s <file size> -t <number of threads> -b <benchmark name>";
	
	private static Options initOptions() {
		Options options = new Options();
		
		options.addOption(helpOption);
		options.addOption(targetDirOption);
		options.addOption(fileSizeOption);
		options.addOption(threadCountOption);
		options.addOption(benchmarkNameOption);
		
		return options;
	}

	public static void main(String[] args) {
		String targetDir = null;
		long fileSize = 0;
		int threadCount = 0;
		String benchmarkName = null;
		
		Options options = initOptions();
		
		try {
			CommandLineParser parser = new DefaultParser();
		
			CommandLine cmd = parser.parse( options, args);
		
			if (cmd.hasOption('h')) {
				System.out.println(helpMsg);
				System.exit(1);
			}
			targetDir = cmd.getOptionValue('d');
			fileSize = 1024*1024*Long.parseLong(cmd.getOptionValue('s'));
			threadCount = Integer.parseInt(cmd.getOptionValue('t', "1"));
			benchmarkName = cmd.getOptionValue('b');
		}
		catch (ParseException exp) {
			System.out.println(helpMsg);
			System.exit(-1);
		}
		
		Benchmark.start(targetDir, benchmarkName, fileSize, threadCount);
	}

}
