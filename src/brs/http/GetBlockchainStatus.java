package brs.http;

import brs.Block;
import brs.Blockchain;
import brs.BlockchainProcessor;
import brs.Burst;
import brs.peer.Peer;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetBlockchainStatus extends APIServlet.APIRequestHandler {

  private final BlockchainProcessor blockchainProcessor;
  private final Blockchain blockchain;

  GetBlockchainStatus(BlockchainProcessor blockchainProcessor, Blockchain blockchain) {
    super(new APITag[] {APITag.BLOCKS, APITag.INFO});
    this.blockchainProcessor = blockchainProcessor;
    this.blockchain = blockchain;
  }

  @Override
  JSONStreamAware processRequest(HttpServletRequest req) {
    JSONObject response = new JSONObject();
    response.put("application", Burst.APPLICATION);
    response.put("version", Burst.VERSION);
    response.put("time", Burst.getEpochTime());
    Block lastBlock = blockchain.getLastBlock();
    response.put("lastBlock", lastBlock.getStringId());
    response.put("cumulativeDifficulty", lastBlock.getCumulativeDifficulty().toString());
    response.put("numberOfBlocks", lastBlock.getHeight() + 1);
    Peer lastBlockchainFeeder = blockchainProcessor.getLastBlockchainFeeder();
    response.put("lastBlockchainFeeder", lastBlockchainFeeder == null ? null : lastBlockchainFeeder.getAnnouncedAddress());
    response.put("lastBlockchainFeederHeight", blockchainProcessor.getLastBlockchainFeederHeight());
    response.put("isScanning", blockchainProcessor.isScanning());
    return response;
  }

}
