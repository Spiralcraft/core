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

/**
 * A generic exception parsing text
 */
public class ParseException
  extends Exception
{
  private static final long serialVersionUID = 1L;

  private int _offset;
  
  public ParseException(String message, int offset)
  { 
    super(message+"(@"+Integer.toString(offset)+")");
    _offset=offset;
  }
  
  public ParseException(String message, int offset, Exception cause)
  {
    super(message+"(@"+Integer.toString(offset)+")",cause);
    _offset=offset;
  }

  public ParseException(int offset, Exception cause)
  {
    super(cause);
    _offset=offset;
  }
  
  public int getOffset()
  { return _offset;
  }
}
