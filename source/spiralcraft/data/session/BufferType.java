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
package spiralcraft.data.session;


import spiralcraft.data.DataException;
import spiralcraft.data.Key;
import spiralcraft.data.Type;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.Field;

import spiralcraft.data.core.KeyField;
import spiralcraft.data.core.TypeImpl;
import spiralcraft.data.core.SchemeImpl;

import java.net.URI;
import java.util.Iterator;




public class BufferType
  extends TypeImpl<Buffer>
{
  
//  public static final BufferType getBufferType(Type<?> bufferedType)
//    throws DataException
//  {
//    return (BufferType) Type.resolve
//      (URI.create
//         (bufferedType.getURI().toString().concat(".buffer"))
//      ); 
//    
//  }
  
  
  private boolean linked;
  
  public BufferType(TypeResolver resolver,URI typeURI,Type<?> archetype)
  { 
    
    super(resolver,typeURI);
    // log.fine("Buffer type for "+archetype);
    this.archetype=archetype;
    if (archetype.getBaseType()!=null)
    { this.baseType=getBufferType(archetype.getBaseType());
    }
    if (this.archetype.isAggregate())
    { 
      this.aggregate=true;
      this.contentType=getBufferType(archetype.getContentType());
    }
    if (archetype.getDebug())
    { debug=true;
    }
  }
  
  /**
   * Buffer archetype 
   */
  @Override
  public void link()
    throws DataException
  {
    if (linked)
    { return;
    }
    linked=true;

    if (this.scheme==null)
    { this.scheme=new SchemeImpl();
    }
    this.scheme.setArchetypeScheme(this.archetype.getScheme());
    
    if (this.archetype.getScheme()!=null && !isAggregate()) 
    { 
      for (Field<?> field : this.archetype.getScheme().fieldIterable())
      { 
        if (field.getType()==null)
        { 
          log.fine("Field type is null "+field);
          continue; 
        }

        if (field.getName()==null)
        { 
          log.fine("Field name is null "+field+" of type "+field.getType());
          continue;
        }

        Key<?> primaryKey=this.archetype.getPrimaryKey();
        
        // Primitives are immutable
        if (primaryKey!=null && !field.getType().isPrimitive())
        {
          if (field instanceof KeyField<?>
              && isChildKey
                (((KeyField<?>) field).getKey().fieldIterable()
                ,primaryKey.fieldIterable()
                )
              )
          {
            // Only buffer Key fields with parent-child relationships
            
            // If we didn't buffer it already
            if (this.scheme.getLocalFieldByName(field.getName())==null)
            { 
              // AutoBuffer 
              BufferField newField=new BufferField();
              newField.setName(field.getName());
              newField.setType(getBufferType(field.getType()));
              scheme.addField(newField);

            }
          }

        }
      }
    }
    else
    { 
      if (!isAggregate())
      {
        log.warning("Archetype scheme is null: "
          +archetype+" in BufferType"+toString());
      }
    }
    super.link();

//    addMethods();

  }

  

  /**
   * Indicate whether the given relation includes the primary key of this
   *   Type's Scheme.
   * 
   * @param relation
   * @param primary
   * @return
   */
  private boolean isChildKey
    (Iterable<? extends Field<?>> relation,Iterable<? extends Field<?>> primary)
  {
    Iterator<? extends Field<?>> primaryField=primary.iterator();
    for (Field<?> childField: relation)
    {
      if (!primaryField.hasNext())
      { 
        // Primary key has fewer fields than used in relation
        return true;
      }
      if (!childField.getName().equals(primaryField.next().getName()))
      { return false;
      }
     
    }
    
    if (primaryField.hasNext())
    {
      // Primary key has more fields than used in relation
      //   so child is really a parent (less specific)
      return false;
    }
    return true;
    
  }
}

