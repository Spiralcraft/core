package spiralcraft.builder;

import java.net.URI;

import spiralcraft.stream.Resolver;
import spiralcraft.stream.Resource;

import java.io.IOException;

import spiralcraft.sax.ParseTreeFactory;
import spiralcraft.sax.ParseTree;
import spiralcraft.sax.Node;
import spiralcraft.sax.Element;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

import java.util.List;
import java.util.Iterator;

/**
 * Reads an assembly from an XML resource
 */
public class AssemblyFactory
{

  /** 
   * Instantiate an assembly defined by the XML document obtained
   *   from the specified resource.
   */
  public static AssemblyClass loadAssemblyDefinition(URI resourceUri)
    throws IOException
  {
    Resource resource=Resolver.getInstance().resolve(resourceUri);
    
    InputStream in=resource.getInputStream();
    if (in==null)
    { return null;
    }

    ParseTree parseTree;
    try
    { parseTree=ParseTreeFactory.fromInputStream(in);
    }
    catch (SAXException x)
    { throw new IOException(x.toString());
    }
    finally
    { in.close();
    }

    Element root=parseTree.getDocument().getRootElement();
    AssemblyClass assemblyClass=readAssemblyClass(resourceUri,root,null);
    
    
    return assemblyClass;
  }

  /**
   * Define an AssemblyClass based on the information in an XML Element
   */
  public static AssemblyClass readAssemblyClass(URI localUri,Element node,AssemblyClass containerClass)
  {
    System.out.println(node.toString());

    String packageUriString = node.getURI();
    if (packageUriString!=null)
    {
      if (packageUriString.equals(""))
      { packageUriString=null;
      }
      else if (!packageUriString.endsWith("/"))
      { packageUriString=packageUriString.concat("/");
      }
    }

    URI packageUri
      =packageUriString!=null
      ?localUri.resolve(packageUriString)
      :localUri.resolve("./").normalize();
      ;

    AssemblyClass assemblyClass
      =new AssemblyClass
        (localUri
        ,packageUri
        ,node.getLocalName()
        ,containerClass
        );
        
    if (node.hasChildren())
    { 
      Iterator it=node.getChildren().iterator();
      while (it.hasNext())
      { 
        Node child = (Node) it.next();
        if (child instanceof Element)
        { readProperty(localUri,(Element) child,assemblyClass);
        }
      }
    }

    return assemblyClass;
  }

  public static void readProperty(URI localUri,Element node,AssemblyClass containerClass)
  {
    PropertySpecifier prop=new PropertySpecifier(containerClass,node.getLocalName());
    
    containerClass.addPropertySpecifier(prop);
  }

}
