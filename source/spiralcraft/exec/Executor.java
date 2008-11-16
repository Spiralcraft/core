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

import spiralcraft.data.persist.AbstractXmlObject;
import spiralcraft.data.persist.PersistenceException;

import spiralcraft.util.ArrayUtil;
import spiralcraft.util.Arguments;
import spiralcraft.util.ContextDictionary;

import spiralcraft.lang.BindException;
import spiralcraft.registry.Registry;
import spiralcraft.registry.Registrant;
import spiralcraft.registry.RegistryNode;

import java.net.URI;
import java.net.URISyntaxException;

import java.io.IOException;

/**
 * <p>The Executor is the standard "Main" class for invoking coarse grained
 *   functionality in the Spiralcraft framework.
 * </p>
 * 
 * <p>The Executor loads an Executable using the 
 *   <code>spiralcraft.data.persist.XmlObject</code>
 *   persistence mechanism, using a Type resource URI and/or an instance 
 *   resource URI.
 * </p>
 * 
 * <p>The Type resource URI, if specified, must resolve to a
 *   BuilderType (spiralcraft.data.builder.BuilderType), which means that a 
 *   resource <code>[Type resource URI].assy.xml</code> must exist, and must
 *   build an object that implements the Executable interface.
 * </p>
 * 
 * <p>The instance resource URI, if it points to an existing resource, must
 *   contain an object of a Type that satisfies the above constraint.
 * </p>
 * 
 * <p>If a Type resource URI is specified and either the instance resource URI
 *   is not specified, or the resource it refers to does not exist, a new 
 *   instance of the builder Assembly will be created. If a Type resource URI
 *   is provided, the properties of this Assembly will be saved to the 
 *   specified resource upon normal termination of the Executable.
 * </p>
 */
public class Executor
  implements Registrant,Executable
{
  private URI typeURI;
  private URI instanceURI;
  private boolean persistOnCompletion;
  private AbstractXmlObject<Executable,?> wrapper;
  private RegistryNode registryNode;  
  private int argCounter=-2;
  
  protected ExecutionContext context
    =ExecutionContext.getInstance();
  
  protected ContextDictionary properties
    =new ContextDictionary(ContextDictionary.getInstance());
    
  protected String[] arguments=new String[0];
  
  /**
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

  public void register(RegistryNode registryNode)
  { this.registryNode=registryNode;
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
   * <p>Load and execute an Executable (the executable is specified by the 
   *   first two arguments).
   * </p>
   * 
   * <p>The first argument is the Type resource URI. If no Type resource is
   *   specified, use "-" in the argument in place of the URI.
   * </p>
   *   
   * <p>The second argument is the instance resource URI. If no instance resource
   *   is specified, use "-" in the argument in place of the URI.
   * </p>
   *   
   * The remaining arguments will be passed along to the Executable in its
   *   execute method.
   */
  public void execute(String ... args)
    throws ExecutionException
  {

    
    ExecutionContext.pushInstance(context);
    ContextDictionary.pushInstance(properties);
    try
    { 
      
      processArguments(args);
      if (argCounter<0)
      { 
        throw new IllegalArgumentException
          ("Executor requires at least 2 arguments");
      }

      if (typeURI==null && instanceURI==null)
      { 
        throw new IllegalArgumentException
          ("No Type URI or instance URI specified. Nothing to execute.");
      }
    
      if (typeURI!=null)
      { typeURI=context.canonicalize(typeURI);
      }
    
      if (instanceURI!=null)
      { instanceURI=context.canonicalize(instanceURI);
      }
    
      Executable executable=resolveExecutable(); 
      
      
      executable.execute(arguments);
      if (persistOnCompletion && instanceURI!=null)
      { wrapper.save();
      }

    
    }
    catch (Exception x)
    { 
      throw new ExecutionException
        ("Error executing "+ArrayUtil.format(args," ",null),x);
    }
    finally
    { 
      ContextDictionary.popInstance();
      ExecutionContext.popInstance();
    }
  }

  private Executable resolveExecutable()
    throws PersistenceException
  {
    try
    { 
      wrapper
        =AbstractXmlObject.<Executable>create
          (typeURI,instanceURI,registryNode,null);
    }
    catch (BindException x)
    { throw new PersistenceException("Error instantiating Executable",x);
    }
    
    if (registryNode==null)
    { registryNode=Registry.getLocalRoot();
    }

    wrapper.register(registryNode);
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
        if (argCounter==-2)
        { 
          typeURI=URI.create(argument);
          argCounter++;
        }
        else if (argCounter==-1)
        { 
          instanceURI=URI.create(argument);
          argCounter++;
        }
        else
        { 
          arguments=(String[]) ArrayUtil.append(arguments,argument);
          argCounter++;
        }
        return true;
      }

      @Override
      public boolean processOption(String option)
      { 
        if (argCounter==-2)
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
          else if (!option.equals(""))
          { 
            throw new IllegalArgumentException
              ("Unrecognized option '-"+option+"': "
              +"Type URI for Executable (or '-' for unspecified Type)"
              +" must be specified as first parameter to Executor."    
              );
          }
          else
          { argCounter++;
          }
        }
        else if (argCounter==-1)
        {
          if (!option.equals(""))
          { 
            throw new IllegalArgumentException
              ("Unrecognized option '-"+option+"': "
              +"Instance URI for Executable (or '-' for unspecified data resource)"
              +" must be specified as second parameter to Executor."    
              );
          }
          argCounter++;
        }
        else
        { 
          arguments=(String[]) ArrayUtil.append(arguments,"-"+option);
          argCounter++;
        }
        return true;
      }

    }.process(args,'-');
  }    

}
