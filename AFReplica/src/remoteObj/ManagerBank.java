package remoteObj;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public interface ManagerBank {
	@WebMethod
	String createAccountRecord(String managerID, String firstName, String lastName, String address, String phone, String branch);

	@WebMethod
	String editRecord(String managerID, String customerID, String fieldName, String newValue);

	@WebMethod
	String getAccountCount(String managerID);

	@WebMethod
	String deposit(String managerID, String customerID, double amt);

	@WebMethod
	String withdraw(String managerID, String customerID, double amt);

	@WebMethod
	String getBalance(String managerID, String customerID);

	@WebMethod
	String transferFund(String managerID, String sourceCustomerID, double amount, String destinationCustomerID);
}
