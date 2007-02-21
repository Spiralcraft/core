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
package spiralcraft.lang;

import spiralcraft.lang.parser.Node;

import spiralcraft.lang.parser.ExpressionParser;

import java.util.HashMap;

public class Expression
{
  private static final HashMap<String,Expression> _CACHE
    =new HashMap<String,Expression>();

  private Node _root;
  private String _text;
  
  public static Expression parse(String text)
    throws ParseException
  { 
    synchronized (_CACHE)
    { 
      Expression ret=(Expression) _CACHE.get(text);
      if (ret==null)
      { 
        ret=new ExpressionParser().parse(text);
        _CACHE.put(text,ret);
      }
      return ret;
    }
  }

  public Expression(Node root,String text)
  { 
    _root=root;
    _text=text;
  }

  public String getText()
  { return _text;
  }

  /**
   * Create a Channel by binding this Expression to a Focus. This method
   *   is intended to be used by Focus implementors.
   *
   * Users should use Focus.bind(Expression exp) to permit the Focus to 
   *   re-use Channels defined by the same Expression.
   */
  public Channel bind(Focus focus)
    throws BindException
  { 
    if (_root==null)
    { throw new BindException("No way to bind expresion '"+_text+"'");
    }
    return new Channel(focus,_root.bind(focus),this); 
  }

  public void dumpParseTree(StringBuffer out)
  { _root.dumpTree(out,"\r\n");
  }
}
