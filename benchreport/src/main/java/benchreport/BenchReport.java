package benchreport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Create;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.CellFormat;
import com.google.api.services.sheets.v4.model.DimensionProperties;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.GridData;
import com.google.api.services.sheets.v4.model.NumberFormat;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.TextFormat;

public class BenchReport {
	private final String googleCredJsonFile;
	private Map<? extends Object, ? extends Object> data;
	
	public BenchReport(String dataFile) throws FileNotFoundException, IOException {
		this(dataFile, null);
	}
	
	public BenchReport(String dataFile, String googleCredJsonFile) throws FileNotFoundException, IOException {
		this.googleCredJsonFile = googleCredJsonFile;
		Properties props = new Properties();
		data = props;
		props.load(new FileInputStream(dataFile));
	}
	
	public void publish() throws IOException, GeneralSecurityException {
		Spreadsheet spreadSheet= new Spreadsheet();
		List<Sheet> dmSheetList = new ArrayList<>();
		dmSheetList.add(createTouchSheet());
		dmSheetList.add(createIozoneSheet());
		dmSheetList.add(createMediaSheet());
		dmSheetList.add(createFsmdSheet());
		spreadSheet.setSheets(dmSheetList)
			.setProperties(new SpreadsheetProperties().setTitle((String) data.get("bench_title")));
		Create createHandle = getSheets().spreadsheets().create(spreadSheet);
		Spreadsheet result = createHandle.execute();
		assert(result != null);
	}
	
	private HttpRequestInitializer getInitializer(HttpTransport transport, JsonFactory jsonFactory) throws IOException {
		Credential credential;
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(),
				new InputStreamReader(googleCredJsonFile == null ? BenchReport.class.getResourceAsStream("/cred.json") :
																	new FileInputStream(googleCredJsonFile)));
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(transport,
				jsonFactory,
				clientSecrets,
				Arrays.asList(SheetsScopes.SPREADSHEETS))
				.setDataStoreFactory(new FileDataStoreFactory(new File("my.json")))
				.setAccessType("offline")
				.build();
		credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
		return credential;
	}
	
	private Sheets getSheets() throws GeneralSecurityException, IOException {
		HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		HttpRequestInitializer initializer = getInitializer(transport, jsonFactory);
		return new Sheets.Builder(transport, jsonFactory, initializer)
				.setApplicationName("benchresult")
				.build();
	}
	
	private Sheet createTouchSheet() {
		Sheet touch = new Sheet().setProperties(new SheetProperties().setTitle("touch"));
		List<GridData> grid = new ArrayList<>();
		int row = 0;		
		List<DimensionProperties> colProps = new ArrayList<>();
		colProps.add(new DimensionProperties().setPixelSize(120));
		grid.add(CellOps.populateGrid(row++, 0,
					CellOps.newCell("Benchmark", CellOps.FORMAT_1),
					CellOps.newCell("Touch (create)", null)).
				setColumnMetadata(colProps));
		row++;
		grid.add(CellOps.populateGrid(row++, 1,
				CellOps.newCell("shared cache", CellOps.FORMAT_2),
				CellOps.newCell("dirsync", CellOps.FORMAT_2),
				CellOps.newCell("dirAsync", CellOps.FORMAT_2)));

		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("throughput (ops/s)", CellOps.FORMAT_3)));

		row++;		
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("1 connector", CellOps.FORMAT_2)));
		row = insertTouchThroughput(grid, "c1", row);

		row++;
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("3 connectors", CellOps.FORMAT_2)));
		row = insertTouchThroughput(grid, "c3", row);

		row++;
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("6 connectors (2 per server)", CellOps.FORMAT_2)));
		row = insertTouchThroughput(grid, "c6", row);
		
		row += 2;
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("latency in ms", CellOps.FORMAT_3)));

		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("1 connector", CellOps.FORMAT_2)));
		row = insertTouchLatency(grid, "c1", row);

		row++;
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("3 connectors", CellOps.FORMAT_2)));
		row = insertTouchLatency(grid, "c3", row);

		row++;
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("6 connectors (2 per server)", CellOps.FORMAT_2)));
		row = insertTouchLatency(grid, "c6", row);

		touch.setData(grid);
		return touch;
	}

	private Sheet createIozoneSheet() {
		Sheet iozone = new Sheet().setProperties(new SheetProperties().setTitle("iozone"));
		List<GridData> grid = new ArrayList<>();
		int row = 0;		
		List<DimensionProperties> colProps = new ArrayList<>();
		colProps.add(new DimensionProperties().setPixelSize(120));
		grid.add(CellOps.populateGrid(row++, 0,
					CellOps.newCell("Benchmark", CellOps.FORMAT_1),
					CellOps.newCell("iozone 256 threads, 100MB file size", null)).
				setColumnMetadata(colProps));
		row++;
		grid.add(CellOps.populateGrid(row++, 1,
				CellOps.newCell("shared cache", CellOps.FORMAT_2),
				CellOps.newCell("dirsync", CellOps.FORMAT_2),
				CellOps.newCell("dirAsync", CellOps.FORMAT_2)));

		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("throughput (kB/s)", CellOps.FORMAT_3)));

		row++;
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("1 connector", CellOps.FORMAT_2)));
		row = insertIozoneThroughput(grid, "c1", row);
		
		row++;
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("3 connectors", CellOps.FORMAT_2)));
		row = insertIozoneThroughput(grid, "c3", row);

		row++;
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("6 connectors (2 per server)", CellOps.FORMAT_2)));
		row = insertIozoneThroughput(grid, "c6", row);

		iozone.setData(grid);
		return iozone;
	}

	private Sheet createMediaSheet() {
		Sheet media = new Sheet().setProperties(new SheetProperties().setTitle("media"));
		List<GridData> grid = new ArrayList<>();
		int row = 0;		
		List<DimensionProperties> colProps = new ArrayList<>();
		colProps.add(new DimensionProperties().setPixelSize(120));
		grid.add(CellOps.populateGrid(row++, 0,
					CellOps.newCell("Benchmark", CellOps.FORMAT_1),
					CellOps.newCell("media workload, 10% read, 90% write. File sizes: 8, 12, 48 MB, 10 users per connector", null)).
				setColumnMetadata(colProps));
		row++;
		grid.add(CellOps.populateGrid(row++, 1,
				CellOps.newCell("shared cache", CellOps.FORMAT_2),
				CellOps.newCell("dirsync", CellOps.FORMAT_2),
				CellOps.newCell("dirAsync", CellOps.FORMAT_2)));

		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("throughput (kB/s)", CellOps.FORMAT_3)));
		
		row++;
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("1 connector", CellOps.FORMAT_2)));
		row = insertMediaThroughput(grid, "c1", row);
		
		row++;
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("3 connectors", CellOps.FORMAT_2)));
		row = insertMediaThroughput(grid, "c3", row);

		row++;
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("6 connectors (2 per server)", CellOps.FORMAT_2)));
		row = insertMediaThroughput(grid, "c6", row);
		
		media.setData(grid);
		return media;
	}

	private Sheet createFsmdSheet() {
		Sheet fsmd = new Sheet().setProperties(new SheetProperties().setTitle("fsmd"));
		List<GridData> grid = new ArrayList<>();
		int row = 0;
		List<DimensionProperties> colProps = new ArrayList<>();
		colProps.add(new DimensionProperties().setPixelSize(140));
		grid.add(CellOps.populateGrid(row++, 0,
					CellOps.newCell("Benchmark", CellOps.FORMAT_1),
					CellOps.newCell("multi-level directory operations", null)).
				setColumnMetadata(colProps));
		row++;
		grid.add(CellOps.populateGrid(row++, 1,
				CellOps.newCell("shared cache", CellOps.FORMAT_2),
				CellOps.newCell("dirsync", CellOps.FORMAT_2),
				CellOps.newCell("dirAsync", CellOps.FORMAT_2)));

		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("throughput (ops/s)", CellOps.FORMAT_3)));

		row++;
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("1 connector", CellOps.FORMAT_2)));
		row = insertFsmdThroughput(grid, "c1", row);
		row++;
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("3 connectors", CellOps.FORMAT_2)));
		row = insertFsmdThroughput(grid, "c3", row);
		row++;
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("6 connectors", CellOps.FORMAT_2)));
		row = insertFsmdThroughput(grid, "c6", row);
		fsmd.setData(grid);
		return fsmd;
	}

	private int insertTouchThroughput(List<GridData> grid, String cLevel, int row) {
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell((String) data.get("touch_sharedcache_" + cLevel + "_total") + " total ops", null)));		
		grid.add(CellOps.populateGrid(row++, 1,
				CellOps.newCell(validateVaue("touch_sharedcache_" + cLevel + "_ops", "touch_sharedcache_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("touch_dirsync_" + cLevel + "_ops", "touch_dirsync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("touch_dirasync_" + cLevel + "_ops", "touch_dirasync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6)));
		grid.add(CellOps.populateGrid(row++, 1,
				CellOps.newCell((String) data.get("touch_sharedcache_" + cLevel + "_errors"), CellOps.FORMAT_5),
				CellOps.newCell((String) data.get("touch_dirsync_" + cLevel + "_errors"), CellOps.FORMAT_5),
				CellOps.newCell((String) data.get("touch_dirasync_" + cLevel + "_errors"), CellOps.FORMAT_5)));
		return row;
	}

	private int insertTouchLatency(List<GridData> grid, String cLevel, int row) {
		grid.add(CellOps.populateGrid(row++, 1,
				CellOps.newCell(validateVaue("touch_sharedcache_" + cLevel + "_latency", "touch_sharedcache_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("touch_dirsync_" + cLevel + "_latency", "touch_dirsync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("touch_dirasync_" + cLevel + "_latency", "touch_dirasync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6)));
		return row;
	}

	private int insertIozoneThroughput(List<GridData> grid, String cLevel, int row) {
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell((String) data.get("iozone_sharedcache_" + cLevel + "_total") + " total files", null)));
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("initial write", CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("iozone_sharedcache_" + cLevel + "_initial-writers", "iozone_sharedcache_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("iozone_dirsync_" + cLevel + "_initial-writers", "iozone_dirsync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("iozone_dirasync_" + cLevel + "_initial-writers", "iozone_dirasync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6)));
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("rewrite", CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("iozone_sharedcache_" + cLevel + "_rewriters", "iozone_sharedcache_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("iozone_dirsync_" + cLevel + "_rewriters", "iozone_dirsync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("iozone_dirasync_" + cLevel + "_rewriters", "iozone_dirasync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6)));
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("initial read", CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("iozone_sharedcache_" + cLevel + "_readers", "iozone_sharedcache_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("iozone_dirsync_" + cLevel + "_readers", "iozone_dirsync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("iozone_dirasync_" + cLevel + "_readers", "iozone_dirasync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6)));
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("reread", CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("iozone_sharedcache_" + cLevel + "_re-readers", "iozone_sharedcache_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("iozone_dirsync_" + cLevel + "_re-readers", "iozone_dirsync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("iozone_dirasync_" + cLevel + "_re-readers", "iozone_dirasync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6)));
		return row;
	}
	
	private int insertMediaThroughput(List<GridData> grid, String cLevel, int row) {
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell((String) data.get("media_sharedcache_" + cLevel + "_total") + " total files", null)));
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("total read", CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("media_sharedcache_" + cLevel + "_read_sum", "media_sharedcache_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("media_dirsync_" + cLevel + "_read_sum", "media_dirsync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("media_dirasync_" + cLevel + "_read_sum", "media_dirasync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6)));
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("user level variance", CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("media_sharedcache_" + cLevel + "_read_var", "media_sharedcache_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("media_dirsync_" + cLevel + "_read_var", "media_dirsync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("media_dirasync_" + cLevel + "_read_var", "media_dirasync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6)));
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("errors", CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("media_sharedcache_" + cLevel + "_read_err", "media_sharedcache_" + cLevel + "_failed"), CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("media_dirsync_" + cLevel + "_read_err", "media_dirsync_" + cLevel + "_failed"), CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("media_dirasync_" + cLevel + "_read_err", "media_dirasync_" + cLevel + "_failed"), CellOps.FORMAT_5)));
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("total write", CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("media_sharedcache_" + cLevel + "_write_sum", "media_sharedcache_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("media_dirsync_" + cLevel + "_write_sum", "media_dirsync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("media_dirasync_" + cLevel + "_write_sum", "media_dirasync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6)));
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("user level variance", CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("media_sharedcache_" + cLevel + "_write_var", "media_sharedcache_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("media_dirsync_" + cLevel + "_write_var", "media_dirsync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("media_dirasync_" + cLevel + "_write_var", "media_dirasync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6)));
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("errors", CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("media_sharedcache_" + cLevel + "_write_err", "media_sharedcache_" + cLevel + "_failed"), CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("media_dirsync_" + cLevel + "_write_err", "media_dirsync_" + cLevel + "_failed"), CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("media_dirasync_" + cLevel + "_write_err", "media_dirasync_" + cLevel + "_failed"), CellOps.FORMAT_5)));
		return row;
	}

	private int insertFsmdThroughput(List<GridData> grid, String cLevel, int row) {
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell(String.format("%s levels, %s, sub-directorys per directory per client, 5 files per directory per client",
						(String) data.get("fsmd_sharedcache_" + cLevel + "_num_levels"),
						(String) data.get("fsmd_sharedcache_" + cLevel + "_num_dirs"),
						(String) data.get("fsmd_sharedcache_" + cLevel + "_num_files")), null)));
		row++;
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell(String.format("CREATE DIR (%s)", (String) data.get("fsmd_sharedcache_" + cLevel + "_create_dir_total")), null),
				CellOps.newCell(validateVaue("fsmd_sharedcache_" + cLevel + "_create_dir_thr", "fsmd_sharedcache_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("fsmd_dirsync_" + cLevel + "_create_dir_thr", "fsmd_dirsync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("fsmd_dirasync_" + cLevel + "_create_dir_thr", "fsmd_dirasync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6)));
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("errors", CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("fsmd_sharedcache_" + cLevel + "_create_dir_error", "fsmd_sharedcache_" + cLevel + "_failed"), CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("fsmd_dirsync_" + cLevel + "_create_dir_error", "fsmd_dirsync_" + cLevel + "_failed"), CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("fsmd_dirasync_" + cLevel + "_create_dir_error", "fsmd_dirasync_" + cLevel + "_failed"), CellOps.FORMAT_5)));
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell(String.format("CREATE FILE (%s)", (String) data.get("fsmd_sharedcache_" + cLevel + "_create_file_total")), null),
				CellOps.newCell(validateVaue("fsmd_sharedcache_" + cLevel + "_create_file_thr", "fsmd_sharedcache_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("fsmd_dirsync_" + cLevel + "_create_file_thr", "fsmd_dirsync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("fsmd_dirasync_" + cLevel + "_create_file_thr", "fsmd_dirasync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6)));
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("errors", CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("fsmd_sharedcache_" + cLevel + "_create_file_error", "fsmd_sharedcache_" + cLevel + "_failed"), CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("fsmd_dirsync_" + cLevel + "_create_file_error", "fsmd_dirsync_" + cLevel + "_failed"), CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("fsmd_dirasync_" + cLevel + "_create_file_error", "fsmd_dirasync_" + cLevel + "_failed"), CellOps.FORMAT_5)));
		row++;
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell(String.format("UPDATE DIR (%s)", (String) data.get("fsmd_sharedcache_" + cLevel + "_update_dir_total")), null),
				CellOps.newCell(validateVaue("fsmd_sharedcache_" + cLevel + "_update_dir_thr", "fsmd_sharedcache_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("fsmd_dirsync_" + cLevel + "_update_dir_thr", "fsmd_dirsync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("fsmd_dirasync_" + cLevel + "_update_dir_thr", "fsmd_dirasync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6)));
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("errors", CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("fsmd_sharedcache_" + cLevel + "_update_dir_error", "fsmd_sharedcache_" + cLevel + "_failed"), CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("fsmd_dirsync_" + cLevel + "_update_dir_error", "fsmd_dirsync_" + cLevel + "_failed"), CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("fsmd_dirasync_" + cLevel + "_update_dir_error", "fsmd_dirasync_" + cLevel + "_failed"), CellOps.FORMAT_5)));
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell(String.format("UPDATE FILE (%s)", (String) data.get("fsmd_sharedcache_" + cLevel + "_update_file_total")), null),
				CellOps.newCell(validateVaue("fsmd_sharedcache_" + cLevel + "_update_file_thr", "fsmd_sharedcache_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("fsmd_dirsync_" + cLevel + "_update_file_thr", "fsmd_dirsync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("fsmd_dirasync_" + cLevel + "_update_file_thr", "fsmd_dirasync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6)));
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("errors", CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("fsmd_sharedcache_" + cLevel + "_update_file_error", "fsmd_sharedcache_" + cLevel + "_failed"), CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("fsmd_dirsync_" + cLevel + "_update_file_error", "fsmd_dirsync_" + cLevel + "_failed"), CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("fsmd_dirasync_" + cLevel + "_update_file_error", "fsmd_dirasync_" + cLevel + "_failed"), CellOps.FORMAT_5)));
		row++;
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell(String.format("DELETE DIR (%s)", (String) data.get("fsmd_sharedcache_" + cLevel + "_delete_dir_total")), null),
				CellOps.newCell(validateVaue("fsmd_sharedcache_" + cLevel + "_delete_dir_thr", "fsmd_sharedcache_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("fsmd_dirsync_" + cLevel + "_delete_dir_thr", "fsmd_dirsync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("fsmd_dirasync_" + cLevel + "_delete_dir_thr", "fsmd_dirasync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6)));
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("errors", CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("fsmd_sharedcache_" + cLevel + "_delete_dir_error", "fsmd_sharedcache_" + cLevel + "_failed"), CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("fsmd_dirsync_" + cLevel + "_delete_dir_error", "fsmd_dirsync_" + cLevel + "_failed"), CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("fsmd_dirasync_" + cLevel + "_delete_dir_error", "fsmd_dirasync_" + cLevel + "_failed"), CellOps.FORMAT_5)));
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell(String.format("DELETE FILE (%s)", (String) data.get("fsmd_sharedcache_" + cLevel + "_delete_file_total")), null),
				CellOps.newCell(validateVaue("fsmd_sharedcache_" + cLevel + "_delete_file_thr", "fsmd_sharedcache_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("fsmd_dirsync_" + cLevel + "_delete_file_thr", "fsmd_dirsync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6),
				CellOps.newCell(validateVaue("fsmd_dirasync_" + cLevel + "_delete_file_thr", "fsmd_dirasync_" + cLevel + "_failed"), CellOps.FORMAT_4, CellOps.FORMAT_6)));
		grid.add(CellOps.populateGrid(row++, 0,
				CellOps.newCell("errors", CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("fsmd_sharedcache_" + cLevel + "_delete_file_error", "fsmd_sharedcache_" + cLevel + "_failed"), CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("fsmd_dirsync_" + cLevel + "_delete_file_error", "fsmd_dirsync_" + cLevel + "_failed"), CellOps.FORMAT_5),
				CellOps.newCell(validateVaue("fsmd_dirasync_" + cLevel + "_delete_file_error", "fsmd_dirasync_" + cLevel + "_failed"), CellOps.FORMAT_5)));
		return row;
	}
	
	private Object validateVaue(String dataTarget, String dataCheck) {
		return "0".equals(data.get(dataCheck)) ? Double.parseDouble((String) data.get(dataTarget)) :
													(String) data.get(dataCheck) + " clients failed to use the target directory";
	}

	public static void main(String[] args) {
		try {
			BenchReport br = null;
			switch (args.length) {
			case 1:
				br = new BenchReport(args[0]);
				break;
			case 2:
				br = new BenchReport(args[0], args[1]);
				break;
			default:
				System.out.println("Usage: benchreport result_data> [<google credential json>]");
				System.exit(-1);
			}
			br.publish();
		} catch (GeneralSecurityException | IOException e1) {
			e1.printStackTrace();
		}
	}
	
	static class CellOps {
		static CellFormat FORMAT_1 = new CellFormat().setTextFormat(new TextFormat().setFontSize(14).setBold(true));
		static CellFormat FORMAT_2 = new CellFormat().setTextFormat(new TextFormat().setBold(true));
		static CellFormat FORMAT_3 = new CellFormat().setTextFormat(new TextFormat().setFontSize(11).setBold(true));
		static CellFormat FORMAT_4 = new CellFormat().setHorizontalAlignment("Right")
				.setNumberFormat(new NumberFormat().setType("NUMBER").setPattern("0.00"));
		static CellFormat FORMAT_5 = new CellFormat().setHorizontalAlignment("Right");
		static CellFormat FORMAT_6 = new CellFormat().setHorizontalAlignment("Left");
		
		static CellData newCell(Object data, CellFormat format) {
			return newCell(data, format, format);
		}

		static CellData newCell(Object data, CellFormat format, CellFormat altFormat) {
			if (data instanceof Double) {
				return new CellData().setUserEnteredValue(new ExtendedValue().setNumberValue((Double)data)).setUserEnteredFormat(format);				
			}
			return new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(data.toString())).setUserEnteredFormat(altFormat);
		}

		static GridData populateGrid(Integer startRow, Integer startColumn, CellData...cellData) {
			return new GridData().setStartRow(startRow).setStartColumn(startColumn)
					.setRowData(Arrays.asList(new RowData().setValues(Arrays.asList(cellData))));
		}
	}

}
