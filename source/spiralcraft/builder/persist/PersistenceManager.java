package spiralcraft.builder.persist;

import spiralcraft.tuple.Tuple;

import spiralcraft.tuple.sax.TupleReader;

import spiralcraft.sax.ParseFactory;

import spiralcraft.builder.BuildException;
import spiralcraft.builder.PersistenceException;

import java.net.URI;

import java.util.List;

/**
 * Manages the lifecycle of PersistentReferences.
 */
public class PersistenceManager
{
  private final AssemblyClassSchemeResolver resolver
    =new AssemblyClassSchemeResolver();
    
  /**
   * Activate the PersistentReference stored at the given location 
   */
  public PersistentReference activate(URI storeName)
    throws BuildException
  { 
    try
    {
      // XXX The PersistenceManager should be instantiated with a
      // XXX   Store which abstracts the storage mechanism (ie. this
      // XXX   method should not know about XML. 
      TupleReader reader=new TupleReader(resolver,null);
      new ParseFactory().parseURI
        (storeName
        ,reader
        );
      
      List<Tuple> list=reader.getTupleList();
      if (list.size()>0)
      { return new PersistentReference(list.get(0));
      }
      else
      { throw new PersistenceException("No object data in "+storeName);
      }
    }
    catch (BuildException x)
    { throw x;
    }
    catch (Throwable x)
    { throw new PersistenceException("Error resolving object URI "+storeName+":",x);
    }    
  }
  
  /**
   * Deactive the specified PersistentReference. The persistence data
   *   will be flushed and the reference will no longer be accessible.
   */
  public void deactivate(PersistentReference reference)
  {
  }

}
