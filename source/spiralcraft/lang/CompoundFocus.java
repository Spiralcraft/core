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

import java.net.URI;
import java.util.HashMap;


/**
 * <p>A grouping of Focus objects that cross-cuts application layers.
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
public class CompoundFocus<T>
  extends SimpleFocus<T>
{

  private HashMap<String,Focus<?>> layers;

  
  public CompoundFocus
    (Focus<?> parentFocus
    ,Channel<T> subject
    )
  { 
    setParentFocus(parentFocus);
    setSubject(subject);
  }
  
  public CompoundFocus()
  {
  }
  
  /**
   * Bind a Focus to a name that will referenced via findFocus(), or by the
   *   <CODE>[<I>name</I>]</CODE> or <CODE>[<I>namespace:name</I>]</CODE>
   *   operator in the expression language.
   */
  public synchronized void bindFocus(String layerName,Focus<?> focus)
    throws BindException
  { 
    
    if (layers==null)
    { layers=new HashMap<String,Focus<?>>();
    }
    if (layers.get(layerName)==null)
    { layers.put(layerName,focus);
    }
    else
    { throw new BindException("Layer Name '"+layerName+"' already bound");
    }
  }

  public Focus<?> findFocus(URI uri)
  { 
    if (isFocus(uri))
    {
      String query=uri.getQuery();
      String fragment=uri.getFragment();

      if (query==null)
      {
        if (fragment==null || fragment.equals(getLayerName()))
        { return this;
        }
        
        Focus<?> altLayer=layers.get(fragment);
        if (altLayer!=null)
        { return altLayer;
        }
      }
      else
      { // XXX Figure out how to deal with query
      }

    }
    
    if (layers!=null)
    {
    
      for (Focus<?> focus:layers.values())
      {
        if (focus.isFocus(uri))
        { 
          String query=uri.getQuery();
          String fragment=uri.getFragment();
          if (query==null)
          {
            if (fragment==null)
            {
              return focus;
            }
            else
            {
              // TODO: Investigate utility of allowing a deeper layer to
              //   access a shallower layer. We might need to delegate fragment
              //   resolution to the "sub" Focus.

              Focus<?> altLayer=layers.get(fragment);
              if (altLayer!=null)
              { return altLayer;
              }
            }

          }
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
  

}
