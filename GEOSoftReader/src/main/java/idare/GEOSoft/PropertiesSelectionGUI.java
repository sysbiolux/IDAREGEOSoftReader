package idare.GEOSoft;

import idare.GEOSoft.GEOSoftReader.Interpretation;
import idare.ThirdParty.BoundsPopupMenuListener;
import idare.imagenode.internal.Utilities.GUIUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * A GUI that collects information about a GEO Soft File and how to interpret it.
 * @author Thomas Pfau
 *
 */
public class PropertiesSelectionGUI extends JPanel{

	/**
	 * Selectors for Labels and IDs used.
	 */
	private JComboBox<String> IDSelector = new JComboBox<String>();
	private JComboBox<String> LabelSelector = new JComboBox<String>();
	
	
	/**
	 * Buttons to select the type of interpretation
	 */
	private ButtonGroup interpretationOptions;
	JRadioButton oneSheetButton = new JRadioButton("Use a single sheet, with samples as entries.");
	JRadioButton singleSheetAverageButton = new JRadioButton("Use a single sheet with groups as entries and calc averages for groups.");
	JRadioButton multipleSheetButtonNumeric = new JRadioButton("<html>Use multiple sheets, one per group.<br> This option should be used with Scatter plot representation.</html>");
	JRadioButton multipleSheetButtonGroup = new JRadioButton("Use multiple sheets, Each Sheet representing the average of a group.");
	private Interpretation selectedInterpretation;
	
	/**
	 * Information obtained from the reader to adjust the display of the selection options.
	 */
	private String Title;
	private Vector<String> properties;
	private HashMap<String,Set<String>> subSetGroups;
	private boolean twocolumn;
	private Color background;
		
		
	/**
	 * The Gui needs some information for proper display.
	 * @param Title The Title of the DataSet
	 * @param properties the properties available in the dataset
	 * @param setDescriptions the groups available in the dataset
	 * @param twocolumn whether this is for a twocolumn or onecolum dataset.
	 */
	public PropertiesSelectionGUI(String Title, Vector<String> properties, HashMap<String,Set<String>> setDescriptions, boolean twocolumn)
	{
		super();
		this.Title = Title;
		this.twocolumn = twocolumn;		
		this.properties = properties;
		this.subSetGroups = setDescriptions;
		
		//set up the buttoins to update the Interpretation
		oneSheetButton.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if(e.getStateChange() == ItemEvent.SELECTED)
				{
					selectedInterpretation = Interpretation.SINGLE_SHEET_SAMPLES;
				}
			}
		});
		multipleSheetButtonNumeric.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if(e.getStateChange() == ItemEvent.SELECTED)
				{
					selectedInterpretation = Interpretation.MULTI_SHEET_NUMERIC;
				}
			}
		});
		
		multipleSheetButtonGroup.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if(e.getStateChange() == ItemEvent.SELECTED)
				{
					selectedInterpretation = Interpretation.MULTI_SHEET_GROUPS;
				}
			}
		});
		
		
		singleSheetAverageButton.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if(e.getStateChange() == ItemEvent.SELECTED)
				{
					selectedInterpretation  = Interpretation.SINGLE_SHEET_AVERAGE;
				}
			}
		});
	
		setupPanel();
	}

	public void setupPanel()
	{
		background = this.getBackground();		
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		//add the title
		JTextField titleField = new JTextField();
		if(Title.length() > 40)
		{
			Title = Title.substring(0, 40) + "...";
		}
		titleField.setText("Select properties for Dataset \"" + Title + "\"");
		titleField.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
		titleField.setBackground(this.getBackground());
		titleField.setCursor(null);
		titleField.setEditable(false);

		this.add(titleField);
		//add the Label/ID Selection
		this.add(createIDAndLabelSelection());
		//add Interpretation selection
		this.add(createInterpretationSelection());			
	}

	private JPanel createIDAndLabelSelection()
	{
		JPanel LabelAndIDSelection = new JPanel();
		LabelAndIDSelection.setLayout(new BoxLayout(LabelAndIDSelection, BoxLayout.PAGE_AXIS));
		BoundsPopupMenuListener listener = new BoundsPopupMenuListener(true, false);
		//Set up the ID Selector and the Label Selector 
		IDSelector = new JComboBox<String>(new DefaultComboBoxModel<String>(properties));
		IDSelector.setSelectedIndex(1);
		IDSelector.setSelectedIndex(0);
		IDSelector.setEditable(false);		
		IDSelector.addPopupMenuListener( listener );
		LabelSelector = new JComboBox<String>(new DefaultComboBoxModel<String>(properties));		
		LabelSelector.setSelectedIndex(1);
		LabelSelector.setEditable(false);
		LabelSelector.addPopupMenuListener( listener );
		LabelAndIDSelection.add(GUIUtils.createSelectionPanel("Property to determine IDs", IDSelector,background));
		//only add the Labelselection if this is twocolumns (otherwise this would be pointless)
		if(twocolumn)
		{
			LabelAndIDSelection.add(GUIUtils.createSelectionPanel("Property to determine Labels", LabelSelector,background));
		}		
		return LabelAndIDSelection;
	}

	
	

	private JPanel createInterpretationSelection()
	{
		JPanel interpretationSelection = new JPanel();
		//JRadioButton oneSheetButton = new JRadioButton("Use a single sheet, with samples as entries.");
		//JRadioButton multipleSheetButton = new JRadioButton("Use multiple sheets, one per group (see right).");
		//JRadioButton singleSheetAverageButton = new JRadioButton("Use a single sheet with groups as entries and calc averages for groups.");		
		interpretationOptions = new ButtonGroup();
		interpretationOptions.add(oneSheetButton);
		interpretationOptions.add(multipleSheetButtonNumeric);
		interpretationOptions.add(singleSheetAverageButton);
		interpretationOptions.add(multipleSheetButtonGroup);
		interpretationOptions.setSelected(oneSheetButton.getModel(), true);		
		interpretationSelection.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridheight = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridy = 0;
		interpretationSelection.add(oneSheetButton, gbc);
		gbc.gridy = 1;
		interpretationSelection.add(singleSheetAverageButton, gbc);
		gbc.gridy = 2;		
		interpretationSelection.add(multipleSheetButtonNumeric, gbc);
		gbc.gridy = 3;		
		interpretationSelection.add(multipleSheetButtonGroup, gbc);
		
		gbc.gridy = 0;
		
		gbc.gridheight = 4;
		JPanel GroupPanel = createGroupPanel();
		gbc.gridx = 1;
		interpretationSelection.add(GroupPanel,gbc);
		return interpretationSelection;

	}
	
	/**
	 * Create the Table holding the groups.
	 * @return a JPanel containing the Group Table.
	 */
	private JPanel createGroupPanel()
	{
		JPanel grouppanel = new JPanel();
		//add a Title
		JTextField titleField = new JTextField();
		titleField.setText("Available Groups:");
		titleField.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
		titleField.setBackground(grouppanel.getBackground());
		titleField.setCursor(null);
		titleField.setEditable(false);
		grouppanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weighty = 1;
		gbc.gridy = 0;		
		gbc.fill = GridBagConstraints.BOTH;
		grouppanel.add(titleField,gbc);
		//generate the table
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		Vector<String> ColumnNames = new Vector<String>();		
		int maxVectorSize = 0;
		for(String groupid : subSetGroups.keySet())
		{
			//the titles are the groupIDs 
			ColumnNames.add(groupid);
			maxVectorSize = Math.max(maxVectorSize,subSetGroups.get(groupid).size());
			//the entries are the individual samples
			for(int i = data.size(); i < maxVectorSize; i++)
			{
				data.add(new Vector<Object>(Arrays.asList(new String[subSetGroups.keySet().size()])));
			}			
		}
		
		int group = 0;
		for(String groupid : ColumnNames)
		{
			int i = 0;
			for(String sampleid : subSetGroups.get(groupid))
			{
				data.get(i).set(group, sampleid);
				i++;
			}
			group++;
		}
		DefaultTableModel tm = new DefaultTableModel(data,ColumnNames);

		JTable GroupTable = new JTable(tm);		
		GroupTable.setPreferredScrollableViewportSize(new Dimension(80*tm.getColumnCount(),Math.min(11, tm.getRowCount()+1)*GroupTable.getRowHeight()));
		JScrollPane pane = new JScrollPane(GroupTable);
		pane.setPreferredSize(new Dimension(300,150));
		gbc.gridy = 1;
		gbc.weighty = 10;
		grouppanel.add(pane,gbc);		
		return grouppanel;
	}
	
	/**
	 * Get the Values for the Tunable based on the selection in the GUI.
	 * @return
	 */
	GEOSoftTunables getGEOSoftTunables()
	{
		GEOSoftTunables tunables = new GEOSoftTunables();
		tunables.IDString = IDSelector.getSelectedItem().toString();
		if(!twocolumn)
		{
			tunables.LabelString = tunables.IDString;
		}
		else
		{
			tunables.LabelString = LabelSelector.getSelectedItem().toString();					
		}
		tunables.Settings = selectedInterpretation;
		return tunables;
	}
}
