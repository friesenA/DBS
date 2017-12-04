import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Sequencer2 implements Runnable {
	
	private static int sequenceId = 1000;
	private HashMap <Integer, String> sequenceLogs = new HashMap <Integer, String> ();
	private DatagramSocket serverSocket;
	private DatagramSocket serverRmSocket;
	private DatagramPacket receivePacket;
	private DatagramPacket sendPacket;
	
	public static void main(String [] args) throws IOException
	{
		Sequencer2 sequencer = new Sequencer2();
		new Thread(sequencer).start();
		System.out.println("Sequencer started");
	}
	
	
	public void run()
	{
		while(true){
		// Receive request from the client
		try {
			
		serverSocket = new DatagramSocket(9876);
		//serverRmSocket = new DatagramSocket(9998);
		
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
				
		receivePacket = new DatagramPacket(receiveData, receiveData.length);
		serverSocket.receive(receivePacket);
		//serverRmSocket.receive(receivePacket);
		
		String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
		InetAddress IPAddress = receivePacket.getAddress();
		String ipAddress = IPAddress.toString();
		ipAddress = ipAddress.substring(1);
		int port = receivePacket.getPort();
		System.out.println(ipAddress + "  " + Integer.toString(port));
		
		
		
		// Check if the packet it received is from the replica managers or from the front end client
		if(port == 5600 || port == 5700)  // 5600 and 5700 are port numbers from replica managers
		{
			String[] arrParam = sentence.split(",");
			String requestMsg;
			
			// Get the sequence id from the request message from rm
			int getSequence = Integer.parseInt(arrParam[0]);
			requestMsg = sequenceLogs.get(getSequence);
			
			// Verify if it is acknowledge or not
			if(arrParam[1].equalsIgnoreCase("ACK") == true)
			{
				System.out.println("\n Replica manager: received message");
				// removeSequenceLog(getSequence);
			}
			else
			{
				String multicastResponse = multicastRm(requestMsg);
				System.out.println(multicastResponse);
				/*DatagramSocket repSocket = new DatagramSocket();
				sendData = requestMsg.getBytes();
				DatagramPacket rmPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				repSocket.send(rmPacket);
				repSocket.close();*/
				
			}
			
			
		}
		else
		{
			// This is a front end client request
			System.out.println("\n Client request received");
			
			// Replace and add sequence id into message
			String msgRm = addSequencerNum(sentence, port, ipAddress);
							
			// Multicast message to all replica managers
			String multicastResponse = multicastRm(msgRm);
							
			// Store message into list for future reference for resend request from replica managers
			saveRequestMessage(msgRm);
			
			// Successful message should be in the seq to rm method, get response, timeout implementation needed
			String replyClient = "Successful";
			
			
			sendData = replyClient.getBytes();
			
			sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			
			// Send packet to all replica managers
		    serverSocket.send(sendPacket);
		    
		    
		    
		 // TESTING HASHMAP, DISPLAYING VALUES TO CHECK IF ALL GOOD
			Set set = sequenceLogs.entrySet();
			Iterator iterator = set.iterator();
			
			System.out.println("\n Printing all sequences saved in log");
			
			while(iterator.hasNext())
			{
				Map.Entry <Integer, String> me = (Map.Entry) iterator.next();
				System.out.print(me.getKey() + ", ");
			}
			// END OF TESTING
			System.out.println("\n");			
		}
			
		
	    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		serverSocket.close();
		
		}
	}
	
	
	
	
	
	
	
	// Add the sequence id to the message and increment the sequence id
	public static synchronized String addSequencerNum(String sentence, int port, String address)
	{
		String[] arrSentence = sentence.split(",");
		arrSentence[0] = Integer.toString(sequenceId);
		
		String addSeq = arrSentence[0] + "," + address + "," + port;
		
		for(int i = 1; i < arrSentence.length; i++)
		{
			addSeq += "," + arrSentence[i];
		}
		
		//addSeq = Integer.toString(sequenceId) + "," + sentence;
		sequenceId++;
		return addSeq;
	}
	
	
	
	
	// Send request to all RM by using multicast
	public static synchronized String multicastRm(String message) throws IOException
	{
		DatagramSocket mSocket = new DatagramSocket();
		InetAddress group = InetAddress.getByName("230.0.0.0");
		//mSocket.joinGroup(group);
		byte[] buffer = message.getBytes();
		
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, 4000);
		mSocket.send(packet);
		
		mSocket.close();
		
		return "Successfully send to RM";
	}
	
	
	
	
	// Save request into hashmap to keep track of sequence logs
	public synchronized void saveRequestMessage(String message) throws NumberFormatException, UnknownHostException
	{
		String[] arrMessage = message.split(",");
		
		//  IGNORE THE FIRST TWO LINES, ORIGINALLY STORED INTO HASHMAP AS PACKET.JAVA OBJECT
		//  KEPT FOR FUTURE REFERENCES
		//String[] arrParameters = Arrays.copyOfRange(arrMessage, 4, arrMessage.length - 1);
		
		//packetObj = new Packet(arrMessage[0], InetAddress.getByName(arrMessage[1]), Integer.parseInt(arrMessage[2]), arrMessage[3], arrParameters);
		
		//sequenceLogs.put(Integer.parseInt(arrMessage[0]), arrMessage);
		
		sequenceLogs.put(Integer.parseInt(arrMessage[0]), message);
		
		System.out.println("Sequence id added into sequence logs");
	}
	
	

}
