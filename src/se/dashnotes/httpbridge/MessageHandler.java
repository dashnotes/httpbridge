package se.dashnotes.httpbridge;

import org.xmpp.packet.Packet;
import org.xmpp.packet.Message;
import org.xmpp.packet.Presence;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Roster;
import org.jivesoftware.util.Log;

import java.net.URL;
import java.net.MalformedURLException;

/**
 * Created by IntelliJ IDEA.
 * User: jegt
 * Date: May 8, 2009
 * Time: 1:37:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class MessageHandler implements BotzPacketReceiver
{
  BotzConnection bot;
  URL callbackUrl;
  CallbackHandler callback;

  public MessageHandler(String callbackUrl) throws MalformedURLException
  {
    this.callbackUrl = new URL(callbackUrl);
    this.callback = new CallbackHandler(this.callbackUrl);
  }

  public void initialize(BotzConnection bot)
  {
    this.bot = bot;
    callback.setBot(bot);
  }

  public void processIncoming(Packet packet)
  {
    Log.debug(packet.toString());

    if (packet instanceof Message && ((Message)packet).getBody() != null && packet.getFrom().getNode() != null)
    {
      callback.post((Message)packet);
      // Echo <message/> back to sender
      //packet.setTo(packet.getFrom());
      //bot.sendPacket(packet);
    }
    else if(packet instanceof Presence)
    {
      Presence presence = (Presence)packet;
      if(presence.getType() == Presence.Type.subscribe)
      {
        Log.debug("Subscribe request from "+presence.getFrom().toString());

        Presence p = new Presence(Presence.Type.subscribe);
        p.setFrom(bot.getIdentity());
        p.setTo(presence.getFrom());
        bot.sendPacket(p);

        Roster r = new Roster(IQ.Type.set);
        r.addItem(presence.getFrom(), Roster.Subscription.both);
        r.setFrom(bot.getIdentity());
        bot.sendPacket(r);

        p = new Presence(Presence.Type.subscribed);
        p.setFrom(bot.getIdentity());
        p.setTo(presence.getFrom());
        bot.sendPacket(p);
      }
    }
    else if(packet instanceof IQ)
    {
    }
    else
    {
    }
  }

  public void processIncomingRaw(String rawText)
  {
  };

  public void terminate()
  {
  };
  
}
