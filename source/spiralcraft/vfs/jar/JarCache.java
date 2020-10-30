package spiralcraft.vfs.jar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import spiralcraft.io.InputStreamWrapper;
import spiralcraft.log.ClassLog;
import spiralcraft.time.Clock;
import spiralcraft.util.string.StringPool;

/**
 * Caches information about jar files for sharing by multiple JarFileResource.
 * 
 * 
 * @author mike
 *
 */
public class JarCache
{
  private static final ClassLog log=ClassLog.getInstance(JarCache.class);
  private static final HashMap<String,JarCache> fileCache = new HashMap<>();
  
  public static final JarCache get(File file)
  {
    String absPath=StringPool.INSTANCE.get(file.getAbsolutePath());
    JarCache jarCache=fileCache.get(absPath);
    if (jarCache==null)
    {
      synchronized (fileCache)
      {
        jarCache=fileCache.get(absPath);
        if (jarCache==null)
        { 
          jarCache=new JarCache(absPath);
          fileCache.put(absPath,jarCache);
        }
      }
      
    }
    return jarCache;
  }
  
  private final File file;
  private volatile JarFile jarFile;
  private long lastModified=0;
  private final LinkedList<JarEntry> entryList=new LinkedList<JarEntry>();
  private final HashMap<String,JarEntry> entries=new HashMap<String,JarEntry>();
  private final HashMap<String,JarEntry> dirEntries=new HashMap<String,JarEntry>();
  private int openCount;
  private long lastChecked=0;
  
  JarCache(String absPath)
  { this.file=new File(absPath);
  }

  private synchronized void check()
    throws IOException
  {
    long time=Clock.instance().approxTimeMillis();
    if (openCount==0 
       && time-lastChecked > 5000 
       && ( lastModified==0
           || file.lastModified()>lastModified
         )
       )
    {
      lastModified=file.lastModified();
      entryList.clear();
      entries.clear();
      dirEntries.clear();
      openCountInc();
      try
      {
        
        Enumeration<JarEntry> en=jarFile.entries();
        while (en.hasMoreElements())
        { 
          JarEntry entry=en.nextElement();
          entryList.add(entry);
          String entryName=entry.getName();
          entries.put(entryName, entry);
          if (entryName.endsWith("/"))
          { dirEntries.put(entryName.substring(0,entryName.length()-1), entry);
          }
          // log.fine(file+"  +>  "+entry.getName());
        }    
      }
      finally
      { openCountDec();
      }
      lastChecked=time;
    }
  }
  
  private synchronized void openCountInc()
    throws IOException
  { 
    // log.fine(file+" openCount="+openCount);
    if (openCount==0)
    { 
      log.fine("Opening "+file);
      jarFile=new JarFile(file);
    }
    openCount++;
  }

  private synchronized void openCountDec()
    throws IOException
  { 
    openCount--;
    if (openCount==0)
    { 
      jarFile.close();
      jarFile=null;
      // log.fine("Closed "+file);
    }
    if (openCount<0)
    { throw new IllegalStateException(file+" openCount="+openCount);
    }
  }
  
  public JarEntry getJarEntry(String path)
    throws IOException
  {
    check();
    JarEntry entry=entries.get(path);
    if (entry==null)
    { entry=dirEntries.get(path);
    }
    return entry;
  }
  
  public synchronized Enumeration<JarEntry> entries()
    throws IOException
  { 
    check();
    return Collections.enumeration(entryList);
  }
  
  public synchronized InputStream getInputStream(JarEntry entry)
    throws IOException
  {
    check();
    openCountInc();
    if (jarFile==null)
    { log.fine(file+" is closed. openCount="+openCount);
    }
    InputStream ret=
      new InputStreamWrapper(jarFile.getInputStream(entry))
      {
        private boolean closed;
        
        @Override
        public void close()
          throws IOException
        { 
          if (!closed)
          {
            closed=true;
          
            try
            { super.close();
            }
            finally
            { openCountDec();
            }
          }
        } 
      };
    return ret;
  }
}

