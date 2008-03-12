//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.lang;

import java.util.HashMap;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Simple implementation of a Focus
 */
public class SimpleFocus<T>
  extends BaseFocus<T>
  implements Focus<T>
{

  private URI containerURI;
  private String layerName;
  private HashMap<String,URI> namespaces;
  private boolean namespaceRoot;
  
  public SimpleFocus()
  {
  }

  public SimpleFocus(Channel<T> subject)
  { this.subject=subject;
  }

  public SimpleFocus(Focus<?> parentFocus,Channel<T> subject)
  { 
    setParentFocus(parentFocus);
    this.subject=subject;
  }

    
  /**
   * Indicate that namespace resolution should end at this Focus node.
   * 
   * @param val
   */
  public void setNamespaceRoot(boolean val)
  { this.namespaceRoot=val;
  }
  
  public void setLayerName(String layerName)
  { this.layerName=layerName;
  }
  
  public void setContainerURI(URI containerURI)
  { this.containerURI=containerURI;
  }
  
//  public synchronized void mapNamespace(String name,URI namespace)
//  {
//    if (namespaces==null)
//    { namespaces=new HashMap<String,URI>();
//    }
//    namespaces.put(name,namespace);
//  }
  
  
  /**
   * A Focus accessible from expressions embedded in a container should have
   *   a URI that is discoverable to the expression writer using that 
   *   container. This is called the container URI.
   * 
   * @return The container URI of this Focus.
   */
  public URI getContainerURI()
  { return containerURI;
  }
  
  /**
   * Identifies the application layer represented by the subject of the Focus.
   * 
   * @return A String conforming to the syntax of Java package names, 
   *   representing the package which "implements" the layer. The layer name
   *   is provided by the container.
   */  
  public String getLayerName()
  { return layerName;
  }
  
  
  public boolean isFocus(URI uri)
  { 
    if (containerURI!=null 
        && containerURI.relativize(uri)!=uri
       )
    { return true;
    }
    
    if (subject==null)
    { return false;
    }
    
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

      if (query==null)
      {
        if (fragment==null || fragment.equals(layerName))
        { return this;
        }
      }

    }
    
    if (parent!=null)
    { return parent.findFocus(uri);
    }
    else
    { return null;
    }
  }


  @Override
  public NamespaceResolver getNamespaceResolver()
  { 
    if (namespaceResolver!=null)
    { return namespaceResolver;
    }
    else if (!namespaceRoot && parent!=null)
    { return parent.getNamespaceResolver();
    }
    return null;
  }
  
  public String toString()
  { 
    return super.toString()
      +(containerURI!=null?containerURI.toString():"")
      +"(#"+(layerName!=null?layerName:"")+")";
  }
}
