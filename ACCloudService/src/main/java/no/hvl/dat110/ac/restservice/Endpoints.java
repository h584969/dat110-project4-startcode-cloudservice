package no.hvl.dat110.ac.restservice;

public enum Endpoints {
	HELLO("/accessdevice/hello"),
	POST_LOG("/accessdevice/log"),
	GET_LOGS("/accessdevice/log"),
	GET_LOG("/accessdevice/log/:id"),
	PUT_CODE("/accessdevice/code"),
	GET_CODE("/accessdevice/code"),
	DELETE_LOGS("/accessdevice/log");
	
	private final String endpoint;
	
	
	private Endpoints(String endpoint) {
		this.endpoint = endpoint;
	}
	
	@Override
	public String toString() {
		return this.endpoint;
	}
}
