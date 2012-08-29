
	  package defaultrepository.impl.ports;

import kieker.monitoring.annotation.OperationExecutionMonitoringProbe;
      
   
	  /** 
	   * Operation Provide Role Port class for IServer_server 
	   */
	  public class IServer_server extends de.uka.ipd.sdq.prototype.framework.port.AbstractBasicPort<defaultrepository.impl.Iserver> implements defaultrepository.IServer {	  

		  protected static org.apache.log4j.Logger logger = 
			org.apache.log4j.Logger.getLogger(IServer_server.class.getName());
				
		  public IServer_server(defaultrepository.impl.Iserver myComponent, String assemblyContextParentStructure) throws java.rmi.RemoteException {
		  		this.myComponent = myComponent;
				de.uka.ipd.sdq.prototype.framework.registry.RmiRegistry.registerPort(de.uka.ipd.sdq.prototype.framework.registry.RmiRegistry.getRemoteAddress(), this, assemblyContextParentStructure + "IServer_server");
		  }
		    
	      


   @OperationExecutionMonitoringProbe	      
   public 
	
   
   de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object>

   entpacke0
   ( 
	de.uka.ipd.sdq.simucomframework.variables.StackContext ctx
 )
 throws java.rmi.RemoteException
 {
   	  
   
	return myComponent.iServer_entpacke0(
	     
	ctx
);


   }   

	  }


   