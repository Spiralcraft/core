package spiralcraft.lang;

import spiralcraft.lang.parser.Node;

import spiralcraft.lang.parser.ExpressionParser;
import spiralcraft.lang.parser.ParseException;

import java.util.HashMap;

public class Expression
{
  private static final HashMap _CACHE=new HashMap();

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

  public Channel createChannel(Focus focus)
    throws BindException
  { return new Channel(_root.bind(focus),this); 
  }

  public void dumpParseTree(StringBuffer out)
  { _root.dumpTree(out,"\r\n");
  }
}
