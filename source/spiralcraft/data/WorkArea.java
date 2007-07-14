//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.data;

/**
 * <P>A part of a DataSession for viewing and editing one or more Tuples of a specific Type.
 * 
 * <P>A WorkArea consists of:
 * 
 * <UL>
 *   <LI>A View, which provides the data to view and edit</LI>
 * </UL>
 * 
 * <UL>
 *   <LI>A ScrollableCursor, which is used to navigate the View</LI>
 * </UL>
 * 
 */
public class WorkArea
{
  private Type<?> type;
  private boolean resolved=false;
  
  public Type<?> getType()
  { return type;
  }
  
  public void setType(Type<?> type)
  { 
    assertUnresolved();
    this.type=type;
  }
  
  private void assertUnresolved()
  { 
    if (resolved)
    { throw new IllegalStateException("WorkArea has already been activated");
    }
  }
}
