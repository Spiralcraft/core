//
// Copyright (c) 2009 Michael Toth
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
package spiralcraft.lang.spi;

import java.net.URI;
import java.util.HashMap;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.ChannelFactory;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.Signature;
import spiralcraft.log.ClassLog;

/**
 * A Reflector that can be programmatically defined at run-time
 * 
 * @author mike
 *
 * @param <T>
 */
public class GenericReflector<T>
  extends AbstractReflector<T>
{
  private static final ClassLog log
    =ClassLog.getInstance(GenericReflector.class);
  
  protected final Class<T> contentType;
  protected final URI typeURI;
  protected HashMap<Signature,ChannelFactory<?,?>> enhancerMap;
  private boolean debug;

  public GenericReflector(URI typeURI,Class<T> contentType)
  {
    this.contentType=contentType;
    this.typeURI=typeURI;
  }
  
  public GenericReflector(URI typeURI,Reflector<T> base)
  { 
    super(base);
    contentType=base.getContentType();
    this.typeURI=base.getTypeURI();
  }
  
  public GenericReflector(Reflector<T> base)
  { this(base.getTypeURI(),base);
  }

  public void setDebug(boolean debug)
  { this.debug=debug;
  }

  @Override
  public <D extends Decorator<T>> D decorate(
    Channel<T> source,
    Class<D> decoratorInterface)
    throws BindException
  {
    if (base!=null)
    { return base.<D>decorate(source,decoratorInterface);
    }
    return null;
  }

  @Override
  public Reflector<?> disambiguate(
    Reflector<?> alternate)
  {
    if (base!=null)
    { 
      Reflector<?> r=base.disambiguate(alternate);
      if (r==base)
      { return this;
      }
      else
      { return r;
      }
    }
    return this;
  }


  @SuppressWarnings("unchecked")
  @Override
  public Class<T> getContentType()
  {
    if (contentType!=null)
    { return contentType;
    }
    if (base!=null)
    { return base.getContentType();
    }
    
    return (Class<T>) Void.class;
  }

  @Override
  public URI getTypeURI()
  {
    if (typeURI!=null)
    { return typeURI;
    }
    if (base!=null)
    { return base.getTypeURI();
    }
    return null;
  }

  @Override
  public boolean isAssignableTo(
    URI typeURI)
  {
    if (debug)
    { log.fine(""+typeURI+"  this:"+this.typeURI);
    }
    if (base!=null)
    { return base.isAssignableTo(typeURI);
    }
    else if (typeURI.equals(this.typeURI))
    { return true;
    }
    else
    { return false;
    }
  }

  private void ensureEnhancerMap()
  { 
    if (enhancerMap==null)
    { enhancerMap=new HashMap<Signature,ChannelFactory<?,?>>();
    }
  }
  
  private class AspectFactory<X>
    implements ChannelFactory<X,X>
  {
    private final Reflector<X> enhancer;
    
    public AspectFactory(Reflector<X> enhancer)
    { this.enhancer=enhancer;
    }
    
    @Override
    public Channel<X> bindChannel(
      Channel<X> source,
      Focus<?> focus,
      Expression<?>[] arguments)
      throws BindException
    { return new AspectChannel<X>(enhancer,source);
    }
  }
  
  /**
   * <p>Provide an enhanced reflector for the return value of the specified
   *   signature
   * </p>
   * 
   * @param <X>
   * @param name
   * @param params
   * @param enhancer
   */
  public <X> void enhance(String name,final Reflector<X> enhancer)
  { 
    ensureEnhancerMap();
    enhancerMap.put
      (new Signature(name,null)
      ,new AspectFactory<X>(enhancer)
      );
  }
  
  /**
   * <p>Provide an enhanced reflector for the return value of the specified
   *   signature
   * </p>
   * 
   * @param <X>
   * @param name
   * @param params
   * @param enhancer
   */
  public <X> void enhance
    (String name
    ,Reflector<?>[] params
    ,final Reflector<X> enhancer
    )
  { enhance(name,params,new AspectFactory<X>(enhancer));
  }
  
  public <X> void enhance
    (String name
    ,Reflector<?>[] params
    ,ChannelFactory<X,X> aspect
    )
  {
    ensureEnhancerMap();
    enhancerMap.put
      (new Signature(name,null,params)
      ,aspect
      );
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public <X> Channel<X> resolve(
    Channel<T> source,
    Focus<?> focus,
    String name,
    Expression<?>[] params)
    throws BindException
  {
    Channel<X> result=null;
    if (name.startsWith("@"))
    { result= resolveMeta(source,focus,name,params);
    }
    if (base!=null)
    { result= base.resolve(source,focus,name,params);
    }
    
    if (result!=null && enhancerMap!=null)
    { 

      
      if (params==null)
      { 
        ChannelFactory<X,X> functor
          =(ChannelFactory<X,X>) enhancerMap.get(new Signature(name,null));
        if (functor!=null)
        { 
          result=functor.bindChannel
            ( result,focus, null);
        }
      }
      else if (params.length==0)
      {
        ChannelFactory<X,X> functor
          =(ChannelFactory<X,X>) enhancerMap.get
            (new Signature(name,null,new Reflector[0]));
        if (functor!=null)
        { 
          result=functor.bindChannel( result, focus, new Expression<?>[0]);
          
        }
      }
      else
      {
        Channel[] chans=new Channel[params.length];
        Reflector[] sig=new Reflector[chans.length];
        int i=0;
        for (Expression e : params)
        { 
          chans[i]=focus.bind(e);
          sig[i]=chans[i].getReflector();
          i++;
        }
        
        ChannelFactory<X,X> functor
          =(ChannelFactory<X,X>) enhancerMap.get
            (new Signature(name,null,sig));
        if (functor!=null)
        { 
          result=functor.bindChannel( result,focus, params);
          
        }
        
      }      
    }
    // log.fine("GenericReflector resolved "+name+" to "+result);
    return result;
  }

  @Override
  public String toString()
  { return super.toString()+(base!=null?" base="+base.toString():"");
  }

}
