package spiralcraft.builder;

import java.net.URI;

import spiralcraft.stream.Resolver;
import spiralcraft.stream.Resource;

import java.io.IOException;

import spiralcraft.sax.ParseTreeFactory;
import spiralcraft.sax.ParseTree;
import spiralcraft.sax.Node;
import spiralcraft.sax.Element;
import spiralcraft.sax.Characters;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

import java.util.List;
import java.util.Iterator;
import java.util.HashMap;

/**
 * Reads an assembly from an XML resource
 */
public class AssemblyLoader
{
  private static final AssemblyLoader _INSTANCE=new AssemblyLoader();
  
  private final HashMap _cache=new HashMap();
  
  /**
   * Return the singleton instance of the AssemblyLoader
   */
  public static AssemblyLoader getInstance()
  { return _INSTANCE;
  }

  /** 
   * Retrieve an AssemblyClass defined by the XML document obtained
   *   from the specified resource. The AssemblyClass will be retrieved
   *   from a cache if it has not been loaded, otherwise it will be
   *   loaded.
   */
  public synchronized AssemblyClass findAssemblyDefinition(URI resourceUri)
    throws BuildException
  { 
    AssemblyClass ret=(AssemblyClass) _cache.get(resourceUri);
    if (ret==null)
    { 
      ret=loadAssemblyDefinition(resourceUri);
      if (ret!=null)
      { _cache.put(resourceUri,ret);
      }
    }
    return ret;
  }

  /** 
   * Load an AssemblyClass defined by the XML document obtained
   *   from the specified resource.
   */
  private AssemblyClass loadAssemblyDefinition(URI resourceUri)
    throws BuildException
  {
    

    ParseTree parseTree;
    InputStream in=null;

    try
    { 
      Resource resource=Resolver.getInstance().resolve(resourceUri);
      in=resource.getInputStream();
      if (in==null)
      { return null;
      }

      parseTree=ParseTreeFactory.fromInputStream(in);
    }
    catch (SAXException x)
    { throw new BuildException("Error parsing "+resourceUri.toString(),x);
    }
    catch (IOException x)
    { throw new BuildException("Error reading "+resourceUri.toString(),x);
    }
    finally
    { 
      if (in!=null)
      {
        try
        { in.close();
        }
        catch (IOException x)
        { }
      }
    }

    Element root=parseTree.getDocument().getRootElement();
    AssemblyClass assemblyClass=readAssemblyClass(resourceUri,root,null);
    assemblyClass.resolve();
    
    return assemblyClass;
  }

  /**
   * Define an AssemblyClass based on the information in an XML Element
   */
  public AssemblyClass readAssemblyClass(URI localUri,Element node,AssemblyClass containerClass)
  {
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
        ,this
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

  public void readProperty(URI localUri,Element node,AssemblyClass containerClass)
  {
    PropertySpecifier prop=new PropertySpecifier(containerClass,node.getLocalName());
    
    containerClass.addPropertySpecifier(prop);
    if (node.hasChildren())
    {
      Iterator it=node.getChildren().iterator();
      while (it.hasNext())
      { 
        Node child = (Node) it.next();
        if (child instanceof Element)
        { prop.addAssemblyClass(readAssemblyClass(localUri,(Element) child,containerClass));
        }
        else if (child instanceof Characters)
        { prop.addCharacters( ((Characters) child).getCharacters());
        }
      }

    }
  }

}
