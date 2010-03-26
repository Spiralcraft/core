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
package spiralcraft.app.spi;


import spiralcraft.app.Component;
import spiralcraft.app.Container;
import spiralcraft.common.LifecycleException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;

public class AbstractContainer
  implements Container
{

  protected Component[] children;
  
  
  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  { 
    bindChildren(focusChain);
    return focusChain;
  }
  
  
  protected final void bindChildren(Focus<?> focusChain)
    throws BindException
  { 
    for (Component child:children)
    { 
      child.bind(focusChain);
      child.getParent().registerChild(child);
    }
  }
  
 

  @Override
  public Component getChild(
    int childNum)
  { return children[childNum];
  }


  @Override
  public int getChildCount()
  { return children.length;
  }


  @Override
  public Component[] getChildren()
  { return children;
  }
  



  @Override
  public void start()
    throws LifecycleException
  {
    for (Component child:children)
    { 
      child.start();
      
    }
    // TODO Auto-generated method stub
    
  }


  @Override
  public void stop()
    throws LifecycleException
  {
    for (Component child:children)
    { 
      child.stop();
      
    }
    
  }



}
