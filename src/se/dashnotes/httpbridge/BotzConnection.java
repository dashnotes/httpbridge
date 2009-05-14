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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.SessionPacketRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.roster.Roster;
import org.jivesoftware.openfire.auth.AuthToken;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.net.VirtualConnection;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.LocalClientSession;
import org.jivesoftware.openfire.user.UserAlreadyExistsException;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.Log;
import org.jivesoftware.util.StringUtils;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.StreamError;

/**
 * The objective of BotzConnection class is to create a robot/bot application as
 * an internal user of the main XMPP server. The class's login methods performs
 * the necessary (virtual) connection to the server. The bot can login as an
 * anonymous or a real user.
 * 
 * <p>
 * The class's object uses a BotzPacketReceiver object passed to it via one of
 * it's constructors or via calls to
 * {@link #setPacketReceiver(BotzPacketReceiver)} method to receive packets from
 * other XMPP entities to the bot. The bot can reply to these packets with
 * {@link #sendPacket(Packet)} method. Thus, a class that wants to handle bot
 * packets must implement {@link BotzPacketReceiver} class.
 * 
 * <p>
 * Below is a sample parrot bot code snippet illustrating how to use
 * BotzConnection and BotzPacketReceiver:
 * 
 * <blockquote>
 * 
 * <pre>
 * *
 * *	BotzPacketReceiver packetReceiver = new BotzPacketReceiver() {
 * *	    BotzConnection bot;
 * *	    public void initialize(BotzConnection bot) { this.bot = bot; }
 * *	    public void processIncoming(Packet packet) {
 * *			if (packet instanceof Message) {
 * *		    	packet.setTo(packet.getFrom());
 * *		    	bot.sendPacket(packet);
 * *			}
 * *		}
 * *	    public void processIncomingRaw(String rawText) {};
 * *		public void terminate() {};
 * *	};
 * *
 * *	BotzConnection bot = new BotzConnection(packetReceiver);
 * *	try {
 * *	    bot.login(&quot;MyUsername&quot;);
 * *	    Presence presence = new Presence();
 * *	    presence.setStatus(&quot;Online&quot;);
 * *	    bot.sendPacket(presence);
 * *	} catch (Exception e) {
 * *	}
 * *
 * </pre>
 * 
 * </blockquote>
 * 
 * @author Aznidin Zainuddin
 * @see BotzPacketReceiver
 */
public class BotzConnection extends VirtualConnection {
	/**
	 * The packet receiver object that will handle receiving of packets.
	 */
	private BotzPacketReceiver packetReceiver;
	/**
	 * Holds the initialization state of the packet receiver.
	 */
	private boolean initPacketReceiver;
	/**
	 * Holds the session for the bot.
	 */
	private LocalClientSession localClientSession;

  private Roster roster;

  /**
	 * Creates a new instance of BotzConnection.
	 */
	public BotzConnection() {
	}

	/**
	 * Creates a new instance of BotzConnection with the specified packet
	 * receiver.
	 * 
	 * <p>
	 * When login is attempted with an instance created with this constructor,
	 * the packetReceiver traps incoming packets and texts as soon as the bot
	 * logs on.
	 * 
	 * @param packetReceiver
	 *            BotzConnection packetReceiver
	 */
	public BotzConnection(BotzPacketReceiver packetReceiver) {
		this.packetReceiver = packetReceiver;
	}


  public LocalClientSession getLocalClientSession()
  {
    return localClientSession;
  }

  public Roster getRoster()
  {
    return roster;
  }

  /**
	 * The method will be implicitly called by the server when the bot's
	 * connection is (virtually) closed. The method terminates the packet
	 * receiver.
	 */
	@Override
	public void closeVirtualConnection() {
		if (packetReceiver != null && initPacketReceiver) {
			packetReceiver.terminate();
			initPacketReceiver = false;
		}
	}

	/**
	 * Calls to this method is made by the server to deliver packets to the bot.
	 * This method will in turn call
	 * {@link BotzPacketReceiver#processIncoming(Packet)} of the packet receiver
	 * associated with the bot.
	 * 
	 * @param packet
	 *            XMPP packet
	 * @throws UnauthorizedException
	 *             When packets could not be delivered due to authorization
	 *             problem.
	 */
	public void deliver(Packet packet) throws UnauthorizedException {
		if (packetReceiver == null)
			return;
		packetReceiver.processIncoming(packet);
	}

	/**
	 * Calls to this method is made by the server to deliver raw text to the
	 * bot. This method will in turn call
	 * {@link BotzPacketReceiver#processIncomingRaw(String)} of the packet
	 * receiver associated with the bot.
	 * 
	 * @param text
	 *            The text string delivered to the bot.
	 */
	public void deliverRawText(String text) {
		if (packetReceiver == null)
			return;
		packetReceiver.processIncomingRaw(text);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jivesoftware.openfire.Connection#getAddress()
	 */
	//@Override
	public byte[] getAddress() throws UnknownHostException {
		return InetAddress.getLocalHost().getAddress();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jivesoftware.openfire.Connection#getHostAddress()
	 */
	//@Override
	public String getHostAddress() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostAddress();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jivesoftware.openfire.Connection#getHostName()
	 */
	//@Override
	public String getHostName() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostName();
	}

	/**
	 * Get the bot's packet receiver
	 * 
	 * @return BotzPacketReceiver packetReceiver
	 */
	public BotzPacketReceiver getPacketReceiver() {
		return packetReceiver;
	}

	/**
	 * Get the resource portion of the bot's JID.
	 * 
	 * @return Resource portion of the bot's JID.
	 */
	public String getResource() {
		if (localClientSession == null)
			return null;
		return localClientSession.getAddress().getResource();
	}

	/**
	 * Get the node's portion of the bot's JID.
	 * 
	 * @return Node portion of the bot's JID.
	 */
	public String getUsername() {
		if (localClientSession == null)
			return null;
		return localClientSession.getAddress().getNode();
	}

  	/**
	 * Get the node's portion of the bot's JID.
	 *
	 * @return Node portion of the bot's JID.
	 */
	public JID getIdentity() {
		if (localClientSession == null)
			return null;
		return localClientSession.getAddress();
	}

  /**
	 * Check whether the bot session is still active.
	 * 
	 * @return <tt>true</tt> if the bot is still active, <tt>false</tt>
	 *         otherwise.
	 */
	public boolean isLoggedOn() {
		return !isClosed();
	}

	/**
	 * Login to the XMPP server as an anonymous user. This method creates a
	 * virtual connection to the XMPP server and establish a user session. If
	 * the packet receiver is already defined, initialize it.
	 * 
	 * @throws BotzSessionAlreadyExistsException
	 *             If the users session already exists.
	 */
	public void login() throws BotzSessionAlreadyExistsException {
		if (isClosed())
			throw new BotzSessionAlreadyExistsException();
		localClientSession = SessionManager.getInstance().createClientSession(
				this);
		localClientSession.setAnonymousAuth();
		if (packetReceiver != null) {
			packetReceiver.initialize(this);
			initPacketReceiver = true;
		}
		return;
	}

	/**
	 * A convenient way to login. It uses the default "Botz" as the JID resource
	 * and auto create the user if it doesn't exist.
	 * 
	 * @param username
	 *            The username to login with.
	 * @throws BotzSessionAlreadyExistsException
	 *             If the bot's session already exists.
	 * @throws UserNotFoundException
	 *             If it fails to create the user.
	 * 
	 * @see #login(String, String, boolean)
	 */
	public void login(String username)
			throws BotzSessionAlreadyExistsException, UserNotFoundException {
		login(username, "Botz", true);
	}

	/**
	 * A convenient way to login. It auto create the user if it doesn't exist.
	 * 
	 * @param username
	 *            The username to login with.
	 * @param resource
	 *            The resource the user will bind to.
	 * @throws BotzSessionAlreadyExistsException
	 *             If the bot's session already exists.
	 * @throws UserNotFoundException
	 *             If it fails to create the user.
	 * 
	 * @see #login(String, String, boolean)
	 */
	public void login(String username, String resource)
			throws BotzSessionAlreadyExistsException, UserNotFoundException {
		login(username, resource, true);
	}

	/**
	 * Login to the XMPP server and establish a non-anonymous user session using
	 * the given username and resource. When <tt>createIfNotExist</tt> is
	 * <tt>true</tt>, a new user with the username will be created and stored
	 * in the database if it does not exist. When <tt>false</tt>, and the
	 * user does not exist, the method will not attempt the login. Whenever
	 * there's an error, the bot will not login.
	 * 
	 * @param username
	 *            Username to login with.
	 * @param resource
	 *            The resource the user will bind to.
	 * @param createIfNotExist
	 *            When specified as <tt>true</tt>, a new user will be created
	 *            and stored in the database if it does not exist.
	 * @throws BotzSessionAlreadyExistsException
	 *             If the bot's session already exists.
	 * @throws UserNotFoundException
	 *             If it fails to create the user.
	 */
	public void login(String username, String resource, boolean createIfNotExist)
			throws BotzSessionAlreadyExistsException, UserNotFoundException {
		if (isClosed())
			throw new BotzSessionAlreadyExistsException();

		JID jid = new JID(username.toLowerCase(), XMPPServer.getInstance().getServerInfo().getXMPPDomain(), resource);
		ClientSession oldSession = XMPPServer.getInstance().getRoutingTable()
				.getClientRoute(jid);

		// Check for session conflict
		if (oldSession != null) {
			try {
				oldSession.incrementConflictCount();
				int conflictLimit = SessionManager.getInstance()
						.getConflictKickLimit();
				if (conflictLimit != SessionManager.NEVER_KICK) {
					// Kick out the old connection that is conflicting with the
					// new one
					StreamError error = new StreamError(
							StreamError.Condition.conflict);
					oldSession.deliverRawText(error.toXML());
					oldSession.close();
				} else
					throw new BotzSessionAlreadyExistsException();
			} catch (Exception e) {
				Log.error("Error during login", e);
			}
		}

		if (!XMPPServer.getInstance().getUserManager().isRegisteredUser(
				jid.getNode())) {
			if (createIfNotExist) {
				try {
					// Bot doesn't care of whatever password it is.
					XMPPServer.getInstance().getUserManager().createUser(
							jid.getNode(), StringUtils.randomString(15), null,
							null);
				} catch (UserAlreadyExistsException e) {
					// Ignore
				}
			} else {
				throw new UserNotFoundException();
			}
		}

		localClientSession = SessionManager.getInstance().createClientSession(
				this);
		localClientSession.setAuthToken(new AuthToken(jid.getNode()), jid
				.getResource());
		if (packetReceiver != null) {
			packetReceiver.initialize(this);
			initPacketReceiver = true;
		}

    this.roster = XMPPServer.getInstance().getRosterManager().getRoster(username);

  }

	/**
	 * Logout the bot and destroy the active session. This method need not be
	 * called explicitly unless, for example, when callers need to refresh the
	 * assign a different username or resource (re-login).
	 */
	public void logout() {
		close();
	}

	/**
	 * Send a packet out to an XMPP entity. The packet must be one of
	 * <message/>, <iq/> or <presence/>. Callers need not specify the
	 * <tt>from</tt> attribute inside the packet because it will be
	 * automatically inserted with/replaced by the bot's real JID.
	 * 
	 * @param packet
	 *            The packet to send.
	 */
	public void sendPacket(Packet packet) {
		if (isClosed())
			throw new IllegalStateException("No valid session");
		SessionPacketRouter router = new SessionPacketRouter(localClientSession);
		router.route(packet);
	}

	/**
	 * Assign a packet receiver ({@link BotzPacketReceiver}) object that will
	 * receive packets to the bot. The method can be called repeatedly if
	 * necessary to dynamically change different packet receivers during a
	 * login. If the previous packet receiver is in an initialized state during
	 * this call, it will be terminated; and the new packet receiver will be
	 * initialized.
	 * 
	 * <p>
	 * If the previous packetReceiver is the same with the new one, this method
	 * will ignore the assignment.
	 * 
	 * @param packetReceiver
	 *            The packetReceiver object
	 */
	public void setPacketReceiver(BotzPacketReceiver packetReceiver) {
		if (this.packetReceiver == packetReceiver)
			return;
		if (this.packetReceiver != null && initPacketReceiver) {
			this.packetReceiver.terminate();
			initPacketReceiver = false;
		}
		this.packetReceiver = packetReceiver;
		if (!isClosed()) {
			this.packetReceiver.initialize(this);
			initPacketReceiver = true;
		}
	}

	/**
	 * Calls to this method is made by the server to notify about server
	 * shutdown to the bot.
	 */
	public void systemShutdown() {
		close();
	}
}