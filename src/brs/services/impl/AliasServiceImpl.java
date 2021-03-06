package brs.services.impl;

import brs.Alias;
import brs.Alias.Offer;
import brs.db.BurstKey;
import brs.db.VersionedEntityTable;
import brs.db.store.AliasStore;
import brs.services.AliasService;

public class AliasServiceImpl implements AliasService {

  private final AliasStore aliasStore;
  private final VersionedEntityTable<Alias> aliasTable;
  private final BurstKey.LongKeyFactory<Alias> aliasDbKeyFactory;
  private final VersionedEntityTable<Offer> offerTable;
  private final BurstKey.LongKeyFactory<Offer> offerDbKeyFactory;

  public AliasServiceImpl(AliasStore aliasStore) {
    this.aliasStore = aliasStore;
    this.aliasTable = aliasStore.getAliasTable();
    this.aliasDbKeyFactory = aliasStore.getAliasDbKeyFactory();
    this.offerTable = aliasStore.getOfferTable();
    this.offerDbKeyFactory = aliasStore.getOfferDbKeyFactory();
  }

  public Alias getAlias(String aliasName) {
    return aliasStore.getAlias(aliasName);
  }

  public Alias getAlias(long id) {
    return aliasTable.get(aliasDbKeyFactory.newKey(id));
  }

  @Override
  public Offer getOffer(Alias alias) {
    return offerTable.get(offerDbKeyFactory.newKey(alias.getId()));
  }

  @Override
  public long getAliasCount() {
    return aliasTable.getCount();
  }

}
