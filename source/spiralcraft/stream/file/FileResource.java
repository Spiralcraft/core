package spiralcraft.stream.file;

import spiralcraft.stream.Resource;
import spiralcraft.stream.AbstractResource;
import spiralcraft.stream.Container;
import spiralcraft.stream.UnresolvableURIException;

import java.net.URI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileResource
  extends AbstractResource
  implements Container
{
  private File _file;
  private Resource[] _contents;

  public FileResource(URI uri)
    throws UnresolvableURIException
  { 
    super(uri);
    _file=new File(uri);
  }

  public InputStream getInputStream()
    throws IOException
  { 
    if (_file.isDirectory())
    { return null;
    }
    return new FileInputStream(_file);
  }

  public boolean supportsRead()
  { return true;
  }

  public OutputStream getOutputStream()
    throws IOException
  { 
    if (_file.isDirectory())
    { return null;
    }
    return new FileOutputStream(_file);
  }

  public boolean supportsWrite()
  { return true;
  }

  public Container asContainer()
  { 
    if (_file.isDirectory())
    { return this;
    }
    return null;
  }

  public Resource getParent()
  { 
    try
    { return new FileResource(_file.getParentFile().toURI());
    }
    catch (UnresolvableURIException x)
    { 
      x.printStackTrace();
      return null;
    }
  }

  public Resource[] listContents()
  { 
    makeContents();
    return _contents;
  }

  public Resource[] listChildren()
  {
    makeContents();
    return _contents;
  }

  public Resource[] listLinks()
  { 
    // We don't know how to determine symbolic link
    return null;
  }

  public Resource createChild(String name)
    throws UnresolvableURIException
  { return new FileResource(new File(_file,name).toURI());
  }

  public Resource createLink(String name,Resource resource)
    throws UnresolvableURIException
  { throw new UnsupportedOperationException();
  }

  private void makeContents()
  { 
    try
    {
      File[] contents=_file.listFiles();
      if (contents!=null)
      {
        _contents=new Resource[contents.length];
        for (int i=0;i<contents.length;i++)
        { _contents[i]=new FileResource(contents[i].toURI());
        }
      }
      else
      { _contents=null;
      }
    }
    catch (UnresolvableURIException x)
    { x.printStackTrace();
    }
  }
  
  public boolean exists()
  { return _file.exists();
  }
}
