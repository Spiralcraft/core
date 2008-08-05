//
// Copyright (c) 1998,2008 Michael Toth
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
package spiralcraft.data.sax;

import spiralcraft.data.DataException;
import spiralcraft.lang.Setter;

/**
 * <p>Maps an XML element that serves as a container for another element
 *   but does not map directly to data.
 * </p>
 * 
 * <p>
 * The AttributeBinding expressions must be bound to an Expression scoped
 *   to a parent FrameHandler, since this FrameHandler does not expose data
 * </p>
 * 
 * @author mike
 *
 */
public class ContainerFrameHandler
  extends FrameHandler
{  
  
  
  
  @Override
  protected void openData()
    throws DataException
  { 
  }
  
  @Override
  protected void closeData()
    throws DataException
  {     

    Setter.applyArrayIfNull(defaultSetters);
    
  }
  
  
  
}
