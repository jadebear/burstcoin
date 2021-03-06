package brs.services.impl;

import brs.Order.Ask;
import brs.Order.Bid;
import brs.db.BurstIterator;
import brs.db.BurstKey.LongKeyFactory;
import brs.db.VersionedEntityTable;
import brs.db.store.OrderStore;
import brs.services.OrderService;

public class OrderServiceImpl implements OrderService {

  private final OrderStore orderStore;
  private final VersionedEntityTable<Ask> askOrderTable;
  private final LongKeyFactory<Ask> askOrderDbKeyFactory;
  private final VersionedEntityTable<Bid> bidOrderTable;
  private final LongKeyFactory<Bid> bidOrderDbKeyFactory;

  public OrderServiceImpl(OrderStore orderStore) {
    this.orderStore = orderStore;
    this.askOrderTable = orderStore.getAskOrderTable();
    this.askOrderDbKeyFactory = orderStore.getAskOrderDbKeyFactory();
    this.bidOrderTable = orderStore.getBidOrderTable();
    this.bidOrderDbKeyFactory = orderStore.getBidOrderDbKeyFactory();
  }

  @Override
  public Ask getAskOrder(long orderId) {
    return askOrderTable.get(askOrderDbKeyFactory.newKey(orderId));
  }

  @Override
  public Bid getBidOrder(long orderId) {
    return bidOrderTable.get(bidOrderDbKeyFactory.newKey(orderId));
  }

  @Override
  public BurstIterator<Ask> getAllAskOrders(int from, int to) {
    return askOrderTable.getAll(from, to);
  }

  @Override
  public BurstIterator<Bid> getAllBidOrders(int from, int to) {
    return bidOrderTable.getAll(from, to);
  }

  @Override
  public BurstIterator<Bid> getSortedBidOrders(long assetId, int from, int to) {
    return orderStore.getSortedBids(assetId, from, to);
  }

  @Override
  public BurstIterator<Ask> getAskOrdersByAccount(long accountId, int from, int to) {
    return orderStore.getAskOrdersByAccount(accountId, from, to);
  }

  @Override
  public BurstIterator<Ask> getAskOrdersByAccountAsset(final long accountId, final long assetId, int from, int to) {
    return orderStore.getAskOrdersByAccountAsset(accountId, assetId, from, to);
  }

  @Override
  public BurstIterator<Ask> getSortedAskOrders(long assetId, int from, int to) {
    return orderStore.getSortedAsks(assetId, from, to);
  }

  @Override
  public int getBidCount() {
    return bidOrderTable.getCount();
  }

  @Override
  public int getAskCount() {
    return askOrderTable.getCount();
  }

  @Override
  public BurstIterator<Bid> getBidOrdersByAccount(long accountId, int from, int to) {
    return orderStore.getBidOrdersByAccount(accountId, from, to);
  }

  @Override
  public BurstIterator<Bid> getBidOrdersByAccountAsset(final long accountId, final long assetId, int from, int to) {
    return orderStore.getBidOrdersByAccountAsset(accountId, assetId, from, to);
  }

}
