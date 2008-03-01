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
import spiralcraft.data.Type;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.Field;

import spiralcraft.data.core.TypeImpl;
import spiralcraft.data.core.SchemeImpl;
import spiralcraft.log.ClassLogger;


import java.net.URI;



public class BufferType
  extends TypeImpl<Buffer>
{
  private static final ClassLogger log=new ClassLogger(BufferType.class);
  
  public static final BufferType getBufferType(Type<?> baseType)
    throws DataException
  {
    return (BufferType) Type.resolve
      (URI.create
         (baseType.getURI().toString().concat(".buffer"))
      ); 
    
  }
  
  
  private boolean linked;
  
  public BufferType(TypeResolver resolver,URI typeURI,Type<?> archetype)
  { 
    
    super(resolver,typeURI);
    // log.fine("Buffer type for "+archetype);
    this.archetype=archetype;
  }
  
  /**
   * Buffer archetype 
   */
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
    
    for (Field field : this.archetype.getScheme().fieldIterable())
    {
      if (field.getType()==null)
      { 
        log.fine("Field type is null "+field);
        continue;
      }
      
      // Primitives are immutable
      if (!field.getType().isPrimitive())
      {
        // If we didn't buffer it already
        if (this.scheme.getLocalFieldByName(field.getName())==null)
        { 
          // AutoBuffer 
          BufferField newField=new BufferField();
          newField.setName(field.getName());
          newField.setType
            (Type.resolve
              (URI.create(field.getType().getURI().toString()+".buffer"))
            );
          scheme.addField(newField);
          
        }

      }
    }

//    addMethods();

    super.link();
  }

  
  
}
