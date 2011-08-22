//
// Copyright (c) 2007,2011 Michael Toth
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

import spiralcraft.lang.kit.CoercionChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.util.lang.NumericCoercion;

/**
 * Copies the value of a source channel to a target
 * 
 * @author mike
 */
public class Setter<T>
{
  private static final ClassLog log=ClassLog.getInstance(Setter.class);
  
  public static final void applyArray(Setter<?>[] setters)
  {
    if (setters!=null)
    { 
      for (Setter<?> setter:setters)
      { setter.set();
      }
    }
  }

  public static final void applyArrayIfNull(Setter<?>[] setters)
  {
    if (setters!=null)
    { 
      for (Setter<?> setter: setters)
      { 
        if (setter.getTarget().get()==null)
        { setter.set();
        }
      }
    }
  }

  private Channel<? extends T> source;
  private Channel<T> target;
  private boolean debug;
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Setter(Channel<? extends T> source,Channel<T> target)
    throws BindException
  { 
    this.source=source;
    this.target=target;
    if (target!=null && !target.getReflector().isAssignableFrom(source.getReflector()))
    { 
      NumericCoercion coercion
        =NumericCoercion.instance(target.getContentType());
  
      if (coercion==null)
      {
      
        throw new BindException
          ("Cannot assign a "+source.getReflector().getTypeURI()
          +" to a location of type "+target.getReflector().getTypeURI()
          );
      }
      else
      { 
        this.source
          =new CoercionChannel
            (target.getReflector()
            ,source
            ,coercion
            );
      }

    }
  }
  
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  public boolean set()
  { 
    if (target!=null)
    { 
      if (debug)
      { log.fine("setting "+source.get()+" from "+source+" to "+target);
      }
      return target.set(source.get());
    }
    else
    { 
      source.get();
      return true;
    }
  }
  
  public Channel<? extends T> getSource()
  { return source;
  }
  
  public Channel<T> getTarget()
  { return target;
  }
  
  @Override
  public String toString()
  { return super.toString()+"\r\n    source="+source+"\r\n   target="+target;
  }
}
