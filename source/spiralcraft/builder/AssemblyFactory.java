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
/**
 * Reads an assembly from an XML resource
 */
public class AssemblyFactory
{

  /** 
   * Instantiate an assembly defined by the XML document obtained
   *   from the specified resource.
   */
  public static AssemblyClass loadAssemblyClass(URI resourceUri)
    throws IOException,ClassNotFoundException
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
    System.out.println(root.toString());

    String packageUriString = root.getURI();
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
      ?resourceUri.resolve(packageUriString)
      :resourceUri.resolve("./").normalize();
      ;

    URI baseUri
      =packageUri.resolve(root.getLocalName()+".assembly.xml");
      
    String className
      =packageUri.getPath().substring(1).replace('/','.')+root.getLocalName();

    System.err.println("Base URI="+baseUri);
    System.err.println("ClassName="+className);

    
    AssemblyClass baseClass=null;

    // Recursive descent resolution of inheritance hierarchy-
    if (!baseUri.equals(resourceUri))
    { baseClass=loadAssemblyClass(baseUri);
    }

    AssemblyClass assemblyClass;

    if (baseClass!=null)
    { assemblyClass = new AssemblyClass(resourceUri,baseClass);
    }
    else
    { 
      Class javaClass
        =Class.forName
          (className
          ,false
          ,Thread.currentThread().getContextClassLoader()
          );
      assemblyClass = new AssemblyClass(resourceUri,javaClass);
      
    }
       
    return assemblyClass;
  }

}
