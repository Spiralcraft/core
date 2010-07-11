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
 * <p>Defines how recurring Instants are expanded across a time span. Based
 *   on the RFC-2445 RRULE production.
 * </p>
 * 
 * @author mike
 *
 */
public class RecurrenceRule
{
  private Frequency frequency;
  private int interval=1;

  /**
   * The frequency of recurrence (daily, weekly, hourly, etc)
   * 
   * @return
   */
  public Frequency getFrequency()
  { return frequency;
  }
  
  /**
   * The frequency of recurrence (daily, weekly, hourly, etc)
   * 
   * @return
   */
  public void setFrequency(
    Frequency frequency)
  { this.frequency = frequency;
  }

  /**
   * The size of the frequency interval (eg. every 3 weeks, every 2 hours)
   * 
   * @return
   */
  public int getInterval()
  { return interval;
  }
  
  /**
   * The size of the frequency interval (eg. every 3 weeks, every 2 hours)
   * 
   * @return
   */
  public void setInterval(
    int interval)
  { this.interval = interval;
  }

  /**
   * Return an Iterator that iterates through all the recurrences within
   *   the specified Interval. The basis Instant is the first scheduled
   *   instant and provides the fixed time fields. 
   *   
   * 
   * @param basis
   * @param start
   * @return
   */
  public Iterator<Instant> iterator
    (Calendar calendar,Instant basis,Interval range)
  { return new RecurrenceIterator(calendar,basis,range);
  }
  
  /**
   * Return the next instance of the recurrence that occurs strictly after 
   *   the specified mark time, given the specified basis.
   * 
   * @param calendar
   * @param basis
   * @param mark
   * @return
   */
  public Instant next(Calendar calendar,Instant basis,Instant mark)
  {
    if (basis==null)
    { throw new NullPointerException("Basis Instant cannot be null");
    }
    
    if (mark.compareTo(basis)<=0)
    { return basis;
    }
    
    // Obtain the number of frequency units between the basis and the mark 
    Duration duration=calendar.subtract
      (mark,basis,new Chronom[] {frequency.getChronom()});
    
    
    Instant lastOccurrence;
    if (duration!=null)
    {
      long unitsToMark=duration.getCount();
    
      // The whole number of intervals between the basis and mark 
      //   last potential occurrence
      long intervalsToMark=unitsToMark/interval;
    
      // Frequency units between basis and last potential occurrence
      Duration basisToLastOccurrence
        =new Duration(intervalsToMark*interval,frequency.getChronom());
    
      // Instant of last potential occurrence
      lastOccurrence=calendar.add(basis,basisToLastOccurrence);
    }
    else
    { lastOccurrence=basis;
    }
    
    Instant nextOccurrence
      =calendar.add
        (lastOccurrence
        ,new Duration(interval,frequency.getChronom())
        );
    return nextOccurrence;

       
  }
  
  class RecurrenceIterator
    implements Iterator<Instant>
  {
    private Instant start;
    private Instant next;
    private final Iterator<Instant> majorIntervalIterator;
    
    public RecurrenceIterator
      (Calendar calendar
      ,Instant basis
      ,Interval range
      )
    { 
      this.start=range.getStart();
      this.majorIntervalIterator
        =calendar.iterate
          (basis,frequency.getTimeField(),interval,range.getEnd());
      computeNext();
    }
    
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
      computeNext();
      return ret;
    }
    
    private void computeNext()
    {
      while (majorIntervalIterator.hasNext())
      {
        Instant next=majorIntervalIterator.next();
        if (next.compareTo(start)<0)
        { continue;
        }
        else
        {
          this.next=next;
          break;
        }
      }
    }

    @Override
    public void remove()
    { 
      throw new UnsupportedOperationException
        ("RecurrenceIterator is read-only");
    } 
    
  }
}
