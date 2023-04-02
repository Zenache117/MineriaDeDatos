package Portafolio;

public class Instancias {
	
	private String CaseID;
	private String Activity;
	private String StartDate;
	private String EndDate;
	private String AgentPosition;
	private String CustID;
	private String Product;
	private String ServiceType;
	private String Resource;
	
	public String getCaseID() {
		return CaseID;
	}
	public void setCaseID(String caseID) {
		CaseID = caseID;
	}
	public String getActivity() {
		return Activity;
	}
	public void setActivity(String activity) {
		Activity = activity;
	}
	public String getStartDate() {
		return StartDate;
	}
	public void setStartDate(String startDate) {
		StartDate = startDate;
	}
	public String getEndDate() {
		return EndDate;
	}
	public void setEndDate(String endDate) {
		EndDate = endDate;
	}
	public String getAgentPosition() {
		return AgentPosition;
	}
	public void setAgentPosition(String agentPosition) {
		AgentPosition = agentPosition;
	}
	public String getCustID() {
		return CustID;
	}
	public void setCustID(String custID) {
		CustID = custID;
	}
	public String getProduct() {
		return Product;
	}
	public void setProduct(String product) {
		Product = product;
	}
	public String getServiceType() {
		return ServiceType;
	}
	public void setServiceType(String serviceType) {
		ServiceType = serviceType;
	}
	public String getResource() {
		return Resource;
	}
	public void setResource(String resource) {
		Resource = resource;
	}
	
	@Override
	public String toString() {
		
		return CaseID;
		
	}
	

}
