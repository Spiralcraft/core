package spiralcraft.data.transaction;

import javax.transaction.xa.Xid;

import spiralcraft.codec.text.HexCodec;
import spiralcraft.util.ByteBuffer;

public class XidImpl
  implements Xid
{

  
  private final int formatId;
  private final byte[] globalTransactionId;
  private final byte[] branchQualifier;
  
  XidImpl
    (long transactionId
    ,long branchNo
    )
  {
    this.formatId=0x1234;
    this.globalTransactionId=new byte[64];
    this.branchQualifier=new byte[64];

    ByteBuffer buf=new ByteBuffer(globalTransactionId);
    buf.write(transactionId);
    
    buf=new ByteBuffer(branchQualifier);
    buf.write(branchNo);
  }
  
  public XidImpl
    (int formatId
    ,byte[] globalTransactionId
    ,byte[] branchQualifier
    )
  { 
    this.formatId=formatId;
    this.globalTransactionId=globalTransactionId;
    this.branchQualifier=branchQualifier;
  }
  
  @Override
  public int getFormatId()
  { return formatId;
  }

  @Override
  public byte[] getGlobalTransactionId()
  { return globalTransactionId;
  }

  @Override
  public byte[] getBranchQualifier()
  { return branchQualifier;
  }

  @Override
  public String toString()
  { 
    return Integer.toHexString(this.formatId)
      +"."+HexCodec.encodeHex(globalTransactionId)
      +"."+HexCodec.encodeHex(this.branchQualifier)
      ;
  }

}
