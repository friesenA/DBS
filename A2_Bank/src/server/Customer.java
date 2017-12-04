package server;

public class Customer {
	
	private String customerId;
	private String firstName;
	private String lastName;
	private String address;
	private String phone;
	private String branch;
	private String balance;
	
	public Customer(String custId, String fname, String lname, String custAddress, String custPhone, String custBranch, String custBalance)
	{
		customerId = custId;
		firstName = fname;
		lastName = lname;
		address = custAddress;
		phone = custPhone;
		branch = custBranch;
		balance = custBalance;
	}
	
	public void setCustomerId(String custId)
	{
		customerId = custId;
	}
	
	public String getCustomerId()
	{
		return customerId;
	}
	
	
	public void setFirstName(String fname)
	{
		firstName = fname;
	}
	
	
	public String getFirstName()
	{
		return firstName;
	}
	
	public void setLastName(String lname)
	{
		lastName = lname;
	}
	
	public String getLastName()
	{
		return lastName;
	}
	
	public void setAddress(String custAddress)
	{
		address = custAddress;
	}
	
	public String getAddress()
	{
		return address;
	}
	
	public void setPhone(String custPhone)
	{
		phone = custPhone;
	}
	
	public String getPhone()
	{
		return phone;
	}
	
	public void setBranch(String custBranch)
	{
		branch = custBranch;
	}
	
	public String getBranch()
	{
		return branch;
	}
	
	public void setBalance(String custBalance)
	{
		balance = custBalance;
	}
	
	public String getBalance()
	{
		return balance;
	}

}
