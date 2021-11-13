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
package spiralcraft.data;

import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.util.string.StringConverter;

/**
 * Thrown when a BufferTuple is committed and the change is incompatible with
 *   changes made in another journal update.
 */
public class UpdateConflictException
  extends DataException
{
  private static final StringConverter<?> objectToString=
      new StringConverter<Object>()
      {
        @Override
        public Object fromString(String val)
        { return null;
        }
      };
      
  @SuppressWarnings({ "rawtypes", "unchecked"})
  static String formatConflict(Field<?>[] fields,DeltaTuple requested,DeltaTuple existing)
  {
    
    StringBuilder buf=new StringBuilder();
    for (Field<?> field:fields)
    { 
      
      buf.append("\r\n  Field Conflict: ");
      buf.append(field.getURI());
      StringConverter converter=field.getContentReflector().getStringConverter();
      if (converter==null)
      { converter=objectToString;
      }
      try
      { 
        buf
          .append("\r\n    New Value: ")
          .append(requested!=null?converter.toString(field.getValue(requested)):"")
          .append ("\r\n   Existing Value: ")
          .append(existing!=null?converter.toString(field.getValue(existing)):"")
          .append("\r\n    ");
      }
      catch (DataException x)
      { 
        ClassLog.getInstance(UpdateConflictException.class)
          .log(Level.WARNING,"Exception reporting UpdateConflictExcetpion ",x);
      }
    }
    return buf.toString();
  }
  
  private static final long serialVersionUID=1;
  
  // private BufferTuple buffer;
  // private JournalTuple journal;
  public UpdateConflictException
    (DeltaTuple requested, DeltaTuple existing,Field<?>[] conflictingFields)
  { 
    super("Update Conflict in "+requested.getType().getArchetype().getURI()
      +", fields ["+formatConflict(conflictingFields,requested,existing)+"]"
      );
    // this.buffer=buffer;
    // this.journal=journal;
    // XXX Generate message for conflict
  }
  
  public UpdateConflictException
    (DeltaTuple requested, DeltaTuple existing)
  { 
    super("Update Conflict: "+requested+" conflicts with "+existing);
    // this.buffer=buffer;
    // this.journal=journal;
    // XXX Generate message for conflict
  }
  
}
