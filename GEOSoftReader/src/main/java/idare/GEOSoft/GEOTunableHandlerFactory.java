package idare.GEOSoft;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableHandlerFactory;
import org.cytoscape.work.swing.GUITunableHandlerFactory;

/**
 * Simple {@link TunableHandlerFactory} for a {@link GEOSoftReader}
 * @author Thomas Pfau
 *
 */
public class GEOTunableHandlerFactory implements GUITunableHandlerFactory<GEOTunableGUIHandler> {
	private GEOSoftReader reader;

	/**
	 * The factory needs to be associated with a specific reader.
	 * @param reader - the {@link GEOSoftReader} used
	 */
	public GEOTunableHandlerFactory(GEOSoftReader reader) {
		// TODO Auto-generated constructor stub
		this.reader = reader;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.cytoscape.work.TunableHandlerFactory#createTunableHandler(java.lang.reflect.Field, java.lang.Object, org.cytoscape.work.Tunable)
	 */
	@Override
	public GEOTunableGUIHandler createTunableHandler(Field arg0, Object arg1,
			Tunable arg2) {
		// if the TunableClass fits do something, else don't
		if(!GEOSoftTunables.class.isAssignableFrom(arg0.getType()))
		{
			return null;
		}

		return new GEOTunableGUIHandler(arg0,arg1,arg2,reader);
	}
	/*
	 * (non-Javadoc)
	 * @see org.cytoscape.work.TunableHandlerFactory#createTunableHandler(java.lang.reflect.Method, java.lang.reflect.Method, java.lang.Object, org.cytoscape.work.Tunable)
	 */
	@Override
	public GEOTunableGUIHandler createTunableHandler(Method arg0,
			Method arg1, Object arg2, Tunable arg3) {
		// if the TunableClass fits do something, else don't
		if(!GEOSoftTunables.class.isAssignableFrom(arg0.getReturnType()))
		{
			return null;
		}
		return new GEOTunableGUIHandler(arg0,arg1,arg2,arg3,reader);
	}

}
