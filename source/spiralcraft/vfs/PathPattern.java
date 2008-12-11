//
// Copyright (c) 2008,2008 Michael Toth
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
package spiralcraft.vfs;

import java.util.ArrayList;
import java.util.List;

import spiralcraft.util.Path;
import spiralcraft.util.string.StringUtil;

/**
 * <p>A Path matching pattern.
 * </p>
 * 
 * <p>General syntax derived from DOS and Apache Ant
 * </p>
 * 
 * <p>'*' matches 0 or more chars, '?' matches a single char, and '**' matches
 *   a directory tree prefix or suffix
 * </p> 
 * 
 * XXX Unfinished
 * 
 * @author mike
 *
 */
public class PathPattern
{
  enum Type
  { TREE,STAR,CHAR,LITERAL;
  }

  class Token
  {
    final Type type;
    final String val;

    public Token(Type type)
    { 
      this.type=type;
      this.val=null;
    }

    public Token(Type type,String val)
    { 
      this.type=type;
      this.val=val;
    }
  }
  
  private Token[] tokens;
  
  public PathPattern(String pattern)
  { this.tokens=parse(pattern);
  }
  
  public boolean matches(Path path)
  {
    String[] elements=path.elements();
    int tokenPos=0;
    int elementPos=0;
    int charPos=0;
    String element;
    
    while (true)
    {
      Type type=tokens[tokenPos].type;
      element=elements[elementPos];
      if (type==Type.LITERAL)
      {
        if (!elements[elementPos].startsWith(tokens[tokenPos].val))
        { return false;
        }
      }
    }

  }
  
  
  /**
   * Convert a glob expression to
   *   a regexp pattern.
   */
  private Token[] parse(String pattern)
  {
    String[] tokens=StringUtil.tokenize(pattern,"*?\\/",true);
    
    List<Token> tokenList=new ArrayList<Token>(tokens.length);
    StringBuilder buf=new StringBuilder();
    
    for (int i=0;i<tokens.length;i++)
    {
      if (tokens[i].equals("\\") && i<tokens.length-1)
      { buf.append(tokens[++i]);
      }
      else if (tokens[i].equals("*"))
      {
        if (buf.length()>0)
        { 
          tokenList.add(new Token(Type.LITERAL,buf.toString()));
          buf.setLength(0);
        }
        if (i==tokens.length-1 || !tokens[i+1].equals("*"))
        { tokenList.add(new Token(Type.STAR));
        } 
        else
        { 
          tokenList.add(new Token(Type.TREE));
          i++;
        }
      }
      else if (tokens[i].equals("?"))
      {
        if (buf.length()>0)
        { 
          tokenList.add(new Token(Type.LITERAL,buf.toString()));
          buf.setLength(0);
        }
        tokenList.add(new Token(Type.CHAR));
      }
      else if (tokens[i].equals("/"))
      {
        if (buf.length()>0)
        { 
          tokenList.add(new Token(Type.LITERAL,buf.toString()));
          buf.setLength(0);
        }
      }
      else
      { buf.append(tokens[i]);
      }
      
    }
    
    if (buf.length()>0)
    { 
      String tail=buf.toString();
      tokenList.add(new Token(Type.LITERAL,tail));
      if (tail.endsWith("/"))
      { tokenList.add(new Token(Type.TREE));
      }
    }

    return tokenList.toArray(new Token[tokenList.size()]);
  }
}
