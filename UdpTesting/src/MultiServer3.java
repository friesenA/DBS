import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MultiServer3 {
	
private static int sequenceId = 1000;
	
	
	public static void main(String [] args) throws IOException, ClassNotFoundException
	{
		System.out.println("MultiServer 3 started");
		int i = 0;
		
		while(true){
		byte[] buf = new byte[1024];
		MulticastSocket socket = new MulticastSocket(4000);
		InetAddress group = InetAddress.getByName("230.0.0.0");
		socket.joinGroup(group);
		
		//while(true)
		//{
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);
			
			String received = new String(packet.getData(), 0, packet.getLength());
			
			String test = "90,resend";
			// THIS IS TO TEST THE RESEND REQUEST FROM RM TO SEQUENCER
			// sendMsg is the method to send the request
			// Used it for testing purpose
			/*if(i != 2)
			{
				System.out.println(i);
				sendMsg(test);
				i++;
			}*/
			
			
			String[] arrParam = received.split(",");
			
			// Should implemented a priority queue to hold all request but for now this should work
			if(Integer.parseInt(arrParam[0]) == sequenceId)
			{
				System.out.println("Expected sequence id received");
				System.out.println("Message: " + received);
				sendMsg(sequenceId + ",ACK");
				sequenceId++;
				// NOTES ---------------
				// It is here where u can continue the rest of the rm stuff once u have the right id
			
			}
			else
			{
				System.out.println("Wrong sequence id");
				sendMsg("1000,resend");
			}
			//break;
		//}
			
			socket.leaveGroup(group);
			socket.close();
		}
		
		
	}
	
	
	public static void sendMsg(String message) throws IOException
	{
		DatagramSocket clientSocket = new DatagramSocket(5700);
		InetAddress IPAddress = InetAddress.getByName("localhost");
		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[1024];
		
		// Test sample
		//String sentence = "1000,resend";
		sendData = message.getBytes();
		
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
		clientSocket.send(sendPacket);
		clientSocket.close();
	}


}
