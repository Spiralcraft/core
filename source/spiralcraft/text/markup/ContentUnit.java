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

import java.io.Writer;
import java.io.IOException;


/**
 * A Unit which contains content only, and has no children.
 */
public class ContentUnit
  extends Unit
{
  private CharSequence _content;
  
  public ContentUnit(CharSequence content)
  { _content=content;
  }
  
  public CharSequence getContent()
  { return _content;
  }
  
  public String toString()
  { return super.toString()+"[content]";
  }

}
