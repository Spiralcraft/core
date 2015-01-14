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
 * <p>A Node which represents a primary identifier.
 * </p>
 * 
 * <p>A primary identifier is resolved
 *   against either the "default" Focus, if the identifier is "standalone", or 
 *   by a Focus specified using the <code>[ Focus-name ] identifier</code> syntax,
 *   which is reachable through the "default" Focus.
 * </p>
 *   
 * <p>A primary identifier will try to bind to an attribute in the Context of the
 *   applicable Focus. If no binding is found, the identifier will try to bind to
 *   the subject of the Focus.
 * </p>
 *   
 * <p>To force binding to the subject of the Focus, use the <code>.identifier</code>
 *   form. The dot prefix always refers to the subject of the Focus.
 * </p>
 */
public class ContextIdentifierNode
  extends Node
{

  private final FocusNode _source;
  private final String _identifier;

  public ContextIdentifierNode(FocusNode source,String identifier)
  { 
    _source=source;
    _identifier=identifier;
    this.hashCode=(source.hashCode() *31) + identifier.hashCode();
    
  }

  @Override
  public Node[] getSources()
  { return _source==null?null:new Node[] {_source};
  }
  
  @Override
  public Node copy(Object visitor)
  { 
    ContextIdentifierNode copy
      =new ContextIdentifierNode 
      (_source!=null?(FocusNode) _source.copy(visitor):null,_identifier);
    if (copy._source==_source)
    { return this;
    }
    else
    { return copy;
    }
  }
  
  @Override
  public String reconstruct()
  { return (_source!=null?_source.reconstruct():"")+_identifier;
  }
  
  public FocusNode getSource()
  { return _source;
  }
  
  public String getIdentifier()
  { return _identifier;
  }
  
  public boolean isRelative()
  { return _source instanceof CurrentFocusNode;
  }
  
  @Override
  protected boolean equalsNode(Node node)
  { 
    ContextIdentifierNode cin=(ContextIdentifierNode) node;
    return _source.equals(cin._source) && _identifier.equals(cin._identifier);
  }
  
  @Override
  public Channel<?> bind(final Focus<?> focus)
    throws BindException
  { 
//    String identifier=_identifier.getIdentifier();

    Focus<?> specifiedFocus
      =_source!=null
      ?_source.findFocus(focus)
      :focus;

    
    Channel<?> ret=null;
    
    Focus<?> searchFocus=specifiedFocus;
    while (ret==null && searchFocus!=null)
    {
      Channel<?> context=searchFocus.getContext();
      if (context!=null)
      { 
        if (searchFocus!=specifiedFocus)
        {  
          log.fine
            ("Experimental context search for '"
            +_identifier+"' in "
            +context.getReflector().getTypeURI()
            );
        }
        ret=context.resolve(specifiedFocus,_identifier,null);
      }
      
      // 
      // // XXX Enable experimental code to search up the focus chain for 
      // //   an identifier without resolving a particular context by its
      // //   URI. Given how we can interleave contexts that don't know about
      // //   each other, we probably need a mechanism to constrain this to
      // //   resolve names in parent contexts that have a well defined
      // //   relationship to the current focus (e.g. same containing file or
      // //   type)
      // //
      // searchFocus=searchFocus.getParentFocus();
      //
      searchFocus=null;
    }

    if (ret==null)
    { 
      // XXX This pathway only ends up getting used with a TeleFocus that has
      //   no context, which looks like a legacy scenario that occurred before
      //   the context/subject differentiation was defined more specifically.
      try
      { 
        Channel<?> subject=specifiedFocus.getSubject();
        if (subject!=null)
        {
          ret=subject.resolve(specifiedFocus,_identifier,null);
          if (ret!=null && specifiedFocus.getContext()!=null)
          { log.fine("Found '"+_identifier+"' in subject, not context of "+specifiedFocus);
          }
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

  @Override
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("PrimaryIdentifier '"+_identifier+"'");
    prefix=prefix+"  ";
    if (_source!=null)
    { _source.dumpTree(out,prefix);
    }

  }
  
}
