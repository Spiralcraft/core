//
// Copyright (c) 2009 Michael Toth
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
package spiralcraft.util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utility class to manipulate URIs
 * 
 * @author mike
 *
 */
public class URIUtil
{

  public static final URI addPathSuffix(URI source,String rawSuffix)
  { 
    return URI.create
      (trimToPath(source).toString()
        +rawSuffix
        +(source.getRawQuery()!=null
          ?"?"+source.getRawQuery()
          :""
          )
        +(source.getRawFragment()!=null
          ?"#"+source.getRawFragment()
          :""
         )
      );
  }
  
  /**
   * Replace the query part of the specified URI with the 
   *   specified pre-encoded query string
   * 
   * @return
   */
  public static final URI replaceRawQuery
    (URI source
    ,String rawQuery
    )
  {
    return URI.create
      (trimToPath(source).toString()
        +"?"
        +rawQuery
        +("#"+source.getRawFragment()!=null
          ?source.getRawFragment()
          :""
        )
      );
  }
  
  /**
   * Trim the query and fragment from the specified URI.  
   * 
   * @param input
   * @return
   */
  public static final URI trimToPath(URI input)
  { 
    try
    {
      if (input==null)
      { return null;
      }
      else if (input.getPath()!=null)
      {  
        if (input.getQuery()!=null || input.getFragment()!=null)
        {
          return 
            new URI
              (input.getScheme()
              ,input.getAuthority()
              ,input.getPath()
              ,null
              ,null
              );
        }
        else
        { return input;
        }
      }
      else
      {
        if (input.getFragment()!=null)
        {
          return
            new URI
              (input.getScheme()
              ,input.getSchemeSpecificPart()
              ,null
              );
        }
        else
        { return input;
        }
      }
    }
    catch (URISyntaxException x)
    { throw new IllegalArgumentException(x);
    }
      
  }
  
  
}
