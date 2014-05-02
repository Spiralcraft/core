package spiralcraft.time;

import java.util.Calendar;

public enum DayOfWeek
{
  
  SUNDAY(Calendar.SUNDAY)
  ,MONDAY(Calendar.MONDAY)
  ,TUESDAY(Calendar.TUESDAY)
  ,WEDNESDAY(Calendar.WEDNESDAY)
  ,THURSDAY(Calendar.THURSDAY)
  ,FRIDAY(Calendar.FRIDAY)
  ,SATURDAY(Calendar.SATURDAY)
  ;
  
  public static final DayOfWeek valueOf(int value)
  { return DayOfWeek.values()[value-1];
  }

  final int calendarConst;
  
  private DayOfWeek(int calendarConst)
  { this.calendarConst=calendarConst;
  }
  
}
