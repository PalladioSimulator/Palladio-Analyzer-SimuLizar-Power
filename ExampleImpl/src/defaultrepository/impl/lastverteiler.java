
	   
	

	   
	   package defaultrepository.impl; 

	      
	   
public class lastverteiler

	   

	   implements defaultrepository.impl.Ilastverteiler, java.io.Serializable
	   {
	   	  
     
	private static org.apache.log4j.Logger logger = 
			org.apache.log4j.Logger.getLogger(lastverteiler.class.getName());
			
	
	
	
	
	
	
	public lastverteiler(String assemblyContextParentStructure) {

		
		
		
		
		
			m_portAngeboten_ILastverteiler_Lastverteiler = init_Angeboten_ILastverteiler_Lastverteiler(assemblyContextParentStructure);
		
		
		
	}

 
	      
	
    

	
   	
	
    protected defaultrepository.ILastverteiler m_portAngeboten_ILastverteiler_Lastverteiler = null;

	private defaultrepository.impl.ports.ILastverteiler_lastverteiler init_Angeboten_ILastverteiler_Lastverteiler(String assemblyContextParentStructure) {
		try {
			return new defaultrepository.impl.ports.ILastverteiler_lastverteiler(this, assemblyContextParentStructure);
		} catch (java.rmi.RemoteException e) {
		}
		return null;
	}


   	
   	
   public defaultrepository.ILastverteiler getPortAngeboten_ILastverteiler_Lastverteiler () {
      return m_portAngeboten_ILastverteiler_Lastverteiler;
   }

   	

	      
   
   

   
   

   
   protected defaultrepository.impl.contexts.IlastverteilerContext myContext = null;

   
	
   public void setContext(Object myContext) {
      this.myContext = (defaultrepository.impl.contexts.IlastverteilerContext)myContext;
      
   }



	      
	
	
   public 
	
   de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object>

      iLastverteiler_entpacke0
         (
	de.uka.ipd.sdq.simucomframework.variables.StackContext ctx
)

   {
   	  
 
      
   	  
   	  	
	      
   
    

 
  
   
    // measure time for SEFF  
    
	

   
  
 


	
	
		de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object> resultStackFrame =
			new de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object>();
		de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object> methodBodyStackFrame =
			ctx.getStack().currentStackFrame();
		if (this.myDefaultComponentStackFrame.getContents().size() > 0) {
			methodBodyStackFrame.addVariables(this.myDefaultComponentStackFrame);
		}
		if (this.myComponentStackFrame.getContents().size() > 0) {
			methodBodyStackFrame.addVariables(this.myComponentStackFrame);
		}
		
		
			
				
					
				
			
		
	


   
      
   
	
	{
		
			
				double u_BP7O0OuUEeCuhfIsXFGDcQ = (Double)ctx.evaluate("DoublePDF[(1;1.0)]",Double.class);
				double sum_BP7O0OuUEeCuhfIsXFGDcQ = 0;
				
	if (sum_BP7O0OuUEeCuhfIsXFGDcQ <= u_BP7O0OuUEeCuhfIsXFGDcQ && u_BP7O0OuUEeCuhfIsXFGDcQ < sum_BP7O0OuUEeCuhfIsXFGDcQ + 0.0 )
	{
		
   
    


	
	


   
      
   
/* ExternalCallAction - START */
 	{ //this scope is needed if the same service is called multiple times in one SEFF. Otherwise there is a duplicate local variable definition.

		    
	
	
	try {
	
	// Start Simulate an external call
	de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object> currentFrame = ctx.getStack().currentStackFrame();
	// prepare stackframe
	de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object> stackframe = ctx.getStack().createAndPushNewStackFrame();
	
		
			
				
					stackframe.addValue("datei.BYTESIZE",
					   	ctx.evaluate("datei.BYTESIZE",currentFrame));
				
			
		
	


	
		
	

	
	de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object> callResult =


   	myContext.getRoleBen�tigt_IServer2_Lastverteiler().entpacke0
	   	(
	ctx
);
	
	
	// Stop the time measurement
	
		
	

	
	
	
		
			
		
	

	
	} catch (java.rmi.RemoteException e) {
		
	}
	finally
	{
		
 	ctx.getStack().removeStackFrame();

	}
	// END Simulate an external call



		    	
	}
/* ExternalCallAction - END */

   
      
   
	


	
	


   

   

   

	}
	sum_BP7O0OuUEeCuhfIsXFGDcQ += 0.0;

	if (sum_BP7O0OuUEeCuhfIsXFGDcQ <= u_BP7O0OuUEeCuhfIsXFGDcQ && u_BP7O0OuUEeCuhfIsXFGDcQ < sum_BP7O0OuUEeCuhfIsXFGDcQ + 1.0 )
	{
		
   
    


	
	


   
      
   
/* ExternalCallAction - START */
 	{ //this scope is needed if the same service is called multiple times in one SEFF. Otherwise there is a duplicate local variable definition.

		    
	
	
	try {
	
	// Start Simulate an external call
	de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object> currentFrame = ctx.getStack().currentStackFrame();
	// prepare stackframe
	de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object> stackframe = ctx.getStack().createAndPushNewStackFrame();
	
		
			
				
					stackframe.addValue("datei.BYTESIZE",
					   	ctx.evaluate("datei.BYTESIZE",currentFrame));
				
			
		
	


	
		
	

	
	de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object> callResult =


   	myContext.getRoleBen�tigt_IServer1_Lastverteiler().entpacke0
	   	(
	ctx
);
	
	
	// Stop the time measurement
	
		
	

	
	
	
		
			
		
	

	
	} catch (java.rmi.RemoteException e) {
		
	}
	finally
	{
		
 	ctx.getStack().removeStackFrame();

	}
	// END Simulate an external call



		    	
	}
/* ExternalCallAction - END */

   
      
   
	


	
	


   

   

   

	}
	sum_BP7O0OuUEeCuhfIsXFGDcQ += 1.0;

			
		
	}


   
      
   
	

 
  
   
    
	

   
  
 


	
	
	return resultStackFrame;
	


   

   

   

    	
   	  
   	  
   }   


	

		  
	
	// Component Parameter Defaults
	// TODO: The stackframes are not yet initialised by calling setComponentFrame in Protocom, thus initialise them here, too
	protected de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object> myDefaultComponentStackFrame = new de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object>();

	// Component Parameter setter
	// TODO: The stackframes are not yet initialised by calling setComponentFrame in Protocom, thus initialise them here, too
	protected de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object> myComponentStackFrame = new de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object>();
	
	public void setComponentFrame(de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object> myComponentStackFrame) {
		this.myComponentStackFrame = myComponentStackFrame;	
		this.myDefaultComponentStackFrame = new de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object>();
		
			
		
	}

	
	
		
	public static void main(String[] args) {
		String ip = de.uka.ipd.sdq.prototype.framework.registry.RmiRegistry.getIpFromArguments(args);
		de.uka.ipd.sdq.prototype.framework.registry.RmiRegistry.setRemoteAddress(ip);
		
		new defaultrepository.impl.lastverteiler("");
	}

	

	   }
	   

	