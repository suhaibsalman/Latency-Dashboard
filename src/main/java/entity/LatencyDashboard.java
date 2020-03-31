package entity;

public class LatencyDashboard {
	private String date;
	private String sessionID;
	private String serviceName;
	private String diffWithLastRequest;
	private String type;
	private String transID;
	
	public LatencyDashboard(String date, String sessionID, String serviceName, String diffWithLastRequest, String type,
			String transID) {
		super();
		this.date = date;
		this.sessionID = sessionID;
		this.serviceName = serviceName;
		this.diffWithLastRequest = diffWithLastRequest;
		this.type = type;
		this.transID = transID;
	}
	
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getSessionID() {
		return sessionID;
	}
	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getDiffWithLastRequest() {
		return diffWithLastRequest;
	}
	public void setDiffWithLastRequest(String diffWithLastRequest) {
		this.diffWithLastRequest = diffWithLastRequest;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getTransID() {
		return transID;
	}
	public void setTransID(String transID) {
		this.transID = transID;
	}
	
}
