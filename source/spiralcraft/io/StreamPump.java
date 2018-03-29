//
// Copyright (c) 2000,2012 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;


import spiralcraft.io.StreamEvent;
import spiralcraft.io.StreamListener;
import spiralcraft.io.StreamListenerSupport;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

/**
 * A Runnable that continuously copies
 *   an InputStream into an OutputStream
 */
public class StreamPump
  implements Runnable
{
  private static final ClassLog _log=ClassLog.getInstance(StreamPump.class);

  private static int ID=0;

  private int id=ID++;
  private InputStream in;
  private OutputStream out;
  private IOException exception;
  private int bufsize=8192;
  private boolean checkAvailable=false;
  private boolean done;
  private boolean stopped=false;
  private boolean ignoreTimeouts=false;
  private int pollIntervalMs=100;
  private boolean closeStreams=true;
  private StreamListenerSupport listeners
    =new StreamListenerSupport();
  private OutputStream traceStream;
  private boolean alwaysBlock;
  private boolean draining;
  private boolean debug;

  public StreamPump(InputStream in,OutputStream out)
  {
    this.in=in;
    this.out=out;
  }

  public void setTraceStream(OutputStream trace)
  { this.traceStream=trace;
  }

  public void setBufferSize(int bufsize)
  { this.bufsize=bufsize;
  }
  
  public void setDebug(boolean debug)
  { this.debug=debug;
  }

  /**
   * Specify the interval at which an inactive stream will
   *   be polled. Default=10ms
   */  
  public void setPollIntervalMs(int interval)
  { pollIntervalMs=interval;
  }

  /** 
   * Indicate that the pump should poll the stream
   *   for available data instead of blocking to
   *   read full buffers.
   */
  public void setCheckAvailable(boolean check)
  { this.checkAvailable=check;
  }

  /**
   * Close streams when stopping. Default is enabled.
   */
  public void setCloseStreams(boolean closeStreams)
  { this.closeStreams=closeStreams;
  }

  public void drainAndJoin(int waitTime)
  { 
    draining=true;
    join(waitTime);
  }

  public void stop()
  { 
    done=true;
    synchronized (this)
    { notify();
    }
  }

  public void join(int waitTime)
  {
    try
    {
      synchronized (this)
      {
        if (!stopped)
        { wait(waitTime);
        }
      }
    }
    catch (InterruptedException x)
    { }
    stop();
  }
  
  /**
   * Indicate that the pump should always try to
   *   read at least one byte from the stream, even
   *   when getAvailable() returns 0
   *
   * Note that on some implementations, close() will not close
   *   a blocked Socket stream. For this to work properly, make sure
   *   that any streams from Sockets have Socket.soTimeout set to 
   *   a small value (under 1 second), and also setIgnoreTimeouts(true).
   */
  public void setAlwaysBlock(boolean val)
  { alwaysBlock=val;
  }

  /**
   * Indicate whether InterruptedIOExceptions
   *   should be ignored. Default is false. Set to
   *   true to allow streams to remain inactive longer than
   *   their timeout value.
   */
  public void setIgnoreTimeouts(boolean val)
  { ignoreTimeouts=val;
  }

  @Override
  public void run()
  {
    synchronized (this)
    { stopped=false;
    }

    try
    { 
      byte[] buffer=new byte[bufsize];
      while (!done)
      {
        // Default to read the buffer size
        int bytesToRead=bufsize;
        if (checkAvailable)
        { 
          // Don't read more than what is available
          bytesToRead=Math.min(in.available(),bufsize);
        }
        else if (draining)
        { 
          // Don't block if we're draining
          bytesToRead=0;
        }
        if (bytesToRead==0 && alwaysBlock && !draining)
        { 
          // If we always need to block and we aren't draining
          //   read at least 1 byte
          bytesToRead=1;
        }

        if (bytesToRead>0)
        {
          int read=0;
          try
          { 
            if (debug)
            { _log.log(Level.FINE,id+":Reading "+bytesToRead);
            }
            read=in.read(buffer,0,bytesToRead);
            if (debug)
            { _log.log(Level.FINE,id+":Read "+read);
            }
          }
          catch (InterruptedIOException x)
          {
            if (!ignoreTimeouts)
            { 
              if (debug)
              { _log.log(Level.DEBUG,id+":Timed out");
              }
              throw x;
            }
          }
          if (read==-1)
          { break;
          }
          else if (read>0)
          { 
            if (traceStream!=null)
            { 
              traceStream.write(buffer,0,read);
              traceStream.flush();
            }
            out.write(buffer,0,read);
            out.flush();
          }
        }
        else if (bytesToRead<0 || draining)
        { break;
        }
        else
        {
          try
          {
            synchronized(this)
            { wait(pollIntervalMs);
            }
          }
          catch (InterruptedException x)
          { break;
          }
        }
      }
    }
    catch (IOException x)
    { exception=x;
    }
    finally
    {
      synchronized(this)
      {
        stopped=true;
        if (closeStreams)
        { 
          try
          { in.close();
          }
          catch (IOException x)
          { }
          try
          { 
            out.flush();
            out.close();
          }
          catch (IOException x)
          { }
        }
        listeners.streamClosed(new StreamEvent(in));
        notify();
      }
    }
  }

  public void addStreamListener(StreamListener listener)
  { listeners.add(listener);
  }

  public void removeStreamListener(StreamListener listener)
  { listeners.remove(listener);
  }

  public IOException getException()
  { return exception;
  }

}
