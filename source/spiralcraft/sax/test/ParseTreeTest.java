package spiralcraft.sax.test;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.xml.sax.SAXException;

import spiralcraft.sax.ParseTree;
import spiralcraft.sax.Node;
import spiralcraft.sax.Element;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.File;

import java.util.Iterator;

import spiralcraft.util.Arguments;

public class ParseTreeTest
{

  private File _file=null;
  private boolean _dump=false;
  private int _repeats=0;

  public static void main(String[] args)
  {
    final ParseTreeTest test=new ParseTreeTest();
    test.run(args);
 

  }
  
  public void run(String[] args)
  {
    new Arguments()
    {

      protected boolean processOption(String option)
      {
        if (option=="file")
        { _file=new File(nextArgument());
        }
        else if (option=="dump")
        { _dump=true;
        }
        else if (option=="repeats")
        { _repeats=Integer.parseInt(nextArgument());
        }
        else
        { return false;
        }
        return true;
      }
    }.process(args,'-');

    SAXParserFactory factory = SAXParserFactory.newInstance();
    try 
    {
      SAXParser saxParser = factory.newSAXParser();
      System.err.println(saxParser);

      ParseTree parseTree=new ParseTree();
      
      long time=System.currentTimeMillis();
      if (_file==null)
      {
        String resourceName="test1.xml";
        saxParser.parse(ParseTreeTest.class.getResourceAsStream(resourceName),parseTree);
      }
      else
      { saxParser.parse(_file,parseTree);
      }
      System.err.println("Initial read time "+(System.currentTimeMillis()-time));
      
      if (_dump)
      {
        PrintWriter out=new PrintWriter(new OutputStreamWriter(System.err),true);
        dump(out,parseTree.getDocument(),null);
      }

      if (_repeats>0)
      {
        time=System.currentTimeMillis();      
        for (int i=0;i<_repeats;i++)
        {

          if (_file==null)
          {
            String resourceName="test1.xml";
            saxParser.parse(ParseTreeTest.class.getResourceAsStream(resourceName),parseTree);
          }
          else
          { saxParser.parse(_file,parseTree);
          }
        }
        System.err.println(_repeats+" repeats time "+(System.currentTimeMillis()-time));
      }
    } 
    catch (IOException e)
    {
      System.err.println(e.getMessage());
    }
    catch (ParserConfigurationException e)
    {
      System.err.println(e.getMessage());
    }
    catch (SAXException e) 
    {
      System.err.println(e.getMessage());
    }

  }

  private static void dump(PrintWriter out,Node node,String prefix)
  {
    if (prefix==null)
    { prefix="";
    }
    out.println(prefix+node.toString());
    if (node instanceof Element)
    { prefix=prefix+"|";
    }
    else
    { prefix="-"+prefix;
    }
    if (node.getChildren()!=null)
    {
      Iterator it=node.getChildren().iterator();
      while (it.hasNext())
      { dump(out,(Node) it.next(),prefix);
      }
    }
    
  }
}
