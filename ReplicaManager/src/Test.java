import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.TimeUnit;

public class Test {

	public static void main(String[] args) throws IOException{
		ReplicaManager rm = new ReplicaManager(5432, 7654, "path", true);
		new Thread(rm).start();
		
		String request = new String("3,123456,8888,deposit,QCC0001,100");
		DatagramSocket mSocket = new DatagramSocket();
		InetAddress group = InetAddress.getByName("230.0.0.0");
		//mSocket.joinGroup(group);
		byte[] buffer = request.getBytes();
		
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, 4000);
		
		while(true){
		try {
			mSocket.send(packet);
			TimeUnit.SECONDS.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		
		
		//mSocket.close();
		
		

	}

}
