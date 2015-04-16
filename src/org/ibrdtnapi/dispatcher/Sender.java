package org.ibrdtnapi.dispatcher;

import java.util.logging.Logger;

import org.ibrdtnapi.Api;
import org.ibrdtnapi.dispatcher.Dispatcher.State;
import org.ibrdtnapi.entities.Bundle;

/**
 * 
 * This class is used to send bundle.
 * It sends only bundle with a single block.
 * 
 * It is launched by {@link Dispatcher}.
 *
 */
public class Sender implements Runnable {
	private static final Logger log = Logger.getLogger(Sender.class.getName());
	private CommunicatorOutput communicatorOutput = null;
	private Dispatcher dispatcher = null;
	private Bundle bundle = null;

	public Sender(Dispatcher dispatcher, CommunicatorOutput communicatorOutput,	Bundle bundle) {
		synchronized(Api.lockSender) {
			this.communicatorOutput = communicatorOutput;
			this.dispatcher = dispatcher;
			this.bundle = bundle;
		}
	}

	@Override
	public void run() {
		synchronized(Api.lockSender) {
			Sender.log.fine("Sending started with:" + this.bundle);
			//Request the daemon to receive a bundle:
			this.communicatorOutput.query("bundle put plain");
			while(this.dispatcher.getState() != State.PUTTING);
			this.communicatorOutput.query("Source: api:me");
			this.communicatorOutput.query("Destination: " + this.bundle.getDestination());
			this.communicatorOutput.query("Processing flags: 144");//Why 144? Just the default, let's stick to it for now.
			this.communicatorOutput.query("Blocks: 1");
			this.communicatorOutput.query("");
			this.communicatorOutput.query("Block: 1");
			this.communicatorOutput.query("Flags: LAST_BLOCK");
			this.communicatorOutput.query("Length: " + this.bundle.getDataLength());
			this.communicatorOutput.query("");
			this.communicatorOutput.query(this.bundle.getEncoded());
			this.communicatorOutput.query("");
			this.communicatorOutput.query("");
			while(this.dispatcher.getState() != State.BDL_REGISTERED);
			this.communicatorOutput.query("bundle send");
			while(this.dispatcher.getState() != State.BDL_SENT);
			
			Sender.log.fine("Sending finished with:" + this.bundle);
		}
	}
}