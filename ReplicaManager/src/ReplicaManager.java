import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ReplicaManager implements Runnable {
	
	private Process replica;
	private int port;
	private int sequencerPort;
	private int frontEndPort;
	private int replicaPort;
	private String replicaPath; 
	private boolean isRebooting = false;
	private Thread VM;
	int[] otherRMs;
	private static int lastSequenceID;
	ArrayList<DatagramPacket> holdBackBuffer = null;
	ArrayList<DatagramPacket> deliveryBuffer = null;
	private boolean status = true;  // TRUE = AMANDA, FALSE = BRANDON
	
	public ReplicaManager(int port, int replicaPort, int sequencerPort){//, int[] otherRMs, String replicaPath){
		this.port = port;
		this.replicaPort = replicaPort;
	//	this.replicaPath = replicaPath;
		this.sequencerPort = sequencerPort;
	//	this.otherRMs = otherRMs;
		this.isRebooting = false;
	}
	
	public int[] getRMPorts(){
		return otherRMs;
	}
	
	public void setOtherRMs(int[] otherRMs){
		this.otherRMs = otherRMs;
	}
	
	public int getSequencerPort(){
		return sequencerPort;
	}
	
	public void setSequencerPort(int sequencerPort){
		this.sequencerPort = sequencerPort;
	}
	
	public int getPort(){
		return this.port;
	}
	
	public int getReplicaPort(){
		return this.replicaPort;
	}
	
	public boolean isRebooting(){
		return isRebooting;
	}
	
	public void setRebooting(boolean isRebooting){
		this.isRebooting = isRebooting;
	}

	/**
	 * Kill replica
	 * @return
	 */
	public synchronized boolean killReplica(){
		
		try{
			if(replica == null){
			return true;
			}
			if(replica.isAlive()){
				replica.destroy();
				replica.waitFor();
				resetVM();
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
		try {
			Runtime runtime = Runtime.getRuntime();
			if(replica != null){
				replica.destroy();
				replica.waitFor();
			}
			replica = runtime.exec(replicaPath);
			resetVM();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Initialize Replica
	 */
	
	/*
	private void initializeReplica(){		
		try {
			Runtime runtime = Runtime.getRuntime();
			replica = runtime.exec(replicaPath);
			resetVM();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	*/
	
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
	
	/**
	 * Request handler.. for now
	 * @param arguments
	 */
	public void handleRequest(String[] arguments, int port){

		if (arguments[2].equalsIgnoreCase("deposit")){
			if (arguments[3].substring(2, 3).equals("C")){
				// Execute customer deposit -- to do!
				String customerID = arguments[3];
				String branchId = customerID.substring(0, 2);  // BRANCH ID QB,MB,NB,BC
				int portNum = findReplica(status, branchId);
				double amount = Double.parseDouble(arguments[4]);
			}
			else if (arguments[3].substring(2,3).equals("M")){
				// Execute manager deposit -- to do!
				String managerID = arguments[3];
				String customerID = arguments[4];
				String branchId = customerID.substring(0, 2);
				int portNum = findReplica(status, branchId);
				double amount = Double.parseDouble(arguments[5]);
			}
		}
		else if (arguments[2].equalsIgnoreCase("withdraw")){
			if (arguments[3].substring(2, 3).equals("C")){
				// Execute customer withdraw -- to do!
				String customerID = arguments[3];
				String branchId = customerID.substring(0, 2);
				int portNum = findReplica(status, branchId);
				double amount = Double.parseDouble(arguments[4]);
			}
			else if (arguments[3].substring(2, 3).equals("M")){
				// Execute manager withdraw -- to do!
				String managerID = arguments[3];
				String customerID = arguments[4];
				String branchId = customerID.substring(0, 2);
				int portNum = findReplica(status, branchId);
				double amount = Double.parseDouble(arguments[5]);
			}
		}
		else if (arguments[2].equalsIgnoreCase("getBalance")){
			if (arguments[3].substring(2, 3).equals("C")){
				// Execute customer get balance -- to do!
				String customerID = arguments[3];
				String branchId = customerID.substring(0, 2);
				int portNum = findReplica(status, branchId);
				
			}
			else if (arguments[3].substring(2, 3).equals("M")){
				// Execute manager get balance -- to do!
				String managerID = arguments[3];
				String customerID = arguments[4];
				String branchId = customerID.substring(0, 2);
				int portNum = findReplica(status, branchId);
			}
		}
		else if (arguments[2].equalsIgnoreCase("getAccountCount")){
			// Execute get account count -- to do!
			String managerID = arguments[3];
			String branchId = managerID.substring(0, 2);
			int portNum = findReplica(status, branchId);
		}
		else if (arguments[2].equalsIgnoreCase("transferFund")){
			if (arguments[3].substring(2, 3).equals("C")){
				// Execute customer transfer fund -- to do!
				String srcCustomerID = arguments[3];
				double amount = Double.parseDouble(arguments[4]);
				String destCustomerID = arguments[5];
				String branchId = destCustomerID.substring(0, 2);
				int portNum = findReplica(status, branchId);
			}
			else if (arguments[3].substring(2, 3).equals("M")){
				// Execute manager transfer fund -- to do!
				String managerID = arguments[3];
				String srcCustomerID = arguments[4];
				double amount = Double.parseDouble(arguments[5]);
				String destCustomerID = arguments[6];
				String branchId = destCustomerID.substring(0, 2);
				int portNum = findReplica(status, branchId);
			}
		}
	}
	
	public static int findReplica(boolean status, String initial)
	{
		int portNum = 0;
		
		if(status == true)  // For amanda implementation
		{
			if(initial.equalsIgnoreCase("bc") == true)  // bc = 1, mb = 2 nb = 3 qc = 4
			{
				portNum = 6501;
			}
			else if(initial.equalsIgnoreCase("mb"))
			{
				portNum = 6502;
			}
			else if(initial.equalsIgnoreCase("nb"))
			{
				portNum = 6503;
			}
			else
			{
				portNum = 6504;
			}
		}
		else  // Brandon implementation
		{
			portNum = 6500;  // MAKE SURE THE PORT NUM IS EXACTLY LIKE BRANDON
		}
		
		return portNum;
	}
	
	
	/**
	 * Validates order of sequence numbers
	 * @param sequenceID
	 * @return
	 */
	public boolean validateSequenceID(int sequenceID){
		if (sequenceID - lastSequenceID >= 2){
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Receives sequencer requests and sends ACKs
	 */
	private void receiveRequests(){
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket(port);
			System.out.println("Testing requests to RM. Socket started.");
			while (true) {
				byte[] receiveData = new byte[1024];
				byte[] sendData = new byte[1024];
			
				ArrayList<String> arguments = null;
				
				DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
				socket.receive(packet);
				InetAddress IPAddress = packet.getAddress();
				String request = new String(packet.getData(), packet.getOffset(), packet.getLength());
				arguments = parse(request);
				int sequenceID = Integer.parseInt(arguments.get(0));
				if (sequenceID < lastSequenceID){
					// Ignore request -- to do
				}
				else {
					if(sequenceID - lastSequenceID >= 2){
					holdBackBuffer.add(packet);
					}
					else {
						deliveryBuffer.add(packet);
					}
				}
				
				for (int i=0; i<holdBackBuffer.size(); i++){
					String req = new String(holdBackBuffer.get(i).getData(), holdBackBuffer.get(i).getOffset(), holdBackBuffer.get(i).getLength());
					ArrayList<String> args = parse(req);
					int seqID = Integer.parseInt(args.get(0));
					if (seqID < lastSequenceID){
						// Ignore request
					}
					else {
						if(validateSequenceID(seqID)){
							
						}
					}
				}
				
				/*
				String arguments[] = request.split(",");
				int sequenceID = Integer.parseInt(arguments[0]);
				
				if (validateSequenceID(sequenceID)){
					int clientPort = Integer.parseInt(arguments[1]);
					String ack = sequenceID + ", received";
					handleRequest(arguments, clientPort);
					lastSequenceID = sequenceID;
					sendData = ack.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getPort());
					socket.send(sendPacket);
				} else {
					String reply = sequenceID + ", resend";
					sendData = reply.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getPort());
					socket.send(sendPacket);
				}
				*/
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (socket != null){
				socket.close();
			}
		}
	}
	
	/**
	 * Parse input string into arguments
	 * @param command
	 * @return
	 */
	private static ArrayList<String> parse(String command) {
		ArrayList<String> arguments = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(command, ",");
		String argument = "";
		while (tokenizer.hasMoreTokens()) {
			argument = tokenizer.nextToken();
			argument = argument.trim();
			arguments.add(argument);
		}
		return arguments;
	}
	
	public void iterateHoldBackBuffer(ArrayList<DatagramPacket> holdBackBuffer){
		
	}
	

	

	@Override
	public void run() {
//		initializeReplica();
		receiveRequests();
	}
}
