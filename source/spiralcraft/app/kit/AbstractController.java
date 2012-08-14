//
// Copyright (c) 2012 Michael Toth
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
package spiralcraft.app.kit;

import spiralcraft.app.Component;
import spiralcraft.app.Message;
import spiralcraft.app.State;

/**
 * <p>A Component implementation geared towards managing the relationship
 *   between a set of model components and a set of UI components. 
 * </p> 
 * 
 * <p>This component can have stateful properties that are directly addressable
 *   by children.
 * </p>
 * 
 * 
 * @author mike
 *
 */
public abstract class AbstractController<Tstate extends State>
  extends AbstractComponent
{
  protected StateReferenceHandler<Tstate> stateReference;
  
  @Override
  public void setContents(Component[] contents)
  { super.setContents(contents);
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  protected void addHandlers()
  { 
    super.addHandlers();
    stateReference=new StateReferenceHandler(getStateClass());
    addHandler(stateReference);
  } 
  
  public void notify(Message message)
  { this.notify(stateReference.get(),message);
  }
  
  protected Tstate getState()
  { return stateReference.get();
  }
  
}
