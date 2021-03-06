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
package spiralcraft.exec;

import spiralcraft.bundle.Library;
import spiralcraft.cli.Arguments;
import spiralcraft.common.LifecycleException;
import spiralcraft.common.Initializer;
import spiralcraft.common.namespace.NamespaceContext;
import spiralcraft.common.namespace.StandardPrefixResolver;
import spiralcraft.data.Type;
import spiralcraft.data.TypeNotFoundException;
import spiralcraft.data.persist.AbstractXmlObject;
import spiralcraft.data.persist.PersistenceException;

import spiralcraft.time.Scheduler;
import spiralcraft.util.ArrayUtil;
import spiralcraft.util.ContextDictionary;
import spiralcraft.util.Path;
import spiralcraft.util.URIUtil;
import spiralcraft.util.refpool.URIPool;

import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;
import spiralcraft.vfs.context.ContextResourceMap;
import spiralcraft.vfs.file.FileResource;
import spiralcraft.lang.BindException;
import spiralcraft.log.ConsoleHandler;
import spiralcraft.log.EventHandler;
import spiralcraft.log.GlobalLog;
import spiralcraft.log.RotatingFileHandler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Properties;
import java.util.ServiceLoader;

import java.io.IOException;
import java.io.InputStream;

/**
 * <p>The Executor is the standard "Main" class for invoking coarse grained
 *   functionality in the Spiralcraft framework via the Executable interface.
 * </p>
 * 
 * <p>The Executor loads an Executable using the 
 *   <code>spiralcraft.data.persist.XmlObject</code>
 *   persistence mechanism, using a Type resource URI and/or an instance 
 *   resource URI. However the XmlObject is specified, the constructed 
 *   instance must implement the spiralcraft.exec.Executable interface.
 * </p>
 * 
 * <p>Typically, the Type resource URI, if specified, resolves to a
 *   BuilderType (spiralcraft.data.builder.BuilderType), which means that a 
 *   resource <code>[Type resource URI].assy.xml</code> must exist, and must
 *   build an object that implements the Executable interface.
 * </p>
 * 
 * <p>
 *   The Executor is invoked via its static launch(String ... args) method
 *     from another ClassLoader, and <b><i>expects to be a singleton within its
 *     own ClassLoader</i></b>. As such, options preceding the Executable
 *     URI will be used to configure static fields and other invariants of
 *     the application context such as logging, thread scheduling, etc.
 * </p>
 * 
 * <p> 
 * </p>
 */
public class Executor
{
  private URI uri;
  private URI typeURI;
  private URI instanceURI;
  private URI codeURI;
  private boolean persistOnCompletion;
  private AbstractXmlObject<Executable,?> wrapper;
  private int argCounter=-1;
  private EventHandler logHandler;
  private EventHandler consoleHandler;
  private boolean consoleLog;
  private URI altContextURI;
  private URI configURI;
  private URI logURI;
  private URI dataURI;
  
  protected ExecutionContext context
    =ExecutionContext.getInstance();
  
  protected ContextDictionary properties
    =new ContextDictionary(ContextDictionary.getInstance());
    
  protected String[] arguments=new String[0];
  
  protected StandardPrefixResolver prefixResolver
    =new StandardPrefixResolver();
  
  
  /**
   * Legacy entry point from launcher
   * 
   * @see execute(String[] args)
   * @param args
   * @throws IOException
   * @throws URISyntaxException
   * @throws PersistenceException
   * @throws ExecutionException
   */
  public static void launch(String ... args)
    throws IOException
      ,URISyntaxException
      ,PersistenceException
      ,ExecutionException
  { new Executor().execute(args);
  }
  
  
  /**
   * Entry point from launcher
   * 
   * @see execute(String[] args)
   * @param args
   * @throws IOException
   * @throws URISyntaxException
   * @throws PersistenceException
   * @throws ExecutionException
   */
  public static void launch(HashMap<String,Object> contextMap,String ... args)
    throws IOException
            ,URISyntaxException
            ,PersistenceException
            ,ExecutionException
  {  
    ExecutionContext.pushInstance
      (new ExecutionContext(ExecutionContext.getInstance(),contextMap));
    try
    { new Executor().execute(args);
    }
    finally
    { ExecutionContext.popInstance();
    }
  }
  
  public void setTypeURI(URI typeURI)
  { this.typeURI=typeURI;
  }
  
  public void setInstanceURI(URI instanceURI)
  { this.instanceURI=instanceURI;
  }
  
  public void setArguments(String[] arguments)
  { this.arguments=arguments;
  }
  
  /**
   * <p>Load and execute an Executable specified by a URI
   * </p>
   * 
   * <p>The first (non-option) argument either refers to either a Type or an
   *   instance resource that provides an implementation of 
   *   spiralcraft.exec.Executable.
   * </p>
   *   
   * <p>Remaining options and arguments after the URI are passed to the
   *   Executable via its execute() method.
   * </p>
   *   
   */
  protected void execute(String ... args)
    throws ExecutionException
  {
    ExecutionContext.pushInstance(context);
    ContextDictionary.pushInstance(properties);
    
    ContextResourceMap contextResourceMap
      =new ContextResourceMap()
      {{
        putDefault(context.focusURI());
        put("run",context.focusURI());
      }};
      
      
    contextResourceMap.push();
    

    prefixResolver.mapPrefix("",URIPool.create("dynamic:/"));
    NamespaceContext.push(prefixResolver);
    
    for (Initializer loader: ServiceLoader.load(Initializer.class))
    { loader.getClass();
    }
    
    boolean logStarted=false;
    boolean schedulerCreated=false;
    Library library=null;
    try
    { 
      
      processArguments(args);
      resolveLogLocation(contextResourceMap);
      if (logHandler!=null)
      {
        try
        { 
          GlobalLog.instance().addHandler(logHandler);
          
          if (consoleLog)
          { 
            consoleHandler=new ConsoleHandler();
            GlobalLog.instance().addHandler(consoleHandler);
          }
          logStarted=true;
        }
        catch (LifecycleException x)
        { throw new ExecutionException("Error starting log handler "+logHandler);
        }
      }
      
      resolveContextConfig(contextResourceMap);
      resolveDataLocation(contextResourceMap);
      resolveCodeLocation(contextResourceMap);
      
      library=resolveLibrary();
      if (library!=null)
      { Library.set(library);
      }
      
      Scheduler.push(new Scheduler());
      schedulerCreated=true;
      
      if (argCounter<0)
      { 
        throw new IllegalArgumentException
          ("Executor requires at least 1 argument");
      }

      if (uri==null)
      {
        throw new IllegalArgumentException
          ("No Type URI or instance URI specified. Nothing to execute.");
      }
    
      uri=context.canonicalize(uri);
      
      try
      { 
        Type.resolve(uri);
        typeURI=uri;
      }
      catch (TypeNotFoundException x)
      { 
        if (instanceURI!=null)
        { throw x;
        }
        instanceURI=uri;
      }
        
      
      try
      {
        if (altContextURI!=null)
        {
          ExecutionContext.pushInstance
            (new ExecutionContext(context)
              {
                @Override
                public URI focusURI()
                { return altContextURI;
                }
              }
            );
        }
        
        Executable executable=resolveExecutable(); 
        executable.execute(arguments);
        if (persistOnCompletion && instanceURI!=null)
        { wrapper.save();
        }
      }
      finally
      {
        if (altContextURI!=null)
        { ExecutionContext.popInstance();
        }
      }
      
    
    }
    catch (Exception x)
    { 
      throw new ExecutionException
        ("Error executing "+ArrayUtil.format(args," ",null),x);
    }
    finally
    { 
      if (logHandler!=null && logStarted)
      { 
        
        try
        { 
          GlobalLog.instance().removeHandler(logHandler);
          if (consoleLog)
          { GlobalLog.instance().removeHandler(consoleHandler);
          }
        }
        catch (LifecycleException x)
        { x.printStackTrace();
        }
        
        
      }
      
      if (schedulerCreated)
      { Scheduler.pop();
      }
     
      NamespaceContext.pop();
      contextResourceMap.pop();
      ContextDictionary.popInstance();
      ExecutionContext.popInstance();
    }
  }

  /**
   * Resolve the location referenced by the log authority where we can create logs
   * 
   * @param contextResourceMap
   */
  private void resolveLogLocation(ContextResourceMap contextResourceMap)
  {
    if (logURI==null)
    {
      String logDir=properties.find("spiralcraft.log.uri");
      if (logDir==null)
      { 
        logDir="log";
      }
      logURI=URIPool.create(logDir);
    }
    
    if (!logURI.isAbsolute())
    {
      contextResourceMap.put
        ("log"
        ,URIUtil.ensureTrailingSlash
          (URIUtil.addPathSegment
            (contextResourceMap.get("")
            ,logURI.getPath()
            )
          )
        );
      
    }
    else
    { 
      contextResourceMap.put
        ("log"
        ,URIUtil.ensureTrailingSlash(logURI)
        );

    }
          
    
  }

  /**
   * Resolve the config authority where we reference the assembly package
   *  that defines the executable
   * 
   * @param contextResourceMap
   */
  private void resolveContextConfig(ContextResourceMap contextResourceMap)
  {
    if (configURI==null)
    {
      String configDir=properties.find("spiralcraft.config.uri");
      if (configDir==null)
      { 
        configDir="config";
        if (properties.find("spiralcraft.config.id")!=null)
        { configDir=configDir+"."+properties.find("spiralcraft.config.id");
        }
      }
      configURI=URIPool.create(configDir);
    }
    
    if (!configURI.isAbsolute())
    {
      contextResourceMap.put
        ("config"
        ,URIUtil.ensureTrailingSlash
          (URIUtil.addPathSegment
            (contextResourceMap.get("")
            ,configURI.getPath()
            )
          )
        );
      
    }
    else
    { 
      contextResourceMap.put
        ("config"
        ,URIUtil.ensureTrailingSlash(configURI)
        );

    }
          
    
  }

  /**
   * Resolve the default code location for components of this executable
   * 
   * @param contextResourceMap
   */
  private void resolveCodeLocation(ContextResourceMap contextResourceMap)
  {
    
    if (codeURI==null)
    {
      String codeDir=properties.find("spiralcraft.code.location");
      if (codeDir==null)
      { codeDir=".";
      }
      codeURI=URIPool.create(codeDir);
    }
    
    if (!codeURI.isAbsolute())
    {
      contextResourceMap.put
        ("code"
        ,URIUtil.ensureTrailingSlash
          (URIUtil.addPathSegment
            (contextResourceMap.get("")
            ,codeURI.getPath()
            )
          )
        );
      
    }
    else
    { 
      contextResourceMap.put
        ("code"
        ,URIUtil.ensureTrailingSlash(codeURI)
        );

    }
          
    
  }
  
  /**
   * Resolve the default data location for data that will be operated on
   *   by this Executable
   * 
   * @param contextResourceMap
   */
  private void resolveDataLocation(ContextResourceMap contextResourceMap)
  {
    
    if (dataURI==null)
    {
      String dataDir=properties.find("spiralcraft.data.location");
      if (dataDir==null)
      { dataDir="data";
      }
      dataURI=URIPool.create(dataDir);
    }
    
    if (!dataURI.isAbsolute())
    {
      contextResourceMap.put
        ("data"
        ,URIUtil.ensureTrailingSlash
          (URIUtil.addPathSegment
            (contextResourceMap.get("")
            ,dataURI.getPath()
            )
          )
        );
      
    }
    else
    { 
      contextResourceMap.put
        ("data"
        ,URIUtil.ensureTrailingSlash(dataURI)
        );

    }
          
    
  }
  
  /**
   * Map the contents of all packages contained in context:/packages into
   *   the contextResourceMap
   * 
   * @param contextResourceMap
   */
  private Library resolveLibrary()
  {
    try
    {
      Resource libraryResource
        =Resolver.getInstance().resolve("context:/packages");
      Container container=libraryResource.asContainer();
      if (container!=null)
      { return new Library(container);
      }
    }
    catch (UnresolvableURIException x)
    { x.printStackTrace();
    }
    return null;
  }
  
  
  private Executable resolveExecutable()
    throws PersistenceException
  {
    try
    { 
      wrapper
        =AbstractXmlObject.<Executable>create
          (typeURI,instanceURI);
    }
    catch (BindException x)
    { throw new PersistenceException("Error instantiating Executable",x);
    }
    

    Object o=wrapper.get();
    if (!(o instanceof Executable))
    { 
      throw new IllegalArgumentException
        ("Cannot execute "
        +(typeURI!=null?typeURI.toString():"(unspecified Type)")
        +" instance "
        +(instanceURI!=null?instanceURI.toString():"(no instance data)")
        +": Found a "+o.getClass()+" which is not an executable"
        );
    }
    return wrapper.get();
  }
  
  private void loadProperties(URI propsFile)
  { 
    try
    {
      Resource res=Resolver.getInstance().resolve(propsFile);
      if (res!=null)
      {
        try (InputStream in = res.getInputStream())
        {
          Properties props=new Properties();
          props.load(in);
          for (Object k: props.keySet())
          { properties.set((String) k,props.getProperty((String) k));
          }
        }
        catch (IOException x)
        { 
          throw new IllegalArgumentException
            ("Error processing properties.",x);
        }
      }
      else
      { 
        throw new IllegalArgumentException
          ("Error processing properties.",new IOException("Unable to resolve resource: "+uri));
      }
    }
    catch (UnresolvableURIException x)
    { throw new IllegalArgumentException("Error processing properties",x);
    }
  }
  
  /**
   * Process arguments. The first non-option argument is the Type URI. If there
   *   is more than one argument, the second non-option argument is either
   *   an instance URI or a "-", indicating no instance.
   *   
   */
  private void processArguments(String[] args)
  {
//    context.err().println(ArrayUtil.format(args, ",", "\""));

    new Arguments()
    {
      @Override
      public boolean processArgument(String argument)
      { 
        if (argCounter==-1)
        { 
          uri=URIPool.create(argument);
          argCounter++;
        }
        else
        { 
          arguments=ArrayUtil.append(arguments,argument);
          argCounter++;
        }
        return true;
      }

      @Override
      public boolean processOption(String option)
      { 
        if (argCounter==-1)
        { 
          if (option.startsWith("D"))
          { 
            String assignment=option.substring(1);
            int eqpos=assignment.indexOf('=');
            if (eqpos<0)
            { properties.set(assignment,null);
            }
            else
            { 
              properties.set
                (assignment.substring(0,eqpos)
                ,assignment.substring(eqpos+1)
                );
//              context.err().println(assignment);
            }
          }
          else if (option.equals("-log"))
          { 
            RotatingFileHandler handler=new RotatingFileHandler();
            handler.setPath(new Path(nextArgument(),'/'));
            Executor.this.logHandler=handler;
          }
          else if (option.equals("-logURI"))
          {
            String logURI=nextArgument();
            Path path=null;
            try
            {            
              Resource res=Resolver.getInstance().resolve(logURI);
              if (res!=null)
              { res=res.unwrap(FileResource.class);
              }
              if (res==null)
              { throw new IllegalArgumentException("Not a file "+logURI);
              }
              path=new Path( ((FileResource) res).getURI().getPath(),'/');
            }
            catch (IOException x)
            { throw new IllegalArgumentException("Error resolving "+logURI,x);
            }
            RotatingFileHandler handler=new RotatingFileHandler();
            handler.setPath(path);
            Executor.this.logHandler=handler;
          }
          else if (option.equals("-consoleLog"))
          { Executor.this.consoleLog=true;
          }
          else if (option.equals("-context"))
          { 
            URI contextUri=URIPool.create(nextArgument());
            if (!contextUri.isAbsolute())
            { contextUri=ExecutionContext.getInstance().canonicalize(contextUri);
            }
            ContextResourceMap.get().putDefault(contextUri);
            altContextURI=contextUri;
          }
          else if (option.equals("-config"))
          { configURI=URIPool.create(nextArgument());
          }
          else if (option.equals("-data"))
          { dataURI=URIPool.create(nextArgument());
          }
          else if (option.equals("-persist"))
          { 
            instanceURI=URIPool.create(nextArgument());
            persistOnCompletion=true;
          }
          else if (option.equals("-props"))
          { loadProperties(URIPool.create(nextArgument()));
          }
          else 
          { 
            ExecutionContext.getInstance().err()
              .println("Unrecognized Executor option '-"+option+"'");
            ExecutionContext.getInstance().err()
              .println("Valid options: -D,--log,--consoleLog,--context,--config");
            
            throw new IllegalArgumentException
              ("Unrecognized Executor option '-"+option+"'"    
              );
          }
        }
        else
        { 
          if (argCounter==0 && option.equals(""))
          { 
            ExecutionContext.getInstance().err()
              .println("Executor parameter '-' is deprecated");
          }
          else
          { arguments=ArrayUtil.append(arguments,"-"+option);
          }
          argCounter++;
        }
        return true;
      }

    }.process(args);
  }    

}
