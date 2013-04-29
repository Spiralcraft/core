package spiralcraft.bundle;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import spiralcraft.classloader.JarArchive;
import spiralcraft.classloader.Loader;
import spiralcraft.classloader.ResourceArchive;
import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.bundle.BundleResource;
import spiralcraft.vfs.file.FileResource;


/**
 * <p>ClassLoader implementation to load classes from a bundle library
 * </p>
 * 
 * @author mike
 */
public class BundleClassLoader
  extends Loader
{

  public BundleClassLoader(String[] classBundles,String[] jarLibraryBundles) 
    throws IOException
  { 
    super(Thread.currentThread().getContextClassLoader());
    
    for (String classBundle:classBundles)
    {
      Resource classResource
        =new BundleResource
            (URI.create("bundle://"+classBundle+"/"));
      
      addPrecedentArchive
        (new ResourceArchive
          (classResource
          )
        );
    }
    
    for (String jarBundle:jarLibraryBundles)
    {
      
      Container libResource
        =new BundleResource
            (URI.create("bundle://"+jarBundle+"/"))
        .asContainer();
      if (libResource==null)
      { throw new IOException("Bundle '"+jarBundle+"' not found");
      }
      
      for (Resource res: libResource.listContents())
      {
        String localName=res.getLocalName();
        if (localName.endsWith(".jar"))
        { 
          File jarFile=File.createTempFile
            (jarBundle+"."
            +localName.split("\\.")[0]
            ,".jar"
            );
          jarFile.deleteOnExit();
          FileResource jarFileResource=new FileResource(jarFile);
          jarFileResource.copyFrom(res);
          addPrecedentArchive(new JarArchive(jarFileResource));
        }
      }
    }
  }
}