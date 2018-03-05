package edu.rit.csci759.jsonrpc.server;

/**
* Demonstration of the JSON-RPC 2.0 Server framework usage. The request
* handlers are implemented as static nested classes for convenience, but in 
* real life applications may be defined as regular classes within their old 
* source files.
*
* @author Vladimir Dzhuvinov
* @version 2011-03-05
*/ 

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;
import com.thetransactioncompany.jsonrpc2.server.RequestHandler;

public class JsonHandler {
	
	// Implements a handler for an "echo" JSON-RPC method
	 public static class EchoHandler implements RequestHandler {
		

	     // Reports the method names of the handled requests
	     public String[] handledRequests() {
			
	         return new String[]{"echo"};
	     }
			
			
	      // Processes the requests
	      public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {
				
	          if (req.getMethod().equals("echo")) {
					
	              // Echo first parameter
					
	              List params = (List)req.getParams();
		 
		         Object input = params.get(0);
		 
		         return new JSONRPC2Response(input, req.getID());
	         }
	         else {
		
	             // Method name not supported
					
	             return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
		    }
	     }
	 }
		
		
	 
	 
	 // Implements a handler for "getDate" and "getTime" JSON-RPC methods
	 // that return the current date and time
	 public static class DateTimeHandler implements RequestHandler {
		
		
	     // Reports the method names of the handled requests
		public String[] handledRequests() {
		
		    return new String[]{"getDate", "getTime"};
		}
		
		
		// Processes the requests
		public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {
		
		    if (req.getMethod().equals("getDate")) {
		    
		        DateFormat df = DateFormat.getDateInstance();
			
			String date = df.format(new Date());
			
			return new JSONRPC2Response(date, req.getID());

	         }
	         else if (req.getMethod().equals("getTime")) {
		    
		        DateFormat df = DateFormat.getTimeInstance();
			
			String time = df.format(new Date());
			
			return new JSONRPC2Response(time, req.getID());
	         }
		    else {
		    
		        // Method name not supported
			
			return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
	         }
	     }
	 }
}
