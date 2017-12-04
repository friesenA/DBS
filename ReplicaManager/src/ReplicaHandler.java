import java.io.IOException;
import java.util.ArrayList;

public class ReplicaHandler {
	
	public ReplicaHandler(){
		
	}
	
	private void initializeReplica(ArrayList<Process> replica, int portbase){		
		try {
			Runtime runtime = Runtime.getRuntime();
			if(status == true)  // For amanda implementation
			{
				String[] cmdarray= new String[3];
				cmdarray[0] = replicaPath;

				cmdarray[1] = "BC";
				cmdarray[2] = "" + (portbase + 1);
				replica.add(runtime.exec(cmdarray));
				cmdarray[1] = "MB";
				cmdarray[2] = "" + (portbase + 2);
				replica.add(runtime.exec(cmdarray));
				cmdarray[1] = "NB";
				cmdarray[2] = "" + (portbase + 3);
				replica.add(runtime.exec(cmdarray));
				cmdarray[1] = "QC";
				cmdarray[2] = "" + (portbase + 4);
				replica.add(runtime.exec(cmdarray));
			}
			else  // Brandon implementation
			{
				String[] cmdarray= new String[2];
				cmdarray[0] = replicaPath;
				cmdarray[1] = "" + (portbase + 1);
				replica.add(runtime.exec(replicaPath));
			}
			resetVM();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Kill replica
	 * @return
	 */
	public synchronized boolean killReplica(){
		
		try{
			for(int i = 0; i < replica.size(); i++){
			
				if (replica.get(i) != null && replica.get(i).isAlive()) {
					replica.get(i).destroy();
					replica.get(i).waitFor();
					resetVM(); //or outside loop?
				}
			}
			return true;
		}
		catch(InterruptedException e){
		}
		return false;
	}
	
	/**
	 * Reboot replica
	 * @return
	 */
	public synchronized boolean rebootReplica(){
		initializeReplica();
		//update database
		return false;
	}
	
	/**
	 * Reset thread
	 */
	private void resetVM(){
		Runtime runtime = Runtime.getRuntime();
		if(VM != null){
			runtime.removeShutdownHook(VM);
		}
	
		VM = new Thread(() -> {
			try{
				if(replica != null){
					replica.destroy();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				replica = null;
			}
			});
			
		runtime.addShutdownHook(VM);
	}
	
	public static int findReplicaPort(String initial, int basePortNum)
	{
		int portNum;
		
		if(status == true)  // For amanda implementation
		{
			if(initial.equalsIgnoreCase("bc") == true)  // bc = 1, mb = 2 nb = 3 qc = 4
			{
				portNum = basePortNum + 1;
			}
			else if(initial.equalsIgnoreCase("mb"))
			{
				portNum = basePortNum + 2;
			}
			else if(initial.equalsIgnoreCase("nb"))
			{
				portNum = basePortNum + 3;
			}
			else
			{
				portNum = basePortNum + 4;
			}
		}
		else  // Brandon implementation
		{
			portNum = basePortNum + 1;  // MAKE SURE THE PORT NUM IS EXACTLY LIKE BRANDON
		}
		
		return portNum;
	}

}
