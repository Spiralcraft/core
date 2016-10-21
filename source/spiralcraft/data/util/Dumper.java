package spiralcraft.data.util;

import java.io.IOException;
import java.net.URI;

import spiralcraft.data.Aggregate;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.access.Snapshot;
import spiralcraft.data.access.Store;
import spiralcraft.data.sax.DataWriter;
import spiralcraft.util.URIUtil;
import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;

public class Dumper
{

  
  public void dump(URI uri,Store[] stores)
      throws IOException,DataException
  { dump(Resolver.getInstance().resolve(uri),stores);
  }

  public void dump(Resource resource,Store[] stores)
      throws IOException,DataException
  {
    Container container=resource.ensureContainer();
    for (Store store: stores)
    { dump(container.ensureChildContainer(store.getName()),store);
    }
      
  }
  
  public void dump(Container resource,Store store)
    throws IOException,DataException
  {
    Snapshot snapshot=store.snapshot(0);
    for (Aggregate<Tuple> aggregate:snapshot.getData())
    { 
      String typeName
        =URIUtil.unencodedLocalName
          (aggregate.getType().getCoreType().getURI());
      dump(resource.getChild(typeName+".xml"),aggregate);
      
    }
  }
  
  public void dump(Resource resource,Aggregate<Tuple> data)
    throws IOException,DataException
  { new DataWriter().writeToResource(resource,data);
  }

}
