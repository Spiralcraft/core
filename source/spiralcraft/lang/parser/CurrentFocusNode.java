//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.lang.parser;

import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;

/**
 * Refers to the current implicit Focus.
 */
public class CurrentFocusNode
  extends FocusNode
{
  
  private String invoker;
  
  public CurrentFocusNode()
  { 
    this.hashCode=31;
  }
  
  public CurrentFocusNode(String invoker)
  { 
    this.invoker=invoker;
    this.hashCode=31;
  }
  
  @Override
  public Node[] getSources()
  { return null;
  }
  
  @Override
  public Node copy(Object visitor)
  { return this;
  }

  /**
   * Simply returns the specified Focus.
   */
  @Override
  public Focus<?> findFocus(final Focus<?> focus)
    throws BindException
  { return focus;
  }

  @Override
  public void dumpTree(StringBuffer out,String prefix)
  { out.append(prefix).append("CurrentFocus");
  }
  
  @Override
  public String reconstruct()
  { return invoker!=null?invoker:"";
  }
  
  @Override
  protected boolean equalsNode(Node other)
  { return true;
  }
}
