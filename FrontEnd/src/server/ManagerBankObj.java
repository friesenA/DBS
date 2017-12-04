package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import org.omg.CORBA.ORB;

import BankModule.ManagerBankPOA;

public class ManagerBankObj extends ManagerBankPOA {

	private ORB orb;

	int sequencerPortNum = 9876;
	String sequencerIP = "localhost";

	protected ManagerBankObj() {
		super();
	}

	// -------------------------------------------------------------------IMPLEMENTATION
	// METHODS-----------------------------------------------------------------------//

	// -------------------------------------------
	// Create Account
	// --------------------------------------------
	@Override
	public String createAccountRecord(String managerID, String firstName, String lastName, String address, String phone, String branch) {
		String message = "createAccountRecord," + managerID + "," + firstName + "," + lastName + "," + address + ","
				+ phone + "," + branch;
		String answer = sendUDPrequest(message, sequencerPortNum);
		return answer;
	}

	// ------------------------------------------------
	// Edit Record
	// ------------------------------------------------
	@Override
	public String editRecord(String managerID, String customerID, String fieldName, String newValue) {
		String message = "editRecord," + managerID + "," + customerID + "," + fieldName + "," + newValue;
		String answer = sendUDPrequest(message, sequencerPortNum);
		return answer;
	}

	// ----------------------------------------------
	// Get Account Count
	// ---------------------------------------------
	@Override
	public String getAccountCount(String managerID) {
		String message = "getAccountCount," + managerID;
		String answer = sendUDPrequest(message, sequencerPortNum);
		return answer;
	}

	// -------------------------------------------
	// Deposit
	// --------------------------------------------
	@Override
	public String deposit(String managerID, String customerID, double amt) {
		String message = "deposit," + managerID + "," + customerID + "," + amt;
		String answer = sendUDPrequest(message, sequencerPortNum);
		return answer;
	}

	// ---------------------------------------
	// Withdraw
	// --------------------------------------
	@Override
	public String withdraw(String managerID, String customerID, double amt) {
		String message = "withdraw," + managerID + "," + customerID + "," + amt;
		String answer = sendUDPrequest(message, sequencerPortNum);
		return answer;
	}

	// -------------------------------------
	// Get Balance
	// -------------------------------------
	@Override
	public String getBalance(String managerID, String customerID) {
		String message = "getBalance," + managerID + "," + customerID;
		String answer = sendUDPrequest(message, sequencerPortNum);
		return answer;
	}

	// -------------------------------------
	// Transfer Funds
	// -------------------------------------
	@Override
	public String transferFund(String managerID, String sourceCustomerID, double amount, String destinationCustomerID) {
		String message = "transferFund," + managerID + "," + sourceCustomerID + "," + amount + "," + destinationCustomerID;
		String answer = sendUDPrequest(message, sequencerPortNum);
		return answer;
	}

	// ------------------------------------------------------HELPER
	// METHODS------------------------------------------------------------------------------//

	//	---------------------------------------
	// parse input string into arguments
	// ----------------------------------------
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

	// -------------------------------------------
	// Send/Receive UDP communications
	// -------------------------------------------
	private String sendUDPrequest(String message, int portNum) {
		DatagramSocket socket = null;
		DatagramPacket request = null;
		UDPSocketListener listener = null;
		String answer = "";
		
		try {
			// setup
			socket = new DatagramSocket();
			InetAddress host = InetAddress.getByName(sequencerIP);

			// modify message
			message = System.currentTimeMillis() + "," + message;

			// send request
			byte[] m = message.getBytes();
			request = new DatagramPacket(m, m.length, host, portNum);
			ArrayList<DatagramPacket> packetBuffer = new ArrayList<DatagramPacket>();
			ArrayList<String> responses = new ArrayList<String>();
			
			listener = new UDPSocketListener(socket, packetBuffer, request);
			
			socket.send(request);
			listener.run();

			long start = System.currentTimeMillis();
			boolean sequencerTimeout = true;
			long replicaTimeout;
			while (true) {
				//Check received packet buffer 
				if(!packetBuffer.isEmpty()){
					byte[] in = new byte[packetBuffer.get(0).getLength()];
					System.arraycopy(packetBuffer.get(0).getData(), packetBuffer.get(0).getOffset(), in, 0, packetBuffer.get(0).getLength());
					String response = (new String(in));
					// parse String into arguments
					ArrayList<String> arguments = parse(answer);
					if (arguments.get(0).equals("Success") || arguments.get(0).equals("Failure")) {
						// record source
						responses.add(response);
						packetBuffer.remove(0);
					}
					else if (arguments.get(1).equals("received")){
						sequencerTimeout = false;
						packetBuffer.remove(0);
					}
				}
				//resend request if no confirmation of reciept
				if(sequencerTimeout && System.currentTimeMillis() - start >= 2000){
					listener.resend();
				}
				//Check for answer
				if(responses.size() == 3){
					replicaTimeout = (System.currentTimeMillis() - start)*2;
					//Wait until last RM resolved
					while(packetBuffer.isEmpty() && (System.currentTimeMillis() - start) < replicaTimeout){}
					if(!packetBuffer.isEmpty()){
						//handle last answer packet
						byte[] in = new byte[packetBuffer.get(0).getLength()];
						System.arraycopy(packetBuffer.get(0).getData(), packetBuffer.get(0).getOffset(), in, 0, packetBuffer.get(0).getLength());
						responses.add(new String(in));
						packetBuffer.remove(0);
					}
					else{
						//handle crash
					}
					
					// Analyze for correctness
					int[] error = new int[4];
					for (int i = 0; i < responses.size(); i++) {
						for (int j = i + 1; j < responses.size(); j++) {
							if (!responses.get(i).equals(responses.get(j))) {
								error[i]++;
								error[j]++;
							}
						}
					}
					for(int i = 0; i < responses.size(); i++){
						if(error[i] <= 1){
							answer = responses.get(i);
						}
						if(error[i] >= 2){
							//handle byzantine at i
						}
					}
				}	
			}
		} catch (Exception e) {
			e.printStackTrace();
			
		} finally{
			try {
				listener.exit();
				listener.join();
				socket.close();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return answer;
	}

	// --------------------------------------------------------------CORBA-METHODS----------------------------------------------------------------//

	public void setORB(ORB orb_val) {
		orb = orb_val;
	}

	// implement shutdown() method
	public void shutdown() {
		orb.shutdown(false);
	}
}
