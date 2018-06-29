//
//Copyright (c) 2012 Michael Toth
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
package spiralcraft.app.components;

import spiralcraft.app.Message;

/**
 * Invokes specific functionality in the context of a component
 * 
 * @author mike
 */
public class FlowStateChangedMessage
  extends Message
{
  public static final Type TYPE=new Type();
  
  { 
    multicast=true;
    outOfBand=false;
  }
  
  public FlowStateChangedMessage()
  {
  }
  
  @Override
  public Type getType()
  { return TYPE;
  }

}
