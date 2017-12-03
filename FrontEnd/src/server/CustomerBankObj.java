package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import org.omg.CORBA.ORB;

import BankModule.CustomerBankPOA;

public class CustomerBankObj extends CustomerBankPOA {
	
	private ORB orb;
	
	int sequencerPortNum;
	String sequencerIP;

	protected CustomerBankObj() {
		super();
	}

	// ----------------------------------------------------------IMPLEMENTATION-METHODS-----------------------------------------------------------------------//

	// -------------------------------------------
	// Deposit
	// --------------------------------------------
	@Override
	public String deposit(String customerID, double amt) {
		String message = "deposit," + customerID + "," + amt;
		String answer = sendUDPrequest(message, sequencerPortNum);
		return answer;
	}

	// ---------------------------------------
	// Withdraw
	// --------------------------------------
	@Override
	public String withdraw(String customerID, double amt) {
		String message = "deposit," + customerID + "," + amt;
		String answer = sendUDPrequest(message, sequencerPortNum);
		return answer;
	}

	// -------------------------------------
	// Get Balance
	// -------------------------------------
	@Override
	public String getBalance(String customerID) {
		String message = "deposit," + customerID;
		String answer = sendUDPrequest(message, sequencerPortNum);
		return answer;
	}

	// -------------------------------------
	// Transfer Funds
	// -------------------------------------
	@Override
	public String transferFund(String sourceCustomerID, double amount, String destinationCustomerID) {
		String message = "deposit," + sourceCustomerID + "," + amount + "," + destinationCustomerID;
		String answer = sendUDPrequest(message, sequencerPortNum);
		return answer;
	}


	// ------------------------------------------------------HELPER-METHODS------------------------------------------------------------------------------//
	
	// ---------------------------------------
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
		DatagramPacket response = null;
		String answer = "";
		boolean recieved = false;
		
		try {
			// setup
			socket = new DatagramSocket();
			InetAddress host = InetAddress.getByName(sequencerIP);

			// modify message
			Date d = new Date();
			message = d.getTime() + "," + socket.getInetAddress().getHostAddress() + "," + socket.getPort() + ","
					+ message;

			// send request
			byte[] m = message.getBytes();
			request = new DatagramPacket(m, m.length, host, portNum);

			while (true) {
				try {
					byte[] buffer = new byte[1000];
					response = new DatagramPacket(buffer, buffer.length);
					
					if(!recieved){
						socket.send(request);
						socket.setSoTimeout(100);
					}
					// receive response
					socket.receive(response);
					byte[] in = new byte[response.getLength()];
					System.arraycopy(response.getData(), response.getOffset(), in, 0, response.getLength());
					answer = (new String(in));

					// parse String into arguments
					ArrayList<String> arguments = parse(answer);

					if (arguments.get(0).equals("Success") || arguments.get(0).equals("Failure")) {
						break;
					}
					else if (arguments.get(1).equals("recieved")){
						recieved = true;
					}
					
				} catch (SocketTimeoutException e) {
					continue;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			
		} finally{
			socket.close();
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
