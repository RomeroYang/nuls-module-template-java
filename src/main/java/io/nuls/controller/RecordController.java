package io.nuls.controller;

import io.nuls.Config;
import io.nuls.Constant;
import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.*;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.controller.core.BaseController;
import io.nuls.controller.core.Result;
import io.nuls.controller.vo.CreateRecordReq;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.rpctools.AccountTools;
import io.nuls.rpctools.LegderTools;
import io.nuls.rpctools.TransactionTools;
import io.nuls.rpctools.vo.Account;
import io.nuls.rpctools.vo.AccountBalance;
import io.nuls.service.RecordService;
import io.nuls.service.dto.Record;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

@Path("/")
@Component
public class RecordController implements BaseController {


    @Autowired
    Config config;

    @Autowired
    AccountTools accountTools;

    @Autowired
    LegderTools legderTools;

    @Autowired
    TransactionTools transactionTools;

    @Autowired
    RecordService recordService;


    /**
     * 生成一个存证记录
     * 需要扣除1个NULS作为手续费
     *
     * @param req
     * @return
     */
    @Path("record")
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public Result<String> createRecord(CreateRecordReq req)  {
        return call(() -> {
            Transaction tx = null;
            if (!validMd5String(req.getMd5())) {
                throw new NulsRuntimeException(CommonCodeConstanst.PARAMETER_ERROR);
            }
            //验证账户有效性
            accountTools.accountValid(req.getAddress(), req.getPassword());
            Account account = accountTools.getAccountByAddress(req.getAddress());
            if (recordService.hasRecord(req.getMd5())) {
                throw new NulsRuntimeException(CommonCodeConstanst.FAILED, "address  already set mail address : " + req.getMd5());
            }
            tx = createRecordTxWithoutSign(account, req);
            // 签名交易
            signTransaction(tx, account, req.getPassword());
            if (!transactionTools.newTx(tx)) {
                throw new NulsRuntimeException(CommonCodeConstanst.FAILED);
            }
            return new Result<>(tx.getHash().toHex());
        });
    }

    private Transaction createRecordTxWithoutSign(Account account, CreateRecordReq req) throws NulsException, IOException {
        Transaction tx = null;
        tx = new Transaction();
        tx.setType(Constant.TX_TYPE_RECORD);
        tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
        Record r = new Record();
        r.setAddress(AddressTool.getAddress(account.getAddress()));
        r.setMd5(req.getMd5());
        r.setName(req.getName());
        r.setRecordNumber(req.getRecordNumber());
        r.setAuthor(req.getAuthor());
        tx.setTxData(r.serialize());
        // 烧毁账户所属本链的主资产
        int assetsId = config.getAssetId();
        // 查询账本获取nonce值
        AccountBalance accountBalance = legderTools.getBalanceAndNonce(config.getChainId(), account.getAddress(), config.getChainId(), assetsId);
        byte[] nonce = RPCUtil.decode(accountBalance.getNonce());
        byte locked = 0;
        CoinFrom coinFrom = new CoinFrom(r.getAddress(), config.getChainId(), assetsId, config.getRecordFee(), nonce, locked);
        CoinTo coinTo = new CoinTo(AddressTool.getAddress(Constant.BLACK_HOLE_ADDRESS), config.getChainId(), assetsId, config.getRecordFee());
        int txSize = tx.size() + coinFrom.size() + coinTo.size() + P2PHKSignature.SERIALIZE_LENGTH;
        //计算手续费
        BigInteger fee = TransactionFeeCalculator.getNormalTxFee(txSize);
        //总费用为
        BigInteger totalAmount = config.getRecordFee().add(fee);
        coinFrom.setAmount(totalAmount);
        //检查余额是否充足
        BigInteger mainAsset = accountBalance.getAvailable();
        //余额不足
        if (BigIntegerUtils.isLessThan(mainAsset, totalAmount)) {
            throw new NulsRuntimeException(CommonCodeConstanst.FAILED, "insufficient fee");
        }
        CoinData coinData = new CoinData();
        coinData.setFrom(Arrays.asList(coinFrom));
        coinData.setTo(Arrays.asList(coinTo));
        tx.setCoinData(coinData.serialize());
        //计算交易数据摘要哈希
        tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
        return tx;
    }

    private boolean validMd5String(String str) {
        return StringUtils.isNotBlank(str) && str.trim().length() == 32;
    }

    /**
     * 获取指定的存证记录
     *
     * @param md5
     * @return
     */
    @Path("record/detail/{md5}")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Result<String> getRecord(@PathParam("md5") String md5) {
        return call(()-> new Result<>(recordService.getRecord(md5).get()));
    }

    /**
     * 获取指定地址的存证记录列表
     *
     * @param address
     * @return
     */
    @Path("record/list/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Result<String> getRecordList(@PathParam("address") String address) {
        return call(()-> new Result<>(recordService.getRecordsByAddress(address)));
    }

}
