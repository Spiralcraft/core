package spiralcraft.lang;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * A Telescoped Focus, referencing a context internal to a single expression
 * 
 * @author mike
 *
 */
public class TeleFocus<T>
  extends BaseFocus<T>
  implements Focus<T>
{
  
  public TeleFocus(Focus<?> parentFocus,Channel<T> subject)
  { 
    setParentFocus(parentFocus);
    setSubject(subject);
   
  }
  
  public Channel<?> getContext()
  { 
    if (parent!=null)
    { return parent.getContext();
    }
    return null;
  }
  
  public boolean isFocus(URI uri)
  {     
    try
    {
      URI shortURI
        =new URI(uri.getScheme(),uri.getAuthority(),uri.getPath(),null,null);
      if  (subject.getReflector().isAssignableTo(shortURI))
      { return true;
      }
    }
    catch (URISyntaxException x)
    { x.printStackTrace();
    }
    return false;
  }

  public Focus<?> findFocus(URI uri)
  {       
    if (isFocus(uri))
    {
      String query=uri.getQuery();
      String fragment=uri.getFragment();
      
      if (query==null && fragment==null)
      { return this;
      }
    }
    
    if (parent!=null)
    { return parent.findFocus(uri);
    }
    else
    { return null;
    }
  }
  


}
