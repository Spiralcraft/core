package spiralcraft.lang.test;

import spiralcraft.util.string.StringConverter;

public class Bar
{

  public String publicField="default";
  
  public String[] stringArray
    ={"one","two","three"};
  
  private Object object;
  
  public String getTheField()
  { return publicField;
  }

  public void setTheField(String data)
  { publicField=data;
  }
  
  public String[] getTheArray()
  { return stringArray;
  }

  public void setTheArray(String[] theArray)
  { this.stringArray=theArray;
  }

  public Object getTheObject()
  { return this.object;
  }
  
  public void setTheObject(Object object)
  { this.object=object;
  }
  
  @Override
  public String toString()
  { 
    return super.toString()
      +"[theField="+getTheField()
      +" theArray="
      +StringConverter.<String[]>getInstance(String[].class)
        .toString(stringArray);
  }

}
