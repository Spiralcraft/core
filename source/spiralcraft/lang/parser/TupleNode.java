//
// Copyright (c) 1998,2008 Michael Toth
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
package spiralcraft.lang.parser;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.Signature;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;

public class TupleNode
  extends Node
{

  
  private final LinkedHashMap<String,TupleField> fields
    =new LinkedHashMap<String,TupleField>();
  
  private TypeFocusNode typeNode;
  private Node baseExtentNode;
  
  public TupleNode()
  { 
  }

  @Override
  public Node[] getSources()
  { 
    ArrayList<Node> ret=new ArrayList<Node>();
    for (TupleField field:fields.values())
    { 
      if (field.type!=null)
      { ret.add(field.type);
      }
      if (field.source!=null)
      { ret.add(field.source);
      }
    }
    if (typeNode!=null)
    { ret.add(typeNode);
    }
    if (baseExtentNode!=null)
    { ret.add(baseExtentNode);
    }
    return ret.toArray(new Node[ret.size()]);
  }
  
  @Override
  public Node copy(Object visitor)
  {
    TupleNode copy=new TupleNode();
    
    for (TupleField field: fields.values())
    {
      copy.addField(field.copy(visitor));
    }
    if (typeNode!=null)
    { copy.setType((TypeFocusNode) typeNode.copy(visitor));
    }
    if (baseExtentNode!=null)
    { copy.setBaseExtentNode(baseExtentNode.copy(visitor));
    }
    return copy;
  }
  
  @Override
  public String reconstruct()
  { 
    StringBuilder builder=new StringBuilder();
    builder.append(" { ");
    
    if (typeNode!=null)
    { builder.append(" : "+typeNode.reconstruct());
    }
    
    if (baseExtentNode!=null)
    { builder.append(" := "+baseExtentNode.reconstruct()+" : ");
    }

    boolean first=true;
    for (TupleField field : fields.values())
    { 
      builder.append(" ");
      if (first)
      { first=false;
      }
      else
      { builder.append(" , ");
      }
      builder.append(field.name)
        .append(" : ");
      if (field.type!=null)
      { builder.append(field.type.reconstruct());
      }
      else if (field.source!=null)
      { 
        builder.append(field.passThrough?"~":"=")
          .append(field.source.reconstruct());
      }
    }
    builder.append(" } ");
    return builder.toString();
  }
  
  public void setType(TypeFocusNode typeNode)
  { this.typeNode=typeNode;
  }
  
  public void setBaseExtentNode(Node baseExtentNode)
  { this.baseExtentNode=baseExtentNode;
  }
  
  public void addField(TupleField field)
  { 
    field.index=fields.size();
    if (field.name==null)
    { field.name="_"+field.index;
    }
    fields.put(field.name,field);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Channel<?> bind(final Focus<?> focus)
    throws BindException
  { 
    if (baseExtentNode!=null && fields.isEmpty())
    { 
      // simple reference case
      return focus.bind(new Expression(baseExtentNode));
    }
    else
    { 
      // tuple wrapper case
      return new TupleChannel(new TupleReflector(focus));
    }
  }
  
  
  public class TupleReflector
    extends Reflector<Tuple>
  {
    

    
    final Channel<?>[] channels=new Channel[fields.size()];
    
    final TupleField[] fieldArray
      =fields.values().toArray(new TupleField[fields.size()]);
    
    final Channel<?> sourceChannel;
    
    
    final ThreadLocalChannel<Tuple> thisChannel
      =new ThreadLocalChannel<Tuple>(this);
    
    final URI typeURI;
    final Reflector<?> type;
    
    final Channel<Object> baseChannel;
    
    @SuppressWarnings("unchecked")
    public TupleReflector(Focus<?> focus)
      throws BindException
    {
    
      sourceChannel=focus.getSubject();

      if (typeNode==null)
      { 
        typeURI
          =URI.create
            (sourceChannel.getReflector().getTypeURI()
            +";tuple"+hashCode()
            );
        type=null;
      }
      else
      { 
        type=focus.<Reflector>bind(new Expression<Reflector>(typeNode)).get();
        
        
        typeURI=type.getTypeURI();
      }
      
      
      if (baseExtentNode!=null)
      { baseChannel=focus.bind(new Expression(baseExtentNode));
      }
      else
      { baseChannel=null;
      }
       
      
      // Telescope context for self resolution
      focus=focus.telescope(thisChannel);
        
      // log.fine(" "+sourceChannel.getReflector());
      
      int i=0;
      for (TupleField field: fieldArray)
      { 
        if (field.source!=null)
        { 
//          log.fine("Binding "+field.name+" to "+field.source.reconstruct());
          channels[i]= field.source.bind(focus);
        }
        else if (field.type!=null)
        { 
          channels[i]= new SimpleChannel
            ((Reflector) field.type.bind(focus).get());
        }
        i++;
      }
      
      if (type!=null)
      { checkType();
        
      }
    }

    private void checkType()
      throws BindException
    {
      LinkedList<Signature> signatures=type.getSignatures(thisChannel);
      for (Signature sig: signatures)
      {
        if (!sig.getName().startsWith("@"))
        {
          if (sig.getParameters()!=null)
          { 
            throw new BindException
              ("Tuple is missing implementation of "+typeURI+":"+sig); 
          }
          
          TupleField field=fields.get(sig.getName());
          if (field==null)
          {
            throw new BindException
              ("Tuple is missing property "+typeURI+":"+sig); 
          }
          
          Reflector<?> channelType=channels[field.index].getReflector();
          if (!sig.getType()
                .isAssignableFrom(channelType)
             )
          { 
            throw new BindException
              ("Type "+channelType.getTypeURI()+" for field "+field.name
                +" cannot substitute for "+sig.getType().getTypeURI()
              );
            
          }
        }
      }

    }
    
    @Override
    public LinkedList<Signature> getSignatures(Channel<?> source)
      throws BindException
    {
      LinkedList<Signature> ret=super.getSignatures(source);
      

      if (baseChannel!=null)
      { ret.addFirst(new Signature("@super",baseChannel.getReflector()));
      }
      for (TupleField field:fieldArray)
      { 
        if (channels[field.index]!=null)
        {
          ret.addFirst
            (new Signature
              (field.name,channels[field.index].getReflector()));
        }
      }
      
      if (baseChannel!=null)
      {
        LinkedList<Signature> baseSigs
          =baseChannel.getReflector().getSignatures(baseChannel);
        for (Signature sig:baseSigs)
        { 
          boolean found=false;
          for (Signature retsig:ret)
          { 
            if (retsig.hides(sig))
            { 
              found=true;
              break;
            } 
          }
          if (!found)
          { ret.add(sig);
          }
        }
      }      
      return ret;
    }
    
    public TupleField[] getFields()
    { return fieldArray;
    }
    
    public Tuple newTuple()
    { 
      Object[] data=new Object[channels.length];
      Tuple tuple=new Tuple(this,data,baseChannel!=null?baseChannel.get():null);
      thisChannel.push(tuple);
      try
      {
        int i=0;
        for (TupleField field: fieldArray)
        { 
          if (field.source!=null && !field.passThrough)
          { data[i]=channels[i].get();
          }
          i++;
        }
      }
      finally
      { thisChannel.pop();
      }
      // log.fine("Created tuple with data "+ArrayUtil.format(data,",","|"));
      return tuple;
    }
    
    @SuppressWarnings("unchecked")
    public boolean update(Tuple val)
    {
      boolean updated=false;
      if (val==null)
      {
        for (Channel<?> channel: channels)
        { 
          if (channel.isWritable())
          { 
            channel.set(null);
            updated=true;
          }
        }
      }
      else
      {
        if (val.data.length>channels.length)
        { 
          throw new AccessException
            ("Supplied Tuple is larger ("+val.data.length+")"
            +" than the bound field list ("+channels.length+")"
            );
        }
          
        int i=0;
        for (Channel channel: channels)
        { 
          if (channel.isWritable())
          {
            if (i<val.data.length)
            { channel.set(val.data[i]);
            }
            else
            { channel.set(null);
            }
          }
          i++;
        }
          
      }
      return updated;
    }
    
    public boolean isWritable()
    {
      for (Channel<?> channel: channels)
      { 
        if (channel.isWritable())
        { return true;
        }
      }
      return false;
    }

    @Override
    public <D extends Decorator<Tuple>> D decorate(
      Channel<Tuple> source,
      Class<D> decoratorInterface)
      throws BindException
    { return null;
    }

    @Override
    public Class<Tuple> getContentType()
    { return Tuple.class;
    }

    @Override
    public URI getTypeURI()
    { return typeURI;
    }
    
    @Override
    public boolean isAssignableTo(
      URI typeURI)
    { 
      if (baseChannel!=null)
      { 

        // Allow this Reflector to be returned when a Focus for the base
        //  channel is requested        
        return baseChannel.getReflector().isAssignableTo(typeURI);
      }
      return false;
    }
    

    @SuppressWarnings("unchecked")
    @Override
    public <X> Channel<X> resolveMeta(
      final Channel<Tuple> source,
      Focus<?> focus,
      String name,
      Expression<?>[] params)
      throws BindException
    { 
      if (name.equals("@super"))
      { 
        Channel<Object> baseExtentChannel
          =source.getCached("@super");
        if (baseExtentChannel==null)
        {
          baseExtentChannel=new BaseExtentChannel(source);
          source.cache("@super",baseExtentChannel);
        }
        return (Channel) baseExtentChannel;
      }
      return super.resolveMeta(source,focus,name,params);
    
    }    

    @SuppressWarnings("unchecked")
    @Override
    public <X> Channel<X> resolve(
      final Channel<Tuple> source,
      Focus<?> focus,
      String name,
      Expression<?>[] params)
      throws BindException
    {
      if (name.startsWith("@"))
      { return resolveMeta(source,focus,name,params);
      }
        
      if (params==null)
      {
        
        final TupleField field=fields.get(name);
       
       if (field!=null)
       {
          final Channel target=channels[field.index];
        
          if (!field.passThrough)
          { return (Channel<X>) new FieldChannel(field,source,target);
          }
          else
          { return (Channel<X>) new PassThroughChannel(field,source,target);
          }
        }
      }
      
      if (baseChannel!=null)
      { 
        
        return baseChannel.getReflector().resolve
          (new BaseExtentChannel(source), focus, name, params);
      }
      return null;
    }
    
    class BaseExtentChannel
      extends AbstractChannel<Object>
    {
      private final Channel<Tuple> source;
      
      public BaseExtentChannel(Channel<Tuple> source)
      { 
        super(baseChannel.getReflector());
        this.source=source;
      }
      
      @Override
      protected Object retrieve()
      { return source.get().baseExtent;
      }    
      
      @Override
      protected boolean store(Object val)
      { return false;
      }
    }
    
    class FieldChannel
      extends AbstractChannel<Object>
    {
      private final Channel<Tuple> source;
      private final Channel<Object> target;
      // private final TupleField field;
      private final int index;
      
      public FieldChannel
        (final TupleField field
        ,final Channel<Tuple> source
        ,final Channel<Object> target
        )
      { 
        super(target.getReflector());
        // this.field=field;
        this.source=source;
        this.target=target;
        this.index=field.index;
      }

      @Override
      protected Object retrieve()
      { return source.get().data[index];
      }

      @Override
      protected boolean store(
        Object val)
        throws AccessException
      { 
        // log.fine("Store "+val+" to tuple field "+field.name);
        if (target.isWritable())
        { 
          source.get().data[index]=val;
          return true;
        }
        // log.fine(field.name+" is not writable");
        return false;
      }
            
      @Override
      public boolean isWritable()
      { return target.isWritable();
      }
      
    }
    
    
    class PassThroughChannel
      extends AbstractChannel<Object>
    {
      private final Channel<Tuple> source;
      private final Channel<Object> target;
      // private final TupleField field;
      
      public PassThroughChannel
        (final TupleField field
        ,final Channel<Tuple> source
        ,final Channel<Object> target
        )
      { 
        super(target.getReflector());
        // this.field=field;
        this.source=source;
        this.target=target;
      }
      
      @Override
      protected Object retrieve()
      { 
        thisChannel.push(source.get());
        try
        { return target.get();
        }
        finally
        { thisChannel.pop();
        }
      }

      @Override
      protected boolean store(
        Object val)
          throws AccessException
      { 
        // log.fine("Store "+val+" to tuple field "+field.name);              
        thisChannel.push(source.get());
        try
        { 
          if (target.isWritable())
          { return target.set(val);
          }
          return false;
        }
        finally
        { thisChannel.pop();
        }              
      }
            
      @Override
      public boolean isWritable()
      { return target.isWritable();
      }

    }
    
  }
  
  
  class TupleChannel
    extends AbstractChannel<Tuple>
  {

    private final TupleReflector reflector;
    
    public TupleChannel(TupleReflector reflector)
    { super(reflector);
      this.reflector=reflector;
    
    } 


    @Override
    protected Tuple retrieve()
    { return reflector.newTuple();
    }
    
      @Override
    protected boolean store(Tuple val)
      throws AccessException
    { return reflector.update(val);
    }
      
    @Override
    public boolean isWritable()
    { return reflector.isWritable();
    }
      
    

  }
  
  @Override
  public void dumpTree(StringBuffer out,String prefix)
  {
    out.append(prefix).append("Tuple: ");
    prefix=prefix+"  ";

    if (fields!=null)
    {
      out.append(prefix).append(" { ");
      
      
      if (typeNode!=null)
      { 
        out.append(prefix).append(":");
        typeNode.dumpTree(out,prefix);
      }
      
      if (baseExtentNode!=null)
      { 
        out.append(prefix).append(":=");
        typeNode.dumpTree(out,prefix);
      }

      boolean first=true;
      for (TupleField field : fields.values())
      { 
        if (!first)
        { out.append(prefix).append(",");
        }
        else
        { first=false;
        }
        field.dumpTree(out,prefix);
      }
      out.append(prefix).append(" } ");
    }
  }
  
  @Override
  public String toString()
  { return super.toString()+"{"+fields.toString()+"}";
  }

}
