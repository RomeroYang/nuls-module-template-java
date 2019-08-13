package io.nuls;

import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.core.rpc.modulebootstrap.RpcModuleState;
import io.nuls.rpctools.TransactionTools;

import java.io.File;

@Component
public class MyModule {

    @Autowired
    Config config;

    @Autowired
    TransactionTools transactionTools;

    /**
     * 启动模块
     * 模块启动后，当申明的依赖模块都已经准备就绪将调用此函数
     * @param moduleName
     * @return
     */
    public RpcModuleState startModule(String moduleName){
        //初始化数据存储文件夹
        File file = new File(config.getDataPath());
        if(!file.exists()){
            file.mkdir();
        }
        //注册交易
        transactionTools.registerTx(moduleName, Constant.TX_TYPE_RECORD);
        return RpcModuleState.Running;
    }

    /**
     * 申明需要依赖的其他模块
     * @return
     */
    public Module[] declareDependent() {
        return new Module[]{
                Module.build(ModuleE.LG),
                Module.build(ModuleE.TX),
                Module.build(ModuleE.NW)
        };
    }

}
