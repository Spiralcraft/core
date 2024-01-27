package spiralcraft.lang.functions;

import java.rmi.AccessException;
import java.time.Instant;
import java.util.HashMap;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflectable;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.UnaryFunctionBinding;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.GenericReflector;
import spiralcraft.lang.spi.SimpleChannel;
import java.util.Map;

/**
 * Maintains a key-value mapping with facilities to coordinate with
 *   backing storage.
 *   
 * @author mike
 * 
 * XXX:WIP
 */
public class CacheMap<K,V>
  implements Contextual,Reflectable<CacheMap<K,V>>
{

  private GenericReflector<CacheMap<K,V>> selfReflector;
  private Reflector<K> keyReflector;
  private Reflector<V> valueReflector;

  private Binding<K> keyFnBinding;
  
  private UnaryFunctionBinding<V,K,Exception> keyFn;
  private UnaryFunctionBinding<V,V,Exception> storeFn;
  private UnaryFunctionBinding<K,V,Exception> fetchFn;
  
  private Channel<Map<K,MapEntry<K,V>>> map;
  
  
  public CacheMap
    (Reflector<K> keyReflector
    ,Reflector<V> valueReflector
    ,Binding<K> keyFnBinding
    )
    
  { 
    this.keyReflector=keyReflector;
    this.valueReflector=valueReflector;
    this.keyFnBinding=keyFnBinding;
  }
  
  
  
  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws ContextualException
  {
    selfReflector
      =new GenericReflector<CacheMap<K,V>>
        (BeanReflector.<CacheMap<K,V>>getInstance(getClass()));
    selfReflector.enhance("get", new Reflector<?>[] { keyReflector }, valueReflector);
    selfReflector.enhance("put", new Reflector<?>[] { valueReflector }, valueReflector);

    if (keyFnBinding==null)
    { throw new BindException("CacheMap requires a KeyFn (key function)");
    }
    this.keyFn=new UnaryFunctionBinding<V,K,Exception>(keyFnBinding);
    this.keyFn.setInputReflector(valueReflector);

    keyFn.bind(focusChain);

    if (this.storeFn!=null)
    { this.storeFn.bind(focusChain);
    }
    
    if (this.fetchFn!=null)
    { this.fetchFn.bind(focusChain);
    }
    
    map=new SimpleChannel<Map<K,MapEntry<K,V>>>(new HashMap<K,MapEntry<K,V>>(),true);
    return focusChain;
  }

  
  @Override
  public Reflector<CacheMap<K,V>> reflect()
    throws BindException
  { 
    // XXX Init the selfReflector and infer key and value types 
    return selfReflector;
  }

  public void setStoreFn(Binding<V> storeFn)
  {
    this.storeFn=new UnaryFunctionBinding<V,V,Exception>(storeFn);
    this.storeFn.setInputReflector(valueReflector);
  }

  public void setFetchFn(Binding<V> fetchFn)
  {
    this.fetchFn=new UnaryFunctionBinding<K,V,Exception>(fetchFn);
    this.fetchFn.setInputReflector(keyReflector);
  }
  
  public synchronized V get(K key)
    throws AccessException
  { 
    Map<K,MapEntry<K,V>> map=this.map.get();
    MapEntry<K,V> entry=map.get(key);
    if (entry==null)
    { 
      if (fetchFn!=null)
      { 
        try
        {
          V value=fetchFn.evaluate(key);
          if (value!=null)
          {
            key=keyFn.evaluate(value);
            map.put(key,new MapEntry<K,V>(key,value));
            return value;
          }
        }
        catch (Exception x)
        { throw new AccessException("Error retrieving value",x);
        }
      }
    }
    else
    { return entry.value;
    }
    return null;
  }

  public synchronized V put(V value)
    throws AccessException
  { 
    Map<K,MapEntry<K,V>> map=this.map.get();
    
    try
    {
      if (storeFn!=null)
      { value=storeFn.evaluate(value);
      }
      K key=keyFn.evaluate(value);
      map.put(key,new MapEntry<K,V>(key,value));
    }
    catch (Exception x)
    { throw new AccessException("Error retrieving value",x);
    }

    
    return value;
  }
}

class MapEntry<K,V>
{
  final K key;
  final V value;
  final Instant cachedTime=Instant.now();

  MapEntry(K key, V value)
  { 
    this.key=key;
    this.value=value;
  }
  
}