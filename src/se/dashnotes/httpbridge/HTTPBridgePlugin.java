package se.dashnotes.httpbridge;


import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.Log;
import org.xmpp.packet.Presence;

import java.io.File;
import java.util.Set;
import java.util.Enumeration;
import java.util.Iterator;

import net.sf.kraken.KrakenPlugin;

public class HTTPBridgePlugin implements Plugin
{
  HTTPHandler http;
  BotzConnection bot;
  PluginManager pluginManager;
  KrakenPlugin kraken;

  /*
    * (non-Javadoc)
    *
    * @see org.jivesoftware.openfire.container.Plugin#destroyPlugin()
    */
  //@Override
  public void destroyPlugin()
  {
    http.close();
    bot.close();
  }

  /*
    * (non-Javadoc)
    *
    * @see org.jivesoftware.openfire.container.Plugin#initializePlugin(org.jivesoftware.openfire.container.PluginManager,
    *      java.io.File)
    */
  //@Override
  public void initializePlugin(PluginManager manager, File pluginDirectory)
  {
    try
    {
      //this.setCallbackUrl("http://dashnotes.dev/openfire");
      //this.setUser("bot");

      this.pluginManager = manager;

      Iterator it = manager.getPlugins().iterator();
      while(it.hasNext())
      {
        Plugin plugin = (Plugin)it.next();
        String name = manager.getName(plugin);
        Log.debug(name +" - "+plugin.toString());
        if(name.equals("Kraken IM Gateway"))
        {
          this.kraken = (KrakenPlugin)plugin;
        }
      }
      //this.kraken = (KrakenPlugin)manager.getPlugin("Kraken IM Gateway");
      Log.debug("Kraken: "+this.kraken);
      //Set transports = this.kraken.getTransports();


      BotzPacketReceiver packetReceiver = new MessageHandler(this.getCallbackUrl());

      this.bot = new BotzConnection(packetReceiver);

      this.http = new HTTPHandler(bot, this.kraken);

      bot.login(this.getUser());
      Presence presence = new Presence();
      presence.setStatus("Online");
      bot.sendPacket(presence);

      Log.info("HTTPBridge initialized");

    } catch (Exception e)
    {
      Log.error(e);
    }

  }


  public void setCallbackUrl(String url)
  {
    JiveGlobals.setProperty("plugin.httpbridge.callback_url", url);
  }

  public String getCallbackUrl()
  {
    return JiveGlobals.getProperty("plugin.httpbridge.callback_url");
  }


  public void setUser(String url)
  {
    JiveGlobals.setProperty("plugin.httpbridge.user", url);
  }

  public String getUser()
  {
    return JiveGlobals.getProperty("plugin.httpbridge.user", "bot");
  }


}

