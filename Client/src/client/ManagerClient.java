package client;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import BankModule.ManagerBank;
import BankModule.ManagerBankHelper;

public class ManagerClient extends Client{

//-----------------------------------------------------CONSTRUCTOR----------------------------------------------------------------------------//
	public ManagerClient(String customerID, PrintWriter log, ORB orb) {
		super(customerID,log, orb);
	}

//----------------------------------------------------METHODS--------------------------------------------------------------------------//
	@Override
	protected void getCommandOptions() {
		System.out.println("List Options					Help");
		System.out.println("Make a Deposit					Deposit, <CustomerID>, <Amount>");
		System.out.println("Make a Withdrawl				Withdraw, <CustomerID>, <Amount>");
		System.out.println("Check the Balance				Balance, <CustomerID>");
		System.out.println("Add a New Account				NewAccount, <FirstName>, <LastName>, <Address>, <Phone>, <Branch>");
		System.out.println("Edit a Record					EditRecord, <CustomerID>, <FieldName>, <NewValue>");
		System.out.println("Get the number of Accounts			AccountCount");
		System.out.println("Transfer Funds					Transfer, <Source ID>, <Amount>, <Recipient ID>");
		System.out.println("Logout						Exit");	
	}

	@Override
	public boolean enactCommand(String command) {
		//Check if null
		if(command == null){
			return false;
		}
		ArrayList<String> arguments = this.parse(command);
		String datetime = (LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		ManagerBank obj = null;
		String message = "";
		double amount;
		
		try{
		
			switch(getChoice(arguments.get(0))){
			
			case 0: //Exit
				return false;
				
			case 1: //Help
				this.getCommandOptions();
				break;
				
			case 2: //Deposit
				if(arguments.size() != 3){
					System.out.println("Improper use of arguments");
					return true;
				}
				amount = (new Double(arguments.get(2)).doubleValue());
				this.log.printf("%s: %s for %s - ", datetime, arguments.get(0), arguments.get(1));
				obj = getManagerBankObj(arguments.get(1).substring(0, 2));
				message = obj.deposit(this.customerID, arguments.get(1), amount);
				break;
				
			case 3: //Withdraw
				if(arguments.size() != 3){
					System.out.println("Improper use of arguments");
					return true;
				}
				amount = (new Double(arguments.get(2)).doubleValue());
				this.log.printf("%s: %s for %s - ", datetime, arguments.get(0), arguments.get(1));
				obj = getManagerBankObj(arguments.get(1).substring(0, 2));
				message = obj.withdraw(this.customerID, arguments.get(1), amount);
				break;
				
			case 4: //GetBalance
				if(arguments.size() != 2){
					System.out.println("Improper use of arguments");
					return true;
				}
				this.log.printf("%s: %s for %s - ", datetime, arguments.get(0), arguments.get(1));
				obj = getManagerBankObj(arguments.get(1).substring(0, 2));
				message = obj.getBalance(this.customerID, arguments.get(1));
				break;
				
			case 5:  //Create Account
				if(arguments.size() != 6){
					System.out.println("Improper use of arguments");
					return true;
				}
				//validate branch input
				boolean valid = false;
				for (Branches b : Branches.values()) {
					   if(b.toString().equals(arguments.get(5)))
						   valid = true;
				}
				if(!valid){
					return true;
				}
				
				this.log.printf("%s: %s - ", datetime, arguments.get(0));
				obj = getManagerBankObj(arguments.get(5));
				message = obj.createAccountRecord(this.customerID, arguments.get(1), arguments.get(2), arguments.get(3), arguments.get(4), arguments.get(5));
				break;
				
			case 6: //editRecord
				if(arguments.size() != 4){
					System.out.println("Improper use of arguments");
					return true;
				}
				this.log.printf("%s: %s - ", datetime, arguments.get(0));
				obj = getManagerBankObj(arguments.get(1).substring(0, 2));
				message = obj.editRecord(this.customerID, arguments.get(1), arguments.get(2), arguments.get(3));
				break;
				
			case 7: //AccountCount
				if(arguments.size() != 1){
					System.out.println("Improper use of arguments");
					return true;
				}
				this.log.printf("%s: %s - ", datetime, arguments.get(0));
				obj = getManagerBankObj(this.customerID.substring(0, 2));
				message = obj.getAccountCount(this.customerID);
				break;
				
			case 8: //Transfer Funds
				if (arguments.size() != 4) {
					System.out.println("Improper use of arguments");
					return true;
				}
				amount = (new Double(arguments.get(2)).doubleValue());
				this.log.printf("%s: %s - ", datetime, arguments.get(0));
				obj = getManagerBankObj(arguments.get(1).substring(0, 2));
				message = obj.transferFund(this.customerID, arguments.get(1), amount, arguments.get(3));
				break;
				
			default: //Not valid command
				System.out.println("That is not a valid command. Try again.");
				break;
			}
		}
		catch(NumberFormatException e){
			System.out.println("Improper use of arguments");
			return true;
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(2);
		}
		
		System.out.println(message);
		this.log.println(message);
		log.flush();
		return true;
		
	}
	
	//---------------------------------------------------------CORBA-HELPER------------------------------------------------------------------------//

	private ManagerBank getManagerBankObj(String branch) throws Exception{
		String objname = "mb";
		ManagerBank obj = null;
		
		// Setup CORBA ORB access
		org.omg.CORBA.Object objRef = this.orb.resolve_initial_references("NameService");
		NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
	    obj = (ManagerBank) ManagerBankHelper.narrow(ncRef.resolve_str(objname));
		
		return obj;
	}

}
