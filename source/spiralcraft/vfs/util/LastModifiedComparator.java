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
package spiralcraft.vfs.util;

import java.io.IOException;
import java.util.Comparator;

import spiralcraft.vfs.Resource;

/**
 * Compares Resourced by their last modified date
 * 
 * @author mike
 *
 */
public class LastModifiedComparator
  implements Comparator<Resource>
{

  private int reverse=1;
  
  public LastModifiedComparator(boolean reverse)
  { this.reverse=(reverse?-1:1);
  }
  
  @Override
  public int compare(Resource o1,Resource o2)
  { 
    try
    {
      long lm1=o1.getLastModified();
      long lm2=o2.getLastModified();
    
      if (lm1==lm2)
      { return 0;
      }
      else
      { return reverse*(lm1<lm2?-1:1);
      }
    }
    catch (IOException x)
    { throw new RuntimeException("Error comparing resource lastModified time");
    }
  }


}
