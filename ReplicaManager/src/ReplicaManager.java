import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.StringTokenizer;

import UDP.MulticastListener;
import UDP.UDPSocketListener;

public class ReplicaManager implements Runnable {
	
	private Process replica;
	private int port;
	private int sequencerPort;
	private int replicaPort;
	private String replicaPath; 
	private boolean isRebooting = false;
	private Thread VM;
	int[] otherRMs;
	DatagramSocket receiveSequencer = null;
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
	public void handleRequest(DatagramPacket packet, String[] arguments, int port){

		if (arguments[2].equalsIgnoreCase("deposit")){
			if (arguments[3].substring(2, 3).equals("C")){
				// Execute customer deposit -- to do!
				String customerID = arguments[3];
				String branchId = customerID.substring(0, 2);  // BRANCH ID QB,MB,NB,BC
				int portNum = findReplica(status, branchId);
				forwardToReplica(portNum, packet);
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
	 * Forwards message to replica
	 * @param port
	 * @param operation
	 */
	private void forwardToReplica(int branchPort, DatagramPacket packet){
		DatagramSocket socket = null;
		try{
			socket = new DatagramSocket(port);
			while (true) {
				packet.setPort(branchPort);
				socket.send(packet);
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (socket != null) {
				socket.close();
			}
		}
	}
	
	/**
	 * Receives sequencer requests and sends ACKs
	 */
	private void receiveRequests(){
		MulticastSocket msocket = null;
		InetAddress group = null;
		DatagramSocket socket = null;
		MulticastListener ml = null;
		UDPSocketListener ul = null;
		
		try {
			ArrayList<DatagramPacket> incommingBuffer = new ArrayList<DatagramPacket>();
			//Multicast Setup
			msocket = new MulticastSocket(4000);
			group = InetAddress.getByName("230.0.0.0");
			ml = new MulticastListener(msocket, group, incommingBuffer);
			ml.start();
			
			//Unicast Setup
			//socket = new DatagramSocket(port);
			//ul = new UDPSocketListener(socket, incommingBuffer);
			//ul.start();

			//handle buffer
			
			
			receiveSequencer = new DatagramSocket(port);
			System.out.println("Testing requests to RM. Socket started.");
			while (true) {
				byte[] receiveData = new byte[1024];
				byte[] sendData = new byte[1024];
				ArrayList<String> arguments = null;
				
				DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
				receiveSequencer.receive(packet);
				InetAddress IPAddress = packet.getAddress();
				String request = new String(packet.getData(), packet.getOffset(), packet.getLength());
				arguments = parse(request);
				int sequenceID = Integer.parseInt(arguments.get(0));
				
				// Compare incoming request sequence ID with last sequence ID received
				// Case if it is smaller or equal to the last sequence ID
				if (sequenceID <= lastSequenceID){
					// Ignore request
				}
				// Case if it is bigger than last sequence ID
				else {
					
					// If it is bigger by 2 or more
					if(sequenceID - lastSequenceID >= 2){
					holdBackBuffer.add(packet);
					}
					else {
						if(sequenceID == lastSequenceID +1){
						deliveryBuffer.add(packet);
						lastSequenceID++;
						// Send ACKs to other RMs -- to do
						}
					}
				}
				
				// Iterate through holdBackBuffer to see if the next sequence is stored in it
				findNextSequence();
			
				
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
			try {
				ul.exit();
				ml.exit();
				ul.join();
				ml.join();
				socket.close();
				msocket.close();
			} catch (InterruptedException e) {
				e.printStackTrace();
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
	
	/**
	 * Iterate through holdBackBuffer to see if next sequence is stored in it
	 * @return
	 */
	public void findNextSequence(){
		for (int i=0; i<holdBackBuffer.size(); i++){
			String req = new String(holdBackBuffer.get(i).getData(), holdBackBuffer.get(i).getOffset(), holdBackBuffer.get(i).getLength());
			ArrayList<String> args = parse(req);
			int seqID = Integer.parseInt(args.get(0));
			
			// If next sequence is found in holdBackBuffer, remove it from there and add to deliveryBuffer
			if(seqID == lastSequenceID + 1 ){
				deliveryBuffer.add(holdBackBuffer.get(i));
				lastSequenceID++;
				holdBackBuffer.remove(i);
				//send acks to other RMs -- To do
			}
			else{
				// send resend request to sequencer asking for next sequence (lastSequenceID+1)
				// requestResend(lastSequenceID+1)
			}
		}
	}
	
	@Override
	public void run() {
//		initializeReplica();
		receiveRequests();
	}
}
