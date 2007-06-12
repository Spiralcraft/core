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
package spiralcraft.text.markup;

import spiralcraft.text.ParsePosition;
import spiralcraft.text.ParseException;

public class MarkupException
  extends ParseException
{
  private static final long serialVersionUID = 1L;

  public MarkupException(String message,ParsePosition position)
  { super(message,position);
  }
  
  public MarkupException(String message,ParsePosition position,Throwable cause)
  { super(message,position,cause);
  }

  public MarkupException(ParsePosition position,Throwable cause)
  { super(position,cause);
  }
}
