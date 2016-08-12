package idare.GEOSoft;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;

/**
 * Default handler that generates a GUI for the {@link Tunable} structure used by the {@link GEOSoftReader}
 * @author Thomas Pfau
 *
 */
public class GEOTunableGUIHandler extends AbstractGUITunableHandler {

	private GEOSoftReader reader;
	private PropertiesSelectionGUI mypanel;

	
	protected GEOTunableGUIHandler(Method getter, Method setter,
			Object instance, Tunable tunable, GEOSoftReader reader) {
		super(getter, setter, instance, tunable);
		this.reader = reader;
		init();
	}

	protected GEOTunableGUIHandler(Field field, Object instance,
			Tunable tunable, GEOSoftReader reader) {
		super(field, instance, tunable);
		this.reader = reader;
		init();
	}
	
	/**
	 * Set up the GUI according to the information from the reader.
	 */
	private void init()
	{
		mypanel = new PropertiesSelectionGUI(reader.DataSetTitle,reader.propertyIDs,reader.groupSets,reader.twocolumn);
		panel = mypanel;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.cytoscape.work.swing.AbstractGUITunableHandler#handle()
	 */
	@Override
	public void handle() {
		try{
			setValue(mypanel.getGEOSoftTunables());
		}
		catch(IllegalAccessException| InvocationTargetException e)
		{
			e.printStackTrace(System.out);
		}

	}

}
