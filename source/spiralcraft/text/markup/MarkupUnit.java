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


/**
 * A Unit which represents markup. May contain other Units if the markup
 *   language supports containership. This is a base class from which 
 *   language specific MarkupUnits are derived.
 */
public class MarkupUnit
  extends Unit
{
  
  // private final CharSequence _code;
  private boolean _open=true;
  
  public MarkupUnit(CharSequence code)
    throws MarkupException
  { // _code=code;
  }
  
  
  public boolean isOpen()
  { return _open;
  }

  public void close()
    throws MarkupException
  { _open=false;
  }


  public String toString()
  { return super.toString()+"[name="+getName()+"]";
  }

}
