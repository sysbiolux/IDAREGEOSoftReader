package idare.GEOSoft;

import idare.imagenode.Interfaces.DataSetReaders.IDAREReaderSetupTask;

import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.RequestsUIHelper;
import org.cytoscape.work.swing.TunableUIHelper;

/**
 * A SetupTask that has a single Tunable that is handled by its own Handler.
 * This tunable is set as soon as the Task is executed.
 * @author Thomas Pfau
 *
 */
public class GEOSetupTask extends IDAREReaderSetupTask implements RequestsUIHelper{

	
	@Tunable
	public GEOSoftTunables params;
		
	/**
	 * The {@link GEOSoftReader} to set up.
	 */
	private GEOSoftReader reader;
	
	/**
	 * Generate the SetupTask for a specified {@link GEOSoftReader}.
	 * @param reader
	 */
	public GEOSetupTask(GEOSoftReader reader) {
		super(reader);
		this.reader = reader;
	}	
	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSetReaders.IDAREReaderSetupTask#execute(org.cytoscape.work.TaskMonitor)
	 */
	@Override
	public void execute(TaskMonitor taskMonitor) {
		// TODO Auto-generated method stub
		reader.setIDColumn(params.IDString);
		reader.setLabelColumn(params.LabelString);
		reader.setInterpretation(params.Settings);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.cytoscape.work.swing.RequestsUIHelper#setUIHelper(org.cytoscape.work.swing.TunableUIHelper)
	 */
	@Override
	public void setUIHelper(TunableUIHelper arg0) {
		// TODO Auto-generated method stub
		
	}

}
