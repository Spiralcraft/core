package spiralcraft.sax;

import spiralcraft.stream.Resolver;
import spiralcraft.stream.Resource;

import java.net.URI;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.xml.sax.SAXException;

public class ParseTreeFactory
{

  /**
   * Load a ParseTree from a URI. 
   *
   *@return The ParseTree, or null if the target of the URI does
   *  not exist.
   */
  public static ParseTree fromURI(URI uri)
    throws SAXException,IOException
  { return fromResource(Resolver.getInstance().resolve(uri));
  }

  /**
   * Write a ParseTree to a URI
   */     
  public static void toURI(ParseTree parseTree,URI uri)
    throws SAXException,IOException
  { toResource(parseTree,Resolver.getInstance().resolve(uri));
  }

  /**
   * Load a ParseTree from a resource. 
   *
   *@return The ParseTree, or null if the target of the resource does
   *  not exist.
   */
  public static ParseTree fromResource(Resource resource)
    throws SAXException,IOException
  { 
    InputStream in=resource.getInputStream();
    try
    { return fromInputStream(in);
    }
    finally
    { 
      if (in!=null)
      { in.close();
      }
    }
  }

  /**
   * Write a ParseTree to a resource. 
   */
  public static void toResource(ParseTree parseTree,Resource resource)
    throws SAXException,IOException
  { 
    OutputStream out=resource.getOutputStream();
    if (out==null)
    { throw new IOException("Resource '"+resource+"' cannot be written to");
    }

    try
    { toOutputStream(parseTree,out);
    }
    finally
    { 
      if (out!=null)
      { out.close();
      }
    }
  }

  /**
   * Load a parse tree from an InputStream.
   *@return The ParseTree, or null if the supplied InputStream is null.
   */
  public static ParseTree fromInputStream(InputStream in)
    throws SAXException,IOException
  {
    if (in==null)
    { return null;
    }

    SAXParserFactory factory=SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);

    SAXParser parser; 
    try
    { parser = factory.newSAXParser(); 
    }
    catch (ParserConfigurationException x)
    { 
      // Shouldn't happen
      x.printStackTrace();
      throw new IOException(x.toString());
    }
    ParseTree parseTree=new ParseTree();
    parser.parse(in,parseTree);
    return parseTree;
  }

  /**
   * Write a parse tree to an OutputStream.
   */
  public static void toOutputStream(ParseTree tree,OutputStream out)
    throws SAXException,IOException
  {
    XmlWriter writer=new XmlWriter(out);
    tree.playEvents(writer);
  }

}
