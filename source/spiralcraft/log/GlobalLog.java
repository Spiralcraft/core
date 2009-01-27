//
// Copyright (c) 2009 Michael Toth
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
// "AS 
package spiralcraft.log;

/**
 * References the root logger
 * 
 * @author mike
 */
public class GlobalLog
  extends GenericLog
{

  private static GlobalLog _INSTANCE
    =new GlobalLog();
  
  public static GlobalLog instance()
  { return _INSTANCE;
  }
  
}
