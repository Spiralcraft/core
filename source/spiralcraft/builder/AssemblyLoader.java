package spiralcraft.builder;

import java.net.URI;

import spiralcraft.stream.Resolver;
import spiralcraft.stream.Resource;

import spiralcraft.util.StringUtil;

import java.io.IOException;

import spiralcraft.sax.ParseTreeFactory;
import spiralcraft.sax.ParseTree;
import spiralcraft.sax.Node;
import spiralcraft.sax.Element;
import spiralcraft.sax.Characters;
import spiralcraft.sax.Attribute;

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
    
    if (!resourceUri.isAbsolute())
    { throw new BuildException("The assembly URI '"+resourceUri+"' is not absolute and cannot be resolved");
    }

    ParseTree parseTree=null;
    
    try
    { parseTree=ParseTreeFactory.fromURI(resourceUri);
    }
    catch (SAXException x)
    { throw new BuildException("Error parsing "+resourceUri.toString(),x);
    }
    catch (IOException x)
    { throw new BuildException("Error reading "+resourceUri.toString(),x);
    }

    if (parseTree==null)
    { return null;
    }

    Element root=parseTree.getDocument().getRootElement();
    AssemblyClass assemblyClass=readAssemblyClass(resourceUri,root,null);
    assemblyClass.resolve();
    
    return assemblyClass;
  }

  /**
   * Define an AssemblyClass based on the information in an XML Element
   */
  private AssemblyClass readAssemblyClass(URI localUri,Element node,AssemblyClass containerClass)
    throws BuildException
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
        
    assemblyClass.setDeclarationName(node.getLocalName());

    Attribute[] attribs
      =node.getAttributes();
    if (attribs!=null)
    {
      for (int i=0;i<attribs.length;i++)
      {
        String name=attribs[i].getLocalName().intern();
        if (name=="singletons")
        { 
          String value=attribs[i].getValue();
          String[] interfaceNames=
            StringUtil.tokenize(value,",");
          assemblyClass.setSingletonNames(interfaceNames);
        }
        else if (name=="name")
        { assemblyClass.setDeclarationName(attribs[i].getValue());
        }
        else
        { 
          throw new BuildException
            ("Unknown attribute '"+name+"'");
        }
      }
    }

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

  private void readProperty(URI localUri,Element node,AssemblyClass containerClass)
    throws BuildException
  {
    PropertySpecifier prop=new PropertySpecifier(containerClass,node.getLocalName());

    Attribute[] attribs = node.getAttributes();
    if (attribs!=null)
    {
      for (int i=0;i<attribs.length;i++)
      {
        String name=attribs[i].getLocalName().intern();
        if (name=="focus")
        { prop.setFocus(attribs[i].getValue());
        }
        else if (name=="expression")
        { prop.setExpression(attribs[i].getValue());
        }
        else if (name=="whitespace")
        { prop.setLiteralWhitespace(readBoolean(attribs[i]));
        }
        else if (name=="preference")
        { prop.setPreference(readBoolean(attribs[i]));
        }
        else
        { 
          throw new BuildException
            ("Unknown attribute '"+name+"'");
            
        }
      }
    }
    
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

  private static final boolean readBoolean(Attribute attrib)
    throws BuildException
  {
    String value=attrib.getValue();
    if (value.equals("true"))
    { return true;
    }
    else if (value.equals("false"))
    { return false;
    }
    else
    { 
      throw new BuildException
        ("Unexpected value '"+attrib.getValue()+"' for '"+attrib.getLocalName()+"' attribute."
        +"Either 'true' or 'false' required"
        );
    }
  }
}
