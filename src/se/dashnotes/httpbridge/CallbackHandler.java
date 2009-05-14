package se.dashnotes.httpbridge;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.xmpp.packet.Message;
import org.jivesoftware.util.Log;

import java.io.IOException;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: jegt
 * Date: May 9, 2009
 * Time: 9:03:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class CallbackHandler
{
  URL callbackUrl;
  BotzConnection bot;

  public CallbackHandler(URL callbackUrl)
  {
    this.callbackUrl = callbackUrl;
  }

  public void setBot(BotzConnection bot)
  {
    this.bot = bot;
  }

  public void post(Message msg)
  {
    HttpClient client = new HttpClient();

    // Create a method instance.
    PostMethod method = new PostMethod(this.callbackUrl.toString());
    method.addRequestHeader("Content-Type", PostMethod.FORM_URL_ENCODED_CONTENT_TYPE+"; charset=UTF-8");

    // Provide custom retry handler is necessary
    //method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
    //		new DefaultHttpMethodRetryHandler(3, false));

    NameValuePair[] data = {
        new NameValuePair("message[from]", msg.getFrom().toString()),
        new NameValuePair("message[body]", msg.getBody()),
        new NameValuePair("message[subject]", msg.getSubject()),
        new NameValuePair("message[type]", msg.getType().toString()),
        new NameValuePair("message[to]", msg.getTo().toString())
        //new NameValuePair("message[network]", "Jabber")
    };

    method.setRequestBody(data);

    String responseBody = null;
    
    try
    {
      // Execute the method.
      int statusCode = client.executeMethod(method);

      if (statusCode != HttpStatus.SC_OK)
      {
        Log.error("Method failed: " + method.getStatusLine());
      }

      // Read the response body.
      responseBody = new String(method.getResponseBody());

      // Deal with the response.
      // Use caution: ensure correct character encoding and is not binary data
      Log.debug("HTTP Response: "+responseBody);

    } catch (HttpException e)
    {
      Log.error("Fatal protocol violation: "+ e.getMessage(), e);
      e.printStackTrace();
    } catch (IOException e)
    {
      Log.error("Fatal transport error: " + e.getMessage(), e);
      e.printStackTrace();
    } finally
    {
      // Release the connection.
      method.releaseConnection();
    }

    if(responseBody != null && responseBody != "")
    {
      Message reply = new Message();
      reply.setTo(msg.getFrom());
      reply.setFrom(bot.getIdentity());
      reply.setBody(responseBody);
      bot.sendPacket(reply);      
    }
    

  }

  /*
  public void post(Message msg)
  {
    try
    {
      HttpURLConnection conn = (HttpURLConnection) callbackUrl.openConnection();
      conn.setDoOutput(true);
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "text/xml");
      OutputStreamWriter wr = new
          OutputStreamWriter(conn.getOutputStream());
      wr.write(msg.toXML());
      wr.flush();

      BufferedReader rd = new BufferedReader(new
          InputStreamReader(conn.getInputStream()));
      String line;
      while ((line = rd.readLine()) != null)
      {
        System.out.println(line);
      }
      wr.close();
      rd.close();
    } catch (Exception e)
    {
      System.out.println("Error" + e);
    }
  }
  */

}
