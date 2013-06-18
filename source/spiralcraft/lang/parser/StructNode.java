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

import spiralcraft.common.Coercion;
import spiralcraft.common.namespace.PrefixResolver;
import spiralcraft.common.namespace.UnresolvedPrefixException;
import spiralcraft.lang.AccessException;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Functor;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.MapDecorator;
import spiralcraft.lang.Range;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.Signature;
import spiralcraft.lang.TeleFocus;
import spiralcraft.lang.kit.AbstractReflector;
import spiralcraft.lang.kit.CoercionChannel;
import spiralcraft.lang.kit.MapLookupChannel;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.lang.spi.AbstractFunctorChannel;
import spiralcraft.lang.spi.ArrayIndexChannel;
import spiralcraft.lang.spi.ArrayRangeChannel;
import spiralcraft.lang.spi.ArraySelectChannel;
import spiralcraft.lang.spi.AspectChannel;
import spiralcraft.lang.spi.SourcedChannel;
//import spiralcraft.lang.spi.ClosureFocus;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.util.lang.NumericCoercion;

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
    throws UnresolvedPrefixException
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

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public Channel<?> bind(final Focus<?> focus)
    throws BindException
  { 
    if (baseExtentNode!=null && fields.isEmpty())
    { 
      // simple reference case
      return focus.bind(Expression.create(baseExtentNode));
    }
    else
    { 
      // struct wrapper case
      return new StructChannel(new StructReflector(focus),focus);
    }
  }
  
  
  public class StructReflector
    extends AbstractReflector<Struct>
    implements Functor<Struct>
  {
    

    
    private final Channel<?>[] channels=new Channel<?>[fields.size()];
    
    private final StructField[] fieldArray
      =fields.values().toArray(new StructField[fields.size()]);
    
    private final ThreadLocalChannel<Struct> thisChannel
      =new ThreadLocalChannel<Struct>(this,true);
    
    private final URI typeURI;
    
    private final Channel<Object> baseChannel;
    
    private final Reflector<Object> iterableItemReflector;
    private final Reflector<Object> mapKeyReflector;
    private final Reflector<Object> mapValueReflector;
    private boolean map=true;
    private final boolean pair;
    private boolean anonymous=true;
    
    
    
    private Focus<?> context;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public StructReflector(Focus<?> focus)
      throws BindException
    {
      this.context=focus;
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
      
    

//
// Struct labels are focus-chain related only- we're not defining a type, so
//   this check is overly restrictive
//
//      if (typeURI!=null && focus.findFocus(typeURI)!=null)
//      { 
//        throw new BindException
//          ("Type URI "+typeURI
//          +" has already been defined and cannot be duplicated"
//          );
//      }
      
      
      if (baseExtentNode!=null)
      { 
        baseChannel=focus.bind(Expression.create(baseExtentNode));
        anonymous=false;
      }
      else
      { baseChannel=null;
      }
       
      
      // Telescope context for self resolution
      focus=focus.telescope(thisChannel);
        
      // log.fine(" "+sourceChannel.getReflector());
      
      Reflector iterableItemReflector=null;
      Reflector mapKeyReflector=null;
      Reflector mapValueReflector=null;
      
      int i=0;
      for (StructField field: fieldArray)
      { 
        if (field.source!=null)
        { 
//          log.fine("Binding "+field.name+" to "+field.source.reconstruct());
          channels[i]= field.source.bind(focus);
          if (channels[i]==null)
          { throw new BindException("Could not bind field '"+field.name+"' to "+field.source);
          } 
          
          if (field.type!=null)
          { 
            // Set up field with a different declared type
            Reflector type=(Reflector) field.type.bind(focus).get();
            Coercion coercion=null;
            
            if (!type.isAssignableFrom(channels[i].getReflector()))
            { 
              // XXX There are other possibilities- may have to query
              //   both reflectors
              coercion
                =NumericCoercion.instance(type.getContentType());
              
              if (coercion==null)
              {
                throw new BindException
                  ("Type "+type.getTypeURI()
                  +" cannot be assigned from expression of type "
                  +channels[i].getReflector().getTypeURI()
                  );
              }
              channels[i]
                =new CoercionChannel(type,channels[i],coercion);
            }
            else
            {
              channels[i]
                =new AspectChannel(type,channels[i]);
            }
            
          }
        }
        else if (field.type!=null)
        { 
          channels[i]= new SimpleChannel
            ((Reflector) field.type.bind(focus).get());
        }
        else
        {
          throw new BindException
            ("Field '"+field.name
            +"' must have a source expression and/or a declared type"
            );
        }
        
        if (!field.anonymous)
        { anonymous=false;
        }
        
        iterableItemReflector
          =commonType(iterableItemReflector,channels[i].getReflector());        
        
        if (channels[i].getReflector() instanceof StructReflector)
        { 
          StructReflector entryReflector
            =(StructReflector) channels[i].getReflector();
          if (entryReflector.pair)
          {
            mapKeyReflector
              =commonType
                (mapKeyReflector
                ,entryReflector.channels[0].getReflector()
                );
            
            mapValueReflector
              =commonType
                (mapValueReflector
                ,entryReflector.channels[1].getReflector()
                );
            
            
          }
          else
          { map=false;
          }
        }
        else
        { map=false;
        }
        
        field.linked=true;
        
        i++;
      }
      
      pair= anonymous && fieldArray.length==2;
      
      this.iterableItemReflector
        =iterableItemReflector!=null
        ?iterableItemReflector
        :BeanReflector.getInstance(Void.class);
      this.mapKeyReflector
        =mapKeyReflector!=null
        ?mapKeyReflector
        :BeanReflector.getInstance(Void.class);
      this.mapValueReflector
        =mapValueReflector!=null
        ?mapValueReflector
        :BeanReflector.getInstance(Void.class);

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Reflector commonType(Reflector rcurrent,Reflector rnew)
    {
      if (rcurrent==null)
      { return rnew;
      }
      else if (rnew==null)
      { return rcurrent;
      }
      else
      { return rcurrent.getCommonType(rnew);
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
    
    @Override
    public LinkedList<Signature> getProperties(Channel<?> source)
      throws BindException
    {
      LinkedList<Signature> ret=super.getProperties(source);
      

      if (baseChannel!=null)
      { ret.addFirst(new Signature("@super",baseChannel.getReflector()));
      }
      for (StructField field:fieldArray)
      { 
        if (channels[field.index]!=null)
        {
          ret.add
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
    
    public Channel<?> getChannel(StructField field)
    { return channels[field.index];
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
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
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
        if (val.size()>channels.length)
        { 
          throw new AccessException
            ("Supplied Struct is larger ("+val.size()+")"
            +" than the bound field list ("+channels.length+")"
            );
        }
          
        int i=0;
        for (Channel channel: channels)
        { 
          if (channel.isWritable())
          {
            if (i<val.size())
            { channel.set(val.size());
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
            { return source.get().iterator();
            }
          }; 
        }
        else if (decoratorInterface.isAssignableFrom(MapDecorator.class))
        {
          if (map)
          {
            return (D) new MapDecorator<Struct,Object,Object>
              (source,mapKeyReflector,mapValueReflector)
            {

              @Override
              public boolean put(
                Struct map,
                Object key,
                Object value)
              {
                // This is not a writable map, but we may reconsider this
                return false;
              }

              @Override
              public Object get(
                Struct map,
                Object key)
              { 
                for (Object o: map)
                {
                  Struct pair=(Struct) o;
                  Object pairKey=pair.get(0);
                  if (key==pairKey 
                      || (key!=null && key.equals(pairKey))
                     )
                  { return pair.get(1);
                  }
                }
                return null;
              }

              @Override
              public Iterator<Object> keys(final Struct map)
              { 
                return new Iterator<Object>()
                {
                  @SuppressWarnings("rawtypes")
                  final Iterator<Struct> pairs
                    =(Iterator<Struct>) (Iterator) map.iterator();

                  @Override
                  public boolean hasNext()
                  { return pairs.hasNext();
                  }

                  @Override
                  public Object next()
                  { return pairs.next().get(0);
                  }

                  @Override
                  public void remove()
                  { return;
                  }
                };
                
              }

              @Override
              public Iterator<Object> values(final Struct map)
              { 
                return new Iterator<Object>()
                {
                  @SuppressWarnings("rawtypes")
                  final Iterator<Struct> pairs
                    =(Iterator<Struct>) (Iterator) map.iterator();

                  @Override
                  public boolean hasNext()
                  { return pairs.hasNext();
                  }

                  @Override
                  public Object next()
                  { return pairs.next().get(0);
                  }

                  @Override
                  public void remove()
                  { return;
                  }
                };
              }

            }; 
          }
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
    

    @SuppressWarnings({ "unchecked", "rawtypes" })
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <X> Channel<X> resolve(
      final Channel<Struct> source,
      Focus<?> focus,
      String name,
      Expression<?>[] params)
      throws BindException
    {
      Channel ret=this.resolveLocal(source,focus,name,params);
      if (ret==null)
      {
        
        // Delegate to source context
        // Requires that Struct maintain a reference
        //   to its source
//        ret=((Channel) context.getSubject()).getReflector().resolve
//          (context.getSubject()
//          ,focus
//          ,name
//          ,params
//          );
//          
      }
      return ret;
      
    }
    
    @Override
    public Channel<Struct> bindChannel(Focus<?> focus,Channel<?>[] params)
      throws BindException
    { 
      if (params.length>0)
      { throw new BindException("Struct constructor does not accept parameters");
      }
      return new StructChannel(this,focus);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <X> Channel<X> resolveLocal(
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
      
      if (name.startsWith("_") && name.length()>1)
      { 
        try
        {
          int index=Integer.parseInt(name.substring(1));
          if (fieldArray.length>index)
          { name=fieldArray[index].name;
          }
        }
        catch (NumberFormatException x)
        { // Ignore
        }
      }
       
      if (name.equals("[]") && map)
      { return (Channel<X>) subscript(source,focus,params[0]);
      }
          
      if (params==null)
      {
        
        final StructField field=fields.get(name);
       
        if (field!=null)
        {
          if (!field.linked)
          { throw new BindException
              ("Field "+field.name+" cannot be forward referenced");
          }
          
          final Channel target=channels[field.index];
        
          if (!field.passThrough)
          { return (Channel<X>) new FieldChannel(field,source,target);
          }
          else
          { return (Channel<X>) new PassThroughChannel(field,source,target);
          }
        }
      }
      else
      {
        // Method
        final StructField field=fields.get(name);
        
        if (field!=null)
        {
          if (!field.linked)
          { throw new BindException
              ("Field '"+field.name+"' cannot be forward referenced");
          }

          final Channel<?> structChannel
            =field.source.bind(context.chain(source));
          
          final Channel target
            =structChannel.resolve(focus,"",params);
          
          if (target==null)
          { 
            throw new BindException
              ("Could not resolve functor channel for field '"+field.name+"' "
                +" from "+structChannel
              );
          }
          return (Channel<X>) new MethodChannel(field,source,target);
        }
        
      }
      
      
      if (baseChannel!=null)
      { 
        
        return baseChannel.getReflector().resolve
          (new BaseExtentChannel(source), focus, name, params);
      }
      return null;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Channel<?> subscript
      (Channel<Struct> source
      ,Focus<?> focus
      ,Expression<?> subscript
      )
      throws BindException
    {
      
      
      ThreadLocalChannel<?> componentChannel
        =new ThreadLocalChannel(mapValueReflector);
      
      TeleFocus teleFocus=new TeleFocus(focus,componentChannel);
      
      Channel<?> subscriptChannel=teleFocus.bind(subscript);
      
      Class subscriptClass=subscriptChannel.getContentType();
      
      if (Integer.class.isAssignableFrom(subscriptClass)
          || Short.class.isAssignableFrom(subscriptClass)
          || Byte.class.isAssignableFrom(subscriptClass)
          )
      {
        return new ArrayIndexChannel
          (mapValueReflector
          ,source
          ,(Channel<Number>) subscriptChannel
          );
  //      return new TranslatorChannel
  //        (source
  //        ,new ArrayIndexTranslator(componentReflector)
  //        ,new Channel[] {subscriptChannel}
  //        );
      }
      else if 
        (Boolean.class.isAssignableFrom(subscriptClass)
        || boolean.class.isAssignableFrom(subscriptClass)
        )
      {
        return new ArraySelectChannel
          (source
           ,componentChannel
           ,subscriptChannel
           );
      }
      else if (Range.class.isAssignableFrom(subscriptClass))
      { 
        return new ArrayRangeChannel
          (source
          ,mapValueReflector
          ,subscriptChannel
          );
      }
      else if (mapKeyReflector.isAssignableFrom
                (subscriptChannel.getReflector())
              )
      { 
        return new MapLookupChannel
          (source.decorate(MapDecorator.class)
          ,subscriptChannel
          );
      }
      else
      {
        throw new BindException
          ("Don't know how to apply the [lookup("
          +subscriptChannel.getContentType().getName()
          +")] operator to a Struct"
          );
      }
    }
    
    
    class BaseExtentChannel
      extends SourcedChannel<Struct,Object>
    {
      
      public BaseExtentChannel(Channel<Struct> source)
      { super(baseChannel.getReflector(),source);
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
      extends SourcedChannel<Struct,Object>
    {
      private final Channel<Object> target;
      private final StructField field;
      private final int index;
      private final boolean constant;
      
      public FieldChannel
        (final StructField field
        ,final Channel<Struct> source
        ,final Channel<Object> target
        )
      { 
        super(target.getReflector(),source);
        this.field=field;
        this.target=target;
        this.index=field.index;
        this.constant=source.isConstant() && source.get().isFrozen();
      }

      @Override
      protected Object retrieve()
      { 
        Struct struct=source.get();
        if (struct!=null)
        { return struct.get(index);
        }
        else
        { return null;
        }
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
          Struct struct=source.get();
          if (struct!=null)
          { 
            struct.set(index,val);
            return true;
          }
          else
          { return false;
          }
        }
      }
            
      @Override
      public boolean isWritable()
      { return !field.passThrough || target.isWritable();
      }
      
      @Override
      public boolean isConstant()
      { return constant;
      }
      
    }
    
    
    class PassThroughChannel
      extends SourcedChannel<Struct,Object>
    {
      private final Channel<Object> target;
      private boolean constant;
      // private final StructField field;
      
      public PassThroughChannel
        (final StructField field
        ,final Channel<Struct> source
        ,final Channel<Object> target
        )
      { 
        super(target.getReflector(),source);
        // this.field=field;
        this.target=target;
        this.constant=target.isConstant();
      }
      
      @Override
      protected Object retrieve()
      { 
        if (constant)
        { return target.get();
        }
        else
        {
          thisChannel.push(source.get());
          try
          { return target.get();
          }
          finally
          { thisChannel.pop();
          }
        }
      }

      @Override
      protected boolean store(
        Object val)
          throws AccessException
      { 
        if (constant)
        { return false;
        }
        else
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
      }
            
      @Override
      public boolean isWritable()
      { return target.isWritable();
      }
      
      @Override
      public boolean isConstant()
      { return constant;
      }

    }
    
    class MethodChannel
      extends SourcedChannel<Struct,Object>
    {
      private final Channel<Object> target;
      private boolean constant;
      // private final StructField field;

      public MethodChannel
      (final StructField field
        ,final Channel<Struct> source
        ,final Channel<Object> target
      )
      { 
        super(target.getReflector(),source);
        // this.field=field;
        this.target=target;
        this.constant=target.isConstant();
      }

      @Override
      protected Object retrieve()
      { 
        if (constant)
        { return target.get();
        }
        else
        {
          Struct struct=source.get();
          thisChannel.push(struct);
          try
          { 
            if (struct!=null)
            {
              synchronized (struct)
              { return target.get();
              }
            }
            else
            { return target.get();
            }
          }
          finally
          { thisChannel.pop();
          }
        }
      }

      @Override
      protected boolean store(
        Object val)
      throws AccessException
      { 
        return false;
      }

      @Override
      public boolean isWritable()
      { return false;
      }

      @Override
      public boolean isConstant()
      { return false;
      }

    }    
    
    @Override
    public String toString()
    { return super.toString()+": "+fields.keySet();
    }
  }
  
  
  class StructChannel
    extends AbstractChannel<Struct>
  {

    private final StructReflector reflector;
    
    
    public StructChannel(StructReflector reflector,Focus<?> context)
    { 
      super(reflector);
      this.reflector=reflector;
      this.context=context;
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
    // private final ClosureFocus<?> closure;
    private final StructReflector reflector;
    private final ThreadLocalChannel<Struct> local;

    // @SuppressWarnings("unchecked")
    protected FunctorChannel
      (Channel<Struct> source
      ,Focus<?> focus
      ,Expression<?>[] params
      )
      throws BindException
    { 
      super(source.getReflector());
//      this.closure=new ClosureFocus(focus);
      this.reflector=(StructReflector) source.getReflector();

      Channel<?>[] boundParams=new Channel<?>[params.length];
      int i=0;
      for (Expression<?> x : params)
      { 
        boundParams[i++]=focus.bind(x);
        // boundParams[i++]=closure.bind(x);
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
      //closure.push();
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
      { //closure.pop();
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

