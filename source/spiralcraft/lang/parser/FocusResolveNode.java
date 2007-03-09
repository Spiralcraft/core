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
import spiralcraft.lang.BindException;
import spiralcraft.lang.Context;


public class FocusResolveNode
  extends Node
{

  private final FocusNode _source;
  private final String _identifier;

  public FocusResolveNode(FocusNode source,String identifier)
  { 
    _source=source;
    _identifier=identifier;
  }

  
  public Optic bind(final Focus focus)
    throws BindException
  { 
//    String identifier=_identifier.getIdentifier();

    Focus specifiedFocus
      =_source!=null?_source.findFocus(focus):focus;

    Context context=specifiedFocus.getContext();
    
    Optic<?> ret=null;
    if (context!=null)
    { ret=context.resolve(_identifier);
    }

    if (ret==null)
    { 
      try
      { 
        Optic<?> subject=specifiedFocus.getSubject();
        if (subject!=null)
        { ret=subject.resolve(specifiedFocus,_identifier,null);
        }
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
    
    if (ret==null)
    {
      throw new BindException
        ("Could not resolve identifier '"
        +_identifier
        +"' in Context or Subject of Focus"
        );
    }
    return ret;  
  }

  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("FocusResolve '"+_identifier+"'");
    prefix=prefix+"  ";
    if (_source!=null)
    { _source.dumpTree(out,prefix);
    }

  }
  
}
