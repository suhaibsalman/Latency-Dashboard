package entity;

public class LatencyDashboard {
	private String date;
	private String sessionID;
	private String serviceName;
	private String diffWithLastRequest;
	private String Type;
	private String TransID;
	
	public LatencyDashboard(String date, String sessionID, String serviceName, String diffWithLastRequest, String type,
			String transID) {
		super();
		this.date = date;
		this.sessionID = sessionID;
		this.serviceName = serviceName;
		this.diffWithLastRequest = diffWithLastRequest;
		Type = type;
		TransID = transID;
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
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}

	public String getTransID() {
		return TransID;
	}

	public void setTransID(String transID) {
		TransID = transID;
	}
	
}
