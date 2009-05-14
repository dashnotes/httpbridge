/**
 * $RCSfile: $
 * $Revision: $
 * $Date: $
 *
 * Copyright (C) 2007 Jive Software. All rights reserved.
 *
 * This software is published under the terms of the GNU Public License (GPL),
 * a copy of which is included in this distribution.
 */
package se.dashnotes.httpbridge;

/**
 * This exception is thrown to indicate that a bot session already exists.
 * 
 * @author Aznidin Zainuddin
 *
 */
public class BotzSessionAlreadyExistsException extends Exception {
	public BotzSessionAlreadyExistsException() {
		super();
	}

	public BotzSessionAlreadyExistsException(String msg) {
		super(msg);
	}

	public BotzSessionAlreadyExistsException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public BotzSessionAlreadyExistsException(Throwable cause) {
		super(cause);
	}
}
