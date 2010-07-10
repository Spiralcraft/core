package spiralcraft.time;

public enum Frequency
{

  SECONDLY(Chronom.SECOND,TimeField.SECOND_OF_MINUTE)
  ,MINUTELY(Chronom.MINUTE,TimeField.MINUTE_OF_HOUR)
  ,HOURLY(Chronom.HOUR,TimeField.HOUR_OF_DAY)
  ,DAILY(Chronom.DAY,TimeField.DAY_OF_MONTH)
  ,WEEKLY(Chronom.WEEK,TimeField.WEEK_OF_YEAR)
  ,MONTHLY(Chronom.MONTH,TimeField.MONTH_OF_YEAR)
  ,YEARLY(Chronom.YEAR,TimeField.YEAR)
  ;
  
  private final Chronom chronom;
  private final TimeField timeField;
  
  Frequency(Chronom chronom,TimeField timeField)
  { 
    this.chronom=chronom;
    this.timeField=timeField;
  }
  
  public Chronom getChronom()
  { return chronom;
  }
  
  public TimeField getTimeField()
  { return timeField;
  }
  
  
}
