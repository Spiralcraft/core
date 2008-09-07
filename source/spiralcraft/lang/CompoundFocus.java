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
   * <p>Publish a Focus into the Focus chain that will be made visible via
   *   this CompoundFocus. 
   * </p>
   * 
   * <p>The differentiator will allow the provided Focus to be referenced
   *   specifically if there are other Focii in the chain which provide
   *   the same type
   * </p>
   *  
   * <p>The Focus is normally referenced using the following construct
   *   <CODE>[<I>namespace</I>:<I>name</I>]</CODE> or 
   *   <CODE>[<I>namespace</I>:<I>name</I>#<I>differentiator</I>]</CODE>
   *   in the expression language.
   * </p>
   */
  public synchronized void bindFocus(String differentiator,Focus<?> focus)
    throws BindException
  { 
    
    if (layers==null)
    { layers=new HashMap<String,Focus<?>>();
    }
    if (layers.get(differentiator)==null)
    { layers.put(differentiator,focus);
    }
    else
    { throw new BindException
        ("Differentiator '"+differentiator+"' already bound");
    }
  }

  @SuppressWarnings("unchecked") // Cast for requested interface
  @Override
  public <X> Focus<X> findFocus(URI uri)
  { 
    if (isFocus(uri))
    {
      String query=uri.getQuery();
      String fragment=uri.getFragment();

      if (query==null)
      {
        if (fragment==null || fragment.equals(getLayerName()))
        { return (Focus<X>) this;
        }
        
        Focus<?> altLayer=layers.get(fragment);
        if (altLayer!=null)
        { return (Focus<X>) altLayer;
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
              return (Focus<X>) focus;
            }
            else
            {
              // TODO: Investigate utility of allowing a deeper layer to
              //   access a shallower layer. We might need to delegate fragment
              //   resolution to the "sub" Focus.

              Focus<?> altLayer=layers.get(fragment);
              if (altLayer!=null)
              { return (Focus<X>) altLayer;
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
  
  @Override
  public String toString()
  { 
    StringBuffer buf=new StringBuffer();
    if (layers!=null)
    {
      for (String name:layers.keySet())
      { buf.append("\r\n  #"+name+"="+layers.get(name));
      }
    }
    
    return super.toString()+"\r\n["+buf.toString()+"\r\n]";
  }

}
