/**
 * Replaces all occurrences of a byte pattern in an
 *   InputStream. 
 */
package spiralcraft.io;

import java.io.IOException;
import java.io.InputStream;

import java.util.LinkedList;

import spiralcraft.util.BytePatternMatcher;
import spiralcraft.util.ByteBuffer;


public class PatternReplacingInputStream
  extends InputStream
{

  private final InputStream in;
  private final LinkedList<byte[]> bufferQueue=new LinkedList<byte[]>();

  private int _bufferPos;
  private byte[] buffer;
  private final byte[] replace;

  private final ByteBuffer pushBuffer=new ByteBuffer();

  private boolean done=false;
  private boolean streamDone=false;
  private final BytePatternMatcher detector;
  private int lastMatchPos=-1;
  
  public PatternReplacingInputStream(InputStream in,byte[] match,byte[] replace)
  { 
    this.in=in;
    detector=new BytePatternMatcher(match);
    this.replace=replace;
  }

  @Override
  public int read()
    throws IOException
  {
    if (done)
    { return -1;
    }
    

    if (buffer==null && bufferQueue.size()==0)
    {  
      while (!streamDone && bufferQueue.size()==0)
      {
        // Filter some more stuff
        int next=in.read();
        if (next==-1)
        {
          streamDone=true;
          if (pushBuffer.length()>0)
          { bufferQueue.add(pushBuffer.toByteArray());
          }
        }
        else
        {
          boolean gotMatch=detector.match((byte) next);
          if (gotMatch)
          {
            lastMatchPos=-1;
            pushBuffer.clear();
            if (replace.length>0)
            {
              bufferQueue.add(replace);
              break;
            }
          }
          else
          {
            if (detector.getMatchPos()==-1)
            {
              lastMatchPos=-1;
              // No current match. Start flushing
              if (pushBuffer.length()==0)
              {
                // Shortcut for efficiency.
                return next;
              }
              else
              {
                pushBuffer.append((byte) next);
                bufferQueue.add(pushBuffer.toByteArray());
                pushBuffer.clear();
                break;
              }
            }
            else 
            {
              // Possible match
              if (detector.getMatchPos()<=lastMatchPos)
              {
                // We slid, but there is still a possible match.
                // Slide the pushBuffer over to suit and flush
                //   what we can.
                int slideCount=(lastMatchPos-detector.getMatchPos())+1;
                bufferQueue.add(pushBuffer.removeBeginning(slideCount));
              }
              lastMatchPos=detector.getMatchPos();
              pushBuffer.append((byte) next);
            }
          }
        }
      }
    }


    if (buffer==null && bufferQueue.size()>0)
    {
      // Queue up the next buffer
      buffer=bufferQueue.removeFirst();
      _bufferPos=0;
    }

    if (buffer!=null && _bufferPos<buffer.length)
    {
      // Spit out an active buffer
      int ret=buffer[_bufferPos++];
      if (_bufferPos==buffer.length)
      { buffer=null;
      }
      return ret;
    }
    else
    {
      // No remaining buffers, so we're done.
      done=true;
      return -1;
    }

  }

  @Override
  public boolean markSupported()
  { return false;
  }

  @Override
  public void close()
    throws IOException
  { in.close();
  }



}
