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
import spiralcraft.lang.spi.Translator;
import spiralcraft.lang.spi.TranslatorChannel;
import spiralcraft.lang.util.LangUtil;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;

import java.net.URI;

/**
 * A ChannelFactory for  VFS Resource resolution 
 * 
 * @author mike
 *
 */
public class Resolve
  implements ChannelFactory<Resource, URI>
{

  private final Reflector<Resource> reflector
    =BeanReflector.<Resource>getInstance(Resource.class);
  
  @Override
  public Channel<Resource> bindChannel(
    Channel<URI> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  {
    
    return new TranslatorChannel<Resource,URI>
      (LangUtil.<URI>ensureType(source,URI.class,focus)
      ,new Translator<Resource,URI>()
        {

          @Override
          public Reflector<Resource> getReflector()
          { return reflector;
          }

          @Override
          public boolean isFunction()
          { return false;
          }

          @Override
          public Resource translateForGet(
            URI source,
            Channel<?>[] modifiers)
          { 
            try
            { return Resolver.getInstance().resolve(source);
            }
            catch (UnresolvableURIException x)
            { throw new AccessException(x);
            }
          }

          @Override
          public URI translateForSet(
            Resource source,
            Channel<?>[] modifiers)
          { return source.getURI();
          }
        }
      ,new Channel<?>[0]
      );
  }

}
