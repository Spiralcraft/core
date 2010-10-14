//
// Copyright (c) 2010 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license fmemework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.test;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.util.LangUtil;

/**
 * Supplies load shaping pamemeters to load and stress tests
 * 
 * @author mike
 *
 */
public class LoadGroup
  extends TestGroup
{

  private int cycleFactor=1;
  private int rateFactor=1;
  private int durationFactor=1;
  private int forkFactor=1;
  private int memFactor=1;
  private int storeFactor=1;
  private LoadGroup loadGroup;
  
  /**
   * @return the cycleFactor
   */
  public int getCycleFactor()
  { return cycleFactor;
  }
  
  /**
   * @pamem cycleFactor the cycleFactor to set
   */
  public void setCycleFactor(int cycleFactor)
  { this.cycleFactor = cycleFactor;
  }
  
  /**
   * @return the rateFactor
   */
  public int getRateFactor()
  { return rateFactor;
  }
  
  /**
   * @pamem rateFactor the rateFactor to set
   */
  public void setRateFactor(int rateFactor)
  { this.rateFactor = rateFactor;
  }
  
  /**
   * @return the durationFactor
   */
  public int getDurationFactor()
  { return durationFactor;
  }
  
  /**
   * @pamem durationFactor the durationFactor to set
   */
  public void setDurationFactor(int durationFactor)
  { this.durationFactor = durationFactor;
  }
  
  /**
   * @return the forkFactor
   */
  public int getForkFactor()
  { return forkFactor;
  }
  
  /**
   * @pamem forkFactor the forkFactor to set
   */
  public void setForkFactor(int forkFactor)
  { this.forkFactor = forkFactor;
  }
  
  /**
   * @return the memFactor
   */
  public int getMemFactor()
  { return memFactor;
  }
  
  /*** 
   * @pamem memFactor the memFactor to set
   */
  public void setMemFactor(int memFactor)
  { this.memFactor = memFactor;
  }
  
  /**
   * @return the storeFactor
   */
  public int getStoreFactor()
  { return storeFactor;
  }
  /**
   * @pamem storeFactor the storeFactor to set
   */
  public void setStoreFactor(int storeFactor)
  { this.storeFactor = storeFactor;
  }
  
  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws BindException
  {
    loadGroup=LangUtil.findInstance(LoadGroup.class,focusChain);
    if (loadGroup!=null)
    { 
      cycleFactor=calc(cycleFactor,loadGroup.getCycleFactor());
      rateFactor=calc(rateFactor,loadGroup.getRateFactor());
      durationFactor=calc(durationFactor,loadGroup.getDurationFactor());
      forkFactor=calc(forkFactor,loadGroup.getForkFactor());
      memFactor=calc(memFactor,loadGroup.getMemFactor());
      storeFactor=calc(storeFactor,loadGroup.getStoreFactor());
    }
    return super.bind(focusChain);
  }
  
  private int calc(int local,int parent)
  { return local*parent;
  }
}
