//
// Copyright (c) 1998,2009 Michael Toth
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
package spiralcraft.vfs.filters;


import spiralcraft.util.Path;
import spiralcraft.util.PathPattern;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.ResourceFilter;

/**
 * A ResourceFilter based on a  PathPattern
 */
public class PatternFilter
  implements ResourceFilter
{
  private PathPattern _pattern;

  public PatternFilter(String expression)
  { _pattern=new PathPattern(expression);
  }
  
  public PatternFilter(Path prefix,String expression)
  { _pattern=new PathPattern(prefix,expression);
  }
  
  public PatternFilter(PathPattern pattern)
  { _pattern=pattern;
  }
  
  @Override
  public boolean accept(Resource resource)
  { return _pattern.matches(new Path(resource.getURI().getPath(),'/'));
  }

  public PathPattern getPattern()
  { return _pattern;
  }


}

