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
package spiralcraft.text;

import spiralcraft.common.ContextualException;

/**
 * A generic exception parsing text
 */
public class ParseException
  extends ContextualException
{
  private static final long serialVersionUID = 1L;

  private ParsePosition position;
  
  public ParseException(String message, ParsePosition position)
  { 
    super(message,position);
    this.position=position;
  }
  
  public ParseException(String message, ParsePosition position, Throwable cause)
  {
    super(message,position,cause);
    this.position=position;
  }

  public ParseException(ParsePosition position, Throwable cause)
  {
    super("", position,cause);
    this.position=position;
  }
  
  public ParsePosition getPosition()
  { return position;
  }
}
