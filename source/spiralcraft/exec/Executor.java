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

import spiralcraft.data.persist.XmlObject;
import spiralcraft.data.persist.PersistenceException;

import spiralcraft.util.ArrayUtil;
import spiralcraft.util.Arguments;

import spiralcraft.registry.Registry;
import spiralcraft.registry.Registrant;
import spiralcraft.registry.RegistryNode;

import spiralcraft.stream.Resolver;

import java.net.URI;
import java.net.URISyntaxException;

import java.io.IOException;
import java.io.File;


/**
 * Loads and activates an Executable based on a URI.
 */
public class Executor
  implements Registrant
{
  private String _uri;
  private String _applicationUri;
  private RegistryNode _registryNode;  
  
  protected ExecutionContext _context
    =new SystemExecutionContext();
    
  protected String[] _arguments=new String[0];
  
  public static void main(String[] args)
    throws IOException
            ,URISyntaxException
            ,PersistenceException
            ,ExecutionException
  { new Executor().execute(args);
  }

  public void register(RegistryNode registryNode)
  { _registryNode=registryNode;
  }
  
  public void setURI(String uri)
  { _uri=uri;
  }
  
  /**
   * Locate and execute an application or assembly appropriate
   *   for the specified URI. The URI must be specified as either
   *   the first argument or via the setURI() method.
   */
  public void execute(String[] args)
    throws ExecutionException
            ,IOException
            ,URISyntaxException
            ,PersistenceException
  {
    processArguments(args);

    if (_uri==null)
    { throw new IllegalArgumentException("No URI specified. Nothing to execute.");
    }
    
    URI uri=URI.create(_uri);
    if (!uri.isAbsolute())
    { 
      // XXX Get user context
      _uri=new File(new File(".").getAbsolutePath()).toURI().resolve(uri).toString();
    }


    Executable executable=resolveExecutable(); 
    executable.execute(_context,_arguments);
  }

  private Executable resolveExecutable()
    throws PersistenceException
  {
    XmlObject application=resolveApplication();
      
    if (_registryNode==null)
    { _registryNode=Registry.getLocalRoot();
    }

    application.register(_registryNode);
    return (Executable) application.get();
  }
  
  /**
   * Create the XmlObject which represents the runtime image of the application
   *   specified in the URI.
   *
   * If the URI ends with ".assembly.xml", the root of the specified Assembly 
   *   must be an Executable, which will be instantiated.
   *
   * If the URI ends with a registered extension, the appropriate Assembly will
   *   be instantiated and the URI will be passed as the first argument to
   *   the Executable.
   *
   * In the default case, the URI will be assumed to point to persistent
   *   object data. The object will be restored and executed.
   */
  private XmlObject resolveApplication()
    throws PersistenceException
  { 
    
    // XXX This seems kind of ugly. We need to revisit the resolution protocol.
    if (_uri.endsWith(".assembly.xml"))
    {
      // We have a Type but no data file = non-persistent application
      return new XmlObject
        (null
        ,URI.create(_uri.substring(0,_uri.indexOf(".assembly.xml")))
        );
    }
    
      
    return new XmlObject(URI.create(_uri),null);
  } 
  
  /**
   * Process arguments. The first non-option argument is treated as the URI
   *   of the target to invoke, if the URI hasn't been preset programmatically.
   *   The remaining arguments are passed through the the Executable.
   */
  private void processArguments(String[] args)
  {
    new Arguments()
    {
      public boolean processArgument(String argument)
      { 
        if (_uri==null)
        { _uri=argument;
        }
        else
        { _arguments=(String[]) ArrayUtil.append(_arguments,argument);
        }
        return true;
      }

      public boolean processOption(String option)
      { 
        if (_uri==null)
        { return false;
        }
        else
        { _arguments=(String[]) ArrayUtil.append(_arguments,"-"+option);
        }
        return true;
      }

    }.process(args,'-');
  }    

}
