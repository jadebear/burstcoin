package brs;

import brs.db.BurstKey;
import brs.db.VersionedEntityTable;
import brs.util.Convert;

public abstract class Order {

  private static void matchOrders(long assetId) {

    Order.Ask askOrder;
    Order.Bid bidOrder;

    while ((askOrder = Ask.getNextOrder(assetId)) != null
           && (bidOrder = Bid.getNextOrder(assetId)) != null) {

      if (askOrder.getPriceNQT() > bidOrder.getPriceNQT()) {
        break;
      }


      Trade trade = Trade.addTrade(assetId, Burst.getBlockchain().getLastBlock(), askOrder, bidOrder);

      askOrder.updateQuantityQNT(Convert.safeSubtract(askOrder.getQuantityQNT(), trade.getQuantityQNT()));
      Account askAccount = Account.getAccount(askOrder.getAccountId());
      askAccount.addToBalanceAndUnconfirmedBalanceNQT(Convert.safeMultiply(trade.getQuantityQNT(), trade.getPriceNQT()));
      askAccount.addToAssetBalanceQNT(assetId, -trade.getQuantityQNT());

      bidOrder.updateQuantityQNT(Convert.safeSubtract(bidOrder.getQuantityQNT(), trade.getQuantityQNT()));
      Account bidAccount = Account.getAccount(bidOrder.getAccountId());
      bidAccount.addToAssetAndUnconfirmedAssetBalanceQNT(assetId, trade.getQuantityQNT());
      bidAccount.addToBalanceNQT(-Convert.safeMultiply(trade.getQuantityQNT(), trade.getPriceNQT()));
      bidAccount.addToUnconfirmedBalanceNQT(Convert.safeMultiply(trade.getQuantityQNT(), (bidOrder.getPriceNQT() - trade.getPriceNQT())));

    }

  }

  static void init() {
    Ask.init();
    Bid.init();
  }


  private final long id;
  private final long accountId;
  private final long assetId;
  private final long priceNQT;
  private final int creationHeight;

  private long quantityQNT;

  private Order(Transaction transaction, Attachment.ColoredCoinsOrderPlacement attachment) {
    this.id = transaction.getId();
    this.accountId = transaction.getSenderId();
    this.assetId = attachment.getAssetId();
    this.quantityQNT = attachment.getQuantityQNT();
    this.priceNQT = attachment.getPriceNQT();
    this.creationHeight = transaction.getHeight();
  }

  protected Order(long id, long accountId, long assetId, long priceNQT, int creationHeight, long quantityQNT) {
    this.id = id;
    this.accountId = accountId;
    this.assetId = assetId;
    this.priceNQT = priceNQT;
    this.creationHeight = creationHeight;
    this.quantityQNT = quantityQNT;
  }

  public long getId() {
    return id;
  }

  public long getAccountId() {
    return accountId;
  }

  public long getAssetId() {
    return assetId;
  }

  public long getPriceNQT() {
    return priceNQT;
  }

  public long getQuantityQNT() {
    return quantityQNT;
  }

  public int getHeight() {
    return creationHeight;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " id: " + Convert.toUnsignedLong(id) + " account: " + Convert.toUnsignedLong(accountId)
        + " asset: " + Convert.toUnsignedLong(assetId) + " price: " + priceNQT + " quantity: " + quantityQNT + " height: " + creationHeight;
  }

  private void setQuantityQNT(long quantityQNT) {
    this.quantityQNT = quantityQNT;
  }

  /*
    private int compareTo(Order o) {
    if (height < o.height) {
    return -1;
    } else if (height > o.height) {
    return 1;
    } else {
    if (id < o.id) {
    return -1;
    } else if (id > o.id) {
    return 1;
    } else {
    return 0;
    }
    }

    }
  */

  public static class Ask extends Order {

    private static final BurstKey.LongKeyFactory<Ask> askOrderDbKeyFactory() {
      return Burst.getStores().getOrderStore().getAskOrderDbKeyFactory();
    }


    private static final VersionedEntityTable<Ask> askOrderTable() {
      return Burst.getStores().getOrderStore().getAskOrderTable();
    }

    public static Ask getAskOrder(long orderId) {
      return askOrderTable().get(askOrderDbKeyFactory().newKey(orderId));
    }

    private static Ask getNextOrder(long assetId) {
      return Burst.getStores().getOrderStore().getNextOrder(assetId);
    }

    static void addOrder(Transaction transaction, Attachment.ColoredCoinsAskOrderPlacement attachment) {
      Ask order = new Ask(transaction, attachment);
      askOrderTable().insert(order);
      matchOrders(attachment.getAssetId());
    }

    static void removeOrder(long orderId) {
      askOrderTable().delete(getAskOrder(orderId));
    }

    static void init() {}


    public final BurstKey dbKey;

    private Ask(Transaction transaction, Attachment.ColoredCoinsAskOrderPlacement attachment) {
      super(transaction, attachment);
      this.dbKey = askOrderDbKeyFactory().newKey(super.id);
    }

    public Ask(long id, long accountId, long assetId, long priceNQT, int creationHeight, long quantityQNT, BurstKey dbKey) {
      super(id, accountId, assetId, priceNQT, creationHeight, quantityQNT);
      this.dbKey = dbKey;
    }



    private void updateQuantityQNT(long quantityQNT) {
      super.setQuantityQNT(quantityQNT);
      if (quantityQNT > 0) {
        askOrderTable().insert(this);
      } else if (quantityQNT == 0) {
        askOrderTable().delete(this);
      } else {
        throw new IllegalArgumentException("Negative quantity: " + quantityQNT
                                           + " for order: " + Convert.toUnsignedLong(getId()));
      }
    }

    /*
      @Override
      public int compareTo(Ask o) {
      if (this.getPriceNQT() < o.getPriceNQT()) {
      return -1;
      } else if (this.getPriceNQT() > o.getPriceNQT()) {
      return 1;
      } else {
      return super.compareTo(o);
      }
      }
    */

  }

  public static class Bid extends Order {

    private static final BurstKey.LongKeyFactory<Bid> bidOrderDbKeyFactory() {
      return Burst.getStores().getOrderStore().getBidOrderDbKeyFactory();
    }

    private static final VersionedEntityTable<Bid> bidOrderTable() {
      return Burst.getStores().getOrderStore().getBidOrderTable();
    }

    public static int getBidCount() {
      return bidOrderTable().getCount();
    }

    public static Bid getBidOrder(long orderId) {
      return bidOrderTable().get(bidOrderDbKeyFactory().newKey(orderId));
    }

    private static Bid getNextOrder(long assetId) {
      return Burst.getStores().getOrderStore().getNextBid(assetId);
    }

    static void addOrder(Transaction transaction, Attachment.ColoredCoinsBidOrderPlacement attachment) {
      Bid order = new Bid(transaction, attachment);
      bidOrderTable().insert(order);
      matchOrders(attachment.getAssetId());
    }

    static void removeOrder(long orderId) {
      bidOrderTable().delete(getBidOrder(orderId));
    }

    static void init() {}


    public final BurstKey dbKey;

    private Bid(Transaction transaction, Attachment.ColoredCoinsBidOrderPlacement attachment) {
      super(transaction, attachment);
      this.dbKey = bidOrderDbKeyFactory().newKey(super.id);
    }

    public Bid(long id, long accountId, long assetId, long priceNQT, int creationHeight, long quantityQNT, BurstKey dbKey) {
      super(id, accountId, assetId, priceNQT, creationHeight, quantityQNT);
      this.dbKey = dbKey;
    }

    private void updateQuantityQNT(long quantityQNT) {
      super.setQuantityQNT(quantityQNT);
      if (quantityQNT > 0) {
        bidOrderTable().insert(this);
      } else if (quantityQNT == 0) {
        bidOrderTable().delete(this);
      } else {
        throw new IllegalArgumentException("Negative quantity: " + quantityQNT
                                           + " for order: " + Convert.toUnsignedLong(getId()));
      }
    }
  }
}
