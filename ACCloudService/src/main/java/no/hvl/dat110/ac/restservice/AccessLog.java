package no.hvl.dat110.ac.restservice;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.google.gson.Gson;

public class AccessLog {
	
	// atomic integer used to obtain identifiers for each access entry
	private AtomicInteger cid;
	protected ConcurrentHashMap<Integer, AccessEntry> log;
	
	public AccessLog () {
		this.log = new ConcurrentHashMap<Integer,AccessEntry>();
		cid = new AtomicInteger(0);
	}

	// TODO: add an access entry to the log for the provided message and return assigned id
	public int add(String message) {
		
		int id = cid.addAndGet(1);
		
		this.log.put(id, new AccessEntry(id, message));
		
		return id;
	}
		
	// TODO: retrieve a specific access entry from the log
	public AccessEntry get(int id) {
		return this.log.get(id);
	}
	
	// TODO: clear the access entry log
	public void clear() {
		this.log.clear();
	}
	
	// TODO: return JSON representation of the access log
	public String toJson () {
    	
		String json = null;
    	
		List<AccessEntry> entries = log.values().stream().collect(Collectors.toList());
		
		Gson gson = new Gson();
		
    	return gson.toJson(entries);
    }
}
