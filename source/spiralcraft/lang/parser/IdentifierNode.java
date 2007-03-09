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

import spiralcraft.lang.Optic;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Context;
import spiralcraft.lang.BindException;

public class IdentifierNode
  extends Node
{

  private final String _identifier;

  public IdentifierNode(String identifier)
  { _identifier=identifier.intern();
  }

  public String getIdentifier()
  { return _identifier;
  }

  public void dumpTree(StringBuffer out,String prefix)
  { out.append(prefix).append("Identifier:").append(_identifier);
  }

  @SuppressWarnings("unchecked") // Heterogeneous operation
  public Optic bind(Focus focus)
    throws BindException
  { 
    Context context=focus.getContext();
    
    Optic<?> ret=null;
    if (context!=null)
    { ret=context.resolve(_identifier);
    }

    if (ret==null)
    { 
      try
      { ret=focus.getSubject().resolve(focus,_identifier,null);
      }
      catch (BindException x)
      { 
        throw new BindException
          ("Could not resolve identifier '"
          +_identifier
          +"' in Context or Subject of Focus"
          );
      }
    }
    return ret;
  }
}
