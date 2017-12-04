package client;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import BankModule.CustomerBank;
import BankModule.CustomerBankHelper;

public class CustomerClient extends Client {

	// -----------------------------------------------------CONSTRUCTOR----------------------------------------------------------------------------//
	public CustomerClient(String customerID, PrintWriter log, ORB orb) {
		super(customerID, log, orb);
	}

	// ----------------------------------------------------METHODS--------------------------------------------------------------------------//
	@Override
	protected void getCommandOptions() {
		System.out.println("List Options					Help");
		System.out.println("Make a Deposit					Deposit, <Amount>");
		System.out.println("Make a Withdrawl				Withdraw, <Amount>");
		System.out.println("Check the Balance				Balance");
		System.out.println("Transfer Funds					Transfer, <Amount>, <Recipient ID>");
		System.out.println("Logout							Exit");
	}

	@Override
	public boolean enactCommand(String command) {
		// Check if null
		if (command == null) {
			return false;
		}
		ArrayList<String> arguments = this.parse(command);
		String datetime = (LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		String message = "";
		double amount;

		try {
			CustomerBank obj = getCustomerBankObj(this.customerID.substring(0, 2));

			switch (getChoice(arguments.get(0))) {

			case 0: // Exit
				return false;

			case 1: // Help
				this.getCommandOptions();
				break;

			case 2: // Deposit
				if (arguments.size() != 2) {
					System.out.println("Improper use of arguments");
					return true;
				}
				amount = (new Double(arguments.get(1)).doubleValue());
				this.log.printf("%s: %s - ", datetime, arguments.get(0));
				message = obj.deposit(this.customerID, amount);
				break;

			case 3: // Withdraw
				if (arguments.size() != 2) {
					System.out.println("Improper use of arguments");
					return true;
				}
				amount = (new Double(arguments.get(1)).doubleValue());
				this.log.printf("%s: %s - ", datetime, arguments.get(0));
				message = obj.withdraw(this.customerID, amount);
				break;

			case 4: // GetBalance
				if (arguments.size() != 1) {
					System.out.println("Improper use of arguments");
					return true;
				}
				this.log.printf("%s: %s - ", datetime, arguments.get(0));
				message = obj.getBalance(this.customerID);
				break;
			
			case 8: // Transfer Funds
				if (arguments.size() != 3) {
					System.out.println("Improper use of arguments");
					return true;
				}
				amount = (new Double(arguments.get(1)).doubleValue());
				this.log.printf("%s: %s - ", datetime, arguments.get(0));
				message = obj.transferFund(this.customerID, amount, arguments.get(2));
				break;

			default: // Not valid command
				System.out.println("That is not a valid command. Try again.");
				break;
			}
		} catch (NumberFormatException e) {
			System.out.println("Improper use of arguments");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(2);
		}

		System.out.println(message);
		this.log.println(message);
		log.flush();
		return true;
	}

	// ---------------------------------------------------------CORBA-HELPER------------------------------------------------------------------------//

	private CustomerBank getCustomerBankObj(String branch) throws Exception {
		String objname = "cb";
		CustomerBank obj = null;
		
		// Setup CORBA ORB access
		org.omg.CORBA.Object objRef = this.orb.resolve_initial_references("NameService");
		NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
		obj = (CustomerBank) CustomerBankHelper.narrow(ncRef.resolve_str(objname));

		return obj;
	}

}
