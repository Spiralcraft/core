package spiralcraft.prefs;

import java.util.prefs.BackingStoreException;

import java.net.URI;

public class BackingStoreFormatException
  extends BackingStoreException
{
  private final URI _uri;
  
  public BackingStoreFormatException(String msg)
  { 
    super(msg);
    _uri=null;
  }

  public BackingStoreFormatException(String msg,URI uri)
  { 
    super(msg);
    _uri=uri;
  }

  public URI getURI()
  { return _uri;
  }
}
