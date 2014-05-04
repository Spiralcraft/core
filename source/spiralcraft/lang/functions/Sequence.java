//
// Copyright (c) 2010 Michael Toth
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
package spiralcraft.lang.functions;

import java.lang.reflect.Array;
import java.util.ArrayList;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Channel;
import spiralcraft.lang.ChannelFactory;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.kit.Callable;
import spiralcraft.lang.reflect.ArrayReflector;
import spiralcraft.lang.reflect.CollectionReflector;
import spiralcraft.lang.spi.SourcedChannel;

/**
 * Generates a sequence of values based on an initial value, a generator
 *   function, and a termination function.
 *   
 * 
 * @author mike
 *
 * @param <T>
 */
public class Sequence<T,Tsource>
  implements ChannelFactory<T[],Tsource>

{

  private Reflector<T> elementType;
  private Expression<T> generator;
  private Expression<Boolean> condition;
  
  public Sequence
    (Reflector<T> elementType
    ,Expression<T> generator
    ,Expression<Boolean> condition
    )
  { 
    this.elementType=elementType;
    this.generator=generator;
    this.condition=condition;
  }
  
  @Override
  public Channel<T[]> bindChannel(
    Channel<Tsource> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  {
    Reflector<T[]> resultReflector
      =ArrayReflector.getInstance(elementType);
    return new SequenceChannel
      (source,resultReflector,focus);
  }
  
  public class SequenceChannel
    extends SourcedChannel<Tsource,T[]>
  {
    private final Callable<ArrayList<T>,Boolean> conditionFn;
    private final Callable<ArrayList<T>,T> generatorFn;
    
    public SequenceChannel
      (Channel<Tsource> source
      ,Reflector<T[]> resultReflector
      ,Focus<?> focus
      ) 
       throws BindException
    { 
      super(resultReflector,source);
      
      if (focus.getSubject()!=source)
      { focus=focus.telescope(source);
      }
      CollectionReflector<ArrayList<T>,T> collectionReflector
        =CollectionReflector.<ArrayList<T>,T>getInstance
          (ArrayList.class,elementType);
      
      generatorFn
        =new Callable<ArrayList<T>,T>
          (focus
          ,collectionReflector
          ,new Binding<T>(generator)
          );
      conditionFn
        =new Callable<ArrayList<T>,Boolean>
          (focus
          ,collectionReflector
          ,new Binding<Boolean>(condition)
          );
    }
    
    @Override
    public boolean isWritable()
    { return false;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected T[] retrieve()
    { 
      
      ArrayList<T> resultList=new ArrayList<T>();
      while (Boolean.TRUE.equals(conditionFn.evaluate(resultList)))
      { resultList.add(generatorFn.evaluate(resultList));
      }
      return resultList.toArray
        ((T[]) Array.newInstance(elementType.getContentType(),resultList.size()));
    }

    @Override
    protected boolean store(
      T[] val)
      throws AccessException
    { return false;
    }
  }

  
}
