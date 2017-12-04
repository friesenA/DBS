import java.io.IOException;
import java.util.ArrayList;

public class ProcessHandler {

	private ArrayList<Process> replica;
	private int port;
	private Thread VM;
	private String replicaPath;
	private boolean status = true;  // TRUE = AMANDA, FALSE = BRANDON
	
	public ProcessHandler(String replicaPath, int port, boolean status){
		this.port = port;
		this.replicaPath = replicaPath;
		this.status = status;
		this.replica = new ArrayList<Process>();
	}
	
	public void initializeReplica(){		
		try {
			Runtime runtime = Runtime.getRuntime();
			if(status == true)  // For amanda implementation
			{
				replica.add(runtime.exec(new String[]{"java", replicaPath,"BC", ("" + (port+1))}));
				replica.add(runtime.exec(new String[]{"java", replicaPath,"MB", ("" + (port+2))}));
				replica.add(runtime.exec(new String[]{"java", replicaPath,"NB", ("" + (port+3))}));
				replica.add(runtime.exec(new String[]{"java", replicaPath,"QC", ("" + (port+4))}));
			}
			else  // Brandon implementation
			{
				String[] cmdarray= new String[2];
				cmdarray[0] = replicaPath;
				cmdarray[1] = "" + (port + 1);
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
				for(int i = 0; i < replica.size(); i++){
					if(replica.get(i) != null){
						replica.get(i).destroy();
					}
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
	
}
