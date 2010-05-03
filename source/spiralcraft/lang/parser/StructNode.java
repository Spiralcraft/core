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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import spiralcraft.common.namespace.PrefixResolver;
import spiralcraft.lang.AccessException;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.Signature;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.lang.spi.AbstractFunctorChannel;
import spiralcraft.lang.spi.AbstractReflector;
import spiralcraft.lang.spi.AspectChannel;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.util.ArrayUtil;

public class StructNode
  extends Node
{

  
  private final LinkedHashMap<String,StructField> fields
    =new LinkedHashMap<String,StructField>();
  
  private Node baseExtentNode;
  
  private URI typeURI;
  private String typeNamespace;
  private String typeName;
  
  public StructNode()
  { 
  }

  @Override
  public Node[] getSources()
  { 
    ArrayList<Node> ret=new ArrayList<Node>();
    for (StructField field:fields.values())
    { 
      if (field.type!=null)
      { ret.add(field.type);
      }
      if (field.source!=null)
      { ret.add(field.source);
      }
    }

    if (baseExtentNode!=null)
    { ret.add(baseExtentNode);
    }
    return ret.toArray(new Node[ret.size()]);
  }
  
  @Override
  public Node copy(Object visitor)
  {
    StructNode copy=new StructNode();
    boolean dirty=false;
    
    if (this.typeURI!=null)
    { 
      copy.setTypeURI(typeURI);
      copy.setTypeNamespace(typeNamespace);
      copy.setTypeName(typeName);
    }
    else if (visitor instanceof PrefixResolver && typeName!=null)
    {
      URI uri=resolveQName(typeNamespace,typeName,(PrefixResolver) visitor);
      if (uri!=null)
      { 
        copy.setTypeURI(uri);
        dirty=true;
      }
      else
      {
        
        copy.setTypeNamespace(typeNamespace);
        copy.setTypeName(typeName);        
      }
    }
    else
    { 
      copy.setTypeNamespace(typeNamespace);
      copy.setTypeName(typeName);
    }
    
    for (StructField field: fields.values())
    {
      StructField fieldCopy=field.copy(visitor);
      copy.addField(fieldCopy);
      if (fieldCopy!=field)
      { dirty=true;
      }
    }

    if (baseExtentNode!=null)
    { 
      copy.setBaseExtentNode(baseExtentNode.copy(visitor));
      if (copy.baseExtentNode!=baseExtentNode)
      { dirty=true; 
      }
    }
    
    if (!dirty)
    { return this;
    }
    return copy;
  }
  
  @Override
  public String reconstruct()
  { 
    StringBuilder builder=new StringBuilder();
    builder.append(" { ");
    
    if (typeName!=null)
    { 
      builder.append
        (" [#"+(typeNamespace!=null?typeNamespace+":":"")+typeName+"]");
    }
    else if (typeURI!=null)
    { 
      builder.append(" [#:"+typeURI+"]");
    }
    
    if (baseExtentNode!=null)
    { builder.append(" {= "+baseExtentNode.reconstruct()+" } ");
    }

    boolean first=true;
    for (StructField field : fields.values())
    { 
      builder.append(" ");
      if (first)
      { first=false;
      }
      else
      { builder.append(" , ");
      }
      if (!field.anonymous)
      {
        builder.append(field.name)
          .append(" : ");
        if (field.type!=null)
        { builder.append(field.type.reconstruct());
        }
        if (field.source!=null)
        { 
          builder.append(field.passThrough?"~":"=")
            .append(field.source.reconstruct());
        }
      }
      else if (field.source!=null)
      { builder.append(field.source.reconstruct());
      }
    }
    builder.append(" } ");
    return builder.toString();
  }
  
  
  private void setTypeNamespace(String typeNamespace)
  { this.typeNamespace=typeNamespace;
  }
  
  private void setTypeName(String typeName)
  { this.typeName=typeName;
  }
  
  public Iterable<StructField> getFields()
  { return fields.values();
  }
  
  public void setTypeQName(String qname)
  {     
    int colonPos=qname.indexOf(':');
    if (colonPos==0)
    { 
      this.typeURI=URI.create(qname.substring(1));
      this.typeNamespace=null;
      this.typeName=null;
    }
    else if (colonPos>0)
    {
      this.typeNamespace=qname.substring(0,colonPos);
      this.typeName=qname.substring(colonPos+1);
      this.typeURI=resolveQName(typeNamespace,typeName);
    }
    else
    { 
      this.typeNamespace=null;
      this.typeName=qname;
      this.typeURI=resolveQName(typeNamespace,typeName);
    }
    
  }
  
  public void setTypeURI(URI typeURI)
  { this.typeURI=typeURI;
  }
  
  public void setBaseExtentNode(Node baseExtentNode)
  { this.baseExtentNode=baseExtentNode;
  }
  
  public void addField(StructField field)
  { 
    field.index=fields.size();
    if (field.name==null)
    { 
      field.name="_"+field.index;
      field.anonymous=true;
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
      // struct wrapper case
      return new StructChannel(new StructReflector(focus));
    }
  }
  
  
  public class StructReflector
    extends AbstractReflector<Struct>
  {
    

    
    private final Channel<?>[] channels=new Channel[fields.size()];
    
    private final StructField[] fieldArray
      =fields.values().toArray(new StructField[fields.size()]);
    
//    private final Channel<?> sourceChannel;
    
    
    private final ThreadLocalChannel<Struct> thisChannel
      =new ThreadLocalChannel<Struct>(this);
    
    private final URI typeURI;
    
    private final Channel<Object> baseChannel;
    
    private final Reflector<Object> iterableItemReflector;
    
    
    @SuppressWarnings("unchecked")
    public StructReflector(Focus<?> focus)
      throws BindException
    {
    
//      sourceChannel=focus.getSubject();
      functor=true;
      
      if (StructNode.this.typeURI==null)
      {
        if (typeName!=null)
        { 
          typeURI
            =resolveQName(typeNamespace,typeName,focus.getNamespaceResolver());
          if (typeURI==null)
          { throw new BindException
              ("Unable to resolve URI from qualified name '"
              +(typeNamespace!=null?typeNamespace+":":"")
              +typeName
              );
          }
        
        }
        else
        { 
          typeURI=URI.create
            ("temp:spiralcraft.lang.parser.StructNode-"
            +Integer.toHexString(System.identityHashCode(this))
            );
        }
      }
      else
      { typeURI=StructNode.this.typeURI;
      }
      
    
      
      if (typeURI!=null && focus.findFocus(typeURI)!=null)
      { 
        throw new BindException
          ("Type URI "+typeURI
          +" has already been defined and cannot be duplicated"
          );
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
      
      Reflector iterableItemReflector=null;
      
      int i=0;
      for (StructField field: fieldArray)
      { 
        if (field.source!=null)
        { 
//          log.fine("Binding "+field.name+" to "+field.source.reconstruct());
          channels[i]= field.source.bind(focus);
          if (field.type!=null)
          { 
            // Set up field with a different declared type
            Reflector type=(Reflector) field.type.bind(focus).get();
            if (!type.isAssignableFrom(channels[i].getReflector()))
            { 
              throw new BindException
                ("Type "+type.getTypeURI()
                +" cannot be assigned from expression of type "
                +channels[i].getReflector().getTypeURI()
                );
            }
            channels[i]
              = new AspectChannel
                (type
                ,channels[i]
                );
          }
        }
        else if (field.type!=null)
        { 
          channels[i]= new SimpleChannel
            ((Reflector) field.type.bind(focus).get());
        }
        
        if (iterableItemReflector==null)
        { iterableItemReflector=channels[i].getReflector();
        }
        else
        { 
          iterableItemReflector
            =iterableItemReflector.getCommonType
              (channels[i].getReflector());
        }
        
        i++;
      }
      
      if (iterableItemReflector!=null)
      { this.iterableItemReflector=iterableItemReflector;
      }
      else
      { this.iterableItemReflector=BeanReflector.getInstance(Void.class);
      }
    }

    
    /**
     * <p>Determine if a value described by the specified Reflector
     *   can be assigned to a location described by this Reflector.
     * </p>
     * 
     * <p>A StructReflector can be assigned from another StructReflector
     *   as long as they have the same number of fields with the same
     *   names, and the type (Reflector) of each field in this StructReflector
     *   is assignable from the type (Reflector) of the corresponding field
     *   in the specified StructReflector.
     * </p>
     */
    @Override
    public boolean isAssignableFrom(Reflector<?> reflector)
    {
      if (reflector==this)
      { return true;
      }
      
      if (reflector instanceof StructReflector)
      {
        StructReflector structReflector=(StructReflector) reflector;
        if (fieldArray.length != structReflector.fieldArray.length)
        { return false;
        }
        
        for (int fi=0;fi<fieldArray.length;fi++)
        { 
          if (!fieldArray[fi].name.equals(structReflector.fieldArray[fi].name))
          { return false;
          }
          if (! (channels[fi].getReflector()
                  .isAssignableFrom(structReflector.channels[fi].getReflector())
                )
             )
          { return false;
          }
        }
        return true;
      }
      
      return super.isAssignableFrom(reflector);
    }
    
//    private void checkTypeCompatibility(Reflector<?> type)
//      throws BindException
//    {
//      LinkedList<Signature> signatures=type.getSignatures(thisChannel);
//      for (Signature sig: signatures)
//      {
//        if (!sig.getName().startsWith("@"))
//        {
//          if (sig.getParameters()!=null)
//          { 
//            throw new BindException
//              ("Struct is missing implementation of "+typeURI+":"+sig); 
//          }
//          
//          StructField field=fields.get(sig.getName());
//          if (field==null)
//          {
//            throw new BindException
//              ("Struct is missing property "+typeURI+":"+sig); 
//          }
//          
//          Reflector<?> channelType=channels[field.index].getReflector();
//          if (!sig.getType()
//                .isAssignableFrom(channelType)
//             )
//          { 
//            throw new BindException
//              ("Type "+channelType.getTypeURI()+" for field "+field.name
//                +" cannot substitute for "+sig.getType().getTypeURI()
//              );
//            
//          }
//        }
//      }
//
//    }
    
    @Override
    public LinkedList<Signature> getSignatures(Channel<?> source)
      throws BindException
    {
      LinkedList<Signature> ret=super.getSignatures(source);
      

      if (baseChannel!=null)
      { ret.addFirst(new Signature("@super",baseChannel.getReflector()));
      }
      for (StructField field:fieldArray)
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
    
    public StructField[] getFields()
    { return fieldArray;
    }
    
    public Struct newStruct()
    { 
      Object[] data=new Object[channels.length];
      Struct struct=new Struct(this,data,baseChannel!=null?baseChannel.get():null);
      thisChannel.push(struct);
      try
      {
        int i=0;
        for (StructField field: fieldArray)
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
      // log.fine("Created struct with data "+ArrayUtil.format(data,",","|"));
      return struct;
    }
    
    @SuppressWarnings("unchecked")
    public boolean update(Struct val)
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
            ("Supplied Struct is larger ("+val.data.length+")"
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

    @SuppressWarnings("unchecked")
    @Override
    public <D extends Decorator<Struct>> D decorate(
      Channel<Struct> source,
      Class<D> decoratorInterface)
      throws BindException
    { 
      if (iterableItemReflector!=null)
      {
        if (decoratorInterface.isAssignableFrom(IterationDecorator.class))
        {
          return (D) new IterationDecorator<Struct,Object>
            (source,iterableItemReflector)
          {
            @Override
            protected Iterator<Object> createIterator()
            { return ArrayUtil.iterator(source.get().data);
            }
          }; 
        }
      }
      return null;
    }

    @Override
    public Class<Struct> getContentType()
    { return Struct.class;
    }

    @Override
    public URI getTypeURI()
    { return typeURI;
    }
    
    @Override
    public boolean isAssignableTo(
      URI typeURI)
    { 
      if (typeURI.equals(this.typeURI))
      { return true;
      }
      else if (baseChannel!=null)
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
      final Channel<Struct> source,
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
      final Channel<Struct> source,
      Focus<?> focus,
      String name,
      Expression<?>[] params)
      throws BindException
    {
      if (name.startsWith("@"))
      { return resolveMeta(source,focus,name,params);
      }
        
      if (name=="")
      { return (Channel<X>) new FunctorChannel(source,focus,params);
      }
      
      if (params==null)
      {
        
        final StructField field=fields.get(name);
       
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
      private final Channel<Struct> source;
      
      public BaseExtentChannel(Channel<Struct> source)
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
      private final Channel<Struct> source;
      private final Channel<Object> target;
      private final StructField field;
      private final int index;
      
      public FieldChannel
        (final StructField field
        ,final Channel<Struct> source
        ,final Channel<Object> target
        )
      { 
        super(target.getReflector());
        this.field=field;
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
        // log.fine("Store "+val+" to struct field "+field.name);
        if (field.passThrough)
        {
          if (target.isWritable())
          { return target.set(val);
          }
          else
          {
            // log.fine(field.name+" is not writable");
            return false;
          }
        }
        else
        {        
          source.get().data[index]=val;
          return true;
        }
      }
            
      @Override
      public boolean isWritable()
      { return !field.passThrough || target.isWritable();
      }
      
    }
    
    
    class PassThroughChannel
      extends AbstractChannel<Object>
    {
      private final Channel<Struct> source;
      private final Channel<Object> target;
      // private final StructField field;
      
      public PassThroughChannel
        (final StructField field
        ,final Channel<Struct> source
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
        // log.fine("Store "+val+" to struct field "+field.name);              
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
  
  
  class StructChannel
    extends AbstractChannel<Struct>
  {

    private final StructReflector reflector;
    
    public StructChannel(StructReflector reflector)
    { super(reflector);
      this.reflector=reflector;
    
    } 


    @Override
    protected Struct retrieve()
    { return reflector.newStruct();
    }
    
      @Override
    protected boolean store(Struct val)
      throws AccessException
    { return reflector.update(val);
    }
      
    @Override
    public boolean isWritable()
    { return reflector.isWritable();
    }
      
    

  }
  
  
  /**
   * Return a Functor executor that creates a new Struct and applies
   *   contextual bindings
   * 
   * @author mike
   *
   * @param <Ttarget>
   * @param <Tcontext>
   * @param <Tresult>
   */
  public class FunctorChannel
    extends AbstractFunctorChannel<Struct>
  {
    //private final ClosureFocus<?> closure;
    private final StructReflector reflector;
    private final ThreadLocalChannel<Struct> local;

    protected FunctorChannel
      (Channel<Struct> source
      ,Focus<?> focus
      ,Expression<?>[] params
      )
      throws BindException
    { 
      super(source.getReflector());
      //this.closure=new ClosureFocus(focus);
      this.reflector=(StructReflector) source.getReflector();

      Channel<?>[] boundParams=new Channel[params.length];
      int i=0;
      for (Expression<?> x : params)
      { boundParams[i++]=focus.bind(x);
      }
      
      local=new ThreadLocalChannel<Struct>(reflector);
      bind(focus,boundParams);
    }
    
    /**
     * Override and call with the parameter context that parameter refs
     *   will be resolved against
     * 
     * @param contextFocus
     * @throws BindException
     */
    @Override
    protected void bindTarget(Focus<?> contextFocus)
      throws BindException
    { super.bindTarget(contextFocus.chain(local));
    }
    
    
    @Override
    protected Struct retrieve()
    {
//      closure.push();
      try
      { 
        Struct ret=reflector.newStruct();
        local.push(ret);
        try
        {
          applyContextBindings();
          return ret;
        }
        finally
        { local.pop();
        }
      }
      finally
      { // closure.pop();
      }
    }

    @Override
    protected boolean store(
      Struct val)
      throws AccessException
    { return false;
    }
    
  }
  
  @Override
  public void dumpTree(StringBuffer out,String prefix)
  {
    out.append(prefix).append("Struct: ");
    prefix=prefix+"  ";

    if (fields!=null)
    {
      out.append(prefix).append(" { ");
      
      if (typeURI!=null)
      { out.append("[#:"+typeURI+"] ");
      }
      
      if (baseExtentNode!=null)
      { 
        out.append(prefix).append("{=");
        baseExtentNode.dumpTree(out,prefix);
        out.append(prefix).append("}");
      }

      boolean first=true;
      for (StructField field : fields.values())
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