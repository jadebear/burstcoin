package brs.http;

import brs.BurstException;
import brs.Trade;
import brs.http.common.Parameters;
import brs.services.TradeService;
import brs.util.FilteringIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static brs.http.common.Parameters.*;

public final class GetAllTrades extends APIServlet.APIRequestHandler {

  private final TradeService tradeService;

  GetAllTrades(TradeService tradeService) {
    super(new APITag[] {APITag.AE}, TIMESTAMP_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER, INCLUDE_ASSET_INFO_PARAMETER);
    this.tradeService = tradeService;
  }
    
  @Override
  JSONStreamAware processRequest(HttpServletRequest req) throws BurstException {
    final int timestamp = ParameterParser.getTimestamp(req);
    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex = ParameterParser.getLastIndex(req);
    boolean includeAssetInfo = !Parameters.isFalse(req.getParameter(INCLUDE_ASSET_INFO_PARAMETER));

    JSONObject response = new JSONObject();
    JSONArray trades = new JSONArray();
    try (FilteringIterator<Trade> tradeIterator = new FilteringIterator<>(tradeService.getAllTrades(0, -1),
                                                                          new FilteringIterator.Filter<Trade>() {
                                                                            @Override
                                                                            public boolean ok(Trade trade) {
                                                                              return trade.getTimestamp() >= timestamp;
                                                                            }
                                                                          }, firstIndex, lastIndex)) {
      while (tradeIterator.hasNext()) {
        trades.add(JSONData.trade(tradeIterator.next(), includeAssetInfo));
      }
    }
    response.put("trades", trades);
    return response;
  }

}
