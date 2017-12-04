package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.omg.CORBA.ORB;

import server.Customer;

public class BankImpl{
	
	// Hashmap variables for storing customer info
	private List <Customer> customerRecords = new ArrayList();
	private HashMap <String, List> database = new HashMap <String, List> ();
	
	private String branchId;
	private int portNum;
	private int totalCustAmt = 0;
	private String dbFile = "";
	
	
	
	public BankImpl(String branchId, int portNum) throws Exception
	{
		super();
		this.branchId = branchId;
		this.portNum = portNum;
		
		// This retrieves the branch customer list files and store it into the hashmap
		String path = getBankRecords(branchId);
		dbFile = path;
		database = getDatabase(path);
		
		Customer customer; 
		
	}
	
	
	//  HANDLES DEPOSIT COMMAND
	public synchronized String deposit(String custId, double amount) throws Exception
	{
		//String path = getBankRecords(custId);
		//database = getDatabase(path);
		
		Set set = database.entrySet();
		Iterator i = set.iterator();
		
		// Go through the entire hashmap
		while(i.hasNext())
		{
			Map.Entry me = (Map.Entry) i.next();
			
			customerRecords = (List) me.getValue();
			
			// Go through the list of records from one of the hashmap value
			for(int x = 0; x < customerRecords.size(); x++)
			{
				Customer record = (Customer) customerRecords.get(x);
				
				if(record.getCustomerId().equalsIgnoreCase(custId))
				{
					// Get the appropriate file to write the log
					String file = getBankFile(custId, record.getBranch());
				    
					File createFile = new File(file);
					createFile.createNewFile();
				    FileWriter fw = new FileWriter(file, true);
				    BufferedWriter bw = new BufferedWriter(fw);
					
					double oldBalance = Double.parseDouble(record.getBalance());
					double newBalance = oldBalance + amount; 
					
					record.setBalance(Double.toString(newBalance));
					customerRecords.set(x, record);
					me.setValue(customerRecords);
					
					// Write the log to the file
					bw.write("Successfully deposit $" + amount + ", total balance: $" + newBalance);
					bw.newLine();
					bw.close();
					
					updateDatabase(dbFile);
					
					return "Success, the new balance $" + newBalance;
					
				}
			}
		}
		
		return "Failure";
	}
	
	
	// HANDLES WITHDRAWAL COMMAND
	public synchronized String withdrawal(String custId, double amount) throws Exception
	{
		//String path = getBankRecords(custId);
		//database = getDatabase(path);
		
		Set set = database.entrySet();
		Iterator i = set.iterator();
		
		// Go through the entire hashmap
		while(i.hasNext())
		{
			Map.Entry me = (Map.Entry) i.next();
			
			customerRecords = (List) me.getValue();
			
			// Go through all the list of records from one hashmap value
			for(int x = 0; x < customerRecords.size(); x++)
			{
				Customer record = (Customer) customerRecords.get(x);
				
				if(record.getCustomerId().equalsIgnoreCase(custId))
				{
					String file = getBankFile(custId, record.getBranch());
					
					File createFile = new File(file);
					createFile.createNewFile();
					FileWriter fw = new FileWriter(file, true);
					BufferedWriter bw = new BufferedWriter(fw);
					
					double oldAmount = Double.parseDouble(record.getBalance());
					double newAmount = oldAmount - amount;
					
					if(newAmount >= 0)
					{
						record.setBalance(Double.toString(newAmount));
						customerRecords.set(x, record);
						me.setValue(customerRecords);
						
						bw.write("Successfully withdrawal $" + amount + ", total balance: $" + newAmount);
						bw.newLine();
						bw.flush();
						bw.close();
						
						updateDatabase(dbFile);
						
						return "Success, the new balance $" + newAmount;
					}
					else
					{
						return "Failure";
					}
				}
			}
		}
		
		return "hi";
	}
	
	
	
	
	// HANDLES GET BALANCE COMMAND
	public synchronized String getBalance(String custId) throws Exception
	{
		//String path = getBankRecords(custId);
		//database = getDatabase(path);
		
		Set set = database.entrySet();
		Iterator i = set.iterator();
		
		while(i.hasNext())
		{
			Map.Entry me = (Map.Entry) i.next();
			customerRecords = (List) me.getValue();
			
			for(int x = 0; x < customerRecords.size(); x++)
			{
				Customer record = customerRecords.get(x);
				
				if(record.getCustomerId().equalsIgnoreCase(custId))
				{
					String file = getBankFile(custId, record.getBranch());
					
					File filename = new File(file);
					filename.createNewFile();
					FileWriter fw = new FileWriter(file, true);
					BufferedWriter bw = new BufferedWriter(fw);
					
					bw.write("User retrieved account balance, total balance: $" + record.getBalance());
					bw.newLine();
					bw.flush();
					bw.close();
					
					return record.getBalance();
				}
			}
		}
		
		return "Failure";
	}
	
	
	
	
	
	// HANDLES CREATING CUSTOMER ACCOUNT
	public synchronized String createAccountRecord(String custId, String fname, String lname, String custAddress, String custPhone, String custBranch, String custBalance) throws Exception
	{
		//String path = getBankRecords(custId);
		//database = getDatabase(path);
		
		String file = "";
		
		if(custBranch.equalsIgnoreCase("quebec") == true)
		{
			file = "../A2_Bank/Cust_Folders/Quebec_Log/QcAccountsList.txt";
		}
		else if(custBranch.equalsIgnoreCase("manitoba"))
		{
			file = "../A2_Bank/Cust_Folders/Manitoba_Log/MbAccountsList.txt";
		}
		else if(custBranch.equalsIgnoreCase("new brunswick"))
		{
			file = "../A2_Bank/Cust_Folders/Nb_Log/NbAccountsList.txt";
		}
		else
		{
			file = "../A2_Bank/Cust_Folders/Bc_Log/BcAccountsList.txt";
		}
		
		File createFile = new File(file);
		createFile.createNewFile();
		FileWriter writer = new FileWriter(file, true);
		BufferedWriter bw = new BufferedWriter(writer);
		
		Set set = database.entrySet();
		Iterator i = set.iterator();
		
		String mapKey = Character.toString(lname.charAt(0));
		
		if(database.containsKey(mapKey) == true)
		{
			customerRecords = database.get(mapKey);
			Customer customer = new Customer(custId, fname, lname, custAddress, custPhone, custBranch, custBalance);
			customerRecords.add(customer);
			database.put(mapKey, customerRecords);
			
			bw.newLine();
			bw.write(custId + "," + fname + "," + lname + "," + custAddress + "," + custPhone + "," + custBranch + "," + custBalance);
			bw.flush();
			bw.close();
			
			updateDatabase(dbFile);
			//return "Success" + custId + " account created";
		}
		else
		{
			customerRecords = new ArrayList();
			Customer customer = new Customer(custId, fname, lname, custAddress, custPhone, custBranch, custBalance);
			customerRecords.add(customer);
			database.put(mapKey, customerRecords);
			
			bw.newLine();
			bw.write(custId + "," + fname + "," + lname + "," + custAddress + "," + custPhone + "," + custBranch + "," + custBalance);
			bw.flush();
			bw.close();
			
			updateDatabase(dbFile);
			//return "Success" + custId + " account created";
		}
		
		return "Success" + custId + " account created";
	}
	
	
	
	
	// HANDLES EDITING CUSTOMER RECORD
	public synchronized String editRecord(String custId, String fieldName, String newValue) throws IOException
	{
		//String path = getBankRecords(custId);
		//database = getDatabase(path);
		
		
		if(fieldName.equalsIgnoreCase("address") || fieldName.equalsIgnoreCase("phone") || fieldName.equalsIgnoreCase("branch"))
		{
			Set set = database.entrySet();
			Iterator i = set.iterator();
			
			while(i.hasNext())
			{
				Map.Entry me = (Map.Entry) i.next();
				customerRecords = (List) me.getValue();
				
				for(int x = 0; x < customerRecords.size(); x++)
				{
					Customer record = customerRecords.get(x);
					
					if(record.getCustomerId().toString().equalsIgnoreCase(custId))
					{
						String file = getBankFile(custId, record.getBranch());
						File createFile = new File(file);
						createFile.createNewFile();
						FileWriter fw = new FileWriter(file, true);
						BufferedWriter bw = new BufferedWriter(fw);
						
						if(fieldName.equalsIgnoreCase("address"))
						{
							record.setAddress(newValue);
							customerRecords.set(x, record);
							me.setValue(customerRecords);
						}
						else if(fieldName.equalsIgnoreCase("phone"))
						{
							record.setPhone(newValue);
							customerRecords.set(x, record);
							me.setValue(customerRecords);
						}
						else
						{
							record.setBranch(newValue);
							customerRecords.set(x, record);
							me.setValue(customerRecords);
						}
						
						bw.write("Successfully edited " + fieldName + " to " + newValue);
						bw.newLine();
						bw.flush();
						bw.close();
						
						updateDatabase(dbFile);
						
						return "Success";
						
					}
				}
				
			}
		}
		else
		{
			return "Failure";
		}
		
		return "Success";
	}
	
	
	
	// RETRIEVE CUSTOMER LOG FILE
	public String getBankFile(String custId, String custBranch)
	{
		String file = "";
		
		if(custBranch.equalsIgnoreCase("quebec") == true)
		{
			file = "../A2_Bank/Cust_Folders/Quebec_log/" + custId + ".txt";
		}
		else if(custBranch.equalsIgnoreCase("manitoba"))
		{
			file = "../A2_Bank/Cust_Folders/Manitoba_Log/" + custId + ".txt";
		}
		else if(custBranch.equalsIgnoreCase("new brunswick"))
		{
			file = "../A2_Bank/Cust_Folders/Nb_Log/" + custId + ".txt";
		}
		else
		{
			file = "../A2_Bank/Cust_Folders/Bc_Log/" + custId + ".txt";
		}
		
		return file;
	}
	
	
	
	// RETRIEVES ACCOUNT LIST FILES
	public String getBankRecords(String branchId)
	{
		String file = "";
		
		if(branchId.equalsIgnoreCase("QC") == true)
		{
			file = "../A2_Bank/Cust_Folders/Quebec_log/QcAccountsList.txt";
		}
		else if(branchId.equalsIgnoreCase("MB"))
		{
			file = "../A2_Bank/Cust_Folders/Manitoba_Log/MbAccountsList.txt";
		}
		else if(branchId.equalsIgnoreCase("NB"))
		{
			file = "../A2_Bank/Cust_Folders/Nb_Log/NbAccountsList.txt";
		}
		else
		{
			file = "../A2_Bank/Cust_Folders/Bc_Log/BcAccountsList.txt";
		}
		
		return file;
	}
	
	
	
	// Fill the hashmap database with records according to the server
	public HashMap <String, List> getDatabase(String file)
	{
		Customer customer;
		String fileRecord = file;
		BufferedReader br = null;
		FileReader fr = null;
		totalCustAmt = 0;
		
		try
		{
			fr = new FileReader(fileRecord);
			br = new BufferedReader(fr);
			
			String line;
			
			while((line = br.readLine()) != null)
			{
				String[] fieldNames = line.split(",");
				char lastNameChar = fieldNames[2].toString().charAt(0);
				totalCustAmt++;
				
				customer = new Customer(fieldNames[0], fieldNames[1], fieldNames[2], fieldNames[3], fieldNames[4], fieldNames[5], fieldNames[6]);
				
				if(database.containsKey(Character.toString(lastNameChar)))
				{
					customerRecords = database.get(Character.toString(lastNameChar));
					customerRecords.add(customer);
					database.put(Character.toString(lastNameChar), customerRecords);
				}
				else
				{
					customerRecords = new ArrayList();
					customerRecords.add(customer);
					database.put(Character.toString(lastNameChar), customerRecords);
				}
			}
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return database;
	}
	
	
	public void updateDatabase(String file) throws IOException
	{
		File createFile = new File(file);
		createFile.createNewFile();
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		
		Set set = database.entrySet();
		Iterator i = set.iterator();
		
		while(i.hasNext())
		{
			Map.Entry me = (Map.Entry) i.next();
			customerRecords = (List) me.getValue();
			
			for(int x = 0; x < customerRecords.size(); x++)
			{
				Customer customer = customerRecords.get(x);
				
				bw.write(customer.getCustomerId() + "," + customer.getFirstName() + "," + customer.getLastName() + "," + customer.getAddress() + "," + customer.getPhone() + "," + customer.getBranch() + "," + customer.getBalance());
				bw.newLine();
			}
		}
		
		bw.flush();
		bw.close();
	}
	
	
	
	


	// TRANSFER FUND FORGET ABOUT THIS METHOD ITS NOT BEING USED, KEPT FOR REFERENCE
	public synchronized String transferFund(String sourceId, double amount, String destinationId) throws IOException {
		// TODO Auto-generated method stub
		String path = getBankRecords(sourceId);
		database = getDatabase(path);
		
		Set set = database.entrySet();
		Iterator i = set.iterator();
		
		while(i.hasNext())
		{
			Map.Entry me = (Map.Entry) i.next();
			customerRecords = (List) me.getValue();
			
			for(int x = 0; x < customerRecords.size(); x++)
			{
				Customer record = customerRecords.get(x);
				
				if(record.getCustomerId().toString().equalsIgnoreCase(sourceId))
				{
					String file = getBankFile(sourceId, record.getBranch());
					File createFile = new File(file);
					createFile.createNewFile();
					FileWriter fw = new FileWriter(file, true);
					BufferedWriter bw = new BufferedWriter(fw);
					
					double oldAmount = Double.parseDouble(record.getBalance());
					double newAmount = oldAmount - amount;
					
					if(newAmount >= 0)
					{
						record.setBalance(Double.toString(newAmount));
						customerRecords.set(x, record);
						me.setValue(customerRecords);
						
						bw.write("Successfully transfer fund of $" + amount + " towards customer #" + destinationId);
						bw.flush();
						bw.close();
						
						Iterator i2 = set.iterator();
						while(i2.hasNext())
						{
							Map.Entry me2 = (Map.Entry) i2.next();
							customerRecords = (List) me2.getValue();
							
							for(int z = 0; z < customerRecords.size(); z++)
							{
								Customer record2 = (Customer) customerRecords.get(x);
								
								if(record2.getCustomerId().toString().equalsIgnoreCase(destinationId))
								{
									double oldAmount2 = Double.parseDouble(record2.getBalance());
									double newAmount2 = oldAmount2 + amount;
									record2.setBalance(Double.toString(newAmount2));
									customerRecords.set(z, record2);
									me.setValue(customerRecords);
									return "Transfer fund successful";
								}
							}
						}
						
						return "Sorry, the customer account you want to transfer does not exist";
					}
					else
					{
						return "Sorry, you do not have enough cash to transfer";
					}
				}
			}
		}
		
		return null;
	}


	// Get total customers in this branch
	public String getCount() throws IOException {
		// TODO Auto-generated method stub
		
		return Integer.toString(totalCustAmt);
	}
	
	
	

}
