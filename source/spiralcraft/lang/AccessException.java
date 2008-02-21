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
package spiralcraft.lang;

/**
 * Thrown when an exception is thrown when reading or writing a value through 
 *   a Channel.
 */
public class AccessException
  extends RuntimeException
{
  private static final long serialVersionUID=1;
  
  public AccessException(String message)
  { super(message);
  }
  
  public AccessException(String message,Throwable cause)
  {
    super(message,cause);
  }
}
