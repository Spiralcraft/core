package spiralcraft.tuple.spi;

import spiralcraft.tuple.TupleFactory;
import spiralcraft.tuple.Tuple;
import spiralcraft.tuple.Scheme;

/**
 * Implementation of a Tuple Factory which creates ArrayTuples.
 */
public class ArrayTupleFactory
  implements TupleFactory
{
  public Tuple createTuple(Scheme scheme)
  { return new ArrayTuple(scheme);
  }
}