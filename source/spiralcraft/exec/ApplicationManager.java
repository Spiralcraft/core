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

import spiralcraft.registry.Registry;
import spiralcraft.registry.RegistryNode;

import spiralcraft.loader.LibraryCatalog;

import spiralcraft.util.ArrayUtil;

import spiralcraft.data.persist.XmlObject;
import spiralcraft.data.persist.PersistenceException;

import spiralcraft.stream.Resolver;
import spiralcraft.stream.Resource;
import spiralcraft.stream.UnresolvableURIException;

import java.io.File;
import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import spiralcraft.command.CommandContext;

/**
 * Controls the execution of applications via one or more ApplicationEnvironments.
 *
 * An ApplicationEnvironment defines the code modules (.jar or directory trees)
 *   and an entry point necessary to run an application.
 *
 * ApplicationEnvironments are stored in Resources identified by a URI, which
 *   is provided to the exec() method.
 *
 * If the URI is relative, the ApplicationManager will search for the
 *   Resource according to the following rules:
 *
 *   1. System environment path (<spiralcraft-home>/env)
 *   2. User environment path (<user-home>/.spiralcraft/env)
 *   3. "user.dir" Java system property
 *
 */
public class ApplicationManager
{


  private static RegistryNode _REGISTRY_ROOT
    =Registry.getLocalRoot().createChild("applicationManager");

    
  private final File _codebase;
      
  private final LibraryCatalog _catalog;
  
  private final String _userId;
  private final RegistryNode _registryNode;

  private final URI _codebaseEnvironmentURI;


  private final URI _userHomeEnvironmentURI
    =new File(System.getProperty("user.home")).toURI().resolve(".spiralcraft/env/");

  private int _nextEnvironmentId=0;

  private boolean DEBUG=false;
  

  public ApplicationManager(String userId,File codebase)
  { 
    _userId=userId;
    _codebase=codebase;
    _codebaseEnvironmentURI=_codebase.toURI().resolve("env/");
    _catalog=
      new LibraryCatalog
        (new File(_codebase,"lib")
        );
    _registryNode=_REGISTRY_ROOT.createChild(_userId);
  }

  public CommandContext newCommandContext()
  { return new ApplicationManagerCommandContext(this);
  }
  
  public void setDebug(boolean val)
  { DEBUG=val;
  }
  
  public void shutdown()
  { _catalog.close();
  }
  
  public LibraryCatalog getLibraryCatalog()
  { return _catalog;
  }

  public void exec(String[] args)
    throws ExecutionTargetException
  { 
    if (args.length==0)
    { 
      // Show environments in-scope
      throw new IllegalArgumentException
        ("Please specify an application environment");
    }

    URI applicationURI=findEnvironment(args[0]);
    if (applicationURI==null)
    { 
      // Show environments in-scope
      throw new IllegalArgumentException
        ("Unknown application environment '"+args[0]+"'");
    }
        
    args=(String[]) ArrayUtil.truncateBefore(args,1);

    try
    {
      URI environmentTypeRef
        =URI.create("java:/spiralcraft/exec/ApplicationEnvironment");
        
      XmlObject environmentRef
        =new XmlObject(applicationURI,environmentTypeRef);
      
      environmentRef.register
        (_registryNode.createChild(Integer.toString(_nextEnvironmentId++)));
      
      ApplicationEnvironment environment=(ApplicationEnvironment) environmentRef.get();
      environment.setApplicationManager(this);
    
      environment.exec(args);
    }
    catch (PersistenceException x)
    { throw new ExecutionTargetException(x);
    }
  }

  /**
   * Search for the named environment, in order of priority:
   *   
   *   1. The system environment path
   *   2. The user environment path
   *   3. The user directory
   *
   */
  private URI findEnvironment(String name)
  {
    URI nameURI=URI.create(name+".environment.xml");
    URI searchURI=null;
    
    
    if (nameURI.isAbsolute() && isEnvironment(nameURI))
    { return nameURI;
    }

    searchURI=_codebaseEnvironmentURI.resolve(nameURI);
    if (isEnvironment(searchURI))
    { return searchURI;
    }

    searchURI=_userHomeEnvironmentURI.resolve(nameURI);
    if (isEnvironment(searchURI))
    { return searchURI;
    }

    searchURI=new File(System.getProperty("user.dir")).toURI().resolve(nameURI);
    if (isEnvironment(searchURI))
    { return searchURI;
    }

    return null;
  }

  private boolean isEnvironment(URI uri)
  {
    if (DEBUG)
    { System.err.println("Searching for "+uri);
    }
    
    try
    { 
      Resource resource = Resolver.getInstance().resolve(uri);
      if (resource.exists())
      { 
        if (DEBUG)
        { System.err.println("Found "+uri);
        }
        return true;
      }
      else
      { return false;
      }
    }
    catch (UnresolvableURIException x)
    { System.err.println(x.toString());
    }
    catch (IOException x)
    { System.err.println(x.toString());
    }
    return false;
  }
}

/*
10/25/2004

The search for ApplicationEnvironments falls under the concern of context. Our
'search' is aware of three contexts- a 'global' system context, a 'user' context
and a 'current directory' context.

The behavior of the current system is such that no lesser priveleged context
can override a greater privelaged context by substituting an execution
environment with the same name. This is accomplished simply be ordering the
search.

This creates ambiguity when a developer creates a new environment that is
duplicated elsewhere, and the expected code doesn't run. 

The problem we are trying to solve by the search and resolution is primarily
associated with the 'shorthand' invocation of functionality, say, from the
command line.

The command line carries much context information though, in the form of the
directory structure that is being navigated. By creating some association with
parent directories, it may be possible to derive a set of nested contexts.

If this occurs, some deterministic way of getting info about the available
ApplicationEnvironments must be worked out. Perhaps cataloging the
environments is a good way to do this.


*/

