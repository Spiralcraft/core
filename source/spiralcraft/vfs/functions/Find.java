//
// Copyright (c) 2011 Michael Toth
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

import java.util.List;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.ChannelFactory;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.SourcedChannel;
import spiralcraft.util.PathPattern;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.ResourceFilter;
import spiralcraft.vfs.batch.Search;
import spiralcraft.vfs.filters.ContainerFilter;
import spiralcraft.vfs.filters.PatternFilter;

public class Find
  implements ChannelFactory<Resource[],Resource>
{

  private Expression<String[]> excludesX;
  private Expression<String> patternX;
  private boolean excludeContainers=true;
  
  @Override
  public Channel<Resource[]> bindChannel(
    Channel<Resource> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  { return new FindChannel(source,focus);
  }
  
  public void setExcludesX(Expression<String[]> excludesX)
  { this.excludesX=excludesX;
  }
  
  public void setPatternX(Expression<String> patternX)
  { this.patternX=patternX;
  }

  class FindChannel
    extends SourcedChannel<Resource,Resource[]>
  {
    private Channel<String[]> excludesX;
    private Channel<String> patternX;
    
    public FindChannel(Channel<Resource> source,Focus<?> focus)
      throws BindException
    { 
      super(BeanReflector.<Resource[]>getInstance(Resource[].class),source);
      if (Find.this.excludesX!=null)
      { this.excludesX=focus.bind(Find.this.excludesX);
      }
      if (Find.this.patternX!=null)
      { this.patternX=focus.bind(Find.this.patternX);
      }
    }

    @Override
    protected Resource[] retrieve()
    {
      
      Resource sourceResource=source.get();
      if (sourceResource==null)
      { return null;
      }
      
      String[] patterns=null;
      if (excludesX!=null)
      { patterns=excludesX.get();
      }

      String pattern=null;
      if (patternX!=null)
      { pattern=patternX.get();
      }
      
      ResourceFilter excludesFilter=null;
      if (patterns!=null)
      { excludesFilter=PatternFilter.any(patterns);
      } 
    
      Search search=new Search();
      if (excludesFilter!=null)
      { search.setExclusionFilter(excludesFilter);
      }
      search.setRootResource(sourceResource);
      if (pattern!=null)
      { search.setPattern(new PathPattern(pattern));
      }
      List<Resource> result
        =excludeContainers
        ?search.list(ContainerFilter.not())
        :search.list();
      return result.toArray(new Resource[result.size()]);
    }

    @Override
    protected boolean store(
      Resource[] val)
      throws AccessException
    { return false;
    }
  }
}
