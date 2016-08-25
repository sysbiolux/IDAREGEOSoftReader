package idare.GEOSoft;

import idare.imagenode.Interfaces.DataSetReaders.IDAREDatasetReader;
import idare.imagenode.Interfaces.DataSetReaders.IDAREReaderSetupTask;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARECell;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARECell.CellType;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDAREWorkbook;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.BasicImplementation.BasicIDARECell;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.BasicImplementation.BasicIDARERow;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.BasicImplementation.BasicIDARESheet;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.BasicImplementation.BasicIDAREWorkbook;
import idare.imagenode.Utilities.StringUtils;
import idare.imagenode.exceptions.io.WrongFormat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

/**
 * A Reader that reads the first DataSet stored in a GEO Soft file. 
 * @author Thomas Pfau
 *
 */
public class GEOSoftReader extends IDAREDatasetReader {

	/**
	 * Several different ways how the data can be interpreted.
	 * @author Thomas Pfau
	 *
	 */
	public enum Interpretation{
		SINGLE_SHEET_SAMPLES,
		MULTI_SHEET_GROUPS,
		SINGLE_SHEET_AVERAGE,
		MULTI_SHEET_NUMERIC,
		CREATE_NO_WORKBOOK,
	}
	
	/**
	 * Some definitions of fields in a GEO File.
	 */
	public static final String FIELDSTART = "^";
	public static final String ENTRYSTART = "!";
	public static final String COMMENTSTART = "#";
	public static final String DATABASEID = "^DATABASE";
	public static final String DATASETID = "^DATASET";
	public static final String SUBSSETID = "^SUBSET";
	public static final String SUBSET_DESCRIPTION = "!subset_description";
	public static final String SUBSET_SAMPLE_IDS = "!subset_sample_id";	
	public static final String DATASET_TABLE_START = "!dataset_table_begin";
	public static final String DATASET_TITLE = "!dataset_title";
	public static final String DATASET_END = "!dataset_table_end";
	

	
	/**
	 * Properties necessary for interpretation of the Dataset
	 */
	private String IDString;
	private String LabelString;
	public Boolean twocolumn;
	private Interpretation setting;
	
	/**
	 * Properties necessary to obtain the status of the reader
	 */
	private File inputFileUsedForSetup;
	
	/**
	 * Data for the dataset that is being read.
	 */
	public String DataSetTitle;
	public Vector<String> propertyIDs;
	HashMap<String,Vector<String>> sampleSets = new HashMap<String, Vector<String>>();
	Vector<String> subsetIDs = new Vector<String>();
	HashMap<String,Integer> columnpositions = new HashMap<String, Integer>();
	public HashMap<String,Set<String>> groupSets = new HashMap<String, Set<String>>();
	HashSet<String> sampleIDs = new HashSet<String>();
	
	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSetReaders.IDAREDatasetReader#readData(java.io.File)
	 */
	@Override
	public IDAREWorkbook readData(File inputfile)
			throws WrongFormat, IOException {
		//If the inputFile does not match the file this reader was set up for, we should not continue.
		if(!inputfile.equals(inputFileUsedForSetup))
		{
			throw new IOException("Inputfile while reading different to input file used for setup!" );
		}
		BufferedReader br = new BufferedReader(new FileReader(inputfile));
		IDAREWorkbook geoWB = null;
		String currentline = br.readLine();
		//Skip to the start of a table.
		while(currentline != null)
		{
			if(currentline.startsWith(DATASET_TABLE_START))
			{
				//and then read the Dataset
				 geoWB = readDataSetData(br);
				break;
			}
			currentline = br.readLine();
		}		
		br.close();
		return geoWB;
	}

	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSetReaders.IDAREDatasetReader#fileTypeAccepted(java.io.File)
	 */
	@Override
	public boolean fileTypeAccepted(File inputfile) {
		//We accept only .soft files
		String Filename = inputfile.getName();		
		return Filename.endsWith(".soft");
	}
	
	/**
	 * Read the Headers. This is necessary to set up the reader for further computation.
	 * @param br - the reader 
	 * @throws WrongFormat if the format is wrong
	 * @throws IOException if the file cannot be read
	 */
	private void readHeaders(BufferedReader br) throws WrongFormat,IOException
	{
		String currentline = br.readLine();
		while(currentline != null)
		{
			if(currentline.startsWith("^"))
			{
				switch(currentline.split("=")[0].trim())
				{
					case DATABASEID:
					{
						currentline = readDataBase(currentline,br);
						break;
					}
					case DATASETID:
					{
						currentline = determineProperties(currentline,br);
						break;
					}
					case SUBSSETID:
					{
						currentline = readSubSetData(currentline,br);
						break;
					}
					default:
						currentline = br.readLine();
				}
			}
			else
			{ 
				currentline = br.readLine();
			}
			System.out.println("Reading: " + currentline);
					
		}				
				
	}
	
	/**
	 * This essentially skips the Database information, as we don't need it.
	 * @param br - the current reader
	 * @param currentline the line the reader is currently at (ignored here)
	 * @return the line the reader is at after skipping the Database information
	 * @throws IOException
	 */
	private String readDataBase(String currentline, BufferedReader br) throws IOException
	{
		currentline = br.readLine();
		while(!currentline.startsWith("^"))
		{
			currentline = br.readLine();
		}
		return currentline;
	}
	
	/**
	 * Read data about subsets in the Dataset. This can be used to define the interpretation of the 
	 * Data.
	 * @param br - the current reader
	 * @param currentline the line the reader is currently at (ignored here)
	 * @return the line the reader is at after skipping the Database information
	 * @throws IOException
	 */
	private String readSubSetData(String currentline,BufferedReader br) throws IOException
	{
		String subsetDescription = "";
		Vector<String> sampleIDs = new Vector<String>();
		currentline = br.readLine();
		while(!currentline.startsWith("^"))
		{
			if(currentline.startsWith(SUBSET_DESCRIPTION))
			{
				//Get the Description (which starts after the = sign.
				subsetDescription = currentline.split(" = ")[1].trim();
			}
			if(currentline.startsWith(SUBSET_SAMPLE_IDS))
			{
				//The Sample IDs are stored with , separating the sample.
				sampleIDs.addAll(Arrays.asList(currentline.split(" = ")[1].trim().split(",")));			
			}
			
			currentline = br.readLine();
		}		
		subsetIDs.add(subsetDescription);
		sampleSets.put(subsetDescription,sampleIDs);
		this.sampleIDs.addAll(sampleIDs);
		return currentline;
	}
	
	/**
	 * Read the properties of the dataset (i.e. the first line and divide it into sample IDs and Annotation data. 
	 * @param currentline the line the reader is currently at (ignored here)
	 * @param br - the current reader
	 * @return the line the reader is at after skipping the Database information
	 * @throws IOException
	 */
	private String determineProperties(String currentline,BufferedReader br) throws IOException
	{
		currentline = br.readLine();
		System.out.println("Reading Dataset Information");
		while(currentline != null && !currentline.startsWith("^"))
		{
			if(currentline.startsWith(DATASET_TABLE_START))
			{
				prepareInformationRequest(br);
				//after this, we are done.
				return null;
			}
			if(currentline.startsWith(DATASET_TITLE))
			{
				DataSetTitle = currentline.split(" = ")[1];
			}
			currentline = br.readLine();
		}
		return currentline;
	}
		
	/**
	 * Prepares the field necessary for the GUI to get the user input. 
	 * @param br - the reader used. (the next line is the property line.
	 * @throws IOException
	 */
	private void prepareInformationRequest(BufferedReader br) throws IOException
	{
		String currentline = br.readLine();
		System.out.println(currentline);
		Vector<String> DataIDs = new Vector<String>(Arrays.asList(currentline.split("\t")));
		//Set up the ID to position map.
		for(int i = 0; i < DataIDs.size(); i++)
		{
			columnpositions.put(DataIDs.get(i),i);
		}
		propertyIDs = new Vector<String>(DataIDs);
		HashMap<String,String> SampleDescriptions = new HashMap<String, String>();
		//Set up the property Vector.
		// And the Map used for Group Description.
		for(String subSetID : subsetIDs)
		{					
			propertyIDs.removeAll(sampleSets.get(subSetID));
			for(String sampleID : sampleSets.get(subSetID))
			{
				if(!SampleDescriptions.containsKey(sampleID))
				{
					SampleDescriptions.put(sampleID, subSetID);
				}
				else
				{
					SampleDescriptions.put(sampleID, SampleDescriptions.get(sampleID) + "_" + subSetID);
				}
			}
		}
		groupSets = new HashMap<String, Set<String>>();
		// Set up the groups (i.e. all samples with the same group id ) are added to one group.
		// this means all samples sharing the same subsets are combined.
		for(String sampleID : SampleDescriptions.keySet())
		{
			String Description = SampleDescriptions.get(sampleID);
			if(!groupSets.containsKey(Description))
			{
				groupSets.put(Description, new HashSet<String>());
			}
			groupSets.get(Description).add(sampleID);
		}
				
	}
	
	/**
	 * Set the interpretation of the input file.
	 * @param setting
	 */
	public void setInterpretation(Interpretation setting)
	{
		this.setting = setting;
		
	}
	
	/**
	 * Set the ID column to use
	 * @param setting
	 */	
	public void setIDColumn(String IDColumn)
	{
		this.IDString = IDColumn;			
	}
	
	/**
	 * Set the Label column to use (only relevant for twocolumn data)
	 * @param setting
	 */
	public void setLabelColumn(String LabelColumn)
	{
		this.LabelString = LabelColumn;
	}
	
	/**
	 * Read the data of the dataset.
	 * @param br
	 * @throws IOException
	 */
	private IDAREWorkbook readDataSetData(BufferedReader br) throws IOException
	{
		HashMap<String,Vector<String>> groups = new HashMap<String, Vector<String>>();
		String sheetTitle = "";
		IDAREWorkbook wb = null;
		if(setting == Interpretation.CREATE_NO_WORKBOOK)
		{
			return wb;
		}
		//Set up the Samples.
		//groups will contain those values which are averaged.
		if(setting == Interpretation.SINGLE_SHEET_SAMPLES)
		{
			for(String sample : sampleIDs)
			{
				Vector<String> singleSample = new Vector<String>();
				singleSample.add(sample);
				groups.put(sample, singleSample);
				sheetTitle = "Individual Samples of GEO Dataset";				
			}
		}
		if(setting == Interpretation.SINGLE_SHEET_AVERAGE)
		{
			for(String sample : groupSets.keySet())
			{
				Vector<String> groupSamples = new Vector<String>();
				groupSamples.addAll(groupSets.get(sample));
				groups.put(sample, groupSamples);
				sheetTitle = "Averaged Sample Groups of GEO Dataset";
			}
		}
		if(setting == Interpretation.MULTI_SHEET_GROUPS || setting == Interpretation.MULTI_SHEET_NUMERIC)
		{
			for(String sample : groupSets.keySet())
			{
				Vector<String> groupSamples = new Vector<String>();
				groupSamples.addAll(groupSets.get(sample));
				groups.put(sample, groupSamples);
			}
		}
		if(setting == Interpretation.SINGLE_SHEET_AVERAGE || setting == Interpretation.SINGLE_SHEET_SAMPLES)
		{
			wb = readSingleSheet(groups,sheetTitle,br);
		}
		if(setting == Interpretation.MULTI_SHEET_NUMERIC)
		{
			wb = readMultiSheet(groups,br,true);
		}
		if(setting == Interpretation.MULTI_SHEET_GROUPS)
		{
			wb = readMultiSheet(groups,br,false);
		}
		return wb;
	}
	
	private IDAREWorkbook readMultiSheet(HashMap<String,Vector<String>> groups,BufferedReader br, boolean numeric) throws IOException
	{
		BasicIDAREWorkbook geowb = new BasicIDAREWorkbook();
		HashMap<String,BasicIDARESheet> sheets = new HashMap<String, BasicIDARESheet>();		
		int IDposition =  columnpositions.get(IDString);
		int LabelPosition = columnpositions.get(LabelString);
		HashMap<String,Vector<Integer>> groupitempositions = new HashMap<String, Vector<Integer>>();
		Random rng = new Random();
		rng.setSeed(System.currentTimeMillis());
		for(String groupid : groups.keySet())
		{
			BasicIDARESheet newSheet = geowb.createSheet(groupid);
			sheets.put(groupid, newSheet);
			BasicIDARERow headerrow = newSheet.createRow();
			headerrow.createCell(CellType.BLANK);
			if(twocolumn)
			{
				headerrow.createCell(CellType.BLANK);	
			}			
			double sheetcenter = sheets.size();			
			groupitempositions.put(groupid, new Vector<Integer>());
			if(numeric)
			{
				for(String element : groups.get(groupid)){
					groupitempositions.get(groupid).add(columnpositions.get(element));
					BasicIDARECell currentcell = headerrow.createCell(CellType.NUMERIC);				
					currentcell.setValue(Double.toString(sheetcenter + rng.nextDouble()*0.3));
				}
			}
			else
			{
				BasicIDARECell currentcell = headerrow.createCell(CellType.STRING);
				currentcell.setValue("Avergage Value");
				for(String element : groups.get(groupid)){
					groupitempositions.get(groupid).add(columnpositions.get(element));
				}

			}
			
		}
		//This is the header line, so we need the next one.
		String currentline = br.readLine();
		currentline = br.readLine();
		double maxrange = 0;
		while(currentline != null && !currentline.startsWith(FIELDSTART) && !currentline.startsWith(ENTRYSTART) && !currentline.startsWith(DATASET_END))
		{
			
//			System.out.println(currentline);
			Vector<Double> values = new Vector<Double>();
			double meanval = 0.;
			double usedvals = 0.;
			String[] items = currentline.split("\t");
			for(String groupid : groupitempositions.keySet())			
			{
				for(Integer pos : groupitempositions.get(groupid))
				{
					if(StringUtils.isNumeric(items[pos]))
					{
						Double val = Double.parseDouble(items[pos]); 
						values.add(val);
						usedvals+=1.;
						meanval+=val;
					}
					else
					{
						values.add(null);
					}					
				}			
			}
			if(usedvals != 0.)
			{
				meanval/=usedvals;
			}
			
			//this is the data set up.
			for(String groupid : groupitempositions.keySet())			
			{
								
				
				BasicIDARESheet sheet = sheets.get(groupid);
				BasicIDARERow currentrow = sheet.createRow();
				
				//First initialize the "Identifiers/Label"
				BasicIDARECell idcell = currentrow.createCell(CellType.STRING);
				idcell.setValue(items[IDposition]);
				if(twocolumn)
				{
					BasicIDARECell labelcell = currentrow.createCell(CellType.STRING);
					labelcell.setValue(items[LabelPosition]);
				}


				if(numeric)
				{
					for(Double d : values)
					{
						if(d!= null)
						{
							BasicIDARECell currentcell = currentrow.createCell(CellType.NUMERIC);
							currentcell.setValue(Double.toString(d-meanval));
							if(maxrange < Math.abs(d-meanval))
							{
								maxrange = Math.abs(d-meanval);
							}
						}
						else
						{
							currentrow.createNullCell();
						}
					}
				}
				else
				{
					if(usedvals != 0)
					{
						BasicIDARECell currentcell = currentrow.createCell(CellType.NUMERIC);
						currentcell.setValue(Double.toString(meanval));
					}
					else
					{
						currentrow.createNullCell();
					}
				}
				
			}
			currentline = br.readLine();
		}
		
		return geowb;
	}

	
	
	private BasicIDAREWorkbook readSingleSheet(HashMap<String,Vector<String>> groups, String Title,BufferedReader br) throws IOException
	{
		BasicIDAREWorkbook geowb = new BasicIDAREWorkbook();
		BasicIDARESheet sheet = geowb.createSheet(Title);
		BasicIDARERow headerrow = sheet.createRow();
		headerrow.createCell(CellType.BLANK);
		if(twocolumn)
		{
			headerrow.createCell(CellType.BLANK);	
		}
		Vector<String> grouporder = new Vector<String>();
		int IDposition =  columnpositions.get(IDString);
		int LabelPosition = columnpositions.get(LabelString);
		
		HashMap<String,Vector<Integer>> groupitempositions = new HashMap<String, Vector<Integer>>();
		for(String groupid : groups.keySet())
		{
			grouporder.add(groupid);
			BasicIDARECell currentcell = headerrow.createCell(CellType.STRING);
			groupitempositions.put(groupid, new Vector<Integer>());
			for(String element : groups.get(groupid)){
				groupitempositions.get(groupid).add(columnpositions.get(element));
			}
			currentcell.setValue(groupid);		
		}
		for(IDARECell cell : headerrow)
		{
			System.out.print(cell.getFormattedCellValue() + "\t");
		}
		System.out.println();
		int maxlines = 0;
		//this is the header line
		String currentline = br.readLine();
		//so we need the next one.
		currentline = br.readLine();
		while(currentline != null && !currentline.startsWith(FIELDSTART) && !currentline.startsWith(ENTRYSTART) && !currentline.startsWith(DATASET_END))
		{
			//System.out.println(currentline);
			//System.out.println("Creating Row");

			BasicIDARERow currentrow = sheet.createRow();
			//System.out.println("Splitting Line");			
			String[] items = currentline.split("\t");
			//First initialize the "Identifiers/Label"
			//System.out.println("Creating Cell");
			BasicIDARECell idcell = currentrow.createCell(CellType.STRING);
			//System.out.println("Setting ID Cell Value");
			idcell.setValue(items[IDposition]);
			if(twocolumn)
			{
				//System.out.println("Creating Label Cell");
				BasicIDARECell labelcell = currentrow.createCell(CellType.STRING);
				//System.out.println("Setting Label cell Value");
				labelcell.setValue(items[LabelPosition]);
			}
			for(String groupid : grouporder)
			{
				double val = 0;
				int validentries = 0;
				for(Integer pos : groupitempositions.get(groupid))
				{					
					//System.out.println("Checking if " + items[pos] + " is numeric");
					if(StringUtils.isNumeric(items[pos]))
					{
						
						validentries++;
						//System.out.println("Parsing Value");
						val += Double.parseDouble(items[pos]);
					}
						
				}
				if(validentries > 0)
				{
					//System.out.println("Calculating value");
					val /= validentries;
				}
				//System.out.println("Creating new Cell");
				BasicIDARECell newcell = currentrow.createCell(CellType.NUMERIC);
				//System.out.println("Setting Cell Value");
				newcell.setValue(Double.toString(val));
			}
			maxlines = Math.max(maxlines, currentrow.getLastCellNum());
			currentline = br.readLine();

		}
		return geowb;
	}



	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSetReaders.IDAREDatasetReader#getSetupTask(java.io.File, boolean)
	 */
	@Override
	public IDAREReaderSetupTask getSetupTask(File inputfile, boolean twocolumn) throws Exception{
		this.twocolumn = twocolumn; 
		inputFileUsedForSetup = inputfile;
		//Now, we read the headers and hopefully don't encounter an error.
		BufferedReader br = new BufferedReader(new FileReader(inputfile));
		readHeaders(br);
		br.close();
		return new GEOSetupTask(this);
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSetReaders.IDAREDatasetReader#resetReader()
	 */
	@Override
	public void resetReader() {
		System.out.println("Resetting Reader");
		//clear the stored information
		sampleSets = new HashMap<String, Vector<String>>();
		subsetIDs = new Vector<String>();
		columnpositions = new HashMap<String, Integer>();
		groupSets = new HashMap<String, Set<String>>();
		sampleIDs = new HashSet<String>();
		
		//reset the modified fields.
		IDString= null;
		LabelString= null;
		twocolumn= null;
		setting= null;
		DataSetTitle = null; 
		inputFileUsedForSetup = null;				
	}	
	
}
