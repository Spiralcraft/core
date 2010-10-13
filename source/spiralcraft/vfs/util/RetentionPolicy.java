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
import java.util.Arrays;

import spiralcraft.time.Calendar;
import spiralcraft.time.Duration;
import spiralcraft.time.Instant;
import spiralcraft.util.ArrayUtil;
import spiralcraft.vfs.Resource;

/**
 * <p>For a set of Resources, determines which Resources should be retained
 *   and which should be removed based on maximum and minimum dates,counts
 *   and/or sizes.
 * </p>
 * 
 * <p>No resource that falls within minCount, minAge or minSize will be removed.
 * </p>
 * 
 * <p>All resources outside of any of the maximums will be removed.
 * </p>
 * 
 * <p>When a resource falls both within a minimum, and outside a maximum, the
 *   minimum will take precedence and will not be removed.
 * </p>
 * 
 * <p>Resources that fall outside the minimums and within all maximums will
 *   be retained.
 * </p>
 * 
 * @author mike
 *
 */
public class RetentionPolicy
{
  private Duration maxAge;
  private Duration minAge;
  private long maxSize;
  private long minSize;
  private int maxCount;
  private int minCount;
  
  /**
   * The maximum combined size of the retained resource set. 
   * 
   * @param maxSize
   */
  public void setMaxSize(long maxSize)
  { this.maxSize=maxSize;
  }
  
  /**
   * The maximum number of resources to retain. 
   * 
   * @param maxSize
   */
  public void setMaxCount(int maxCount)
  { this.maxCount=maxCount;
  }

  /**
   * The minimum number of resources to retain.
   * 
   * @param maxSize
   */
  public void setMinCount(int minCount)
  { this.minCount=minCount;
  }

  
  /**
   * The maximum age of the retained resource set. This condition 
   * 
   * @param maxSize
   */
  public void setMaxAge(Duration maxAge)
  { this.maxAge=maxAge;
  }

  /**
   * The minimum age of the retained resource set. No resource modified later
   *   than this will be removed.
   * 
   * @param maxSize
   */
  public void setMinAge(Duration minAge)
  { this.minAge=minAge;
  }

  /**
   * The Resources that should be retained
   * 
   * @param input
   * @return
   * @throws IOException
   */
  public Resource[] getInclusion(Resource[] input)
    throws IOException
  { 
    Arrays.sort(input,new LastModifiedComparator(true));
    return ArrayUtil.head(input,split(input));
  }
  
  /**
   * The Resources that should not be retained
   * 
   * @param input
   * @return
   * @throws IOException
   */
  public Resource[] getExclusion(Resource[] input)
    throws IOException
  { 
    Arrays.sort(input,new LastModifiedComparator(true));
    return ArrayUtil.tail(input,split(input));
  }
  
  /**
   * Compute the discard list from the specified time-descending-ordered input.
   * 
   * @param input
   * @return
   */
  public int split(Resource[] input)
    throws IOException
  {
    
    Arrays.sort(input,new LastModifiedComparator(true));
    
    Instant now=new Instant();
    long cumulativeSize=0;
    int count=0;
    long minAgeTime=0;
    if (minAge!=null)
    { minAgeTime=Calendar.DEFAULT.subtract(now,minAge).getOffsetMillis();
    }
    long maxAgeTime=0;
    if (maxAge!=null)
    { maxAgeTime=Calendar.DEFAULT.subtract(now,maxAge).getOffsetMillis();
    }
    
    
    for (Resource resource: input)
    { 
      count++;
      cumulativeSize=cumulativeSize+resource.getSize();
      
      if (count<=minCount)
      { continue;
      }
      if (minAge!=null && resource.getLastModified()>=minAgeTime)
      { continue;
      }
      if (cumulativeSize<=minSize)
      { continue;
      }
      
      if ( (maxCount>0 && count>maxCount)
          || (maxSize>0 && cumulativeSize>maxSize)
          || (maxAge!=null && resource.getLastModified()<maxAgeTime)
         )
      { return count-1;
      }
    }
    return count;
  }
  
}
