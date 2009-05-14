package se.dashnotes.httpbridge;

import net.sf.kraken.BaseTransport;
import net.sf.kraken.KrakenPlugin;
import net.sf.kraken.session.TransportSession;
import org.jivesoftware.openfire.roster.RosterItem;
import org.jivesoftware.util.log.Hierarchy;
import org.jivesoftware.util.Log;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Presence;
import org.xmpp.packet.Roster;
import org.xmpp.packet.IQ;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jegt
 * Date: May 8, 2009
 * Time: 1:46:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class HTTPHandler extends AbstractHandler
{
  BotzConnection bot;
  Server server;
  KrakenPlugin kraken;

  public HTTPHandler(BotzConnection bot, KrakenPlugin kraken)
  {
    this.bot = bot;
    this.kraken = kraken;

    try
    {
      server = new Server(6666);
      server.setHandler(this);
      server.start();
    } catch (Exception e)
    {
      Log.error(e);
    }
  }


  public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
      throws IOException, ServletException
  {
    Log.debug("HTTPBridge request for " + target);

    if (target.equals("/add_contact"))
    {
      try
      {
        String jid = request.getParameter("jid").trim();
        String network = request.getParameter("network");
        String nickname = request.getParameter("nickname").trim();

        Log.debug("Adding " + jid);

        Presence p = new Presence(Presence.Type.subscribe);
        p.setFrom(bot.getIdentity());
        p.setTo(jid);
        bot.sendPacket(p);

        Roster r = new Roster(IQ.Type.set);
        r.addItem(new JID(jid), nickname, Roster.Ask.subscribe, Roster.Subscription.both, new ArrayList<String>());
        r.setFrom(bot.getIdentity());
        bot.sendPacket(r);

        if (!network.equals("jabber") && !network.equals("gtalk"))
        {
          Log.debug("Doing gateway addContact for "+jid);
          BaseTransport transport = kraken.getTransportInstance(network).getTransport();
          TransportSession session = transport.getSessionManager().getSession(bot.getIdentity());

          session.addContact(new JID(jid), nickname, new ArrayList<String>());
        }
        

      } catch (Exception e)
      {
        Log.error(e);
      }
    }
    else if (target.equals("/send"))
    {
      String jid = request.getParameter("jid").trim();
      String body = request.getParameter("body");

      Log.debug("Sending message to " + jid);

      Message msg = new Message();
      msg.setFrom(bot.getIdentity());
      msg.setTo(new JID(jid));
      msg.setBody(body);
      bot.sendPacket(msg);

      response.setContentType("text/html");
      response.setStatus(HttpServletResponse.SC_OK);
      response.getWriter().println("sent \"" + msg.getBody() + "\" to " + msg.getTo().toString());

    }

    ((Request) request).setHandled(true);

  }

  public void close()
  {
    try
    {
      server.stop();
    } catch (Exception e)
    {
      Log.error(e);
    }
  }

}
