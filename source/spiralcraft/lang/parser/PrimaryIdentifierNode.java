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

import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;

/**
 * <P>A Node which represents a primary identifier.
 * 
 * <P>A primary identifier is resolved
 *   against either the "default" Focus, if the identifier is "standalone", or 
 *   by a Focus specified using the <code>[ Focus-name ] identifier</code> syntax,
 *   which is reachable throug the "default" Focus.
 *   
 * <P>A primary identifier will try to bind to an attribute in the Context of the
 *   applicable Focus. If no binding is found, the identifier will try to bind to
 *   the subject of the Focus.
 *   
 * <P>To force binding to the subject of the Focus, use the <code>.identifier</code>
 *   form. The dot prefix always refers to the subject of the Focus.
 */
public class PrimaryIdentifierNode
  extends Node
{

  private final FocusNode _source;
  private final String _identifier;

  public PrimaryIdentifierNode(FocusNode source,String identifier)
  { 
    _source=source;
    _identifier=identifier;
  }

  public String reconstruct()
  { return (_source!=null?_source.reconstruct():"")+_identifier;
  }
  
  public FocusNode getSource()
  { return _source;
  }
  
  public String getIdentifier()
  { return _identifier;
  }
  
  public Channel<?> bind(final Focus<?> focus)
    throws BindException
  { 
//    String identifier=_identifier.getIdentifier();

    Focus<?> specifiedFocus
      =_source!=null
      ?_source.findFocus(focus)
      :focus;

    Channel<?> context=specifiedFocus.getContext();
    
    Channel<?> ret=null;
    
    if (context!=null)
    { ret=context.resolve(specifiedFocus,_identifier,null);
    }

    if (ret==null)
    { 
      try
      { 
        Channel<?> subject=specifiedFocus.getSubject();
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
          + specifiedFocus.toString()
          ,x
          );
      }
    }
    
    if (ret==null)
    {
      throw new BindException
        ("Could not resolve identifier '"
        +_identifier
        +"' in Context or Subject of Focus"
        + specifiedFocus.toString()
        );
    }
    return ret;  
  }

  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("PrimaryIdentifier '"+_identifier+"'");
    prefix=prefix+"  ";
    if (_source!=null)
    { _source.dumpTree(out,prefix);
    }

  }
  
}
