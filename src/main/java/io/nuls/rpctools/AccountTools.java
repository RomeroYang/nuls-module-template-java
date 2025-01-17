package io.nuls.rpctools;

import io.nuls.Config;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.MapUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.rpctools.vo.Account;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-12 14:06
 * @Description: 账户模块工具类
 */
@Component
public class AccountTools implements CallRpc {

    @Autowired
    Config config;

    /**
     * 获取账户信息
     * @param address
     * @return
     */
    public Account getAccountByAddress(String address) {
        Map<String, Object> param = new HashMap<>(2);
        param.put("chainId", config.getChainId());
        param.put("address", address);
        return callRpc(ModuleE.AC.name, "ac_getAccountByAddress", param, (Function<Map<String, Object>, Account>) res -> {
                    if (res == null) {
                        return null;
                    }
                    return MapUtils.mapToBean(res, new Account());
                }
        );
    }


    /**
     * 账户验证
     * account validate
     *
     * @param address
     * @param password
     * @return validate result
     */
    public boolean accountValid(String address, String password) throws NulsException {
        return getAddressInfo(address,password,"valid");
    }


    /**
     * 获取账户私钥
     * account validate
     *
     * @param address
     * @param password
     * @return validate result
     */
    public String getAddressPriKey(String address, String password) throws NulsException {
        return getAddressInfo(address,password,"priKey");
    }

    private <T> T getAddressInfo(String address, String password,String key) throws NulsException {
        Map<String, Object> callParams = new HashMap<>(4);
        callParams.put(Constants.CHAIN_ID, config.getChainId());
        callParams.put("address", address);
        callParams.put("password", password);
        return callRpc(ModuleE.AC.abbr, "ac_getPriKeyByAddress", callParams, (Function<Map<String, Object>, T>) res -> (T) res.get(key));
    }

}
