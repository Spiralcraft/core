package spiralcraft.io;

import java.io.InputStream;
import java.io.IOException;

//
// 2009-01-22 Used by:
//   spiralcraft.net.mime.MultipartParser
//

/**
 * <p>Exposes a fixed size lookahead window into another InputStream.
 * </p>
 *
 */
public class WindowInputStream
  extends InputStream
{
  
  private final InputStream _in;
  private final byte[] _window;
  private int _nextPos=0;
  private int _lastPos=0;
  private boolean _draining;
  private final boolean _drain;
  
  /**
   * <p>Create a new WindowInputStream.
   * </p>
   * 
   * <p>Allocates a buffer of windowSize
   * </p>
   *
   *@param in The InputStream to read from
   *@param windowSize The size in bytes of the window
   *@param drain Whether the window contents should be included in 
   *               the output at the end of the stream.
   */
  public WindowInputStream(InputStream in,int windowSize,boolean drain)
    throws IOException
  {
    _window=new byte[windowSize+1];
    _in=in;
    _drain=drain;
    _lastPos=_in.read(_window,0,windowSize);
    if (_lastPos<0)
    { _draining=true;
    }
    
  }

  @Override
  public int read()
    throws IOException
  { 
    if (_nextPos==_lastPos || (_draining && !_drain))
    { 
      // Buffer is empty, or we are not supposed to drain buffer
      return -1;
    }
    else
    {
      if (!_draining)
      {
        final int fill=_in.read();
        if (fill>-1)
        { 
          _window[_lastPos]=(byte) fill;
          _lastPos=(_lastPos+1)%_window.length;
        }
        else
        { _draining=true;
        }
      }

      if (_draining && !_drain)
      { return -1;
      }

      // Remove sign on byte
      final int ret=_window[_nextPos++] & 0xFF;
      
      _nextPos=_nextPos%_window.length;
      return ret;
    }
  }


}
