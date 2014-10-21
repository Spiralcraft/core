//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.data.core;


import java.net.URI;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;

import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.lang.spi.ClosureFocus;
import spiralcraft.util.string.StringUtil;

import spiralcraft.common.ContextualException;
import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.DeletionConstraint;
import spiralcraft.data.Field;
import spiralcraft.data.Key;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;

import spiralcraft.data.access.CursorAggregate;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.core.FieldImpl;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Queryable;

import spiralcraft.data.lang.DataReflector;

/**
 * A Field which provides a reference to an entity related to this one by
 *   relational keys.
 * 
 * @author mike
 *
 * @param <T>
 */
public class RelativeField<T extends DataComposite>
  extends FieldImpl<T>
{
  
  private KeyImpl<Tuple> key;
  private final boolean generated;
  private boolean resolved;
  private String[] fieldNames;
  private String[] referencedFieldNames;
  private String referencedKeyName;
  private boolean child;
  private DeletionConstraint deletionConstraint;
  
  { this.setTransient(true);
  }
  
  public RelativeField(KeyImpl<Tuple> key)
  { 
    // This RelativeField was generated from an explicit key definition
    this.key=key;
    this.generated=true;
  }
  
  public RelativeField()
  { this.generated=false;
  }
  
  public KeyImpl<?> getKey()
  { return key;
  }
  
  public void setKey(KeyImpl<Tuple> key)
  { this.key=key;
  }
  
  public void setFieldList(String fieldList)
  { fieldNames=StringUtil.explode(fieldList,',',(char) 0,2);
  }

  public void setReferencedFieldList(String fieldList)
  { referencedFieldNames=StringUtil.explode(fieldList,',',(char) 0,2);
  }
  
  public void setReferencedKeyName(String keyName)
  { referencedKeyName=keyName;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void setArchetypeField(Field field)
      throws DataException
   {
     super.setArchetypeField(field);
     
     if (field instanceof RelativeField)
     { 
       RelativeField rfield=(RelativeField) field;
       
       if (fieldNames==null)
       { fieldNames=rfield.fieldNames;
       }
       if (referencedFieldNames==null)
       { referencedFieldNames=rfield.referencedFieldNames;
       }
       child=rfield.child;
     }
   }
  
  public void setChild(boolean child)
  { this.child=child;
  }
  
  public void setDeletionConstraint(DeletionConstraint deletionConstraint)
  { this.deletionConstraint=deletionConstraint;
  }
  
  public DeletionConstraint getDeletionConstraint()
  { return deletionConstraint;
  }
  
  /**
   * Whether the referenced data is part of a compound entity that is rooted
   *   in this entity (i.e. the relationship is one to none or many)
   * 
   * @return
   */
  public boolean isChild()
  { return child;
  }
  
  String[] getFieldNames()
  { return fieldNames;
  }
  
  String[] getReferencedFieldNames()
  { return referencedFieldNames;
  }
  
  String getReferencedKeyName()
  { return referencedKeyName;
  }
  
  
  
  @SuppressWarnings({ "unchecked", "rawtypes" }) // Key.getForeignType() is not generic
  @Override
  public void resolve()
    throws DataException
  { 
    if (resolved)
    { return;
    }
    resolved=true;
    
    
    if (!generated)
    { 
     
      if (getType()==null)
      { throw new DataException("Field type cannot be null: "+getURI());
      }
      // Generate the key
      if (key==null)
      { key=new KeyImpl(this);
      }
      
      if (key.getFieldNames()==null)
      { 
        Key primaryKey=getScheme().getType().getPrimaryKey();
        if (primaryKey==null)
        { 
          throw new DataException
            ("Relative Field "+getScheme().getType().getURI()+"#"+getName()
            +" with no relational fieldList"
            +" requires that a primary key be defined in the containing type"
            ); 
        }

        if (isUniqueValue())
        {
          // We just use the primary key
          key.setFieldNames(primaryKey.getFieldNames());
        }
        else
        {
          if (referencedFieldNames!=null)
          { 
            // Use the part of our primary key that corresponds to the
            //   specified set of foreign fields
            String[] fieldNames=new String[referencedFieldNames.length];
            for (int i=0;i<fieldNames.length;i++)
            { fieldNames[i]=primaryKey.getFieldByIndex(i).getName();
            }
            key.setFieldNames(fieldNames);
            
          }
          else
          {
            // Use the part of our primary key that corresponds to the
            //   parent's primary key
            Key foreignKey=getType().getPrimaryKey();
            if (foreignKey==null)
            {
              throw new DataException
                ("Relative Field "+getURI()+" containing non unique value "
                +" requires that a primary key be defined in the referenced"
                +" type "+getType().getURI()
                ); 
            }
            
            if (foreignKey.getFieldCount()>primaryKey.getFieldCount())
            { 
              throw new DataException
                ("Relative Field "+getURI()+" containing non unique value "
                +" requires that the primary key defined in the referenced"
                +" type "+getType().getURI()+" be a subset of the primary "
                +" key defined in this type"
                ); 
              
            }
            String[] fieldNames=new String[foreignKey.getFieldCount()];
            for (int i=0;i<fieldNames.length;i++)
            { fieldNames[i]=primaryKey.getFieldByIndex(i).getName();
            }
            key.setFieldNames(fieldNames);
          }
        }
      }
      getScheme().addKey(key);
      
    }
    else
    {
      this.child=key.isChild();
      // This field was generated from the key
      setName(key.getName());
    
      if (key.getForeignType()==null)
      { throw new DataException("Foreign type is null Key "+key);
      }
    
      if (key.getImportedKey().isUnique())
      { setType((Type) key.getForeignType());
      }
      else
      { setType((Type) Type.getAggregateType(key.getForeignType()));
      }
    }

    if (key.getForeignQuery()!=null)
    { key.getForeignQuery().resolve();
    }
    else
    { 
      throw new DataException
        ("Error resolving "+getURI()+": foreign query is null from "+key);
    }
    super.resolve();
  }
  
  @Override
  public boolean isFunctionalEquivalent(Field<?> field)
  { return false;
  }
  
  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Channel<T> bindChannel
    (Channel<Tuple> source
    ,Focus<?> argFocus
    ,Expression<?>[] args
    )
    throws BindException
  { 
    try
    { resolve();
    }
    catch (DataException x)
    { throw new BindException("Error resolving field "+getURI(),x);
    }
    // Use original binding context, never bind source expression in
    //   argument context
    Focus<?> context=source.getContext();
    if (context==null)
    { 
      throw new BindException
        ("No context for "+this.getURI()+" "+source);
      // context=argFocus;
    }

    if (!context.isContext(source))
    { context=context.chain(source);
    }
    
    ClosureFocus<Tuple> sourceFocus
      =new ClosureFocus(context,source);
    sourceFocus.setDeclarationInfo(getDeclarationInfo());
    
    Focus<Tuple> keyFocus
      =sourceFocus.chain
        (key.bindChannel(sourceFocus.getSubject(),sourceFocus,args));

    
    Query query;
    try
    { 
      query=key.getForeignQuery();
      if (query==null)
      { throw new BindException("No foreign query for "+getURI());
      }
    }
    catch (DataException x)
    { throw new BindException("Error resolving field "+getURI(),x);
    }
    
    
    if (debug)
    { log.fine("Foreign query is "+query);
    }
    Focus<Queryable> queryableFocus
      =sourceFocus.<Queryable>findFocus(Queryable.QUERYABLE_URI);
    if (queryableFocus!=null)
    { 
      
      try
      { 
        Queryable queryable=queryableFocus.getSubject().get();
        if (queryable!=null)
        {  
          BoundQuery boundQuery
            = queryable.query(query,keyFocus);
          if (boundQuery==null)
          {
            throw new BindException
              ("Got null query from "+queryable+" for query "+query);
          }
          boundQuery.resolve();
          return new KeyFieldChannel
            (getType()
            ,boundQuery
            ,keyFocus.getSubject()
            ,sourceFocus
            );
          
        }
        else
        { 
          throw new BindException
            ("No Queryable available in Focus chain "+context.toString());
        }
      }
      catch (DataException x)
      { throw new BindException(x.toString(),x);
      }
    }
    else
    { 
      try
      {
        BoundQuery boundQuery=query.bind(keyFocus);
        boundQuery.resolve();
        return new KeyFieldChannel
          (getType()
          ,boundQuery
          ,keyFocus.getSubject()
          ,sourceFocus
          );
      }
      catch (DataException x)
      { throw new BindException("Error binding query for KeyField "+getURI(),x);
      }
        
    }
  }
  
  
  @SuppressWarnings({"unchecked","rawtypes"})
  public class KeyFieldChannel
    extends AbstractChannel<T>
  {
    private final BoundQuery query;
    
    private final ClosureFocus<Tuple> closure;
    private Channel<Tuple> keyChannel;
    
    public KeyFieldChannel
      (Type<T> type
      ,BoundQuery query
      ,Channel<Tuple> keyChannel
      ,ClosureFocus<Tuple> closure
      )
      throws BindException
    { 
      super(DataReflector.<T>getInstance(type));
      this.query=query;
      this.keyChannel=keyChannel;
      this.closure=closure;
      this.context=closure;
      this.debug=RelativeField.this.debug;
      this.origin=keyChannel;
    }
    
    @Override
    public boolean isWritable()
    { return false;
    }

    @Override
    protected T retrieve()
      throws AccessException
    {
      // Make sure parameter fields are not null
      closure.push();
      
      try
      {
        Tuple keyVal=keyChannel.get();
        if (keyVal==null)
        { 

          if (debug)
          { log.fine("Key value is null for "+getURI());
          }
          return null;
        }
        else
        { 
          if (debug)
          { log.fine("Key value for "+getURI()+" is "+keyVal);
          }
        }

        int count=keyVal.getFieldSet().getFieldCount();
        for (int i=0;i<count;i++)
        { 
          try
          {
            if (keyVal.get(i)==null)
            { 
              if (debug)
              { 
                log.fine
                ("Key field '"
                  +keyVal.getFieldSet().getFieldByIndex(i).getName()
                  +"' value is null for "+getURI()+", returning null"
                );
              }

              return null;
            }
          }
          catch (DataException x)
          { throw new AccessException("Error reading key value for "+getURI(),x);
          }
          catch (AccessException x)
          { 
            throw new AccessException
              ("Error reading key value for "+getURI()+" closure subject="
                +closure.getSubject()+"["+closure.getSubject().get()+"]"
              ,x
              );
          }
          
        }


        try
        { 
          //        log.fine("KeyField "+getURI()+" retrieving...");
          if (getType().isAggregate())
          { 
            SerialCursor cursor=query.execute();
            try
            {
              if (cursor.getResultType()==null)
              { log.fine("cursor result type is null "+cursor);
              }


              CursorAggregate aggregate
              =new CursorAggregate(cursor);
              //            log.fine(aggregate.toString());
              if (debug)
              { log.fine("RelativeField "+getURI()+" returning "+aggregate);
              }
              return (T) aggregate;
            }
            finally
            { cursor.close();
            }

          }
          else
          { 
            Tuple val=null;
            SerialCursor cursor=query.execute();
            try
            {
              while (cursor.next())
              { 
                if (val!=null)
                { 
                  throw new AccessException
                  (getURI()+": Cardinality violation: non-aggregate query returned more" +
                    " than one result for key "+keyVal 
                  );
                }
                else
                { val=cursor.getTuple();
                }
              }
            }
            finally
            { cursor.close();
            }
            if (debug)
            { log.fine("RelativeField "+getURI()+" returning "+val);
            }
            return (T) val;
          }
        }
        catch (RuntimeException x)
        { 
          throw new RuntimeException("Error retrieving "+getURI(),x);
        }
        catch (DataException x)
        { 
          throw new AccessException("Error retrieving "+getURI(),x);
        }

      }
      finally
      { closure.pop();
      }
    }

    @Override
    protected boolean store(DataComposite val)
      throws AccessException
    { 
      // XXX: We may want to re-translate the stored value and update the
      //   key fields
      return false;     
    }
    
    @Override
    public synchronized <X> Channel<X> resolveMeta(Focus<?> focus,URI typeURI)
      throws BindException
    { 
      // log.fine("Resolving meta"+typeURI.toString());
      try
      {
        Channel<X> meta=RelativeField.this.resolveMeta(typeURI);
        if (meta!=null)
        { return meta;
        }
        return super.resolveMeta(focus,typeURI);
      }
      catch (ContextualException x)
      { throw new BindException("Error resolving metadata "+typeURI,x);
      }      
    }
    
    
   
  }


}