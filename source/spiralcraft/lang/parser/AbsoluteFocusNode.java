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
package spiralcraft.lang.parser;

import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;

public class AbsoluteFocusNode
  extends FocusNode
{

  private final Node _selector;
  private final String _focusName;

  public AbsoluteFocusNode(String focusName,Node selector)
  { 
    _focusName=focusName;
    _selector=selector;
  }

  public Focus findFocus(final Focus focus)
    throws BindException
  { 
    if (_focusName==null || _focusName.equals(""))
    { return focus;
    }
    
    Focus newFocus=focus.findFocus(_focusName);
    if (newFocus!=null)
    { return newFocus;
    }
    else
    { throw new BindException("Focus '"+_focusName+"' not found.");
    }
  }

  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Focus");
    prefix=prefix+"  ";
    out.append(prefix).append("name="+(_focusName!=null?_focusName:"(default)"));
    if (_selector!=null)
    { _selector.dumpTree(out,prefix);
    }
  }
  
}
