//
// Copyright (c) 2008,2009 Michael Toth
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
package spiralcraft.task;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Focus;



/**
 * <p>A Scenario which conditionally performs a scenario chain
 * </p>
 * 
 * @author mike

 */
public class If
  extends Branch<Void,Void>
{
  
  private Binding<Boolean> x;
  private Scenario<?,?> thenBranch;
  private Scenario<?,?> elseBranch;
  
  public If()
  {
  }
  
  public If(Binding<Boolean> x)
  { this.x=x;
  }

  public If(Binding<Boolean> x,Scenario<?,?> chain)
  { 
    this.x=x;
    setThen(chain);
  }
      
  /**
   * Provide an expression to resolve the Command object
   */
  public void setX(Binding<Boolean> x)
  { this.x=x;
  }
  
  public void setSequence(Scenario<?,?> ... sequence)
  { setThen(sequence);
  }
  
  public void setThen(Scenario<?,?> ... thenBranch)
  { 
    this.thenBranch=Scenario.sequential(thenBranch);
    children.add(this.thenBranch);
  }
  
  public void setElse(Scenario<?,?> ... elseBranch)
  { 
    this.elseBranch=Scenario.sequential(elseBranch);
    children.add(this.elseBranch);
  }
  
  
  @Override
  protected Focus<?> bindExports(Focus<?> focusChain)
    throws BindException
  { 
    x.bind(focusChain);
    return focusChain;
  }
  
  @Override
  protected Scenario<?,?> select()
  {
    if (Boolean.TRUE.equals(x.get()))
    { 
      if (debug)
      { log.fine("Expression returned TRUE: "+x.getText());
      }
      return thenBranch;
    }
    else
    {
      if (debug)
      { log.fine("Expression did not return TRUE: "+x.getText());
      }
      
      return elseBranch;
    }
  }

}