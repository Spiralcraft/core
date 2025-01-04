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


import spiralcraft.text.ParseException;
import spiralcraft.util.ContextDictionary;
import spiralcraft.util.Path;
import spiralcraft.util.URIUtil;
import spiralcraft.util.refpool.URIPool;
import spiralcraft.util.string.StringPool;
import spiralcraft.vfs.Package;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.context.ContextResourceMap;

import java.io.IOException;

import spiralcraft.common.ContextualException;
import spiralcraft.common.namespace.PrefixResolver;
import spiralcraft.lang.Focus;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.sax.Node;
import spiralcraft.sax.ParseTreeFactory;
import spiralcraft.sax.ParseTree;
import spiralcraft.sax.Element;
import spiralcraft.sax.Characters;
import spiralcraft.sax.Attribute;

import org.xml.sax.SAXException;

import java.util.HashMap;


/**
 * Reads an AssemblyClass defined in an XML resource or via a 'class' URI. 
 *
 * Maintains a static cache of Assemblies indexed by URI.
 */
public class AssemblyLoader
{
  private static final ClassLog log=ClassLog.getInstance(AssemblyLoader.class);
  private static final Level logLevel
    =ClassLog.getInitialDebugLevel(AssemblyLoader.class,Level.INFO);
  
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
    return assembly.get();
    
  }
  
  /**
   * Instantiate an Assembly from the specified URI, in the context of the
   *   specified parent.
   */
  public Assembly<?> instantiateAssembly(URI classUri,Focus<?> parentFocus)
    throws BuildException
  { 
    AssemblyClass assemblyClass=findAssemblyClass(classUri);
    Assembly<?> assembly=assemblyClass.newInstance(parentFocus);
    return assembly;
  }

  /**
   * Retrieve the AssemblyClass that wraps the specified Java class, by
   *   converting the class name into a canonical Assembly URI. The URI
   *   identifies an abstract resource- ie. if <uri>.assy.xml does
   *   not exist, a default assembly for the Java class will be returned.
   */
  public synchronized AssemblyClass findAssemblyClass(Class<?> javaClass)
    throws BuildException
  {
    return findAssemblyClass
      (URIPool.create
        ("class:/"
        +javaClass.getName()
          .replace('.','/')
          .replace('$','-')
        )
      );
  }
  
  /**
   * Indicate whether the abstract resource identified by the URI exists
   *   and can be resolved as an AssemblyClass. The URI
   *   identifies an abstract resource- ie. if <uri>.assy.xml does
   *   not exist, a default assembly for the Java class associated with
   *   the URI will be inferred.
   */
  public synchronized boolean isAssemblyClass(URI classUri)
  {
    if (_classCache.get(classUri)!=null)
    { return true;
    }
    else if (_classCache.containsKey(classUri))
    { return false;
    }
    
    
    try
    {
      URI resourceURI=URIUtil.addPathSuffix(classUri,".assy.xml");
      
      try
      { 
        Resource resource=searchForPackageResource(resourceURI);
        if (resource!=null && resource.exists())
        { return true;
        }

        URI classResourceURI=URIUtil.addPathSuffix(classUri,".class");
        resource=searchForPackageResource(classResourceURI);
        if (resource!=null && resource.exists())
        { return true;
        }
        
        if (logLevel.isFine())
        { log.fine("Did not find .assy.xml or .class for "+classUri);
        }
        
        // Cache negative result
        _classCache.put(classUri,null);
      }
      catch (ContextualException x)
      { log.log(Level.WARNING,"Attempt to access "+resourceURI+" failed",x);
      }
    
    }
    catch (IOException x)
    { return false;
    }    
    
    if (classUri.getScheme()!=null && classUri.getScheme().equals("class"))
    {
    
      try
      {
        Path path=new Path(classUri.getPath().substring(1),'/');
        String className=path.format(".");
        if (Thread.currentThread().getContextClassLoader().loadClass(className)
              !=null
            )
        { return true;
        }
      }
      catch (ClassNotFoundException x)  
      {
      }
    }
      
    return false;
   
  }
  
  public Resource searchForPackageResource(URI resourceURI) 
    throws IOException,ContextualException
  {
    Resource resource=Resolver.getInstance().resolve(resourceURI);
    if (resource.exists())
    { return resource;
    }
    
    Resource container=resource.getParent();
    if (container!=null)
    {
      Package pkg=Package.fromContainer(container);
        
      if (pkg!=null)
      {    
        if (logLevel.isFine())
        { log.fine("Checking package "+pkg+" for "+resource.getURI());
        }
        return pkg.searchForBaseResource(resource);
      }
      else
      { 
        if (logLevel.isFine())
        { log.fine("No package mapped to "+resource.getParent());
        }
        return null;
      }
    }
    else
    { return null;
    }
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
    AssemblyClass ret=_classCache.get(classUri);
    if (ret!=null && ret.isStale())
    { ret=null;
    }
    
    if (ret==null)
    {
      ret=findAssemblyDefinition
        (URIPool.create(classUri.toString()+".assy.xml"));
        
      if (ret!=null)
      { _classCache.put(classUri,ret);
      }
    }
    
    if (ret==null)
    { 
      // Search for a .class resource through the package inheritance mechanism
      try
      {
        Resource classResource=searchForPackageResource
          (URIPool.create(classUri.toString()+".class"));
        if (classResource!=null)
        { classUri=URIUtil.removePathSuffix(classResource.getURI(),".class");
        }
        else if (logLevel.isFine())
        { log.fine("No .class resource for "+classUri);
        }
      }
      catch (ContextualException x)
      { log.log(Level.WARNING,"Error resolving "+classUri,x);
      }
      catch (IOException x)
      { log.log(Level.WARNING,"Error resolving "+classUri,x);
      }
      
      // Convert the class URI to a package and class name
      
      Path path=new Path(classUri.getPath(),'/');
      
      URI packageUri
        =URIPool.get(classUri.resolve(URIPool.create(path.parentPath().format("/"))));
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
        ,null
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
    try
    {
      Resource resource=Resolver.getInstance().resolve(resourceUri);
    
      AssemblyClass ret=_cache.get(resource.getURI());
      if (ret!=null && ret.isStale())
      { 
        log.fine
          ("Ejecting stale copy of "+resourceUri+" ("+resource.getURI()+") "
          );
        ret=null;
        _cache.remove(resource.getURI());
      }
      
      if (ret==null && !_cache.containsKey(resource.getURI()))
      { ret=loadAssemblyDefinition(resource);
      } 
      return ret;
    }
    catch (IOException x)
    { throw new BuildException("Error resolving "+resourceUri,x);
    }
  }

//  /** 
//   * Load an AssemblyClass from the XML document obtained from the
//   *   specified resource.
//   */
//  private AssemblyClass loadAssemblyDefinition(Resource resource)
//    throws BuildException
//  {
//    ParseTree parseTree=null;
//
//    try
//    { 
//      if (!resource.exists())
//      { return null;
//      }
//      parseTree=ParseTreeFactory.fromResource(resource);
//    }
//    catch (SAXException x)
//    { throw new BuildException("Error parsing "+resource.toString(),x);
//    }
//    catch (IOException x)
//    { throw new BuildException("Error reading "+resource.toString(),x);
//    }
//
//    if (parseTree==null)
//    { return null;
//    }
//    else 
//    { return loadAssemblyDefinition(resource.getURI(),parseTree);
//    }
//    
//  }
  
  /** 
   * Load an AssemblyClass defined by the XML document obtained
   *   from the specified resource.
   */
  private AssemblyClass loadAssemblyDefinition(Resource resolvedResource)
    throws BuildException
  {
    // Note: This is coded specifically because we may want to
    //   preserve the originally supplied resourceURI, even though
    //   the loaded resource may have a different canonical URI.
    
    URI resourceUri=resolvedResource.getURI();
//    URI resolvedUri=resolvedResource.getResolvedURI();
    if (!resourceUri.isAbsolute())
    { throw new BuildException("The assembly URI '"+resourceUri+"' is not absolute and cannot be resolved");
    }
    
    Resource resource;
    try
    { 
      resource=searchForPackageResource(resourceUri);
      
      if (resource==null || !resource.exists())
      { return null;
      }
      
      if (!resourceUri.equals(resource.getURI()))
      { 
        if (logLevel.isFine())
        { log.fine("Subclassing "+resource.getURI()+" as "+resourceUri);
        }
        AssemblyClass ret
          =new AssemblyClass
            (resourceUri
            ,resource
            ,findAssemblyClass(URIUtil.removePathSuffix(resource.getURI(),".assy.xml"))
            ,null
            ,this
            );
        _cache.put(resourceUri,ret);
        ret.resolve();
        return ret;
      }
      
      return loadAssemblyDefinition(resource.getURI(),resource);
    
    }
    catch (ContextualException x)
    { throw new BuildException("Error parsing "+resourceUri.toString(),x);
    }
    catch (SAXException x)
    { throw new BuildException("Error parsing "+resourceUri.toString(),x);
    }
    catch (IOException x)
    { throw new BuildException("Error reading "+resourceUri.toString(),x);
    }
    
  }

  private AssemblyClass loadAssemblyDefinition
    (URI resourceUri
    ,Resource resource
    )
    throws BuildException,SAXException,IOException,ContextualException
  {
    ContextResourceMap map=new ContextResourceMap();
    
    URI packageDir=URIUtil.toParentPath(resourceUri);
    if (packageDir==null)
    { throw new BuildException("Package for "+resourceUri+" is null");
    }
    map.put("package",packageDir);
    Package pkg
      =Package.fromContainer
        (Resolver.getInstance().resolve(packageDir));
    if (pkg!=null && pkg.getBase()!=null)
    { map.put("package-base",pkg.getBase());
    } 
    else
    { map.put("package-base",URIPool.create("null:/"));
    }
    map.bind(new SimpleFocus<Void>(null));
    map.push();
    try
    {
      ParseTree parseTree=ParseTreeFactory.fromURI(resourceUri);

      if (parseTree==null)
      { return null;
      }
    
      Element root=parseTree.getDocument().getRootElement();
      AssemblyClass assemblyClass=readAssemblyClass(resourceUri,resource,root,null);
      _cache.put(resourceUri,assemblyClass);
      assemblyClass.resolve();
      return assemblyClass;
    }
    finally
    { map.pop();
    }
  }
  
  /**
   * Define an AssemblyClass based on the information in an XML Element. This
   *   method handles all top-level and nested assembly classes.
   * 
   * @param sourceURI The URI of the document container in which the 
   *   Assemblyclass is referenced
   * @param node The node which contains the AssemblyClass reference
   * @param containerClass The immediately containing AssemblyClass
   */
  private AssemblyClass readAssemblyClass
    (URI sourceUri
    ,Resource resource
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
      

    AssemblyClass assemblyClass
      =new AssemblyClass
        (sourceUri
        ,resource
        ,baseUri
        ,node.getLocalName()
        ,containerClass
        ,this
        );
        
    assemblyClass.setDeclarationName(node.getQName());
    assemblyClass.setPrefixResolver(node.getPrefixResolver());
    assemblyClass.setDeclarationLocation(node.getPosition().toURI());

    boolean whitespace=false;
    
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
        else if (name=="overlayId")
        { assemblyClass.setOverlayId(attribs[i].getValue());
        }
        else if (name=="define" || name=="defineLocal")
        { 
          String assignment=attribs[i].getValue();
          int eqpos=assignment.indexOf("=");
          if (eqpos<1)
          { 
            throw new BuildException(
              "Attribute '"+name+"' requires a value of the form "
              +" '[substitutionVariable] = [substitutionText]', "
              +" eg. 'com.myco.examplevar=someValue' "
            );
          }
          String substName=assignment.substring(0,eqpos);
          String substValue
            =eqpos<assignment.length()-1?assignment.substring(eqpos+1):"";
            
          if (name=="define")
          { 
            assemblyClass.define(substName,substValue);
            log.warning
              ("Deprecated attribute syntax 'define'. "
              +" Use 'context.myprop' instead. "
              +node.getPosition()
              );
          }
          else
          { 
            assemblyClass.defineLocal(substName,substValue);
            log.warning
              ("Deprecated attribute syntax 'defineLocal'. "
              +"Use 'this.myprop' instead. "
              +node.getPosition()
              );
          }
        }
        else if (name=="x")
        { assemblyClass.setInstanceX(attribs[i].getValue());
        }
        else if (name=="whitespace")
        { whitespace=Boolean.valueOf(attribs[i].getValue());
        }
        else if (name.startsWith("context."))
        {
          String prop=name.substring(8);
          assemblyClass.define(prop,attribs[i].getValue());
        }
        else if (name.startsWith("this."))
        {
          String prop=name.substring(5);
          assemblyClass.defineLocal(prop,attribs[i].getValue());
        }
        else if (name.equals("bypass"))
        { assemblyClass.setBypass(Boolean.valueOf(attribs[i].getValue()));
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
      
      for (Node child: node)
      {
        if (child instanceof Element)
        { 
          if (constructorBuff.length()>0)
          { 
            throw new BuildException
              ("Assembly definition '"+node.getQName()+"' cannot contain both constructor text"
              +" and property specifiers"
              );
          }    
          readProperty(sourceUri,resource,(Element) child,assemblyClass);
          readProperties=true;
        }
        else if (child instanceof Characters)
        { 
          String characterString
            =new String(((Characters) child).getCharacters());
          if (!whitespace)
          { characterString=characterString.trim();
          }
          if (characterString.length()>0)
          {
            if (readProperties)
            {
              throw new BuildException
                ("Assembly definition '"+node.getQName()+"'  cannot contain both constructor text"
                +" and property specifiers"
                );
              
            }
            constructorBuff.append(characterString);
          }
        }
      }
      if (constructorBuff.length()>0)
      { 
        assemblyClass.setConstructor
          (StringPool.INSTANCE.get(constructorBuff.toString()));
      }
    }
    return assemblyClass;
  }

  private void readProperty(URI sourceUri,Resource resource,Element node,AssemblyClass containerClass)
    throws BuildException
  {
    PropertySpecifier prop
      =new PropertySpecifier(containerClass,node.getLocalName());
    prop.setDeclarationLocation(node.getPosition().toURI());
    prop.setPrefixResolver(node.getPrefixResolver());
    
    Attribute[] attribs = node.getAttributes();
    if (attribs!=null)
    {
      for (int i=0;i<attribs.length;i++)
      {
        String name=attribs[i].getLocalName().intern();
        if (name=="x")
        { prop.setExpression(attribs[i].getValue());
        }
        else if (name=="expression")
        { 
          log.warning
            ("The spiralcraft.builder assembly attribute 'expression'" +
            		" has been deprecated. Use 'x' instead."
            );
          prop.setExpression(attribs[i].getValue());
        }
        else if (name=="whitespace")
        { prop.setLiteralWhitespace(readBoolean(attribs[i]));
        }
        else if (name=="normalizeEOL")
        { prop.setNormalizeEOL(readBoolean(attribs[i]));
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
        else if (name=="replaceCollection")
        { prop.setReplaceCollection(readBoolean(attribs[i]));
        }
        else if (name=="prepend")
        { prop.setPrepend(readBoolean(attribs[i]));
        }
        else if (name=="export")
        { prop.setExport(readBoolean(attribs[i]));
        }
        else if (name=="uri")
        { 
          prop.addCharacters
            (resolveNsUri
              (attribs[i].getValue()
                ,node.getPrefixResolver()
                ,sourceUri
              ).toCharArray()
            );
        }
        else if (name=="dataURI")
        { 
          String specURI;
          try
          { specURI=ContextDictionary.substitute(attribs[i].getValue());
          }
          catch (ParseException x)
          { throw new BuildException
              ("Error parsing "+attribs[i].getValue()+" in "+node.getURI());
          }
           
          
          String uriStr
            =resolveNsUri
              (specURI
              ,node.getPrefixResolver()
              ,sourceUri
              );
          try
          { prop.setDataURI(Resolver.getInstance().resolve(URIPool.create(uriStr)).getURI());
          }
          catch (IOException x)
          { throw new BuildException("Error resolving data uri "+uriStr);
          }
        }
        else if (name=="debugLevel")
        { 
          String debugLevel=attribs[i].getValue();
          prop.setDebugLevel(Level.valueOf(debugLevel));
        }
        else if (name=="contextualize")
        { prop.setContextualize(readBoolean(attribs[i]));
        }
        else
        { 
          throw new BuildException
            ("Unknown attribute '"+name+"' in "+sourceUri+"."
            +" Allowed attributes are (x,whitespace,persistent,dynamic,"
            +"collection,export,uri,dataURI,debugLevel,contextualize)");
            
        }
      }
    }
    
    containerClass.addPropertySpecifier(prop);
    if (node.hasChildren())
    {
      for (Node child:node)
      {
        if (child instanceof Element)
        { 
          prop.addAssemblyClass
            (readAssemblyClass(sourceUri,resource,(Element) child,containerClass));
        }
        else if (child instanceof Characters)
        { prop.addCharacters( ((Characters) child).getCharacters());
        }
      }

    }
  }

  private String resolveNsUri(String uriStr,PrefixResolver resolver,URI sourceUri)
    throws BuildException
  {           
    // Resolve namespace prefix for URI
    
    int colonPos=uriStr.indexOf(':');
    if (colonPos==0)
    { uriStr=uriStr.substring(1);
    }
    else if (colonPos>0)
    { 
      String nsPrefix=uriStr.substring(0,colonPos);
      URI nsURI=resolver.resolvePrefix(nsPrefix);
      if (nsURI==null)
      { 
        throw new BuildException
          ("Namespace prefix '"+nsPrefix+"' not found in "+sourceUri);
      }
      String nsURIstr=nsURI.toString();
      uriStr=uriStr.substring(colonPos+1);
      if (!nsURIstr.endsWith("/"))
      { uriStr=nsURIstr+"/"+uriStr;
      }
      else
      { uriStr=nsURIstr+uriStr;
      }
      
    }
    return uriStr;

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
