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
package spiralcraft.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import spiralcraft.log.ClassLog;


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
 * 
 * XXX Unfinished
 * 
 * @author mike
 *
 */
public class PathPattern
{
  public static final ClassLog log
    =ClassLog.getInstance(PathPattern.class);
  public static boolean debug;
  private String patternString;
  
  enum Type
  { TREE,GLOB,ROOT;
  }

  class Token
  {
    final Type type;
    final Pattern pattern;

    public Token(Type type)
    { 
      this.type=type;
      this.pattern=null;
    }

    public Token(Type type,Pattern pattern)
    { 
      this.type=type;
      this.pattern=pattern;
    }
    
    @Override
    public String toString()
    { return super.toString()+":"+type+":"+pattern;
    }
  }
  
  private Token[] tokens;
  private Path prefix;
  
  public PathPattern(Path prefix,String pattern)
  { 
    this(pattern);
    this.prefix=prefix;
    
  }
  
  public PathPattern(String pattern)
  { 
    this.patternString=pattern;
    this.tokens=parse(pattern);
  }
  
  @Override
  public String toString()
  { return (prefix!=null?(prefix+patternString):patternString);
  }
  
  public boolean matches(Path path)
  {
    if (debug)
    { log.fine("["+path.format(" : ")+"] : "+ArrayUtil.format(tokens,",","\""));
    }
    
    if (prefix!=null)
    { 
      if (!path.startsWith(prefix))
      { return false;
      }
      path=prefix.relativize(path);
    }
        
    int tokenPos=0;
    
    if (tokens[0].type==Type.ROOT)
    {
      if (!path.isAbsolute())
      { return false;
      }
      else
      { tokenPos++;
      }
    }
    
    boolean tree=false;
    for (String element:path)
    {
      Type type=tokenPos<tokens.length?tokens[tokenPos].type:null;
      
      if (type==Type.TREE)
      { 
        tree=true;
        tokenPos++;
        type=tokenPos<tokens.length?tokens[tokenPos].type:null;
      }
      
      if (debug)
      { log.fine("["+element+"] : "+tokens[tokenPos]);
      }
      
      if (type==Type.GLOB)
      {
        if (!tokens[tokenPos].pattern.matcher(element).matches())
        {
          if (debug)
          { log.fine("No match");
          }
          
          if (!tree)
          { 
            return false;
          }
        }
        else
        {
          if (debug)
          { log.fine("Match");
          }
          if (tree)
          { tree=false;
          }
          tokenPos++; // consume
        }
      }
      if (type==null)
      {
        if (!tree)
        { return false;
        }
        // End of pattern
      }
    }
    
    if (debug)
    { log.fine(tokenPos+" "+tokens.length);
    }
    
    if (tokenPos==tokens.length-1 
        && tokens[tokenPos].type==Type.TREE
        )
    { 
      // A pattern that ends in **, and that has matched all the path elements
      //   should return a match. 
      return true;
    }
    return tokenPos==tokens.length;

  }
  
  
  /**
   * Convert a glob expression to
   *   a regexp pattern.
   */
  private Token[] parse(String pattern)
  {
    Path path=new Path(pattern,'/');
    
    List<Token> tokenList=new ArrayList<Token>(path.size());
    if (path.isAbsolute())
    { tokenList.add(new Token(Type.ROOT));
    }
    
    for (String element:path)
    {
      int size=tokenList.size();
      if (element.equals("**"))
      { 
        if (size==0 || tokenList.get(size-1).type!=Type.TREE)
        { tokenList.add(new Token(Type.TREE));
        }
      }
      else 
      { tokenList.add(new Token(Type.GLOB,globToPattern(element)));
      }
    }

    if (path.isContainer())
    {
      int size=tokenList.size();
      Type lastType=size==0?null:tokenList.get(size-1).type;
      if (lastType!=Type.TREE && lastType!=Type.ROOT)
      { tokenList.add(new Token(Type.TREE));
      }
    }

    return tokenList.toArray(new Token[tokenList.size()]);
  }
  
  /**
   * Convert a glob expression to
   *   a regexp pattern.
   */
  public static Pattern globToPattern(String orig)
    throws PatternSyntaxException
  {
    StringBuffer out=new StringBuffer();
    for (int i=0;i<orig.length();i++)
    { 
      char chr=orig.charAt(i);
      switch (chr)
      {
      case '?':
        out.append('.');
        break;
      case '*':
        out.append(".*");
        break;
      case '.':
      case '[':
      case ']':
      case '\\':
      case '^':
      case '$':
      case '+':
      case '{':
      case '}':
      case '|':
      case '(':
      case ')':
        out.append("\\"+chr);
        break;
      default:
        out.append(chr);
      }
    }
    return Pattern.compile(out.toString());
  }  
}
