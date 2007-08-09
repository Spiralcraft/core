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
package spiralcraft.builder;

import java.net.URI;


import spiralcraft.util.Path;
import spiralcraft.vfs.Resource;

import java.io.IOException;

import spiralcraft.sax.ParseTreeFactory;
import spiralcraft.sax.ParseTree;
import spiralcraft.sax.Node;
import spiralcraft.sax.Element;
import spiralcraft.sax.Characters;
import spiralcraft.sax.Attribute;

import org.xml.sax.SAXException;

import java.util.Iterator;
import java.util.HashMap;


/**
 * Reads an AssemblyClass defined in an XML resource or via a 'class' URI. 
 *
 * Maintains a static cache of Assemblies indexed by URI.
 */
public class AssemblyLoader
{
  private static final AssemblyLoader _INSTANCE=new AssemblyLoader();
  
  private final HashMap<URI,AssemblyClass> _classCache
  	=new HashMap<URI,AssemblyClass>();
  private final HashMap<URI,AssemblyClass> _cache
  	=new HashMap<URI,AssemblyClass>();
  
  /**
   * Return the singleton instance of the AssemblyLoader
   */
  public static AssemblyLoader getInstance()
  { return _INSTANCE;
  }

  /**
   * Instantiate a standalone Object from the specified URI.
   */
  public Object instantiateObject(URI classUri)
    throws BuildException
  { 
    AssemblyClass assemblyClass=findAssemblyClass(classUri);
    Assembly<?> assembly=assemblyClass.newInstance(null);
    return assembly.getSubject().get();
    
  }
  
  /**
   * Instantiate an Assembly from the specified URI, in the context of the
   *   specified parent.
   */
  public Assembly<?> instantiateAssembly(URI classUri,Assembly<?> parent)
    throws BuildException
  { 
    AssemblyClass assemblyClass=findAssemblyClass(classUri);
    Assembly<?> assembly=assemblyClass.newInstance(parent);
    return assembly;
  }

  /**
   * Retrieve the AssemblyClass identified by the specified URI. The URI
   *   identifies an abstract resource- ie. if <uri>.assy.xml does
   *   not exist, a default assembly for the Java class associated with
   *   the URI will be returned.
   */
  public synchronized AssemblyClass findAssemblyClass(URI classUri)
    throws BuildException
  {
    AssemblyClass ret=(AssemblyClass) _classCache.get(classUri);
    if (ret==null)
    {
      ret=findAssemblyDefinition
        (URI.create(classUri.toString()+".assy.xml"));
        
      if (ret!=null)
      { _classCache.put(classUri,ret);
      }
    }
    
    
    if (ret==null)
    { 
      Path path=new Path(classUri.getPath(),'/');
      
      URI packageUri
        =classUri.resolve(URI.create(path.parentPath().format("/")+"/"));
      String className
        =packageUri.relativize(classUri).toString();
      
//      System.out.println
//        ("AssemblyLoader.findAssemblyClass: "+classUri.toString());
//      System.out.println
//        ("AssemblyLoader.findAssemblyClass: "+packageUri.toString());
//      System.out.println
//        ("AssemblyLoader.findAssemblyClass: "+className.toString());
      
      ret=new AssemblyClass
        (null
        ,packageUri
        ,className
        ,null
        ,this
        );
      ret.resolve();  
      _classCache.put(classUri,ret);
    }

    return ret;
    
  }
  
  /** 
   * Retrieve the AssemblyClass defined by the XML document obtained
   *   from the specified resource. The AssemblyClass will be retrieved
   *   from a cache if it has not been loaded, otherwise it will be
   *   loaded and placed into the cache for future use.
   */
  // XXX Use Weak cache
  public synchronized AssemblyClass findAssemblyDefinition(URI resourceUri)
    throws BuildException
  { 
    AssemblyClass ret=(AssemblyClass) _cache.get(resourceUri);
    if (ret==null)
    { 
      ret=loadAssemblyDefinition(resourceUri);

    }
    return ret;
  }

  /** 
   * Load an AssemblyClass from the XML document obtained from the
   *   specified resource.
   */
  public AssemblyClass loadAssemblyDefinition(Resource resource)
    throws BuildException
  {
    ParseTree parseTree=null;
    
    try
    { parseTree=ParseTreeFactory.fromResource(resource);
    }
    catch (SAXException x)
    { throw new BuildException("Error parsing "+resource.toString(),x);
    }
    catch (IOException x)
    { throw new BuildException("Error reading "+resource.toString(),x);
    }

    if (parseTree==null)
    { return null;
    }
    else 
    { return loadAssemblyDefinition(resource.getURI(),parseTree);
    }
    
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
    else 
    { return loadAssemblyDefinition(resourceUri,parseTree);
    }
  }

  private AssemblyClass loadAssemblyDefinition(URI resourceUri,ParseTree parseTree)
    throws BuildException
  {
    Element root=parseTree.getDocument().getRootElement();
    AssemblyClass assemblyClass=readAssemblyClass(resourceUri,root,null);
    _cache.put(resourceUri,assemblyClass);
    assemblyClass.resolve();
    return assemblyClass;
  }
  
  /**
   * Define an AssemblyClass based on the information in an XML Element
   */
  private AssemblyClass readAssemblyClass
    (URI sourceUri
    ,Element node
    ,AssemblyClass containerClass
    )
    throws BuildException
  {
    String baseUriString = node.getURI();
    if (baseUriString!=null)
    {
      if (baseUriString.equals(""))
      { baseUriString=null;
      }
      else if (!baseUriString.endsWith("/"))
      { baseUriString=baseUriString.concat("/");
      }
    }

    URI baseUri
      =baseUriString!=null
      ?sourceUri.resolve(baseUriString)
      :sourceUri.resolve("./").normalize();
      ;

    AssemblyClass assemblyClass
      =new AssemblyClass
        (sourceUri
        ,baseUri
        ,node.getLocalName()
        ,containerClass
        ,this
        );
        
    assemblyClass.setDeclarationName(node.getQName());

    Attribute[] attribs
      =node.getAttributes();
    if (attribs!=null)
    {
      for (int i=0;i<attribs.length;i++)
      {
        String name=attribs[i].getLocalName().intern();

        if (name=="singleton")
        { 
          if (containerClass!=null)
          { 
            throw new BuildException
              ("Only top level assembly classes can be declared as singletons");
          }
          assemblyClass.setSingleton(readBoolean(attribs[i]));
        }
        else if (name=="id")
        { assemblyClass.setId(attribs[i].getValue());
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
      StringBuilder constructorBuff=new StringBuilder();
      boolean readProperties=false;
      
      Iterator<Node> it=node.getChildren().iterator();
      while (it.hasNext())
      { 
        Node child = it.next();
        if (child instanceof Element)
        { 
          if (constructorBuff.length()>0)
          { 
            throw new BuildException
              ("Assembly definition cannot contain both constructor text"
              +" and property specifiers"
              );
          }    
          readProperty(sourceUri,(Element) child,assemblyClass);
          readProperties=true;
        }
        else if (child instanceof Characters)
        { 
          String characterString
            =new String(((Characters) child).getCharacters()).trim();
          if (characterString.length()>0)
          {
            if (readProperties)
            {
              throw new BuildException
                ("Assembly definition cannot contain both constructor text"
                +" and property specifiers"
                );
              
            }
            constructorBuff.append(characterString);
          }
        }
      }
      if (constructorBuff.length()>0)
      { assemblyClass.setConstructor(constructorBuff.toString());
      }
    }
    return assemblyClass;
  }

  private void readProperty(URI sourceUri,Element node,AssemblyClass containerClass)
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
        else if (name=="persistent")
        { prop.setPersistent(readBoolean(attribs[i]));
        }
        else if (name=="dynamic")
        { prop.setDynamic(true);
        }
        else if (name=="collection")
        { prop.setCollectionClassName(attribs[i].getValue());
        }
        else if (name=="export")
        { prop.setExport(readBoolean(attribs[i]));
        }
        else
        { 
          throw new BuildException
            ("Unknown attribute '"+name+"' in "+sourceUri);
            
        }
      }
    }
    
    containerClass.addPropertySpecifier(prop);
    if (node.hasChildren())
    {
      Iterator<Node> it=node.getChildren().iterator();
      while (it.hasNext())
      { 
        Node child = it.next();
        if (child instanceof Element)
        { prop.addAssemblyClass(readAssemblyClass(sourceUri,(Element) child,containerClass));
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
