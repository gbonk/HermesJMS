/* 
 * Copyright 2003,2004 Peter Lee, Colin Crist
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package hermes.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageEOFException;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

/**
 * @author Peter To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MessageUtils
{
   private static final Logger log = Logger.getLogger(MessageUtils.class);

   
   public static String asString(javax.jms.Message m) throws JMSException
   {
      if (m instanceof TextMessage)
      {
         return asString((TextMessage) m);
      }     
      else if (m instanceof BytesMessage)
      {
         return asString((BytesMessage) m);
      }     
      else
      {
         return null;
      }
   }
   public static byte[] asBytes(javax.jms.Message m) throws JMSException
   {
      if (m instanceof TextMessage)
      {
         return asBytes((TextMessage) m);
      }
      else if (m instanceof javax.jms.ObjectMessage)
      {
         return asBytes((ObjectMessage) m);
      }
      else if (m instanceof javax.jms.MapMessage)
      {
         return asBytes((MapMessage) m);
      }
      else if (m instanceof BytesMessage)
      {
         return asBytes((BytesMessage) m);
      }
      else if (m instanceof StreamMessage)
      {
         return asBytes((StreamMessage) m);
      }
      else
      {
         return null;
      }
   }

   public static byte[] asBytes(TextMessage m) throws JMSException
   {
      String s = m.getText().trim();
      byte[] bytes = null;

      if (s != null)
      {
         char[] chars = s.toCharArray();
      Charset cs = Charset.forName ("UTF-8");
        CharBuffer cb = CharBuffer.allocate (chars.length);
        cb.put (chars);
                 cb.flip ();
        ByteBuffer bb = cs.encode (cb);
        
        return bb.array();
      }
      else
         bytes = new byte[0];

      return bytes;
   }

   public static byte[] asBytes(ObjectMessage m) throws JMSException
   {
      byte[] bytes = null;

      Object o = m.getObject();

      ByteArrayOutputStream bytesOut = null;
      ObjectOutputStream objectOut = null;

      try
      {
         bytesOut = new ByteArrayOutputStream(1024);
         objectOut = new ObjectOutputStream(bytesOut);

         objectOut.writeObject(o);
         bytes = bytesOut.toByteArray();
      }
      catch (IOException e)
      {
         throw new IllegalStateException(":" + e.getMessage());
      }
      finally
      {
         IoUtils.closeQuietly(objectOut);
         IoUtils.closeQuietly(bytesOut);
      }

      return bytes;
   }

   public static byte[] asBytes(MapMessage m) throws JMSException
   {
      return new byte[0];
   }

   public static long getBodyLength(BytesMessage m) throws JMSException
   {
      try
      {
         //
         // Method does not exist before JMS 1.1

         return m.getBodyLength();
      }
      catch (NoSuchMethodError e)
      {

      }
      catch (AbstractMethodError e)
      {

      }
      long bytesSize = 0;
      try
      {
         m.reset();

         while (true)
         {
            m.readByte();
            bytesSize++;
         }
      }
      catch (MessageEOFException e2)
      {
      }
      catch (Exception ex)
      {
         log.error("unexpected exception reading bytes message, treating as EOF:" + ex.getMessage(), ex);
      }

      return bytesSize;

   }

   public static String asString(BytesMessage m) throws JMSException
   {
      final StringBuffer sb = new StringBuffer();

      sb.append(MessageUtils.asBytes(m));
      
      return sb.toString() ;
   }
   
   public static String asString(TextMessage m) throws JMSException
   {
      return m.getText().trim() ;
   }
   
   public static byte[] asBytes(BytesMessage m) throws JMSException
   {
      byte[] bytes = null;
      long bytesSize = getBodyLength(m) ;

      bytes = new byte[(int) bytesSize];

      m.reset();
      int i = 0;

      try
      {
         m.readBytes(bytes);
      }
      catch (MessageEOFException e2)
      {
      }
      catch (Exception ex)
      {
         log.error("unexpected exception reading bytes message, treating as EOF:" + ex.getMessage(), ex);
      }

      return bytes;
   }

   public static byte[] asBytes(StreamMessage m) throws JMSException
   {
      m.reset();

      final int CHUNK_SIZE = 1024;
      byte[] buffer = new byte[CHUNK_SIZE];
      ArrayList bytes = new ArrayList(CHUNK_SIZE);

      int bytesCopied = 0;
      int bytesRead = 0;

      do
      {
         bytesRead = m.readBytes(buffer);

         for (int i = 0; i < bytesRead; i++)
            bytes.add(new Byte(buffer[i]));

         bytesCopied += bytesRead;
      }
      while (bytesRead != 0);

      return asBytes(bytes);
   }

   public static byte[] asBytes(List l)
   {
      byte[] bytes = new byte[l.size()];
      for (int i = 0; i < l.size(); i++)
      {
         Byte b = (Byte) l.get(i);
         bytes[i] = b.byteValue();
      }

      return bytes;
   }

}
