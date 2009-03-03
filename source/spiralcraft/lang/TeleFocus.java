package spiralcraft.lang;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * <p>A Telescoped Focus, referencing a context internal to a single expression
 * </p>
 * 
 * <p>The parent focus context is used to resolve all context names 
 *   (non-dot-prefixed names), and subject references (dot-prefixed names)
 *   will resolve to the provided subject
 * </p>
 * 
 * @author mike
 *
 */
public class TeleFocus<T>
  extends BaseFocus<T>
  implements Focus<T>
{
  
 /**
  * <p>The parent focus context is used to resolve all context names 
  *   (non-dot-prefixed names), and subject references (dot-prefixed names)
  *   will resolve to the provided subject
  * </p>
  * 
  * @param parentFocus The parentFocus which provides the context
  * @param subject The channel which provides the subject.
  */
  public TeleFocus(Focus<?> parentFocus,Channel<T> subject)
  { 
    setParentFocus(parentFocus);
    setSubject(subject);
  }
  
  @Override
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
      if  (subject!=null && subject.getReflector().isAssignableTo(shortURI))
      { return true;
      }
    }
    catch (URISyntaxException x)
    { x.printStackTrace();
    }
    return false;
  }

  @SuppressWarnings("unchecked") // Cast for requested interface
  public <X> Focus<X> findFocus(URI uri)
  {       
    if (isFocus(uri))
    {
      String query=uri.getQuery();
      String fragment=uri.getFragment();
      
      if (query==null && fragment==null)
      { return (Focus<X>) this;
      }
    }
    
    if (parent!=null)
    { return parent.<X>findFocus(uri);
    }
    else
    { return null;
    }
  }
  
  @Override
  public String toString()
  {
    return super.toString()
      +(getContext()!=null
        ?"\r\n context:"+getContext().toString()
        :"null context"
       );
  }


}
