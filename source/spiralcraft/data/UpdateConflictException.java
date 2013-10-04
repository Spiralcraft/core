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

/**
 * Thrown when a BufferTuple is committed and the change is incompatible with
 *   changes made in another journal update.
 */
public class UpdateConflictException
  extends DataException
{
  static String formatConflict(Field<?>[] fields,DeltaTuple requested,DeltaTuple existing)
  {
    
    StringBuilder buf=new StringBuilder();
    for (Field<?> field:fields)
    { 
      if (buf.length()>0)
      { buf.append(",");
      }
      buf.append(field.getName());
      
      try
      { 
        buf
          .append("({")
          .append(requested!=null?field.getValue(requested):"")
          .append ("} != {")
          .append(existing!=null?field.getValue(existing):"")
          .append("})");
      }
      catch (DataException x)
      {
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
