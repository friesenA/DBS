package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.PrintWriter;

import org.omg.CORBA.ORB;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientHandler {

	public static void main(String[] args){
		
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		ORB orb = ORB.init(args, null);
		
		//file or manual entry options
		System.out.println("Concurrent files(f) or Manual(m)?");
		String answer = "";
		try {
			answer = stdin.readLine();
			answer.trim();
			
			if(answer.equalsIgnoreCase("m")){
				UserInterface manual = new UserInterface(orb, stdin);
				manual.start();
				manual.join();
			}
			else if(answer.equalsIgnoreCase("f")){
				
				UserInterface[] files = new UserInterface[5];
				
				//Create Threads
				for(int i = 0; i < files.length; i++){
					files[i] = new UserInterface(orb, new BufferedReader(new FileReader("src/CommandFiles/file"+(i+1)+".txt")));
				}
				
				//Start Threads
				for(int i = 0; i < files.length; i++){
					files[i].start();
				}
				
				//End Threads
				for(int i = 0; i < files.length; i++){
					files[i].join();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Program Ended");
		
	}
	
	//Thread class that interacts with the customer/manager
	static class UserInterface extends Thread{
		
		BufferedReader in;
		ORB orb;
		public UserInterface(ORB orb, BufferedReader br){
			this.orb = orb;
			this.in = br;
		}
		
		public void run(){
			
			while(true){
				String customerID = null;
				
				//Get customer ID number
				System.out.println("What is your customer ID number? or enter q to exit.");
				try {
					customerID = in.readLine();
					customerID.trim();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				if(customerID.equalsIgnoreCase("q")){
					break;
				}
				if (!validateCustomerID(customerID)){
					System.out.println("That is not an acceptable custmer ID.");
					continue;
				}
				
				//CustomerID is valid, Start user session
				PrintWriter logStream = openLogStream(customerID);
				Client c = getClient(customerID, logStream, orb);
				
				//Check if manager validation failed
				if(c == null){
					System.out.println("That is not a valid manager ID.");
					logStream.close();
					continue;
				}
				
				System.out.println("Enter any commands you wish to make or \"help\" to see available options.");
				getCommands(c, in);
				
				closeLogStream(logStream);			
				System.out.println("This session is now closed.\n");
			}
			
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private static boolean validateCustomerID(String customerID){
			//Check string is not null
			if(customerID == null){
				return false;
			}
			
			//Check is the proper length
			if(customerID.length() != 7){
				return false;
			}
			//Check pattern is three uppercase letters and four digits
			
			//Check if first two digits are a branch
			String branch = customerID.substring(0, 2);
			boolean valid = false;
			for (Branches b : Branches.values()) {
				   if(b.toString().equals(branch))
					   valid = true;
			}
			if(!valid){
				return false;
			}
			
			//Check if third digit is a customer or manager
			char rank = customerID.charAt(2);
			if(rank != 'C' && rank != 'M'){
				return false;
			}
			
			//All validation checks passed
			return true;
		}

		private static Client getClient(String customerID, PrintWriter logStream, ORB orb) {
			char rank = customerID.charAt(2);
			Client c = null;
			if (rank == 'M'){
				c = new ManagerClient(customerID, logStream, orb);
			}
			else{
				c = new CustomerClient(customerID, logStream, orb);
			}
			return c;
		}
		
		private static void getCommands(Client c, BufferedReader stdin) { //pass stream
			boolean quit = false;
			while(!quit){
				String command = null;
				try{
					command = stdin.readLine();
					command.trim();
					if(!c.enactCommand(command)){
						quit = true;
					}
				}
				catch(IOException e){
					System.out.println("Error reading input");
					e.getStackTrace();
				}
			}
		}
		
		private static PrintWriter openLogStream(String customerID) {
			String filename = "src/" + customerID + "Log.txt";
			FileWriter fw = null;
			BufferedWriter bw = null;
			PrintWriter pw = null;
			try{
				//Open file to append information
				fw = new FileWriter(filename, true);
				bw = new BufferedWriter(fw);
				pw = new PrintWriter(bw);
			}
			catch(Exception e){
				System.out.println("Error creating log file.");
				e.printStackTrace();
				System.exit(1);
			}
			return pw;
		}
		
		private static void closeLogStream(PrintWriter logStream) {
			try{
				logStream.close();
			}
			catch(Exception e){
				System.out.println("Error closing log file.");
				e.printStackTrace();
				System.exit(1);
			}
			
		}
	}
}
