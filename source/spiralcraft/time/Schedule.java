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
package spiralcraft.time;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Specifies a series of Recurrent Instants according to a specific Calendar
 * 
 * @author mike
 *
 */
public class Schedule
  implements Recurrent
{

  
  private RecurrenceRule[] rules;
  private Instant basis;
  private Calendar calendar;
  
  
  public void setRecurrenceRules(RecurrenceRule[] rules)
  { this.rules=rules;
  }
  
  /**
   * The starting or initial occurrence time for this Schedule
   * 
   * @param basis
   */
  public void setBasis(Instant basis)
  { this.basis=basis;
  }
  
  public void setCalendar(Calendar calendar)
  { this.calendar=calendar;
  }

  @Override
  public Iterator<Instant> iterator(final Interval interval)
  { 
    
    return new Iterator<Instant>()
    {
      private Instant next
        =Schedule.this.next(interval.getStart());
      
      @Override
      public boolean hasNext()
      { return next!=null;
      }

      @Override
      public Instant next()
      {
        if (next==null)
        { throw new NoSuchElementException();
        }

        Instant ret=next;
        
        next=Schedule.this.next(next);
        if (next.compareTo(interval.getEnd())>=0)
        { next=null;
        }
        return ret;
        
      }

      @Override
      public void remove()
      { 
        throw new UnsupportedOperationException
          ("Schedule iterator is read only");
      }
    };
    
  }

  @Override
  public Instant next(Instant mark)
  {
    if (basis==null)
    { basis=new Instant();
    }
    Instant next=null;
    for (RecurrenceRule rule : rules)
    {
      Instant instant=rule.next(calendar,basis,mark);
      if (next==null || instant.compareTo(next)<0)
      { next=instant;
      }
    }
    return next;
  }
  
}
