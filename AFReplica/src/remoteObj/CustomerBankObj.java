package remoteObj;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.AbstractMap.SimpleEntry;

import server.Branches;
import server.Account;

public class CustomerBankObj implements CustomerBank {

	private HashMap<Character, ArrayList<Account>> records;
	private String branch;
	final int UDPPortBase = 6500;

	public CustomerBankObj(){
		super();
		this.records = null;
		this.branch = null;
	}
	public CustomerBankObj(HashMap<Character, ArrayList<Account>> records, String branch) {
		super();
		this.records = records;
		this.branch = branch;
	}

	// ----------------------------------------------------------IMPLEMENTATION-METHODS-----------------------------------------------------------------------//

	// -------------------------------------------
	// Deposit
	// --------------------------------------------
	@Override
	public String deposit(String customerID, double amt) {
		String header = "Deposit into " + customerID + " account - ";
		String message;

		// Check that customerID exists and get account
		Account a = this.findAccount(customerID);
		if (a == null) {
			message = "Failure";
			log(header + message);
			return message;
		}

		// Make Deposit
		double newBalance = a.deposit(amt);
		message = "Success, the new balance $" + newBalance;
		log(header + message);
		return message;
	}

	// ---------------------------------------
	// Withdraw
	// --------------------------------------
	@Override
	public String withdraw(String customerID, double amt) {
		String header = "Withdrawl from " + customerID + " account - ";
		String message;

		// Check that customerID exists and get account
		Account a = this.findAccount(customerID);
		if (a == null) {
			message = "Failure";
			log(header + message);
			return message;
		}

		SimpleEntry<Boolean, Double> result = a.withdraw(amt);
		if (!result.getKey()) {
			message = "Failure";
			log(header + message);
			return message;
		}

		// Do Withdrawl
		double newBalance = result.getValue();
		message = "Success, the new balance $" + newBalance;
		log(header + message);
		return message;
	}

	// -------------------------------------
	// Get Balance
	// -------------------------------------
	@Override
	public String getBalance(String customerID) {
		String header = "Get balance for " + customerID + " account - ";
		String message;

		// Check that customerID exists and get account
		Account a = this.findAccount(customerID);
		if (a == null) {
			message = "Failure";
			log(header + message);
			return message;
		}

		// Get Balance
		double balance = a.getBalance();
		message = "Success, the balance is $" + balance;
		log(header + message);
		return message;
	}

	// -------------------------------------
	// Transfer Funds
	// -------------------------------------
	@Override
	public String transferFund(String sourceCustomerID, double amount, String destinationCustomerID) {
		String header = "Transfer $"+ amount +" from " + sourceCustomerID + " account to " + destinationCustomerID + " account - ";
		String message;
		
		//Withdraw from sourceCustomerID
		message = this.withdraw(sourceCustomerID, amount);
		ArrayList<String> result = parse(message);
		
		//Withdraw failed
		if(result.get(0).equals("Failure")){
			log(header + message);
			return message;
		}
		
		//Withdraw succeeded
		else {
			//identify port address of destination customer
			int portNum = UDPPortBase + Branches.valueOf(destinationCustomerID.substring(0, 2)).getValue();
			
			//send deposit request
			String request = "transferFundInternal," + destinationCustomerID + "," + amount; 
			String answer = this.sendUDPrequest(request, portNum);
			//validate success or failure
			message += ", " + answer;
			result = parse(answer);
			
			//Failed Deposit to destination
			if(result.get(0).equals("Failure")){
				//Return money withdrawn
				answer = this.deposit(sourceCustomerID, amount);
				//parse answer to check for success
				result = parse(answer);
				//money return fails SHOULD NEVER HAPPEN!!!!!!!! Just here to catch an error in the system
				if(result.get(0).equals("Failure")){
					message = "Failure";
					log(header + message);
					return message;
				}
				//money return successful
				else{
					message = "Failure";
					log(header + message);
					return message;
				}
			}
			
			//Successful Deposit to destination
			else{
				message = "Success";
				log(header + message);
				return message;
			}
		}
	}


	// ------------------------------------------------------HELPER-METHODS------------------------------------------------------------------------------//

	// ---------------------------------------------
	// Search records to find account of customerID
	// ---------------------------------------------
	private Account findAccount(String customerID) {
		ArrayList<Account> list;
		Account a = null;

		// iterate through hash map
		for (int i = 0; i < 26; i++) {
			list = (ArrayList<Account>) records.get((char) ('A' + i));
			// iterate through array list
			for (Account tmp : list) {
				// check for customerID
				if (customerID.equals(tmp.getCustomerID())) {
					a = tmp;
					break;
				}
			}
		}
		return a;
	}

	// --------------------------------------------------------
	// Add activity to log
	// ----------------------------------------------------------
	private void log(String message) {
		// open file
		String filename = "src/ServerLogs/" + this.branch + "log.txt";
		FileWriter fw = null;
		BufferedWriter bw = null;
		PrintWriter log = null;
		try {
			// Open file to append information
			fw = new FileWriter(filename, true);
			bw = new BufferedWriter(fw);
			log = new PrintWriter(bw);
		} catch (Exception e) {
			System.out.println("Error creating log file.");
			e.printStackTrace();
			System.exit(1);
		}

		// write to file
		String datetime = (LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		log.println(datetime + ": " + message);

		// close file
		try {
			log.flush();
			log.close();
		} catch (Exception e) {
			System.out.println("Error closing log file.");
			e.printStackTrace();
			System.exit(1);
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
	
	// -------------------------------------------
	// Send/Receive UDP communications
	// -------------------------------------------
	private String sendUDPrequest(String message, int portNum){
		DatagramSocket socket = null;
		String answer = "";
		try{
			//setup
			socket = new DatagramSocket();
			InetAddress host = InetAddress.getByName("localhost");
			DatagramPacket request = null;
			DatagramPacket response = null;
			
			//send request
			byte[] m = message.getBytes();
			request = new DatagramPacket(m, m.length, host, portNum);
			socket.send(request);
			
			//receive response
			byte[] buffer = new byte[1000];
			response = new DatagramPacket(buffer, buffer.length);
			socket.receive(response);
			byte[] in = new byte[response.getLength()];
			System.arraycopy(response.getData(), response.getOffset(), in, 0, response.getLength());
			answer = (new String(in));
		}catch(Exception e){
			e.printStackTrace();
		}
		return answer;
	}

}
