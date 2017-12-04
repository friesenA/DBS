package AFReplica.remoteObj;

public interface ManagerBank {
	String createAccountRecord(String managerID, String firstName, String lastName, String address, String phone, String branch);

	String editRecord(String managerID, String customerID, String fieldName, String newValue);

	String getAccountCount(String managerID);

	String deposit(String managerID, String customerID, double amt);

	String withdraw(String managerID, String customerID, double amt);

	String getBalance(String managerID, String customerID);

	String transferFund(String managerID, String sourceCustomerID, double amount, String destinationCustomerID);
}
