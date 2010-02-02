package spiralcraft.data.access;

import java.net.URI;

import spiralcraft.data.Aggregate;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.TypeResolver;

public interface Snapshot
{
  public static final URI TYPE_URI
    =URI.create("class:/spiralcraft/data/access/snapshot");
  
  public static final Type<Snapshot> TYPE
    =TypeResolver.getTypeResolver().resolveSafeFromClass(Snapshot.class);
  
  public long getTransactionId();
  
  public Aggregate<Aggregate<Tuple>> getData();
}
