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
	
	private int port;
	private int sequencerPort;
	private boolean isRebooting = false;
	int[] otherRMs;
	MulticastSocket receiveSequencer = null;
	private int lastSequenceID;
	
	private ProcessHandler replicaHandle;
	
	private boolean status;  // TRUE = AMANDA, FALSE = BRANDON
	
	public ReplicaManager(int port, int sequencerPort, String replicaPath, Boolean status){//, int[] otherRMs, String replicaPath){
		this.port = port;
		this.sequencerPort = sequencerPort;
	//	this.otherRMs = otherRMs;
		this.isRebooting = false;
		this.status = status;
		this.replicaHandle = new ProcessHandler(replicaPath, port, status);
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
	
	public boolean isRebooting(){
		return isRebooting;
	}
	
	public void setRebooting(boolean isRebooting){
		this.isRebooting = isRebooting;
	}
	
	/**
	 * Handle request to forward to appropriate branch
	 * @param arguments
	 */
	public void handleRequest(DatagramPacket packet){
		
		ArrayList<String> arguments = new ArrayList<String>();
		String request = new String(packet.getData(), packet.getOffset(), packet.getLength());
		arguments = parse(request);
		String ID = arguments.get(3);
		String branchId = ID.substring(0,2);  // BRANCH ID QB,MB,NB,BC
		int portNum = findReplicaPort(branchId);
		System.out.println("Forwarding packet to "+ portNum);
		forwardToReplica(portNum, packet);
	}
		
	
	
	/**
	 * Maps the replicas' port numbers
	 * @param status
	 * @param initial
	 * @return
	 */
	public int findReplicaPort(String initial)
	{
		int portNum;
		
		if(status == true)  // For amanda implementation
		{
			if(initial.equalsIgnoreCase("bc") == true)  // bc = 1, mb = 2 nb = 3 qc = 4
			{
				portNum = port + 1;
			}
			else if(initial.equalsIgnoreCase("mb"))
			{
				portNum = port + 2;
			}
			else if(initial.equalsIgnoreCase("nb"))
			{
				portNum = port + 3;
			}
			else
			{
				portNum = port + 4;
			}
		}
		else  // Brandon implementation
		{
			portNum = port + 1;  // MAKE SURE THE PORT NUM IS EXACTLY LIKE BRANDON
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
			ArrayList<DatagramPacket> incomingBuffer = new ArrayList<DatagramPacket>();
			ArrayList<DatagramPacket> deliveryBuffer = new ArrayList<DatagramPacket>();
			
			//Multicast Setup
			msocket = new MulticastSocket(4000);
			System.out.println("Testing requests to RM. Multicast Socket started.");
			group = InetAddress.getByName("230.0.0.0");
			ml = new MulticastListener(msocket, group, incomingBuffer);
			ml.start();
			
			//Unicast Setup
			//socket = new DatagramSocket(port);
			//ul = new UDPSocketListener(socket, incommingBuffer);
			//ul.start();

			//handle incoming buffer
			while(true){
			for (int i=0; i<incomingBuffer.size(); i++){
				String req = new String(incomingBuffer.get(i).getData(), incomingBuffer.get(i).getOffset(), incomingBuffer.get(i).getLength());
				ArrayList<String> args = parse(req);
				int seqID = Integer.parseInt(args.get(0));
				
				// if sequence ID is smaller than last sequence ID received
				if (seqID <= lastSequenceID){
					incomingBuffer.remove(i);
				} else {
					// If next sequence is found in buffer, remove it from there and add to deliveryBuffer
					if(seqID == lastSequenceID + 1 ){
						deliveryBuffer.add(incomingBuffer.get(i));
						lastSequenceID++;
						incomingBuffer.remove(i);
						handleRequest(incomingBuffer.get(i));
						//send acks to other RMs and execute sequence -- To do
					}
					else{
						// send resend request to sequencer asking for next sequence (lastSequenceID+1)
						// requestResend(lastSequenceID+1)
					}
				}
			}
			}
		
			/*
			while (true) {
				byte[] receiveData = new byte[1024];
				byte[] sendData = new byte[1024];
				ArrayList<String> arguments = null;
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
				
			}
			*/
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
				try {
		//		ul.exit();
				ml.exit();
		//		ul.join();
				ml.join();
		//		socket.close();
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
	
	
	@Override
	public void run() {
//		replicaHandle.initializeReplica();
		receiveRequests();
//		replicaHandle.killReplica();
	}
}
