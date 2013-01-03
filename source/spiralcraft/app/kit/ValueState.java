//
//Copyright (c) 2009,2009 Michael Toth
//Spiralcraft Inc., All Rights Reserved
//
//This package is part of the Spiralcraft project and is licensed under
//a multiple-license framework.
//
//You may not use this file except in compliance with the terms found in the
//SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
//at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
//Unless otherwise agreed to in writing, this software is distributed on an
//"AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.app.kit;

/**
 * <p>A State that holds an arbitrary value, and indicates whether
 *   the value is valid or not.
 * </p>
 * 
 * @author mike
 *
 * @param <Tvalue>
 */
public class ValueState<Tvalue>
  extends SimpleState
{

  private volatile Tvalue value;
  private boolean valid;
  
  public ValueState(int childCount,String id)
  { super(childCount,id);
  }
  
  public Tvalue getValue()
  { return value;
  }
  
  /**
   * <p>Set the value, which sets valid to true
   * </p>
   * 
   * @param value
   */
  public void setValue(Tvalue value)
  { 
    Tvalue oldValue=this.value;
    this.value=value;
    this.valid=true;
    if (this.value!=oldValue)
    { onValueChanged(value,oldValue);
    }
  }
  
  protected void onValueChanged(Tvalue value,Tvalue oldValue)
  {
  }
  
  /**
   * <p>Invalidate the state and set the value to null
   * </p>
   * 
   * @param valid
   */
  public void invalidate()
  { 
    this.valid=false;
    value=null;
  }
  
  /**
   * <p>Indicate whether or not the value is valid, or should be recomputed
   * </p>
   */
  public boolean isValid()
  { return valid;
  }


}
