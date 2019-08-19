package io.nuls.txhandler;

import io.nuls.Config;
import io.nuls.Constant;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.*;
import io.nuls.controller.vo.RecordData;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.rpctools.LegderTools;
import io.nuls.rpctools.vo.AccountBalance;
import io.nuls.service.RecordService;
import io.nuls.service.dto.Record;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class RecordProcessor implements TransactionProcessor {

    @Autowired
    RecordService service;

    @Autowired
    LegderTools legderTools;

    @Autowired
    Config config;

    @Override
    public int getType() {
        return Constant.TX_TYPE_RECORD;
    }

    @Override
    public boolean validate(int chainId, Transaction tx, BlockHeader blockHeader) {
        Log.debug("validate tx", tx.getTxData());
        //验证交易hash是否一致
        try {
            NulsHash nulsHash = NulsHash.calcHash(tx.serializeForHash());
            if (!nulsHash.equals(tx.getHash())) {
                return false;
            }
            Record record = new Record();
            record.parse(new NulsByteBuffer(tx.getTxData()));
            //验证 md5 是否已被使用
            if(service.hasRecord(record.getMd5())){
                return false;
            }
            //验证是否转入指定资产到手续费账户
            CoinData coinData = new CoinData();
            coinData.parse(new NulsByteBuffer(tx.getCoinData()));
            List<CoinFrom> from = coinData.getFrom();
            //支付账户只能有一个
            if(from.size() != 1) {
                return false;
            }
            CoinFrom cf = from.get(0);
            //验证支付账户是否和申请账户是同一个
            if(!Arrays.equals(cf.getAddress(),record.getAddress())){
                return false;
            }
            List<CoinTo> to = coinData.getTo();
            //收款账户只能有一个
            if(to.size() != 1) {
                return false;
            }
            CoinTo ct = to.get(0);
            //验证收款地址是否是手续费地址
            if(!Arrays.equals(ct.getAddress(), AddressTool.getAddress(Constant.BLACK_HOLE_ADDRESS))){
                return false;
            }
            //验证支付的申请费用是否正确 出金 = 入金 + 手续费
            if(!((ct.getAmount().add(tx.getFee())).equals(cf.getAmount()) && ct.getAmount().equals(config.getRecordFee()))){
                return false;
            }
            //验证余额是否足够支付申请费用和交易手续费
            AccountBalance accountBalance = legderTools.getBalanceAndNonce(config.getChainId(), AddressTool.getStringAddressByBytes(cf.getAddress()), config.getChainId(), config.getAssetId());
            if(accountBalance.getAvailable().min(ct.getAmount().add(tx.getFee())).equals(accountBalance.getAvailable())){
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NulsException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean commit(int chainId, Transaction tx, BlockHeader blockHeader) {
        Log.info("commit tx");
        Record record = new Record();
        try {
            RecordData recordData = new RecordData();
            record.parse(new NulsByteBuffer(tx.getTxData()));
            recordData.setAddress(AddressTool.getStringAddressByBytes(record.getAddress()));
            recordData.setMd5(record.getMd5());
            recordData.setName(record.getName());
            recordData.setRecordTime(String.valueOf(tx.getTime()));
            recordData.setRecordNumber(record.getRecordNumber());
            recordData.setAuthor(record.getAuthor());
            service.saveRecord(recordData);
        } catch (NulsException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean rollback(int chainId, Transaction tx, BlockHeader blockHeader) {
        //删除 record
        Log.info("rollback tx");
        Record record = new Record();
        try {
            record.parse(new NulsByteBuffer(tx.getTxData()));
            service.removeRecord(record.getMd5());
        } catch (NulsException e) {
            e.printStackTrace();
        }
        return true;
    }
}
