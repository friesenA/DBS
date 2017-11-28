package udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import remoteObj.CustomerBankObj;

import server.Account;

public class PackageProcessor extends Thread {

	DatagramPacket packet;
	HashMap<Character, ArrayList<Account>> customerRecords;
	String branch;

	public PackageProcessor(DatagramPacket packet, HashMap<Character, ArrayList<Account>> customerRecords, String branch) {
		this.packet = packet;
		this.customerRecords = customerRecords;
		this.branch = branch;
	}

	public void run() {
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			
			//Unmarshall
			byte[] in = new byte[packet.getLength()];
			System.arraycopy(packet.getData(), packet.getOffset(), in, 0, packet.getLength());
			// parse String into arguments
			ArrayList<String> arguments = parse(new String(in));
			
			
			// Count Accounts
			if ((new String(in)).equals("accountCount")) {
				// find number of records
				int numRecords = findNumOfRecords(customerRecords);
				// package numRecords and branch name
				String pair = branch + "," + numRecords;
				byte[] message = pair.getBytes();
				DatagramPacket reply = new DatagramPacket(message, pair.length(), packet.getAddress(), packet.getPort());
				socket.send(reply);
				System.out.println("Response sent.");
			}
			// Transfer Funds
			else if (arguments.get(0).equals("transferFund")) {
				// Make deposit
				CustomerBankObj c = new CustomerBankObj(customerRecords, branch);
				double amount = (new Double(arguments.get(2)).doubleValue());
				String depositMessage = c.deposit(arguments.get(1), amount);
				// send reply
				byte[] message = depositMessage.getBytes();
				DatagramPacket reply = new DatagramPacket(message, depositMessage.length(), packet.getAddress(),
						packet.getPort());
				socket.send(reply);
				System.out.println("Response sent.");
			} else {
				// Do nothing
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (socket != null)
				socket.close();
		}
	}
	
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

	// --------------------------------------------
	// find the number of records in the hashmap
	// ---------------------------------------------
	private static int findNumOfRecords(HashMap<Character, ArrayList<Account>> records) {
		int size = 0;
		// add size of each arraylist together
		for (int i = 0; i < 26; i++) {
			size += records.get((char) ('A' + i)).size();
		}
		return size;
	}

}
