package io.nuls;

import io.nuls.core.core.annotation.Configuration;

import java.io.File;
import java.math.BigInteger;

@Configuration(domain = "record")
public class Config {

    /**
     * 当前运行的chain id 来自配置文件
     */
    private int chainId;

    /**
     * 默认资产id
     */
    private int assetId;

    /**
     * 提交存证手续费
     */
    private BigInteger recordFee;

    private String dataPath;


    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public BigInteger getRecordFee() {
        return recordFee;
    }

    public void setRecordFee(BigInteger recordFee) {
        this.recordFee = recordFee;
    }

    public String getDataPath() {
        return dataPath + File.separator + "mail";
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }
}
