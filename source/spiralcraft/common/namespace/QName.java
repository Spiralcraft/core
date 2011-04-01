package spiralcraft.common.namespace;

import java.net.URI;

import spiralcraft.common.Immutable;
import spiralcraft.util.string.StringUtil;

/**
 * <p>A qualified name, which consists of a namespace URI and a local 
 *   name.
 * </p>
 * 
 * @author mike
 *
 */
@Immutable
public class QName
{
  private final URI namespaceURI;
  private final String localName;

  
  /**
   * Resolve a name in the form namespacePrefix+":"+localName using the
   *   contextual PrefixResolver.
   * 
   * @param prefixedName
   * @return a QName
   */
  public QName resolve(String prefixedName)
    throws UnresolvedPrefixException
  { return resolve(prefixedName,NamespaceContext.getPrefixResolver());
  }
  
  /**
   * Resolve a name in the form namespacePrefix+":"+localName using the
   *   specified PrefixResolver.
   * 
   * @param prefixedName
   * @return a QName
   */
  public static QName resolve(String prefixedName,PrefixResolver resolver)
    throws UnresolvedPrefixException
  { 
    String[] pair=StringUtil.explode(prefixedName,':',(char) 0,2);
    switch (pair.length)
    {
      case 1:
        return new QName(null,pair[0]);
      case 2:
        if (resolver==null)
        { throw new IllegalArgumentException
            ("No resolver to resolver "+prefixedName);
        }
        URI uri=resolver.resolvePrefix(pair[0]);
        if (uri==null)
        { throw new UnresolvedPrefixException(pair[0],pair[1],resolver);
        }
        return new QName(uri,pair[1]);
      default:
        throw new IllegalArgumentException("Too many colons in prefixedName");
    }
  }
  
  /**
   * Construct a QName from the canonical form using the "{uri}name" syntax 
   *   defined in http://jclark.com/xml/xmlns.htm
   *    
   * @param canonicalForm
   */
  public QName(String canonicalForm)
  {
    if (canonicalForm.startsWith("{"))
    { 
      int endBracket=canonicalForm.indexOf('}');
      namespaceURI=URI.create(canonicalForm.substring(1,endBracket));
      localName=canonicalForm.substring(endBracket+1);
    }
    else
    {
      namespaceURI=null;
      localName=canonicalForm;
    }
     
  }
  
  public QName(URI namespaceURI,String localName)
  { 
    this.namespaceURI=namespaceURI;
    this.localName=localName;
  }
  
  public URI getNamespaceURI()
  { return namespaceURI;
  }
  
  public String getLocalName()
  { return localName;
  }
  
  /**
   * Generate the canonical form using the "{uri}name" syntax
   *   defined in http://jclark.com/xml/xmlns.htm
   */
  @Override
  public String toString()
  { 
    if (namespaceURI!=null)
    { return "{"+namespaceURI+"}"+localName;
    }
    else
    { return localName;
    }
  }
}
