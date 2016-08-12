package idare.GEOSoft.internal;

import idare.GEOSoft.GEOSoftReader;
import idare.GEOSoft.GEOTunableHandlerFactory;
import idare.imagenode.Interfaces.Plugin.IDAREPlugin;
import idare.imagenode.Interfaces.Plugin.IDAREService;
import idare.imagenode.internal.Utilities.StringUtils;
import idare.imagenode.IDAREImageNodeAppService;

import java.io.File;
import java.util.Properties;
import java.util.Vector;

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyActivator extends AbstractCyActivator implements IDAREPlugin{

	Logger log;
	BundleContext context;
	GEOSoftReader pluginreader;
	@Override
	public void start(BundleContext context) throws Exception {
		this.context = context;
		pluginreader = new GEOSoftReader();
		GEOTunableHandlerFactory thf = new GEOTunableHandlerFactory(pluginreader);
		registerService(context, thf, GUITunableHandlerFactory.class, new Properties());
		try{
			//try whether the app already is registered.
			IDAREImageNodeAppService appserv = getService(context, IDAREImageNodeAppService.class);
			System.out.println("Setting LogFile To " + appserv.getlogFileName());
			System.setProperty("logfile.name", appserv.getlogFileName());			
			log = LoggerFactory.getLogger(CyActivator.class);
			System.out.println("Registering GEO Soft Reader.");
			appserv.registerPlugin(this);
		}
		catch(Exception e)
		{			
			e.printStackTrace(System.out);
		}
	
	}	
	
	public void shutDown()
	{			
		if (log != null)
		{
			System.out.println("DeRegistering GEO Soft Reader.");
		}
		try{
			IDAREImageNodeAppService appserv = getService(context, IDAREImageNodeAppService.class);
			appserv.deRegisterPlugin(this);
		}
		catch(Exception e)
		{
			System.out.println("No App to deregister from.");
		}
	}

	@Override
	public Vector<IDAREService> getServices() {
		// TODO Auto-generated method stub
		Vector<IDAREService> services = new Vector<IDAREService>();
		services.add(pluginreader);
		return services;
	}
}
