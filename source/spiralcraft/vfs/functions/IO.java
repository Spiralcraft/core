//
// Copyright (c) 1998,2010 Michael Toth
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
package spiralcraft.vfs.functions;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.ChannelFactory;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.SourcedChannel;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.util.ByteArrayResource;

import java.io.IOException;

/**
 * A Channel factory which provides access the content of the input resource
 *   as a byte array.
 * 
 * @author mike
 *
 */
public class IO<T>
  implements ChannelFactory<T,Resource>
{

  private Reflector<T> reflector
    =BeanReflector.getInstance(byte[].class);
    
  
  @Override
  public Channel<T> bindChannel(
    Channel<Resource> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  {
    return new SourcedChannel<Resource,T>(reflector,source)
    { 
  
      
      @SuppressWarnings("unchecked")
      @Override
      public T retrieve()
      { 
        try
        { 
          Resource resource=source.get();
          if (resource!=null)
          {
            ByteArrayResource bytes=new ByteArrayResource();
            bytes.copyFrom(resource);
            return (T) bytes.getBackingStore();
          }
          else
          { return null;
          }
        }
        catch (IOException x)
        { throw new AccessException(x);
        }
      }

      @Override
      public boolean store(
        T data
        )
      { 
        Resource resource=source.get();
        try
        {
          if (resource!=null && data!=null)
          { 
            byte[] bytes=(byte[]) data;
            ByteArrayResource bytesResource=new ByteArrayResource(bytes);
            resource.copyFrom(bytesResource);
            return true;
          }
          return false;
        }
        catch (IOException x)
        { throw new AccessException(x);
        }
      }
      
    };
  }

}
