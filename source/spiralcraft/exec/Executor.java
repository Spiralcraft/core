package spiralcraft.exec;

import spiralcraft.builder.XmlObject;
import spiralcraft.builder.BuildException;

import spiralcraft.util.ArrayUtil;
import spiralcraft.util.Arguments;

import spiralcraft.registry.Registry;
import spiralcraft.registry.Registrant;
import spiralcraft.registry.RegistryNode;

import spiralcraft.stream.Resolver;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

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
  private String[] _arguments=new String[0];
  private Preferences _prefs;
  private RegistryNode _registryNode;  
  
  public static final void main(String[] args)
    throws IOException
            ,URISyntaxException
            ,BuildException
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
   *   for the specified URI.
   */
  public void execute(String[] args)
    throws IOException
            ,URISyntaxException
            ,BuildException
  {
    processArguments(args);

    if (_uri==null)
    { throw new IllegalArgumentException("No URI specified");
    }
    
    URI uri=URI.create(_uri);
    if (!uri.isAbsolute())
    { 
      // XXX Get user context
      _uri=new File(new File(".").getAbsolutePath()).toURI().resolve(uri).toString();
    }

    XmlObject application=resolveApplication();
      
    if (_registryNode==null)
    { _registryNode=Registry.getLocalRoot();
    }

    application.register(_registryNode);

    Executable executable=(Executable) application.get();
    executable.execute(_arguments);
  }

  private XmlObject resolveApplication()
    throws BuildException
  { 
    if (_uri.endsWith(".assembly.xml"))
    { return new XmlObject(null,null,_uri.substring(0,_uri.indexOf(".assembly.xml")));
    }
      
    return new XmlObject(_uri,null,null);
  } 
  
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
