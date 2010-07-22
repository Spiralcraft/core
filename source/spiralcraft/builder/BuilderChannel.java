//
// Copyright (c) 2010 Michael Toth
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
package spiralcraft.builder;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.SourcedChannel;

/**
 * <p>Uses an Assembly to manufacture objects or customize objects retrieved 
     from a source Channel.
 * </p>
 *   
 * 
 * @author mike
 *
 * @param <T>
 */
public class BuilderChannel<T>
  extends SourcedChannel<T,T>
{

  private final Assembly<T> assembly;
  
  @SuppressWarnings("unchecked")
  public BuilderChannel
    (Focus<?> context
    ,Channel<T> source
    ,AssemblyClass assemblyClass
    )
    throws BindException
  { 
    super
      (source!=null
      ?source.getReflector()
      :BeanReflector.<T>getInstance(assemblyClass.getJavaClass())
      ,source
      );
    
    try
    { this.assembly=(Assembly<T>) assemblyClass.newFactoryInstance(context);
    }
    catch (BuildException x)
    { throw new BindException("Error instantiating assembly",x);
    }
    if (source!=null)
    { this.assembly.setInstanceSourceChannel(source);
    }
  }
  
  @Override
  protected T retrieve()
  { 
    try
    { assembly.resolve(null);
    }
    catch (BuildException x)
    { throw new AccessException("Error resolving instance",x);
    }
    
    try
    { return assembly.get();
    }
    finally
    { assembly.release();
    }
    
  }

  @Override
  protected boolean store(
    T val)
    throws AccessException
  { 
    if (source!=null)
    { return source.set(val);
    }
    else
    { return false;
    }
  }
  
  @Override
  public boolean isWritable()
  { 
    if (source!=null)
    { return source.isWritable();
    }
    else
    { return false;
    }
  }

}
