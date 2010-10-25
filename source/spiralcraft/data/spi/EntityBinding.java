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
package spiralcraft.data.spi;

import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.access.DeltaTrigger;
import spiralcraft.data.access.Entity;
import spiralcraft.data.access.Updater;
import spiralcraft.data.query.Queryable;
import spiralcraft.data.transaction.TransactionException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.util.ArrayUtil;

/**
 * <p>Encapsulates the state of an Entity within a running Store.</p>
 * 
 * <p>Caches configuration and structural information used by the store
 *   to query and update Entity data and any associated state. 
 * </p> 
 * 
 * @author mike
 *
 */
public class EntityBinding
  implements Contextual,Lifecycle
{

  private final Entity entity;
  private Queryable<Tuple> queryable;
  private Updater<DeltaTuple> updater;
  private boolean authoritative;
  
  
  private DeltaTrigger[] beforeInsert=new DeltaTrigger[0];
  private DeltaTrigger[] afterInsert=new DeltaTrigger[0];
  private DeltaTrigger[] beforeUpdate=new DeltaTrigger[0];
  private DeltaTrigger[] afterUpdate=new DeltaTrigger[0];
  private DeltaTrigger[] beforeDelete=new DeltaTrigger[0];
  private DeltaTrigger[] afterDelete=new DeltaTrigger[0];
  
  public EntityBinding(Entity entity)
  { this.entity=entity;
  }
  
  public Entity getEntity()
  { return entity;
  }
  
  public void setQueryable(Queryable<Tuple> queryable)
  { this.queryable=queryable;
  }
  
  public Queryable<Tuple> getQueryable()
  { return queryable;
  }
  
  public void setUpdater(Updater<DeltaTuple> updater)
  { this.updater=updater;
  }
  
  public Updater<DeltaTuple> getUpdater()
  { return updater;
  }
  
  public boolean isAuthoritative()
  { return authoritative;
  }
  
  public void setAuthoritative(boolean authoritative)
  { this.authoritative=authoritative;
  }
  
  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws BindException
  { 
    focusChain=focusChain.chain(new SimpleChannel<EntityBinding>(this,true));
    focusChain.addFacet
      (focusChain.chain(new SimpleChannel<Entity>(entity,true)));

    Type.getDeltaType(entity.getType());
    if (updater!=null)
    {
      Focus<?> updaterFocus=updater.bind(focusChain);
    
    
    
      DeltaTrigger[] tupleTriggers=entity.getDeltaTriggers();
      if (tupleTriggers!=null)
      {
        for (DeltaTrigger trigger : tupleTriggers)
        { 
          trigger.bind(updaterFocus);
        
          switch (trigger.getWhen())
          {
            case BEFORE:
              if (trigger.isForInsert())
              { beforeInsert=ArrayUtil.append(beforeInsert,trigger);
              }
              if (trigger.isForUpdate())
              { beforeUpdate=ArrayUtil.append(beforeUpdate,trigger);
              }
              if (trigger.isForDelete())
              { beforeDelete=ArrayUtil.append(beforeDelete,trigger);
              }
              break;
            case AFTER:
              if (trigger.isForInsert())
              { afterInsert=ArrayUtil.append(afterInsert,trigger);
              }
              if (trigger.isForUpdate())
              { afterUpdate=ArrayUtil.append(afterUpdate,trigger);
              }
              if (trigger.isForDelete())
              { afterDelete=ArrayUtil.append(afterDelete,trigger);
              }
              break;
          }
        }
      }
      
    }
    else if (entity.getDeltaTriggers()!=null)
    { 
      throw new BindException
        ("Triggers require an Updater in Entity "+entity.getType());
    }
    
    return focusChain;
  }

  
  @Override
  public void start()
    throws LifecycleException
  { 
  }

  @Override
  public void stop()
    throws LifecycleException
  {      
  }
  
  public DeltaTuple beforeInsert(DeltaTuple tuple)
    throws TransactionException
  { return beforeTrigger(beforeInsert,tuple);
  }
  
  public DeltaTuple beforeUpdate(DeltaTuple tuple)
    throws TransactionException
  { return beforeTrigger(beforeUpdate,tuple);
  }

  public DeltaTuple beforeDelete(DeltaTuple tuple)
    throws TransactionException
  { return beforeTrigger(beforeDelete,tuple);
  }

  public void afterInsert(DeltaTuple tuple)
    throws TransactionException
  { afterTrigger(afterInsert,tuple);
  }

  public void afterUpdate(DeltaTuple tuple)
    throws TransactionException
  { afterTrigger(afterUpdate,tuple);
  }

  public void afterDelete(DeltaTuple tuple)
    throws TransactionException
  { afterTrigger(afterDelete,tuple);
  }


  private DeltaTuple beforeTrigger(DeltaTrigger[] triggers,DeltaTuple tuple)
    throws TransactionException
  { 
    for (DeltaTrigger trigger: triggers)
    { 
      tuple=trigger.trigger();
      if (tuple==null)
      { break;
      }

    }
    return tuple;
  }
  
  private void afterTrigger(DeltaTrigger[] triggers,DeltaTuple tuple)
    throws TransactionException
  {
    for (DeltaTrigger trigger: triggers)
    { trigger.trigger();
    }
  }
  
  
}
