package spiralcraft.log;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

import spiralcraft.common.LifecycleException;
import spiralcraft.io.FileSequence;
import spiralcraft.io.RotatingFileOutputAgent;
import spiralcraft.io.TimestampFileSequence;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;
import spiralcraft.vfs.file.FileResource;

public class RotatingFileSink
  extends LogSink
{
  final RotatingFileOutputAgent out=new RotatingFileOutputAgent();
  private URI location;
  private String baseFilename;
  private String suffix;
  private Charset charset=Charset.forName("UTF8");
  private String lineSeparator="\r\n";
  
  public void setLocation(URI location)
  { this.location=location;
  }
  
  public void setBaseFilename(String baseFilename)
  { this.baseFilename=baseFilename;
  }
  
  
  @Override
  public void start()
    throws LifecycleException
  { 
    FileSequence fileSequence=new TimestampFileSequence();
    Resource dir;
    try
    { dir = Resolver.getInstance().resolve(location);
    }
    catch (UnresolvableURIException x)
    { throw new LifecycleException("Can't resolve log sink URI "+location,x);
    }
    FileResource fileResource=dir.unwrap(FileResource.class);
    if (fileResource==null)
    { throw new LifecycleException("Not resolvable to a file: "+location);
    }
    File file=fileResource.getFile();
    File directory;
    if (!file.isDirectory())
    { 
      // We're specifying the full base name with prefix and suffix
      directory=file.getParentFile();
      String filename=file.getName();
      int dot=filename.lastIndexOf(".");
      if (dot>=0)
      {
        if (baseFilename==null)
        { baseFilename=filename.substring(0,dot);
        }
        if (suffix==null)
        { suffix=filename.substring(dot+1);
        }
        
      }
      else
      { 
        if (baseFilename==null)
        { baseFilename=filename;
        }
      }
      
    }
    else
    { 
      directory=file;
      if (baseFilename==null)
      { baseFilename=name;
      }
    }
    if (suffix==null)
    { suffix=".log";
    }
    fileSequence.setDirectory(directory);
    fileSequence.setPrefix(baseFilename);
    fileSequence.setSuffix(suffix);

    out.setAsyncIO(false);
    out.setFileSequence(fileSequence);
    
    super.start();
    out.start();
  }

  @Override
  public void stop()
    throws LifecycleException
  { 
    out.stop();
    super.stop();
  }

  public void write(String line)
    throws IOException
  { out.write( (line+lineSeparator).getBytes(charset));
  }
  
}